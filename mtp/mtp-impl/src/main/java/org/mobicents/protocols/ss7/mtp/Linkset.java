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

    /** The relation between sls and link */
    private int[] map = new int[16];

    public Linkset() {
        logger = Logger.getLogger(Linkset.class).getLogger("linkset");
    }

    /**
     * Adds link to this link set.
     *
     * @param link the link to add
     */
    public boolean add(Mtp2 link) {
        // add link at the first empty place
        int j = -1;
        for (int i = 0; i < links.length; i++) {
            if (links[i] == null) {
                if (j < 0)
                    j = i;
            } else if (links[i] == link)
                return false;
        }
        if (j < 0)
            return false; // Linkset is full
        links[j] = link;
        count++;
        remap();
        return true;
    }

    /**
     * Removes links from linkset.
     *
     * @param link the link to remove.
     */
    public void remove(Mtp2 link) {
        for (int i = 0; i < links.length; i++) {
            if (links[i] == link) {
                links[i] = null;
                count--;
                remap();
                break;
            }
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
        return (count > 0) ? links[map[sls]] : null;
    }

    /**
     * This method is called each time when number of links has changed to reestablish relation between link selection indicator
     * and link
     */
    private void remap() {
        if (count < 1)
            return;
        int i, sls = -1;
        for (i = 0; i < map.length; i++)
            map[i] = -1;
        for (i = 0; i < links.length; i++) {
            if (links[i] == null)
                continue;
            sls = links[i].getSls();
            if ((map[sls] >= 0) && (logger.isEnabledFor(Level.ERROR)))
                logger.error(String.format("(%s) Duplicate SLC", links[i].getName()));
            map[sls] = i;
        }
        int j, k = -1;
        int[] map2 = new int[map.length + 1];
        for (i = j = 0; i < map.length; i++) {
            if (map[i] < 0)
                continue;
            if (i == sls)
                k = j;
            map2[j++] = map[i];
        }
        map2[j] = -1;
        for (i = 1; i < map.length; i++) {
            if (++sls >= map.length)
                sls = 0;
            j = map2[++k];
            if (j < 0) {
                k = 0;
                j = map2[0];
            }
            if (map[sls] < 0)
                map[sls] = j;
        }
    }
}
