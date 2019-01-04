package org.gluu.casa.plugins.accounts.vm;

import org.gluu.casa.core.pojo.User;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.misc.WebUtils;
import org.gluu.casa.plugins.accounts.service.password.PasswordService;
import org.gluu.casa.service.ISessionContext;
import org.gluu.casa.ui.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.*;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

import java.util.stream.Stream;

/**
 * @author jgomer
 */
public class PasswordViewModel {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @WireVariable
    private ISessionContext sessionContext;

    private String newPassword;
    private String newPasswordConfirm;
    private int strength;
    private boolean passwordLess;
    private String contextPath;

    private User user;
    PasswordService ps;

    @DependsOn("strength")
    public String getStrengthText() {
        String str = null;
        if (strength >= 0) {
            str = Labels.getLabel("usr.pass.strength.level." + strength);
        }
        return str;
    }

    public int getStrength() {
        return strength;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }

    public boolean isPasswordLess() {
        return passwordLess;
    }

    @Init
    public void init() {
        resetPassSettings();
        user = sessionContext.getLoggedUser();
        ps = new PasswordService();
        passwordLess = !ps.hasPassword(user.getId());
        contextPath = WebUtils.getServletRequest().getContextPath();
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireEventListeners(view, this);
    }

    @Listen("onData=#new_pass")
    public void notified(Event event) throws Exception {
        if (Utils.isNotEmpty(newPassword)) {
            strength = (int) event.getData();
        } else {
            strength = -1;
        }
        BindUtils.postNotifyChange(null, null, this, "strength");
    }

    @NotifyChange("*")
    @Command
    public void setPass() {

        if (Stream.of(newPassword, newPasswordConfirm).noneMatch(Utils::isEmpty)) {
            if (newPasswordConfirm.equals(newPassword)) {

                if (ps.setPassword(user.getId(), newPassword)) {
                    passwordLess = false;
                    String userName = user.getUserName();
                    resetPassSettings();
                    Messagebox.show(Labels.getLabel("password_set.success", new String[]{userName}), null, Messagebox.OK, Messagebox.INFORMATION,
                            event -> {
                                if (Messagebox.ON_OK.equals(event.getName())) {
                                    //WebUtils.execRedirect(contextPath + "/user.zul");
                                    //Make redirect in Javascript, using HTTP redirect here behaves abnormally
                                    Clients.response(new AuInvoke("redirectTo", contextPath + "/user.zul"));
                                    //After redirecting this page will be inaccessible, but password reset menu will be shown
                                }
                            });
                } else {
                    UIUtils.showMessageUI(false);
                }
            } else {
                resetPassSettings();
                UIUtils.showMessageUI(false, Labels.getLabel("usr.passreset_nomatch"));
            }
        }

    }

    @NotifyChange("*")
    @Command
    public void cancel() {
        resetPassSettings();
    }

    private void resetPassSettings() {
        newPassword = null;
        newPasswordConfirm = null;
        strength = -1;
    }

}
