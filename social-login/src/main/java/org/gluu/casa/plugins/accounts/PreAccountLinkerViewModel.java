package org.gluu.casa.plugins.accounts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;

/**
 * @author jgomer
 */
public class PreAccountLinkerViewModel {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Init
    public void init() {
        logger.debug("Initializing ViewModel");
        String provider = null; //TODO?
        String uid = null; //TODO?
        EventQueues.lookup(SocialLoginViewModel.SOCIAL_LINK_QUEUE, EventQueues.SESSION, true)
                .publish(new Event(provider, null, uid));
    }

}
