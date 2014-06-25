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

package org.mobicents.protocols.ss7.tcapAnsi.asn;

import org.mobicents.protocols.asn.AsnException;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.asn.BitSetStrictLength;
import org.mobicents.protocols.asn.External;
import org.mobicents.protocols.ss7.tcapAnsi.api.asn.EncodeException;
import org.mobicents.protocols.ss7.tcapAnsi.api.asn.ParseException;
import org.mobicents.protocols.ss7.tcapAnsi.api.asn.UserInformationElement;
import org.mobicents.protocols.ss7.tcapAnsi.api.asn.comp.PAbortCause;

/**
*
* @author baranowb
* @author amit bhayani
*
*/
public class UserInformationElementImpl implements UserInformationElement {

    private External ext = new External();


    /*
     * (non-Javadoc)
     *
     * @see org.mobicents.protocols.asn.External#decode(org.mobicents.protocols.asn.AsnInputStream)
     */

    public void decode(AsnInputStream ais) throws ParseException {

        try {
            ext.decode(ais);
        } catch (AsnException e) {
            throw new ParseException(PAbortCause.BadlyStructuredDialoguePortion, "AsnException while decoding UserInformationElement: " + e.getMessage(), e);
        }
    }

    public void encode(AsnOutputStream aos) throws EncodeException {

        try {
            ext.encode(aos);
        } catch (AsnException e) {
            throw new EncodeException("AsnException when encoding UserInformationElement: " + e.getMessage(), e);
        }
    }

    public void encode(AsnOutputStream aos, int tagClass, int tag) throws EncodeException {

        try {
            ext.encode(aos, tagClass, tag);
        } catch (AsnException e) {
            throw new EncodeException("AsnException when encoding UserInformationElement: " + e.getMessage(), e);
        }
    }

    public byte[] getEncodeType() throws AsnException {
        return ext.getEncodeType();
    }

    public void setEncodeType(byte[] data) {
        ext.setEncodeType(data);
    }

    public BitSetStrictLength getEncodeBitStringType() throws AsnException {
        return ext.getEncodeBitStringType();
    }

    public void setEncodeBitStringType(BitSetStrictLength data) {
        ext.setEncodeBitStringType(data);
    }

    public boolean isOid() {
        return ext.isOid();
    }

    public void setOid(boolean oid) {
        ext.setOid(oid);
    }

    public boolean isInteger() {
        return ext.isInteger();
    }

    public void setInteger(boolean integer) {
        ext.setInteger(integer);
    }

    public boolean isObjDescriptor() {
        return ext.isObjDescriptor();
    }

    public void setObjDescriptor(boolean objDescriptor) {
        ext.setObjDescriptor(objDescriptor);
    }

    public long[] getOidValue() {
        return ext.getOidValue();
    }

    public void setOidValue(long[] oidValue) {
        ext.setOidValue(oidValue);
    }

    public long getIndirectReference() {
        return ext.getIndirectReference();
    }

    public void setIndirectReference(long indirectReference) {
        ext.setIndirectReference(indirectReference);
    }

    public String getObjDescriptorValue() {
        return ext.getObjDescriptorValue();
    }

    public void setObjDescriptorValue(String objDescriptorValue) {
        ext.setObjDescriptorValue(objDescriptorValue);
    }

    public boolean isAsn() {
        return ext.isAsn();
    }

    public void setAsn(boolean asn) {
        ext.setAsn(asn);
    }

    public boolean isOctet() {
        return ext.isOctet();
    }

    public void setOctet(boolean octet) {
        ext.setOctet(octet);
    }

    public boolean isArbitrary() {
        return ext.isArbitrary();
    }

    public void setArbitrary(boolean arbitrary) {
        ext.setArbitrary(arbitrary);
    }

}
