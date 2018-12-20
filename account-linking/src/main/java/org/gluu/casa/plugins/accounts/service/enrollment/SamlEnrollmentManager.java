package org.gluu.casa.plugins.accounts.service.enrollment;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.ExternalIdentityPerson;
import org.gluu.casa.plugins.accounts.pojo.Provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<String> uids = removeProvider(p, provider);

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));
        uids.forEach(uid -> list.add(String.format("passport-saml:%s", uid)));
        p.setOxUnlinkedExternalUids(list.toArray(new String[0]));

        addUidIDP(p, uids);
        return updatePerson(p);

    }

    public boolean enable(ExternalIdentityPerson p) {

        List<String> uids = removeProvider(p, provider);

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxExternalUid()));
        uids.forEach(uid -> list.add(String.format("passport-saml:%s", uid)));
        p.setOxExternalUid(list.toArray(new String[0]));

        addUidIDP(p, uids);
        return updatePerson(p);

    }

    private void addUidIDP(ExternalIdentityPerson p, List<String> uids) {
        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxUidIDP()));
        uids.forEach(uid -> list.add(String.format("%s:%s", uid, provider.getName())));
        p.setOxUidIDP(list.toArray(new String[0]));
    }

    private static List<String> removeProvider(ExternalIdentityPerson p, Provider provider) {

        List<String> externalUids = new ArrayList<>();
        String[] array = p.getOxUidIDP();
        if (Utils.isNotEmpty(array)) {

            Set<Integer> removals = new HashSet<>();

            for (int i = 0; i < array.length; i++) {
                String str = array[i];
                int j = str.indexOf(":");

                if (j > 0 && str.substring(j+1).equals(provider.getName())) {
                    externalUids.add(str.substring(0, j));
                    removals.add(i);
                }
            }

            List<String> newUidIDP = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                if (!removals.contains(i)) {
                    newUidIDP.add(array[i]);
                }
            }

            Set<String> newExternalUids = new HashSet<>(Utils.listfromArray(p.getOxExternalUid()));
            Set<String> newUnlinkedUIds = new HashSet<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));
            for (String uid : externalUids) {
                String str = String.format("passport-saml:%s", uid);
                newExternalUids.remove(str);
                newUnlinkedUIds.remove(str);
            }

            p.setOxExternalUid(newExternalUids.toArray(new String[0]));
            p.setOxUnlinkedExternalUids(newUnlinkedUIds.toArray(new String[0]));
            p.setOxUidIDP(newUidIDP.toArray(new String[0]));
        }

        return externalUids;

    }

}
