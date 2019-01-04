/*
 * casa is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.casa.plugins.consent.service;

import com.unboundid.ldap.sdk.DN;
import org.gluu.casa.plugins.consent.ldap.Client;
import org.gluu.casa.core.ldap.BaseLdapPerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Small utility for OpenID clients
 * @author jgomer
 */
public class ClientService {

    private ILdapService ldapService;

    public ClientService() {
        ldapService = Utils.managedBean(ILdapService.class);
    }

    public List<String> getAssociatedPeople(Client client) {
        List<DN> dns = client.getAssociatedPersonAsList();

        return dns.stream().map(dn -> ldapService.get(BaseLdapPerson.class, dn.toString()))
                .filter(person -> person != null).map(BaseLdapPerson::getUid).collect(Collectors.toList());

    }

}
