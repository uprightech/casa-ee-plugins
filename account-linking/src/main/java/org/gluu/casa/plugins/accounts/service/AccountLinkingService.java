package org.gluu.casa.plugins.accounts.service;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.ExternalAccount;
import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.gluu.casa.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author jgomer
 */
public class AccountLinkingService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ILdapService ldapService;

    public AccountLinkingService() {
        ldapService = Utils.managedBean(ILdapService.class);
    }

    public List<ExternalAccount> getAccounts(String id, boolean linked) {

        List<ExternalAccount> externalAccts = new ArrayList<>();
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));

        for (Provider provider : AvailableProviders.get()) {
            String uid = provider.getEnrollmentManager().getUid(p, linked);
            //A null value indicates this provider isn't linked/unlinked for this user
            if (uid != null) {

                ExternalAccount acc = new ExternalAccount();
                acc.setProvider(provider);
                acc.setUid(uid);
                externalAccts.add(acc);
            }
        }
        return externalAccts;

    }

    public boolean link(String id, String provider, String externalId) {
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));
        Provider op = AvailableProviders.getByName(provider).get(); //Assume it exists
        return op.getEnrollmentManager().link(p, externalId);
    }

    public boolean unlink(String id, Provider provider) {
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));
        return provider.getEnrollmentManager().unlink(p);
    }

    public boolean enableLink(String id, Provider provider) {
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));
        return provider.getEnrollmentManager().enable(p);
    }

    public boolean delete(String id, Provider provider) {
        ExternalIdentityPerson p = ldapService.get(ExternalIdentityPerson.class, ldapService.getPersonDn(id));
        return provider.getEnrollmentManager().remove(p);
    }

}
