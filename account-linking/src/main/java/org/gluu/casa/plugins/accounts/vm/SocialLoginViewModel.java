package org.gluu.casa.plugins.accounts.vm;

import org.gluu.casa.plugins.accounts.ldap.ExternalAccount;
import org.gluu.casa.plugins.accounts.pojo.LinkingSummary;
import org.gluu.casa.plugins.accounts.pojo.PendingLinks;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.gluu.casa.plugins.accounts.service.AvailableProviders;
import org.gluu.casa.plugins.accounts.service.SocialLoginService;
import org.gluu.casa.service.ISessionContext;
import org.gluu.casa.ui.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.*;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jgomer
 */
public class SocialLoginViewModel {

    public static final String SOCIAL_LINK_QUEUE="social_queue";

    public static final String EVENT_NAME = "linked";

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

    public Map<String, Pair<Boolean, String>> getAccounts() {
        return accounts;
    }

    @Init
    public void init() {
        logger.debug("Initializing ViewModel");

        userId = sessionContext.getLoggedUser().getId();
        slService = new SocialLoginService();
        providers = AvailableProviders.get(true);
        parseLinkedAccounts();

        if (providers.size() > 0) {

            EventQueues.lookup(SOCIAL_LINK_QUEUE, EventQueues.SESSION, true)
                    .subscribe(event -> {
                        if (event.getName().equals(EVENT_NAME)) {

                            logger.info("Received linked event");
                            LinkingSummary summary = (LinkingSummary) event.getData();
                            String provider = summary.getProvider();

                            PendingLinks.remove(userId, provider);
                            //Linking in social network was successful
                            if (slService.link(userId, provider, summary.getUid())) {
                                parseLinkedAccounts();
                                BindUtils.postNotifyChange(null, null, SocialLoginViewModel.this, "providers");
                            }
                        }
                    });
        }

    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireEventListeners(view, this);
    }

    @Listen("onData=#temp")
    public void link(Event evt) {
        PendingLinks.add(userId, evt.getData().toString(), null);
    }

    @NotifyChange("providers")
    @Command
    public void disable(@BindingParam("provider") String provider) {

        boolean succ = slService.unlink(userId, provider);
        if (succ) {
            parseLinkedAccounts();
        }
        UIUtils.showMessageUI(succ);

    }

    @NotifyChange("providers")
    @Command
    public void enable(@BindingParam("provider") String provider) {

        boolean succ = slService.enableLink(userId, provider);
        if (succ) {
            parseLinkedAccounts();
        }
        UIUtils.showMessageUI(succ);

    }

    @Command
    public void remove(@BindingParam("provider") String provider) {

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

        logger.info("Parsing linked/unlinked accounts for {}", userId);
        List<ExternalAccount> linked = slService.getAccounts(userId, true);
        List<ExternalAccount> unlinked = slService.getAccounts(userId, false);

        accounts = new HashMap<>();
        linked.forEach(acc -> accounts.put(acc.getProvider(), new Pair<>(true, acc.getUid())));
        unlinked.forEach(acc -> accounts.put(acc.getProvider(), new Pair<>(false, acc.getUid())));

    }

}
