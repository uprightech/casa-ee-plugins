package org.gluu.casa.plugins.strongauthn;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * A plugin for handling second factor authentication settings for administrators and users.
 * @author jgomer
 */
public class StrongAuthnSettingsPlugin extends Plugin {

    public StrongAuthnSettingsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}
