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
package org.mobicents.protocols.ss7.cap.service.gprs.primitive;

import java.io.IOException;

import org.mobicents.protocols.asn.AsnException;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.asn.Tag;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPParsingComponentException;
import org.mobicents.protocols.ss7.cap.api.CAPParsingComponentExceptionReason;
import org.mobicents.protocols.ss7.cap.api.service.gprs.primitive.TransferredVolume;
import org.mobicents.protocols.ss7.cap.api.service.gprs.primitive.VolumeIfTariffSwitch;
import org.mobicents.protocols.ss7.cap.primitives.CAPAsnPrimitive;
import org.mobicents.protocols.ss7.map.api.MAPParsingComponentException;

/**
 *
 * @author Lasith Waruna Perera
 *
 */
public class TransferredVolumeImpl implements TransferredVolume, CAPAsnPrimitive {

    public static final String _PrimitiveName = "TransferredVolume";

    public static final int _ID_volumeIfNoTariffSwitch = 0;
    public static final int _ID_volumeIfTariffSwitch = 1;

    private Long volumeIfNoTariffSwitch;
    private VolumeIfTariffSwitch volumeIfTariffSwitch;

    public TransferredVolumeImpl() {

    }

    public TransferredVolumeImpl(Long volumeIfNoTariffSwitch) {
        this.volumeIfNoTariffSwitch = volumeIfNoTariffSwitch;
    }

    public TransferredVolumeImpl(VolumeIfTariffSwitch volumeIfTariffSwitch) {
        this.volumeIfTariffSwitch = volumeIfTariffSwitch;
    }

    public Long getVolumeIfNoTariffSwitch() {
        return this.volumeIfNoTariffSwitch;
    }

    public VolumeIfTariffSwitch getVolumeIfTariffSwitch() {
        return this.volumeIfTariffSwitch;
    }

    public int getTag() throws CAPException {
        if (volumeIfNoTariffSwitch != null) {
            return _ID_volumeIfNoTariffSwitch;
        } else {
            return _ID_volumeIfTariffSwitch;
        }
    }

    public int getTagClass() {
        return Tag.CLASS_CONTEXT_SPECIFIC;
    }

    public boolean getIsPrimitive() {
        if (volumeIfNoTariffSwitch != null) {
            return true;
        } else {
            return false;
        }
    }

    public void decodeAll(AsnInputStream ansIS) throws CAPParsingComponentException {
        try {
            int length = ansIS.readLength();
            this._decode(ansIS, length);
        } catch (IOException e) {
            throw new CAPParsingComponentException("IOException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
                    CAPParsingComponentExceptionReason.MistypedParameter);
        } catch (AsnException e) {
            throw new CAPParsingComponentException("AsnException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
                    CAPParsingComponentExceptionReason.MistypedParameter);
        } catch (MAPParsingComponentException e) {
            throw new CAPParsingComponentException("MAPParsingComponentException when decoding " + _PrimitiveName + ": "
                    + e.getMessage(), e, CAPParsingComponentExceptionReason.MistypedParameter);
        }
    }

    public void decodeData(AsnInputStream ansIS, int length) throws CAPParsingComponentException {

        try {
            this._decode(ansIS, length);
        } catch (IOException e) {
            throw new CAPParsingComponentException("IOException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
                    CAPParsingComponentExceptionReason.MistypedParameter);
        } catch (AsnException e) {
            throw new CAPParsingComponentException("AsnException when decoding " + _PrimitiveName + ": " + e.getMessage(), e,
                    CAPParsingComponentExceptionReason.MistypedParameter);
        } catch (MAPParsingComponentException e) {
            throw new CAPParsingComponentException("MAPParsingComponentException when decoding " + _PrimitiveName + ": "
                    + e.getMessage(), e, CAPParsingComponentExceptionReason.MistypedParameter);
        }

    }

    private void _decode(AsnInputStream ais, int length) throws CAPParsingComponentException, IOException, AsnException,
            MAPParsingComponentException {

        this.volumeIfNoTariffSwitch = null;
        this.volumeIfTariffSwitch = null;

        int tag = ais.getTag();

        if (ais.getTagClass() == Tag.CLASS_CONTEXT_SPECIFIC) {
            switch (tag) {
                case _ID_volumeIfNoTariffSwitch:
                    if (!ais.isTagPrimitive())
                        throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName
                                + ".volumeIfNoTariffSwitch: Parameter is not primitive",
                                CAPParsingComponentExceptionReason.MistypedParameter);
                    this.volumeIfNoTariffSwitch = ais.readIntegerData(length);
                    break;
                case _ID_volumeIfTariffSwitch:
                    if (ais.isTagPrimitive())
                        throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName
                                + ".volumeIfTariffSwitch: Parameter is primitive",
                                CAPParsingComponentExceptionReason.MistypedParameter);
                    this.volumeIfTariffSwitch = new VolumeIfTariffSwitchImpl();
                    ((VolumeIfTariffSwitchImpl) this.volumeIfTariffSwitch).decodeData(ais, length);
                    break;

                default:
                    throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName + ": bad choice tag",
                            CAPParsingComponentExceptionReason.MistypedParameter);
            }
        } else {
            throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName + ": bad choice tagClass",
                    CAPParsingComponentExceptionReason.MistypedParameter);
        }

    }

    public void encodeAll(AsnOutputStream asnOs) throws CAPException {
        this.encodeAll(asnOs, this.getTagClass(), this.getTag());
    }

    public void encodeAll(AsnOutputStream asnOs, int tagClass, int tag) throws CAPException {

        try {
            asnOs.writeTag(tagClass, this.getIsPrimitive(), tag);
            int pos = asnOs.StartContentDefiniteLength();
            this.encodeData(asnOs);
            asnOs.FinalizeContent(pos);
        } catch (AsnException e) {
            throw new CAPException("AsnException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
        }
    }

    public void encodeData(AsnOutputStream asnOs) throws CAPException {
        if (this.volumeIfNoTariffSwitch == null && this.volumeIfTariffSwitch == null || this.volumeIfNoTariffSwitch != null
                && this.volumeIfTariffSwitch != null) {
            throw new CAPException("Error while decoding " + _PrimitiveName + ": One and only one choice must be selected");
        }

        try {
            if (this.volumeIfNoTariffSwitch != null) {
                asnOs.writeIntegerData(volumeIfNoTariffSwitch.longValue());
            } else {
                ((VolumeIfTariffSwitchImpl) this.volumeIfTariffSwitch).encodeData(asnOs);
            }
        } catch (IOException e) {
            throw new CAPException("MAPException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_PrimitiveName + " [");

        if (this.volumeIfNoTariffSwitch != null) {
            sb.append("volumeIfNoTariffSwitch=");
            sb.append(this.volumeIfNoTariffSwitch.toString());
        }

        if (this.volumeIfTariffSwitch != null) {
            sb.append("volumeIfTariffSwitch=");
            sb.append(this.volumeIfTariffSwitch.toString());
        }

        sb.append("]");

        return sb.toString();
    }

}
