package org.gluu.casa.plugins.accounts.service.password;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.PasswordPerson;
import org.gluu.casa.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jgomer
 */
public class PasswordService {

    private ILdapService ldapService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public PasswordService() {
        ldapService = Utils.managedBean(ILdapService.class);
    }

    public boolean hasPassword(String userId) {

        try {
            PasswordPerson p = ldapService.get(PasswordPerson.class, ldapService.getPersonDn(userId));
            return p.hasPassword();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;

    }

    public boolean setPassword(String userId, String newPassword) {

        boolean success = false;
        try {
            PasswordPerson p = ldapService.get(PasswordPerson.class, ldapService.getPersonDn(userId));
            p.setPassword(newPassword);
            success = ldapService.modify(p, PasswordPerson.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return success;

    }

}
