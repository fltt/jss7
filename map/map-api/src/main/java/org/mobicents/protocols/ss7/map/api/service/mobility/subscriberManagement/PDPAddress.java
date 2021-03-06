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

package org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement;

import java.io.Serializable;

/**
 *
 PDP-Address ::= OCTET STRING (SIZE (1..16)) -- Octets are coded according to TS 3GPP TS 29.060 [105]
 *
 * -- The possible size values are: -- 1-7 octets X.25 address type -- 4 octets IPv4 address type -- 16 octets Ipv6 address type
 *
 *
 * @author sergey vetyutnev
 *
 */
public interface PDPAddress extends Serializable {

    byte[] getData();

}