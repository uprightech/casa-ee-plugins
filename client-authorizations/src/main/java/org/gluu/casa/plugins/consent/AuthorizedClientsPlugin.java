/*
 * casa is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.casa.plugins.consent;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * A plugin to solve the need described at <code>https://github.com/GluuFederation/cred-manager/issues/49</code>.
 * @author jgomer
 */
public class AuthorizedClientsPlugin extends Plugin {

    public AuthorizedClientsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}
