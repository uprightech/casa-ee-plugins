package org.gluu.casa.plugins.accounts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gluu.casa.core.ldap.oxCustomScript;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.plugins.accounts.pojo.LinkingSummary;
import org.gluu.casa.plugins.accounts.pojo.PendingLinks;
import org.gluu.casa.service.ILdapService;
import org.gluu.casa.service.ISessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.oxauth.model.common.WebKeyStorage;
import org.xdi.oxauth.model.configuration.AppConfiguration;
import org.xdi.oxauth.model.crypto.CryptoProviderFactory;
import org.xdi.oxauth.model.jwt.Jwt;
import org.zkoss.util.resource.Labels;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * @author jgomer
 */
@Path("/idp-linking")
public class LinkingService {

    public static final String CUSTOM_HEADER = "Linking-Summary";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper = new ObjectMapper();

    private ILdapService ldapService;

    private String keyStoreFile;

    private String keyStorePassword;

    @Context
    private UriInfo uriInfo;

    public LinkingService() {

        try {
            mapper = new ObjectMapper();
            ldapService = Utils.managedBean(ILdapService.class);

            oxCustomScript script = new oxCustomScript();
            script.setDisplayName("passport_social");
            script = ldapService.find(script, oxCustomScript.class, ldapService.getCustomScriptsDn()).get(0);

            Map<String, String> props = Utils.scriptConfigPropertiesAsMap(script);
            keyStoreFile = props.get("key_store_file");
            keyStorePassword = props.get("key_store_password");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.warn("Service for linking external identities may not work properly");
        }

    }

    @GET
    public Response processError(@QueryParam("failure") String msg) throws Exception {
        return Response.serverError().entity(msg).build();
    }

    @POST
    @Path("{provider}")
    public Response doLink(String userJwt, @PathParam("provider") String provider) throws Exception {

        LinkingSummary summary = new LinkingSummary();
        String msg = null;
        try {
            ISessionContext sessionContext = Utils.managedBean(ISessionContext.class);
            String userId = sessionContext.getLoggedUser().getId();

            if (PendingLinks.contains(userId, provider)) {
                Jwt jwt = validateJWT(userJwt);
                if (jwt != null) {
                    String profile = jwt.getClaims().getClaimAsString("data");
                    String uid = mapper.readTree(profile).get("id").asText();

                    //Verify it's not already enrolled by someone
                    if (!alreadyAssigned(provider, uid)) {
                        summary.setProvider(provider);
                        summary.setUid(uid);
                    } else {
                        msg = Labels.getLabel("sociallogin.link_result.already_taken", new String[]{uid, provider});
                        logger.warn(msg);
                    }
                } else {
                    msg = Labels.getLabel("sociallogin.link_result.validation_failed");
                    logger.error(msg);
                }
            } else {
                msg = Labels.getLabel("sociallogin.link_result.unexpected_provider", new String[]{provider});
                logger.warn(msg);
            }
        } catch (Exception e) {
            msg = e.getMessage();
            logger.error(msg, e);
        }
        if (msg != null) {
            summary.setErrorMessage(msg);
        }

        String data = mapper.writeValueAsString(summary);
        String url = uriInfo.getAbsolutePath().toString() + "/../account-linking-result.zul";
        URI uri = new URL(url.replaceFirst("/rest", "")).toURI();
        return Response.seeOther(uri).header(CUSTOM_HEADER, data).build();

    }

    private Jwt validateJWT(String encodedJWT) {

        try {
            //Verify JWT
            Jwt jwt = Jwt.parse(encodedJWT);
            AppConfiguration appCfg = new AppConfiguration();
            appCfg.setWebKeysStorage(WebKeyStorage.KEYSTORE);
            appCfg.setKeyStoreFile(keyStoreFile);
            appCfg.setKeyStoreSecret(keyStorePassword);

            return CryptoProviderFactory.getCryptoProvider(appCfg).verifySignature(jwt.getSigningInput(), jwt.getEncodedSignature(),
                    jwt.getHeader().getKeyId(), null, null, jwt.getHeader().getAlgorithm()) ? jwt : null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    private boolean alreadyAssigned(String provider, String uid) {

        String value = String.format("passport-%s:%s", provider, uid);
        ExternalIdentityPerson p = new ExternalIdentityPerson();
        p.setOxExternalUid(value);
        return ldapService.find(p, ExternalIdentityPerson.class, ldapService.getPeopleDn()).size() > 0;

    }

}
