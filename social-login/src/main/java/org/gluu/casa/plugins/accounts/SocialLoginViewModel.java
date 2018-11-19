package org.gluu.casa.plugins.accounts;

import org.gluu.casa.plugins.accounts.ldap.ExternalAccount;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.gluu.casa.plugins.accounts.service.SocialLoginService;
import org.gluu.casa.service.ISessionContext;
import org.gluu.casa.ui.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jgomer
 */
public class SocialLoginViewModel {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Pair<Boolean, String>> accounts;

    private List<Provider> providers;

    private SocialLoginService slService;

    @WireVariable
    private ISessionContext sessionContext;

    private String userId;

    public List<Provider> getProviders() {
        return providers;
    }

    @Init
    public void init() {
        logger.debug("Initializing ViewModel");

        userId = sessionContext.getLoggedUser().getId();
        slService = new SocialLoginService();
        providers = slService.getAvailableProviders();
        parseLinkedAccounts();
    }

    public Map<String, Pair<Boolean, String>> getAccounts() {
        return accounts;
    }

    @NotifyChange("providers")
    @Command
    public void disable(String provider) {

        boolean succ = slService.unlink(userId, provider);
        if (succ) {
            parseLinkedAccounts();
        }
        UIUtils.showMessageUI(succ);

    }

    @Command
    public void remove(String provider) {

        Messagebox.show(Labels.getLabel("sociallogin.remove_hint"), null, Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                event -> {
                    if (Messagebox.ON_YES.equals(event.getName())) {
                        if (slService.delete(userId, provider)) {
                            parseLinkedAccounts();
                            UIUtils.showMessageUI(true, Labels.getLabel("sociallogin.removed_link", new String[]{provider}));
                            BindUtils.postNotifyChange(null, null, SocialLoginViewModel.this, "providers");
                        } else {
                            UIUtils.showMessageUI(false);
                        }
                    }
                });

    }

    private void parseLinkedAccounts() {

        List<ExternalAccount> linked = slService.getAccounts(userId, true);
        List<ExternalAccount> unlinked = slService.getAccounts(userId, false);

        accounts = new HashMap<>();
        linked.forEach(acc -> accounts.put(acc.getProvider(), new Pair<>(true, acc.getUid())));
        unlinked.forEach(acc -> accounts.put(acc.getProvider(), new Pair<>(false, acc.getUid())));

        providers.stream().map(Provider::getName).forEach(provider -> {
            if (linked.stream().map(ExternalAccount::getProvider).noneMatch(provider::equals)
                && unlinked.stream().map(ExternalAccount::getProvider).noneMatch(provider::equals)){
                accounts.put(provider, new Pair<>(null, null));
            }
        });

    }

}
