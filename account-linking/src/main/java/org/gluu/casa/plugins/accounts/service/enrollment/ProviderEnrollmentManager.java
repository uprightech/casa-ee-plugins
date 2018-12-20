package org.gluu.casa.plugins.accounts.service.enrollment;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.service.ILdapService;

/**
 * @author jgomer
 */
public interface ProviderEnrollmentManager {

    ILdapService ldapService = Utils.managedBean(ILdapService.class);

    String getUid(ExternalIdentityPerson p, boolean linked);
    boolean link(ExternalIdentityPerson p, String externalId);
    boolean remove(ExternalIdentityPerson p);
    boolean unlink(ExternalIdentityPerson p);
    boolean enable(ExternalIdentityPerson p);

}
