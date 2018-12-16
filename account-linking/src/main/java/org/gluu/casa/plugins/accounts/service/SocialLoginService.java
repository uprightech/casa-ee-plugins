package org.gluu.casa.plugins.accounts.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.ExternalAccount;
import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.plugins.accounts.ldap.oxPassportConfiguration;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.gluu.casa.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.passport.PassportConfiguration;
import org.zkoss.util.Pair;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author jgomer
 */
public class SocialLoginService {

    private static final Path OXLDAP_PATH = Paths.get("/etc/gluu/conf/ox-ldap.properties");
    private static final String OXPASSPORT_PROPERTY = "oxpassport_ConfigurationEntryDN";
    private static final String OXEXTERNALUID_PREFIX = "passport-";

    private Logger logger = LoggerFactory.getLogger(getClass());

    //This has to be a codehaus mapper (see PassportConfiguration.class)
    private ObjectMapper mapper;

    private ILdapService ldapService;

    private List<Provider> providers;

    public SocialLoginService() {
        mapper = new ObjectMapper();
        ldapService = Utils.managedBean(ILdapService.class);
        //Lookup the authentication providers supported in the current Passport installation
        parseProviders();
    }

    public List<Provider> getAvailableProviders() {
        return providers;
    }

    public List<ExternalAccount> getAccounts(String id, boolean linked) {

        List<ExternalAccount> externalAccts = new ArrayList<>();
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));

        for (String externalUid : Optional.ofNullable(linked ? p.getOxExternalUid() : p.getOxUnlinkedExternalUids()).orElse(new String[0])) {
            if (externalUid.startsWith(OXEXTERNALUID_PREFIX)) {
                int i = externalUid.indexOf(":");

                if (i > OXEXTERNALUID_PREFIX.length()) {
                    String provider = externalUid.substring(OXEXTERNALUID_PREFIX.length(), i);

                    if (belongToProviders(provider)) {
                        ExternalAccount acc = new ExternalAccount();
                        acc.setProvider(provider);
                        acc.setUid(externalUid.substring(i + 1));
                        externalAccts.add(acc);
                    }
                }
            }
        }
        return externalAccts;

    }

    public boolean unlink(String id, String provider) {

        boolean success = false;
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));

        Pair<String, List<String>> tmp = removeProvider(provider, p.getOxExternalUid());
        List<String> linked = tmp.getY();
        String oxExternalUid = tmp.getX();

        if (oxExternalUid != null) {
            List<String> unlinked = new ArrayList<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));
            unlinked.add(oxExternalUid);

            logger.info("Linked accounts for {} will be {}", id, linked);
            logger.info("Unlinked accounts for {} will be {}", id, unlinked);
            success = updateExternalIdentities(p, linked, unlinked);
        }

        return success;

    }

    public boolean enableLink(String id, String provider) {

        boolean success = false;
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));

        Pair<String, List<String>> tmp = removeProvider(provider, p.getOxUnlinkedExternalUids());
        List<String> unlinked = tmp.getY();
        String oxExternalUid = tmp.getX();

        if (oxExternalUid != null) {
            List<String> linked = new ArrayList<>(Utils.listfromArray(p.getOxExternalUid()));
            linked.add(oxExternalUid);

            logger.info("Linked accounts for {} will be {}", id, linked);
            logger.info("Unlinked accounts for {} will be {}", id, unlinked);
            success = updateExternalIdentities(p, linked, unlinked);
        }

        return success;

    }

    public boolean link(String id, String provider, String externalId) {

        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));
        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxExternalUid()));
        list.add(String.format("passport-%s:%s", provider, externalId));
        logger.info("Linked accounts for {} will be {}", id, list);

        p.setOxExternalUid(list.toArray(new String[0]));
        return ldapService.modify(p, ExternalIdentityPerson.class);

    }

    public boolean delete(String id, String provider) {

        logger.info("Removing provider {} for {}", provider, id);
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));

        List<String> linked = removeProvider(provider, p.getOxExternalUid()).getY();
        List<String> unlinked = removeProvider(provider, p.getOxUnlinkedExternalUids()).getY();
        return updateExternalIdentities(p, linked, unlinked);

    }

    private void parseProviders() {
        providers = new ArrayList<>();
        providers.addAll(retrieveSAMLIDPs());
        providers.addAll(retrieveSocialProviders());
    }

    private List<Provider> retrieveSAMLIDPs() {

        List<Provider> providers = new ArrayList<>();
        logger.info("Loading IDPs list");
        try {
            logger.debug("Parsing passport-saml-config.json");
            byte[] bytes = Files.readAllBytes(Paths.get("/etc/gluu/conf/passport-saml-config.json"));
            Map<String, Object> data = mapper.readValue(new String(bytes, StandardCharsets.UTF_8), new TypeReference<Map<String, Object>>(){});

            for (String key : data.keySet()) {
                Provider prv = new Provider();
                prv.setName(key);

                Object logo = ((Map<String, Object>) data.get(key)).get("logo_img");
                Optional.ofNullable(logo).ifPresent( l -> prv.setLogo(l.toString()));
                providers.add(prv);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return providers;
    }

    private List<Provider> retrieveSocialProviders() {

        List<Provider> providers = new ArrayList<>();
        logger.info("Loading social strategies info");
        try {
            logger.debug("Reading DN of LDAP passport configuration");
            String dn = Files.newBufferedReader(OXLDAP_PATH).lines().filter(l -> l.startsWith(OXPASSPORT_PROPERTY))
                    .findFirst().map(l -> l.substring(OXPASSPORT_PROPERTY.length())).get();
            //skip uninteresting chars
            dn = dn.replaceFirst("[\\W]*=[\\W]*","");

            oxPassportConfiguration passportConfig = ldapService.get(oxPassportConfiguration.class, dn);

            if (passportConfig != null) {
                Stream.of(passportConfig.getGluuPassportConfiguration())
                        .forEach(cfg -> {
                            try {
                                PassportConfiguration pcf = mapper.readValue(cfg, PassportConfiguration.class);
                                Provider provider = new Provider();
                                provider.setName(pcf.getStrategy());

                                logger.info("Found provider {}", provider.getName());
                                //Search the logo
                                String logo = Optional.ofNullable(pcf.getFieldset()).orElse(Collections.emptyList()).stream()
                                        .filter(prop -> prop.getValue1().equals("logo_img")).map(SimpleCustomProperty::getValue2)
                                        .findFirst().orElse(null);
                                if (logo == null) {
                                    logo = "/oxauth/auth/passport/img/" + provider.getName() + ".png";
                                } else if (!logo.startsWith("http")) {
                                    logo = "/oxauth/auth/passport/" + logo;
                                }
                                provider.setLogo(logo);

                                providers.add(provider);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return providers;

    }

    private boolean belongToProviders(String name) {
        return providers.stream().map(Provider::getName).anyMatch(name::equals);
    }

    private Pair<String, List<String>> removeProvider(String provider, String[] externalUids) {

        List<String> list = new ArrayList<>();
        String oxExternalUid = null;

        for (String externalUid : Optional.ofNullable(externalUids).orElse(new String[0])) {
            if (externalUid.startsWith(OXEXTERNALUID_PREFIX)) {
                int i = externalUid.indexOf(":");

                if (i > OXEXTERNALUID_PREFIX.length()) {
                    String prv = externalUid.substring(OXEXTERNALUID_PREFIX.length(), i);

                    if (prv.equals(provider)) {
                        oxExternalUid = externalUid;
                    } else {
                        list.add(externalUid);
                    }
                }
            }
        }
        return new Pair<>(oxExternalUid, list);

    }

    private boolean updateExternalIdentities(ExternalIdentityPerson p, List<String> linked, List<String> unlinked) {
        p.setOxExternalUid(linked.toArray(new String[0]));
        p.setOxUnlinkedExternalUids(unlinked.toArray(new String[0]));
        return ldapService.modify(p, ExternalIdentityPerson.class);
    }

}
