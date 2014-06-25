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

package org.mobicents.protocols.ss7.map.service.mobility.imei;

import org.mobicents.protocols.asn.BitSetStrictLength;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.UESBIIuB;
import org.mobicents.protocols.ss7.map.primitives.BitStringBase;

/**
 *
 * @author normandes
 *
 */
public class UESBIIuBImpl extends BitStringBase implements UESBIIuB {

    public static final String _PrimitiveName = "UESBIIuB";

    public UESBIIuBImpl() {
        super(1, 128, 1, _PrimitiveName);
    }

    public UESBIIuBImpl(BitSetStrictLength data) {
        super(1, 128, data.getStrictLength(), _PrimitiveName, data);
    }

    public BitSetStrictLength getData() {
        return bitString;
    }

}
