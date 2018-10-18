/*
 * casa is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
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
