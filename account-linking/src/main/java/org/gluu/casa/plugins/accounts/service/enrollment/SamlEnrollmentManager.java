package org.gluu.casa.plugins.accounts.service.enrollment;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.plugins.accounts.pojo.Provider;

import java.util.*;

/**
 * @author jgomer
 */
public class SamlEnrollmentManager extends AbstractEnrollmentManager {

    private static final String OXEXTERNALUID_PREFIX = "passport-saml:";

    public SamlEnrollmentManager(Provider provider) {
        super(provider);
    }

    public String getUid(ExternalIdentityPerson p, boolean linked) {

        List<String> list = Utils.listfromArray(linked ? p.getOxExternalUid() : p.getOxUnlinkedExternalUids());
        for (String externalUid : list) {
            if (externalUid.startsWith(OXEXTERNALUID_PREFIX)) {
                String uid = externalUid.substring(OXEXTERNALUID_PREFIX.length());

                if (Utils.listfromArray(p.getOxUidIDP()).stream().anyMatch((uid + ":" + provider.getName())::equals)) {
                    return uid;
                }
            }
        }
        return null;

    }

    public boolean link(ExternalIdentityPerson p, String externalId) {

        Set<String> set = new HashSet<>(Utils.listfromArray(p.getOxExternalUid()));
        set.add(String.format("passport-saml:%s", externalId));

        Set<String> set2 = new HashSet<>(Utils.listfromArray(p.getOxUidIDP()));
        set2.add(String.format("%s:%s", externalId, provider.getName()));
        logger.info("Linked accounts for {} will be {}", p.getUid(), set2);

        p.setOxExternalUid(set.toArray(new String[0]));
        p.setOxUidIDP(set2.toArray(new String[0]));
        return updatePerson(p);
    }

    public boolean remove(ExternalIdentityPerson p) {
        removeProvider(p, provider);
        return updatePerson(p);
    }

    public boolean unlink(ExternalIdentityPerson p) {

        String uid = removeProvider(p, provider);
        if (uid == null) {
            return false;
        }

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));
        list.add(String.format("passport-saml:%s", uid));
        p.setOxUnlinkedExternalUids(list.toArray(new String[0]));

        addUidIDP(p, uid);
        return updatePerson(p);

    }

    public boolean enable(ExternalIdentityPerson p) {

        String uid = removeProvider(p, provider);
        if (uid == null) {
            return false;
        }

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxExternalUid()));
        list.add(String.format("passport-saml:%s", uid));
        p.setOxExternalUid(list.toArray(new String[0]));

        addUidIDP(p, uid);
        return updatePerson(p);

    }

    private void addUidIDP(ExternalIdentityPerson p, String uid) {
        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxUidIDP()));
        list.add(String.format("%s:%s", uid, provider.getName()));
        p.setOxUidIDP(list.toArray(new String[0]));
    }

    private static String removeProvider(ExternalIdentityPerson p, Provider provider) {

        String externalUid = null;
        String[] array = p.getOxUidIDP();
        if (Utils.isNotEmpty(array)) {

            int removal = -1;
            for (int i = 0; i < array.length; i++) {
                String str = array[i];
                int j = str.indexOf(":");

                if (j > 0 && str.substring(j+1).equals(provider.getName())) {
                    externalUid = str.substring(0, j);
                    removal = i;
                    break;
                }
            }

            List<String> newUidIDP = new ArrayList<>(Arrays.asList(array));
            Set<String> newExternalUids = new HashSet<>(Utils.listfromArray(p.getOxExternalUid()));
            Set<String> newUnlinkedUIds = new HashSet<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));

            if (externalUid != null) {
                String str = String.format("passport-saml:%s", externalUid);
                newExternalUids.remove(str);
                newUnlinkedUIds.remove(str);
                newUidIDP.remove(removal);
            }

            p.setOxExternalUid(newExternalUids.toArray(new String[0]));
            p.setOxUnlinkedExternalUids(newUnlinkedUIds.toArray(new String[0]));
            p.setOxUidIDP(newUidIDP.toArray(new String[0]));
        }

        return externalUid;

    }

}
