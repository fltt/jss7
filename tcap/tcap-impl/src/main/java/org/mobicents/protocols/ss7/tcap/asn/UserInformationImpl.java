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

package org.mobicents.protocols.ss7.tcap.asn;

import java.io.IOException;

import org.mobicents.protocols.asn.AsnException;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.asn.BitSetStrictLength;
import org.mobicents.protocols.asn.External;
import org.mobicents.protocols.asn.Tag;
import org.mobicents.protocols.ss7.tcap.asn.comp.PAbortCauseType;

/**
 * <p>
 * According to ITU-T Rec Q.773 the UserInformation is defined as
 * </p>
 * <br/>
 * <p>
 * user-information [30] IMPLICIT SEQUENCE OF EXTERNAL
 * </p>
 * <br/>
 * <p>
 * For definition of EXTERNAL look {@link org.mobicents.protocols.asn.External} from Mobicents ASN module
 * </p>
 *
 * @author baranowb
 * @author amit bhayani
 *
 */
public class UserInformationImpl implements UserInformation {

    private External ext = new External();

    /*
     * (non-Javadoc)
     *
     * @see org.mobicents.protocols.asn.External#decode(org.mobicents.protocols.asn.AsnInputStream)
     */

    public void decode(AsnInputStream ais) throws ParseException {

        try {
            AsnInputStream localAis = ais.readSequenceStream();

            int tag = localAis.readTag();
            if (tag != Tag.EXTERNAL || localAis.getTagClass() != Tag.CLASS_UNIVERSAL)
                throw new AsnException("Error decoding UserInformation.sequence: wrong tag or tag class: tag=" + tag
                        + ", tagClass=" + localAis.getTagClass());

            ext.decode(localAis);
        } catch (IOException e) {
            throw new ParseException(PAbortCauseType.BadlyFormattedTxPortion, null,
                    "IOException when decoding UserInformation: " + e.getMessage(), e);
        } catch (AsnException e) {
            throw new ParseException(PAbortCauseType.BadlyFormattedTxPortion, null,
                    "AsnException when decoding UserInformation: " + e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mobicents.protocols.asn.External#encode(org.mobicents.protocols.asn.AsnOutputStream)
     */

    public void encode(AsnOutputStream aos) throws EncodeException {

        try {
            aos.writeTag(Tag.CLASS_CONTEXT_SPECIFIC, false, _TAG);
            int pos = aos.StartContentDefiniteLength();

            ext.encode(aos);

            aos.FinalizeContent(pos);

        } catch (AsnException e) {
            throw new EncodeException("AsnException when encoding UserInformation: " + e.getMessage(), e);
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
