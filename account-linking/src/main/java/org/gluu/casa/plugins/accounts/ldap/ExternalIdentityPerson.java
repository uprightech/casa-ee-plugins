package org.gluu.casa.plugins.accounts.ldap;

import com.unboundid.ldap.sdk.ReadOnlyEntry;
import com.unboundid.ldap.sdk.persist.FilterUsage;
import com.unboundid.ldap.sdk.persist.LDAPEntryField;
import com.unboundid.ldap.sdk.persist.LDAPField;
import com.unboundid.ldap.sdk.persist.LDAPObject;
import org.gluu.casa.core.ldap.BaseLdapPerson;

/**
 * @author jgomer
 */
@LDAPObject(structuralClass="gluuPerson",
        superiorClass="top")
public class ExternalIdentityPerson extends BaseLdapPerson {

    @LDAPEntryField
    private ReadOnlyEntry ldapEntry;

    // The field used for optional attribute oxExternalUid.
    @LDAPField(filterUsage= FilterUsage.ALWAYS_ALLOWED)
    private String[] oxExternalUid;

    // The field used for optional attribute oxUnlinkedExternalUids.
    @LDAPField(filterUsage=FilterUsage.ALWAYS_ALLOWED)
    private String[] oxUnlinkedExternalUids;

    // The field used for optional attribute oxUnlinkedExternalUids.
    @LDAPField(filterUsage=FilterUsage.ALWAYS_ALLOWED)
    private String[] oxUidIDP;

    /**
     * Retrieves the values for the field associated with the
     * oxExternalUid attribute, if present.
     *
     * @return  The values for the field associated with the
     *          oxExternalUid attribute, or
     *          {@code null} if that attribute was not present in the entry.
     */
    public String[] getOxExternalUid()
    {
        return oxExternalUid;
    }

    /**
     * Retrieves the values for the field associated with the
     * oxUidIDP attribute, if present.
     *
     * @return  The values for the field associated with the
     *          oxUidIDP attribute, or
     *          {@code null} if that attribute was not present in the entry.
     */
    public String[] getOxUidIDP()
    {
        return oxUidIDP;
    }

    /**
     * Retrieves the values for the field associated with the
     * oxUnlinkedExternalUids attribute, if present.
     *
     * @return  The values for the field associated with the
     *          oxUnlinkedExternalUids attribute, or
     *          {@code null} if that attribute was not present in the entry.
     */
    public String[] getOxUnlinkedExternalUids()
    {
        return oxUnlinkedExternalUids;
    }

    /**
     * Sets the values for the field associated with the
     * oxExternalUid attribute.
     *
     * @param  v  The values for the field associated with the
     *            oxExternalUid attribute.
     */
    public void setOxExternalUid(final String... v)
    {
        this.oxExternalUid = v;
    }

    /**
     * Sets the values for the field associated with the
     * oxUidIDP attribute.
     *
     * @param  v  The values for the field associated with the
     *            oxUidIDP attribute.
     */
    public void setOxUidIDP(final String... v)
    {
        this.oxUidIDP = v;
    }

    /**
     * Sets the values for the field associated with the
     * oxExternalUid attribute.
     *
     * @param  v  The values for the field associated with the
     *            oxExternalUid attribute.
     */
    public void setOxUnlinkedExternalUids(final String... v)
    {
        this.oxUnlinkedExternalUids = v;
    }

}
