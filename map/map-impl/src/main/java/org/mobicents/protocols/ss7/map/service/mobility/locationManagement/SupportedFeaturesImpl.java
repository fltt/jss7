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
package org.mobicents.protocols.ss7.map.service.mobility.locationManagement;

import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.SupportedFeatures;
import org.mobicents.protocols.ss7.map.primitives.BitStringBase;

/**
 *
 * @author Lasith Waruna Perera
 *
 */
public class SupportedFeaturesImpl extends BitStringBase implements SupportedFeatures {

    private static final int _INDEX_odbAllApn = 0;
    private static final int _INDEX_odbHPLMNApn = 1;
    private static final int _INDEX_odbVPLMNApn = 2;
    private static final int _INDEX_odbAllOg = 3;
    private static final int _INDEX_odbAllInternationalOg = 4;
    private static final int _INDEX_odbAllIntOgNotToHPLMNCountry = 5;
    private static final int _INDEX_odbAllInterzonalOg = 6;
    private static final int _INDEX_odbAllInterzonalOgNotToHPLMNCountry = 7;
    private static final int _INDEX_odbAllInterzonalOgandInternatOgNotToHPLMNCountry = 8;
    private static final int _INDEX_regSub = 9;
    private static final int _INDEX_trace = 10;
    private static final int _INDEX_lcsAllPrivExcep = 11;
    private static final int _INDEX_lcsUniversal = 12;
    private static final int _INDEX_lcsCallSessionRelated = 13;
    private static final int _INDEX_lcsCallSessionUnrelated = 14;
    private static final int _INDEX_lcsPLMNOperator = 15;
    private static final int _INDEX_lcsServiceType = 16;
    private static final int _INDEX_lcsAllMOLRSS = 17;
    private static final int _INDEX_lcsBasicSelfLocation = 18;
    private static final int _INDEX_lcsAutonomousSelfLocation = 19;
    private static final int _INDEX_lcsTransferToThirdParty = 20;
    private static final int _INDEX_smMoPp = 21;
    private static final int _INDEX_barringOutgoingCalls = 22;
    private static final int _INDEX_baoc = 23;
    private static final int _INDEX_boic = 24;
    private static final int _INDEX_boicExHC = 25;

    public SupportedFeaturesImpl() {
        super(26, 40, 26, "SupportedFeatures");
    }

    public SupportedFeaturesImpl(boolean odbAllApn, boolean odbHPLMNApn, boolean odbVPLMNApn, boolean odbAllOg,
            boolean odbAllInternationalOg, boolean odbAllIntOgNotToHPLMNCountry, boolean odbAllInterzonalOg,
            boolean odbAllInterzonalOgNotToHPLMNCountry, boolean odbAllInterzonalOgandInternatOgNotToHPLMNCountry,
            boolean regSub, boolean trace, boolean lcsAllPrivExcep, boolean lcsUniversal, boolean lcsCallSessionRelated,
            boolean lcsCallSessionUnrelated, boolean lcsPLMNOperator, boolean lcsServiceType, boolean lcsAllMOLRSS,
            boolean lcsBasicSelfLocation, boolean lcsAutonomousSelfLocation, boolean lcsTransferToThirdParty, boolean smMoPp,
            boolean barringOutgoingCalls, boolean baoc, boolean boic, boolean boicExHC) {
        super(26, 40, 26, "SupportedFeatures");

        if (odbAllApn)
            this.bitString.set(_INDEX_odbAllApn);
        if (odbHPLMNApn)
            this.bitString.set(_INDEX_odbHPLMNApn);
        if (odbVPLMNApn)
            this.bitString.set(_INDEX_odbVPLMNApn);
        if (odbAllOg)
            this.bitString.set(_INDEX_odbAllOg);
        if (odbAllInternationalOg)
            this.bitString.set(_INDEX_odbAllInternationalOg);
        if (odbAllIntOgNotToHPLMNCountry)
            this.bitString.set(_INDEX_odbAllIntOgNotToHPLMNCountry);
        if (odbAllInterzonalOg)
            this.bitString.set(_INDEX_odbAllInterzonalOg);
        if (odbAllInterzonalOgNotToHPLMNCountry)
            this.bitString.set(_INDEX_odbAllInterzonalOgNotToHPLMNCountry);
        if (odbAllInterzonalOgandInternatOgNotToHPLMNCountry)
            this.bitString.set(_INDEX_odbAllInterzonalOgandInternatOgNotToHPLMNCountry);
        if (regSub)
            this.bitString.set(_INDEX_regSub);
        if (trace)
            this.bitString.set(_INDEX_trace);
        if (lcsAllPrivExcep)
            this.bitString.set(_INDEX_lcsAllPrivExcep);
        if (lcsUniversal)
            this.bitString.set(_INDEX_lcsUniversal);
        if (lcsCallSessionRelated)
            this.bitString.set(_INDEX_lcsCallSessionRelated);
        if (lcsCallSessionUnrelated)
            this.bitString.set(_INDEX_lcsCallSessionUnrelated);
        if (lcsPLMNOperator)
            this.bitString.set(_INDEX_lcsPLMNOperator);
        if (lcsServiceType)
            this.bitString.set(_INDEX_lcsServiceType);
        if (lcsAllMOLRSS)
            this.bitString.set(_INDEX_lcsAllMOLRSS);
        if (lcsBasicSelfLocation)
            this.bitString.set(_INDEX_lcsBasicSelfLocation);
        if (lcsAutonomousSelfLocation)
            this.bitString.set(_INDEX_lcsAutonomousSelfLocation);
        if (lcsTransferToThirdParty)
            this.bitString.set(_INDEX_lcsTransferToThirdParty);
        if (smMoPp)
            this.bitString.set(_INDEX_smMoPp);
        if (barringOutgoingCalls)
            this.bitString.set(_INDEX_barringOutgoingCalls);
        if (baoc)
            this.bitString.set(_INDEX_baoc);
        if (boic)
            this.bitString.set(_INDEX_boic);
        if (boicExHC)
            this.bitString.set(_INDEX_boicExHC);
    }

    public boolean getOdbAllApn() {
        return this.bitString.get(_INDEX_odbAllApn);
    }

    public boolean getOdbHPLMNApn() {
        return this.bitString.get(_INDEX_odbHPLMNApn);
    }

    public boolean getOdbVPLMNApn() {
        return this.bitString.get(_INDEX_odbVPLMNApn);
    }

    public boolean getOdbAllOg() {
        return this.bitString.get(_INDEX_odbAllOg);
    }

    public boolean getOdbAllInternationalOg() {
        return this.bitString.get(_INDEX_odbAllInternationalOg);
    }

    public boolean getOdbAllIntOgNotToHPLMNCountry() {
        return this.bitString.get(_INDEX_odbAllIntOgNotToHPLMNCountry);
    }

    public boolean getOdbAllInterzonalOg() {
        return this.bitString.get(_INDEX_odbAllInterzonalOg);
    }

    public boolean getOdbAllInterzonalOgNotToHPLMNCountry() {
        return this.bitString.get(_INDEX_odbAllInterzonalOgNotToHPLMNCountry);
    }

    public boolean getOdbAllInterzonalOgandInternatOgNotToHPLMNCountry() {
        return this.bitString.get(_INDEX_odbAllInterzonalOgandInternatOgNotToHPLMNCountry);
    }

    public boolean getRegSub() {
        return this.bitString.get(_INDEX_regSub);
    }

    public boolean getTrace() {
        return this.bitString.get(_INDEX_trace);
    }

    public boolean getLcsAllPrivExcep() {
        return this.bitString.get(_INDEX_lcsAllPrivExcep);
    }

    public boolean getLcsUniversal() {
        return this.bitString.get(_INDEX_lcsUniversal);
    }

    public boolean getLcsCallSessionRelated() {
        return this.bitString.get(_INDEX_lcsCallSessionRelated);
    }

    public boolean getLcsCallSessionUnrelated() {
        return this.bitString.get(_INDEX_lcsCallSessionUnrelated);
    }

    public boolean getLcsPLMNOperator() {
        return this.bitString.get(_INDEX_lcsPLMNOperator);
    }

    public boolean getLcsServiceType() {
        return this.bitString.get(_INDEX_lcsServiceType);
    }

    public boolean getLcsAllMOLRSS() {
        return this.bitString.get(_INDEX_lcsAllMOLRSS);
    }

    public boolean getLcsBasicSelfLocation() {
        return this.bitString.get(_INDEX_lcsBasicSelfLocation);
    }

    public boolean getLcsAutonomousSelfLocation() {
        return this.bitString.get(_INDEX_lcsAutonomousSelfLocation);
    }

    public boolean getLcsTransferToThirdParty() {
        return this.bitString.get(_INDEX_lcsTransferToThirdParty);
    }

    public boolean getSmMoPp() {
        return this.bitString.get(_INDEX_smMoPp);
    }

    public boolean getBarringOutgoingCalls() {
        return this.bitString.get(_INDEX_barringOutgoingCalls);
    }

    public boolean getBaoc() {
        return this.bitString.get(_INDEX_baoc);
    }

    public boolean getBoic() {
        return this.bitString.get(_INDEX_boic);
    }

    public boolean getBoicExHC() {
        return this.bitString.get(_INDEX_boicExHC);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_PrimitiveName);
        sb.append(" [");
        if (this.getOdbAllApn())
            sb.append("odbAllApn, ");
        if (this.getOdbHPLMNApn())
            sb.append("odbHPLMNApn, ");
        if (this.getOdbVPLMNApn())
            sb.append("odbVPLMNApn, ");
        if (this.getOdbAllOg())
            sb.append("odbAllOg, ");
        if (this.getOdbAllInternationalOg())
            sb.append("odbAllInternationalOg, ");
        if (this.getOdbAllIntOgNotToHPLMNCountry())
            sb.append("odbAllIntOgNotToHPLMNCountry, ");
        if (this.getOdbAllInterzonalOg())
            sb.append("odbAllInterzonalOg, ");
        if (this.getOdbAllInterzonalOgNotToHPLMNCountry())
            sb.append("odbAllInterzonalOgNotToHPLMNCountry, ");
        if (this.getOdbAllInterzonalOgandInternatOgNotToHPLMNCountry())
            sb.append("odbAllInterzonalOgandInternatOgNotToHPLMNCountry, ");
        if (this.getRegSub())
            sb.append("regSub, ");
        if (this.getTrace())
            sb.append("trace, ");
        if (this.getLcsAllPrivExcep())
            sb.append("lcsAllPrivExcep, ");
        if (this.getLcsUniversal())
            sb.append("lcsUniversal, ");
        if (this.getLcsCallSessionRelated())
            sb.append("lcsCallSessionRelated, ");
        if (this.getLcsCallSessionUnrelated())
            sb.append("lcsCallSessionUnrelated, ");
        if (this.getLcsPLMNOperator())
            sb.append("lcsPLMNOperator, ");
        if (this.getLcsServiceType())
            sb.append("lcsServiceType, ");
        if (this.getLcsAllMOLRSS())
            sb.append("lcsAllMOLRSS, ");
        if (this.getLcsBasicSelfLocation())
            sb.append("lcsBasicSelfLocation, ");
        if (this.getLcsAutonomousSelfLocation())
            sb.append("lcsAutonomousSelfLocation, ");
        if (this.getLcsTransferToThirdParty())
            sb.append("lcsTransferToThirdParty, ");
        if (this.getSmMoPp())
            sb.append("smMoPp, ");
        if (this.getBarringOutgoingCalls())
            sb.append("barringOutgoingCalls, ");
        if (this.getBaoc())
            sb.append("baoc, ");
        if (this.getBoic())
            sb.append("boic, ");
        if (this.getBoicExHC())
            sb.append("boicExHC ");

        sb.append("]");
        return sb.toString();
    }

}
