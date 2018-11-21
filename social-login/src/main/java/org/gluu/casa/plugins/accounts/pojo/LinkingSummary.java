package org.gluu.casa.plugins.accounts.pojo;

/**
 * @author jgomer
 */
public class LinkingSummary {

    private String provider;
    private String uid;
    private String errorMessage;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
