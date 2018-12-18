package org.gluu.casa.plugins.accounts.pojo;

/**
 * @author jgomer
 */
public class Provider {

    private String logo;
    private String name;
    private String acr;

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public String getAcr() {
        return acr;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAcr(String acr) {
        this.acr = acr;
    }

}
