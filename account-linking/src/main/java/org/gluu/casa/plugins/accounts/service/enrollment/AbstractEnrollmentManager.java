package org.gluu.casa.plugins.accounts.service.enrollment;

import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jgomer
 */
abstract class AbstractEnrollmentManager implements ProviderEnrollmentManager {

    Provider provider;
    Logger logger = LoggerFactory.getLogger(getClass());

    AbstractEnrollmentManager(Provider provider) {
        this.provider = provider;
    }

    boolean updatePerson(ExternalIdentityPerson p) {
        return ldapService.modify(p, ExternalIdentityPerson.class);
    }

}
