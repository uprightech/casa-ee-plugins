package org.gluu.casa.plugins.accounts.ldap;

/**
 * @author jgomer
 */
public class ExternalAccount {

    private String provider;
    private String uid;

    public String getProvider() {
        return provider;
    }

    public String getUid() {
        return uid;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
