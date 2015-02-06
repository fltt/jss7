/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.protocols.ss7.mtp;

import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implements relation between link code and signaling link selection indicator.
 *
 * @author kulikov
 */
public class Linkset {
    /** The list of links. Maximum available 16 links */
    private Mtp2[] links = new Mtp2[16];
    private int count;
    private final Logger logger;

    public Linkset() {
        logger = Logger.getLogger(Linkset.class).getLogger("linkset");
    }

    /**
     * Adds link to this link set.
     *
     * @param link the link to add
     */
    public boolean add(Mtp2 link) {
        if ((count >= links.length) || (link == null))
            return false;
        if (count++ == 0) {
            Arrays.fill(links, link);
            return true;
        }
        int i = link.getSls();
        if ((links[i].getSls() == i) && (logger.isEnabledFor(Level.ERROR)))
            logger.error(String.format("(%s) Duplicate SLC (%s)", link.getName(), links[i].getName()));
        links[i] = link;
        int quota = links.length / count;
        if (--quota == 0)
            return true;
        Vector<Integer> vi;
        Map<Mtp2, Vector<Integer>> assignedSls = new IdentityHashMap<Mtp2, Vector<Integer>>();
        for (i = 0; i < links.length; i++) {
            if (links[i].getSls() == i)
                continue;
            if (links[i] == link)
                return false;
            vi = assignedSls.get(links[i]);
            if (vi == null) {
                vi = new Vector<Integer>();
                assignedSls.put(links[i], vi);
            }
            vi.add(new Integer(i));
        }
        for (Map.Entry<Mtp2, Vector<Integer>> as : assignedSls.entrySet())
            Collections.shuffle(as.getValue());
        Integer sls;
        while (quota-- > 0) {
            Vector<Map.Entry<Mtp2, Vector<Integer>>> linkslist = new Vector<Map.Entry<Mtp2, Vector<Integer>>>(assignedSls.entrySet());
            // Take SLSs for the new link from links with greatest number of assigned SLSs
            Collections.sort(linkslist, new Comparator<Map.Entry<Mtp2, Vector<Integer>>>() {
                    public int compare(Map.Entry<Mtp2, Vector<Integer>> a, Map.Entry<Mtp2, Vector<Integer>> b) {
                        return b.getValue().size() - a.getValue().size();
                    }
                });
            vi = linkslist.firstElement().getValue();
            sls = vi.iterator().next();
            vi.remove(sls);
            links[sls.intValue()] = link;
        }
        return true;
    }

    /**
     * Removes links from linkset.
     *
     * @param link the link to remove.
     */
    public void remove(Mtp2 link) {
        if (link == null)
            return;
        Vector<Integer> vi;
        Map<Mtp2, Vector<Integer>> assignedSls = new IdentityHashMap<Mtp2, Vector<Integer>>();
        for (int i = 0; i < links.length; i++) {
            vi = assignedSls.get(links[i]);
            if (vi == null) {
                vi = new Vector<Integer>();
                assignedSls.put(links[i], vi);
            }
            vi.add(new Integer(i));
        }
        Vector<Integer> freeSls = assignedSls.remove(link);
        if (freeSls == null)
            return;
        if (--count == 0) {
            Arrays.fill(links, null);
            return;
        }
        Collections.shuffle(freeSls);
        for (Integer sls : freeSls) {
            Vector<Map.Entry<Mtp2, Vector<Integer>>> linkslist = new Vector<Map.Entry<Mtp2, Vector<Integer>>>(assignedSls.entrySet());
            // Give freed SLSs to links with smallest number of assigned SLSs
            Collections.sort(linkslist, new Comparator<Map.Entry<Mtp2, Vector<Integer>>>() {
                    public int compare(Map.Entry<Mtp2, Vector<Integer>> a, Map.Entry<Mtp2, Vector<Integer>> b) {
                        return a.getValue().size() - b.getValue().size();
                    }
                });
            linkslist.firstElement().getValue().add(sls);
            links[sls.intValue()] = linkslist.firstElement().getKey();
        }
    }

    /**
     * Gets the state of the link.
     *
     * @return true if linkset has at least one active link.
     */
    public boolean isActive() {
        return count > 0;
    }

    /**
     * Selects the link using specified link selection indicator.
     *
     * @param sls signaling link selection indicator.
     * @return
     */
    public Mtp2 select(byte sls) {
        return links[sls];
    }
}
