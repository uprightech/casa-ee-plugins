package org.gluu.casa.plugins.accounts.ldap;

import com.unboundid.ldap.sdk.persist.LDAPField;
import com.unboundid.ldap.sdk.persist.LDAPObject;
import org.gluu.casa.core.ldap.BaseLdapPerson;
import org.gluu.casa.misc.Utils;

/**
 * @author jgomer
 */
@LDAPObject(structuralClass="gluuPerson",
        superiorClass="top")
public class PasswordPerson extends BaseLdapPerson {

    // The field used for optional attribute userPassword.
    @LDAPField
    private String[] userPassword;

    public boolean hasPassword() {
        return Utils.isNotEmpty(userPassword);
    }

    /**
     * Sets the values for the field associated with the
     * userPassword attribute.
     *
     * @param  v  The values for the field associated with the
     *            userPassword attribute.
     */
    public void setPassword(final String ...v)
    {
        this.userPassword = v;
    }

}
