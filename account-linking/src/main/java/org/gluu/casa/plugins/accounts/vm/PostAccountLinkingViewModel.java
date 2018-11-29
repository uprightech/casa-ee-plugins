package org.gluu.casa.plugins.accounts.vm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gluu.casa.misc.WebUtils;
import org.gluu.casa.plugins.accounts.pojo.LinkingSummary;
import org.gluu.casa.plugins.accounts.pojo.PendingLinks;
import org.gluu.casa.service.ISessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author jgomer
 */
public class PostAccountLinkingViewModel {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @WireVariable
    private ISessionContext sessionContext;

    private ObjectMapper mapper = new ObjectMapper();

    private String title;

    private String text;

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    @Init
    public void init() {
        try {
            logger.debug("Initializing ViewModel");
            title = Labels.getLabel("general.error.general");

            String provider = WebUtils.getQueryParam("provider");
            String userId = sessionContext.getLoggedUser().getId();

            LinkingSummary summary = PendingLinks.get(userId, provider);
            if (summary != null) {
                String uid = summary.getUid();

                if (uid != null) {
                    logger.warn("Notifying linking page...");
                    EventQueues.lookup(SocialLoginViewModel.SOCIAL_LINK_QUEUE, EventQueues.SESSION, true)
                            .publish(new Event(SocialLoginViewModel.EVENT_NAME, null, summary));

                    title = Labels.getLabel("sociallogin.linking_result.success");
                    text = Labels.getLabel("sociallogin.linking_result.success_close");
                } else {
                    text = summary.getErrorMessage();
                }
            }
        } catch (Exception e) {
            text = e.getMessage();
            logger.error(text, e);
        }

    }

}
