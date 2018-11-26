package org.gluu.casa.plugins.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gluu.casa.plugins.accounts.pojo.LinkingSummary;
import org.gluu.casa.plugins.accounts.service.LinkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;

/**
 * @author jgomer
 */
public class PostAccountLinkingViewModel {

    private Logger logger = LoggerFactory.getLogger(getClass());

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
            String data = Executions.getCurrent().getHeader(LinkingService.CUSTOM_HEADER);
            LinkingSummary summary = mapper.readValue(data, LinkingSummary.class);

            String errorMessage = summary.getErrorMessage();
            String provider = summary.getProvider();
            String uid = summary.getUid();

            if (provider != null && uid != null) {
                logger.warn("Notifying linking page...");
                EventQueues.lookup(SocialLoginViewModel.SOCIAL_LINK_QUEUE, EventQueues.SESSION, true)
                        .publish(new Event(provider, null, uid));

                title = Labels.getLabel("sociallogin.linking_result.success");
                text = Labels.getLabel("sociallogin.linking_result.success_close");
            } else {
                title = Labels.getLabel("general.error.general");
                text = errorMessage;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
