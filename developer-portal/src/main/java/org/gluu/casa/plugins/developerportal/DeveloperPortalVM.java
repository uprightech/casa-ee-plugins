package org.gluu.casa.plugins.developerportal;

import org.gluu.credmanager.misc.Utils;
import org.gluu.credmanager.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

/**
 * @author kepuss
 */
public class DeveloperPortalVM {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String message;
    private String organizationName;
    private ILdapService ldapService;

    /**
     * Getter of private class field <code>organizationName</code>.
     * @return A string with the value of the organization name found in your Gluu installation. Find this value in
     * Gluu Server oxTrust GUI at "Configuration" &gt; "Organization configuration"
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Getter of private class field <code>message</code>.
     * @return A string value
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter of private class field <code>message</code>.
     * @param message A string with the contents typed in text box of page index.zul
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Initialization method for this ViewModel.
     */
    @Init
    public void init() {
        logger.info("Hello World ViewModel inited");
        ldapService = Utils.managedBean(ILdapService.class);
    }

    /**
     * The method called when the button on page <code>index.zul</code> is pressed. It sets the value for
     * <code>organizationName</code>.
     */
    @NotifyChange("organizationName")
    @Command
    public void loadOrgName() {
        logger.debug("You typed {}", message);
        organizationName = ldapService.getOrganization().getDisplayName();
    }
}
