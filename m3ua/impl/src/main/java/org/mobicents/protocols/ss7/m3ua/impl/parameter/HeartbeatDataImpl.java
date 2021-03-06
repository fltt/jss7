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

package org.mobicents.protocols.ss7.m3ua.impl.parameter;

import org.mobicents.commons.HexTools;
import org.mobicents.protocols.ss7.m3ua.parameter.HeartbeatData;
import org.mobicents.protocols.ss7.m3ua.parameter.Parameter;

/**
 *
 * @author amit bhayani
 *
 */
public class HeartbeatDataImpl extends ParameterImpl implements HeartbeatData {
    private byte[] value = null;

    protected HeartbeatDataImpl(byte[] value) {
        this.tag = Parameter.Heartbeat_Data;
        this.value = value;
    }

    public byte[] getData() {
        return this.value;
    }

    protected byte[] getValue() {
        return this.value;
    }

    public String toString() {
        return String.format("HeartbeatData : data = %s ", HexTools.dump(this.value, 0));
    }
}
