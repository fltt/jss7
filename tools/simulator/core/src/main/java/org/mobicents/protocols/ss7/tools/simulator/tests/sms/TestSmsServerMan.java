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

package org.mobicents.protocols.ss7.tools.simulator.tests.sms;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Level;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.errors.SMEnumeratedDeliveryFailureCause;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.sms.AlertServiceCentreRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.AlertServiceCentreResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.ForwardShortMessageRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.ForwardShortMessageResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.InformServiceCentreRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;
import org.mobicents.protocols.ss7.map.api.service.sms.MWStatus;
import org.mobicents.protocols.ss7.map.api.service.sms.MoForwardShortMessageRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.MoForwardShortMessageResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.MtForwardShortMessageRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.MtForwardShortMessageResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.ReportSMDeliveryStatusRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.ReportSMDeliveryStatusResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.SMDeliveryOutcome;
import org.mobicents.protocols.ss7.map.api.service.sms.SM_RP_DA;
import org.mobicents.protocols.ss7.map.api.service.sms.SM_RP_OA;
import org.mobicents.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.SmsSignalInfo;
import org.mobicents.protocols.ss7.map.api.smstpdu.AbsoluteTimeStamp;
import org.mobicents.protocols.ss7.map.api.smstpdu.AddressField;
import org.mobicents.protocols.ss7.map.api.smstpdu.DataCodingScheme;
import org.mobicents.protocols.ss7.map.api.smstpdu.NumberingPlanIdentification;
import org.mobicents.protocols.ss7.map.api.smstpdu.ProtocolIdentifier;
import org.mobicents.protocols.ss7.map.api.smstpdu.SmsDeliverTpdu;
import org.mobicents.protocols.ss7.map.api.smstpdu.SmsSubmitTpdu;
import org.mobicents.protocols.ss7.map.api.smstpdu.SmsTpdu;
import org.mobicents.protocols.ss7.map.api.smstpdu.TypeOfNumber;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserData;
import org.mobicents.protocols.ss7.map.smstpdu.AbsoluteTimeStampImpl;
import org.mobicents.protocols.ss7.map.smstpdu.AddressFieldImpl;
import org.mobicents.protocols.ss7.map.smstpdu.DataCodingSchemeImpl;
import org.mobicents.protocols.ss7.map.smstpdu.ProtocolIdentifierImpl;
import org.mobicents.protocols.ss7.map.smstpdu.SmsDeliverTpduImpl;
import org.mobicents.protocols.ss7.map.smstpdu.UserDataImpl;
import org.mobicents.protocols.ss7.tcap.api.MessageType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;
import org.mobicents.protocols.ss7.tools.simulator.common.AddressNatureType;
import org.mobicents.protocols.ss7.tools.simulator.common.TesterBase;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapProtocolVersion;
import org.mobicents.protocols.ss7.tools.simulator.level3.NumberingPlanMapType;
import org.mobicents.protocols.ss7.tools.simulator.management.TesterHost;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class TestSmsServerMan extends TesterBase implements TestSmsServerManMBean, Stoppable, MAPDialogListener, MAPServiceSmsListener {

    public static String SOURCE_NAME = "TestSmsServer";

    private final String name;
    private MapMan mapMan;

    private boolean isStarted = false;
    private int countSriReq = 0;
    private int countSriResp = 0;
    private int countMtFsmReq = 0;
    private int countMtFsmResp = 0;
    private int countMoFsmReq = 0;
    private int countMoFsmResp = 0;
    private int countIscReq = 0;
    private int countErrRcvd = 0;
    private int countErrSent = 0;
    private int countRsmdsReq = 0;
    private int countRsmdsResp = 0;
    private int countAscReq = 0;
    private int countAscResp = 0;
    private String currentRequestDef = "";
    private boolean needSendSend = false;
    private boolean needSendClose = false;

    public TestSmsServerMan() {
        super(SOURCE_NAME);
        this.name = "???";
    }

    public TestSmsServerMan(String name) {
        super(SOURCE_NAME);
        this.name = name;
    }

    public void setTesterHost(TesterHost testerHost) {
        this.testerHost = testerHost;
    }

    public void setMapMan(MapMan val) {
        this.mapMan = val;
    }

    public AddressNatureType getAddressNature() {
        return new AddressNatureType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getAddressNature().getIndicator());
    }

    public String getAddressNature_Value() {
        return new AddressNatureType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getAddressNature().getIndicator()).toString();
    }

    public void setAddressNature(AddressNatureType val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setAddressNature(AddressNature.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public NumberingPlanMapType getNumberingPlan() {
        return new NumberingPlanMapType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlan().getIndicator());
    }

    public String getNumberingPlan_Value() {
        return new NumberingPlanMapType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlan().getIndicator())
                .toString();
    }

    public void setNumberingPlan(NumberingPlanMapType val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setNumberingPlan(NumberingPlan.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public String getServiceCenterAddress() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getServiceCenterAddress();
    }

    public void setServiceCenterAddress(String val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setServiceCenterAddress(val);
        this.testerHost.markStore();
    }

    public MapProtocolVersion getMapProtocolVersion() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getMapProtocolVersion();
    }

    public String getMapProtocolVersion_Value() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getMapProtocolVersion().toString();
    }

    public void setMapProtocolVersion(MapProtocolVersion val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setMapProtocolVersion(val);
        this.testerHost.markStore();
    }

    public int getHlrSsn() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getHlrSsn();
    }

    public void setHlrSsn(int val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setHlrSsn(val);
        this.testerHost.markStore();
    }

    public int getVlrSsn() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getVlrSsn();
    }

    public void setVlrSsn(int val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setVlrSsn(val);
        this.testerHost.markStore();
    }

    public TypeOfNumberType getTypeOfNumber() {
        return new TypeOfNumberType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getTypeOfNumber().getCode());
    }

    public String getTypeOfNumber_Value() {
        return new TypeOfNumberType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getTypeOfNumber().getCode()).toString();
    }

    public void setTypeOfNumber(TypeOfNumberType val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setTypeOfNumber(TypeOfNumber.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public NumberingPlanIdentificationType getNumberingPlanIdentification() {
        return new NumberingPlanIdentificationType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlanIdentification()
                .getCode());
    }

    public String getNumberingPlanIdentification_Value() {
        return new NumberingPlanIdentificationType(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlanIdentification()
                .getCode()).toString();
    }

    public void setNumberingPlanIdentification(NumberingPlanIdentificationType val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData()
                .setNumberingPlanIdentification(NumberingPlanIdentification.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public SmsCodingType getSmsCodingType() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getSmsCodingType();
    }

    public String getSmsCodingType_Value() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getSmsCodingType().toString();
    }

    public void setSmsCodingType(SmsCodingType val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setSmsCodingType(val);
        this.testerHost.markStore();
    }

    public boolean isSendSrsmdsIfError() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().isSendSrsmdsIfError();
    }

    public void setSendSrsmdsIfError(boolean val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setSendSrsmdsIfError(val);
        this.testerHost.markStore();
    }

    public boolean isGprsSupportIndicator() {
        return this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().isGprsSupportIndicator();
    }

    public void setGprsSupportIndicator(boolean val) {
        this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().setGprsSupportIndicator(val);
        this.testerHost.markStore();
    }

    public void putAddressNature(String val) {
        AddressNatureType x = AddressNatureType.createInstance(val);
        if (x != null)
            this.setAddressNature(x);
    }

    public void putNumberingPlan(String val) {
        NumberingPlanMapType x = NumberingPlanMapType.createInstance(val);
        if (x != null)
            this.setNumberingPlan(x);
    }

    public void putMapProtocolVersion(String val) {
        MapProtocolVersion x = MapProtocolVersion.createInstance(val);
        if (x != null)
            this.setMapProtocolVersion(x);
    }

    public void putTypeOfNumber(String val) {
        TypeOfNumberType x = TypeOfNumberType.createInstance(val);
        if (x != null)
            this.setTypeOfNumber(x);
    }

    public void putNumberingPlanIdentification(String val) {
        NumberingPlanIdentificationType x = NumberingPlanIdentificationType.createInstance(val);
        if (x != null)
            this.setNumberingPlanIdentification(x);
    }

    public void putSmsCodingType(String val) {
        SmsCodingType x = SmsCodingType.createInstance(val);
        if (x != null)
            this.setSmsCodingType(x);
    }

    public String getCurrentRequestDef() {
        return "LastDialog: " + currentRequestDef;
    }

    public String getState() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(SOURCE_NAME);
        sb.append(": ");
        sb.append("<br>Count: countSriReq-");
        sb.append(countSriReq);
        sb.append(", countSriResp-");
        sb.append(countSriResp);
        sb.append("<br>countMtFsmReq-");
        sb.append(countMtFsmReq);
        sb.append(", countMtFsmResp-");
        sb.append(countMtFsmResp);
        sb.append("<br> countMoFsmReq-");
        sb.append(countMoFsmReq);
        sb.append(", countMoFsmResp-");
        sb.append(countMoFsmResp);
        sb.append(", countIscReq-");
        sb.append(countIscReq);
        sb.append("<br>countRsmdsReq-");
        sb.append(countRsmdsReq);
        sb.append(", countRsmdsResp-");
        sb.append(countRsmdsResp);
        sb.append(", countAscReq-");
        sb.append(countAscReq);
        sb.append("<br>countAscResp-");
        sb.append(countAscResp);
        sb.append(", countErrRcvd-");
        sb.append(countErrRcvd);
        sb.append(", countErrSent-");
        sb.append(countErrSent);
        sb.append("</html>");
        return sb.toString();
    }

    public boolean start() {
        this.countSriReq = 0;
        this.countSriResp = 0;
        this.countMtFsmReq = 0;
        this.countMtFsmResp = 0;
        this.countMoFsmReq = 0;
        this.countMoFsmResp = 0;
        this.countIscReq = 0;
        this.countErrRcvd = 0;
        this.countErrSent = 0;
        this.countRsmdsReq = 0;
        this.countRsmdsResp = 0;
        this.countAscReq = 0;
        this.countAscResp = 0;

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        mapProvider.getMAPServiceSms().acivate();
        mapProvider.getMAPServiceSms().addMAPServiceListener(this);
        mapProvider.addMAPDialogListener(this);
        this.testerHost.sendNotif(SOURCE_NAME, "SMS Server has been started", "", Level.INFO);
        isStarted = true;

        return true;
    }

    public void stop() {
        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        isStarted = false;
        mapProvider.getMAPServiceSms().deactivate();
        mapProvider.getMAPServiceSms().removeMAPServiceListener(this);
        mapProvider.removeMAPDialogListener(this);
        this.testerHost.sendNotif(SOURCE_NAME, "SMS Server has been stopped", "", Level.INFO);
    }

    public void execute() {
    }

    public String closeCurrentDialog() {
        // TODO Auto-generated method stub
        return null;
    }

    public String performSRIForSM(AddressNatureType destAN, NumberingPlanMapType destNP,
                                  String destIsdnNumber) {
        if (!isStarted)
            return "The tester is not started";
        if (destIsdnNumber == null || destIsdnNumber.equals(""))
            return "DestIsdnNumber is empty";

        currentRequestDef = "";

        return doSendSri(destAN, destNP, destIsdnNumber, this.getServiceCenterAddress(), null);
    }

    private AddressNature curDestAddrNat = null;
    private NumberingPlan curDestNumPlan = null;
    private String curDestIsdnNumber = null;

    private String doSendSri(AddressNatureType destAN, NumberingPlanMapType destNP, String destIsdnNumber,
                             String serviceCentreAddr, MtMessageData messageData) {

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

        MAPApplicationContextVersion vers;
        switch (this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getMapProtocolVersion().intValue()) {
        case MapProtocolVersion.VAL_MAP_V1:
            vers = MAPApplicationContextVersion.version1;
            break;
        case MapProtocolVersion.VAL_MAP_V2:
            vers = MAPApplicationContextVersion.version2;
            break;
        default:
            vers = MAPApplicationContextVersion.version3;
            break;
        }
        MAPApplicationContext mapAppContext = MAPApplicationContext.getInstance(MAPApplicationContextName.shortMsgGatewayContext, vers);

        AddressNature destAddrNat = AddressNature.getInstance(destAN.intValue());
        NumberingPlan destNumPlan = NumberingPlan.getInstance(destNP.intValue());
        ISDNAddressString msisdn = mapProvider.getMAPParameterFactory().createISDNAddressString(destAddrNat, destNumPlan, destIsdnNumber);
        AddressString serviceCentreAddress = mapProvider.getMAPParameterFactory().createAddressString(
                this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getAddressNature(),
                this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlan(), serviceCentreAddr);
        curDestAddrNat = destAddrNat;
        curDestNumPlan = destNumPlan;
        curDestIsdnNumber = destIsdnNumber;

        try {
            MAPDialogSms curDialog = mapProvider.getMAPServiceSms()
                    .createNewDialog(
                            mapAppContext,
                            this.mapMan.createOrigAddress(),
                            null,
                            mapMan.createDestAddress(destIsdnNumber, testerHost.getConfigurationData().getTestSmsServerConfigurationData().getHlrSsn()),
                            null);
            HostMessageData hostMessageData = new HostMessageData();
            hostMessageData.mtMessageData = messageData;
            curDialog.setUserObject(hostMessageData);

            curDialog.addSendRoutingInfoForSMRequest(msisdn, true, serviceCentreAddress, null, this.testerHost.getConfigurationData()
                    .getTestSmsServerConfigurationData().isGprsSupportIndicator(), null, null, null);

            // this cap helps us give SCCP error if any
            // curDialog.setReturnMessageOnError(true);

            curDialog.send();

            String sriData = createSriData(curDialog.getLocalDialogId(), destIsdnNumber, serviceCentreAddr);
            currentRequestDef += "Sent SriReq;";
            this.countSriReq++;
            this.testerHost.sendNotif(SOURCE_NAME, "Sent: sriReq", sriData, Level.DEBUG);

            return "SendRoutingInfoForSMRequest has been sent";
        } catch (MAPException ex) {
            return "Exception when sending SendRoutingInfoForSMRequest: " + ex.toString();
        }
    }

    private String createSriData(long dialogId, String destIsdnNumber, String serviceCentreAddr) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(", destIsdnNumber=\"");
        sb.append(destIsdnNumber);
        sb.append("\", serviceCentreAddr=\"");
        sb.append(serviceCentreAddr);
        sb.append("\"");
        return sb.toString();
    }

    public String performSRIForSM_MtForwardSM(String msg,
                                              AddressNatureType destAN, NumberingPlanMapType destNP,
                                              String destIsdnNumber,
                                              AddressNatureType origAN, NumberingPlanMapType origNP,
                                              String origIsdnNumber) {
        if (!isStarted)
            return "The tester is not started";
        if (origIsdnNumber == null || origIsdnNumber.equals(""))
            return "OrigIsdnNumber is empty";
        if (destIsdnNumber == null || destIsdnNumber.equals(""))
            return "DestIsdnNumber is empty";
        if (msg == null || msg.equals(""))
            return "Msg is empty";
        int maxMsgLen = this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getSmsCodingType().getSupportesMaxMessageLength(0);
        if (msg.length() > maxMsgLen)
            return "Simulator does not support message length for current encoding type more than " + maxMsgLen;

        currentRequestDef = "";

        MtMessageData mmd = new MtMessageData();
        mmd.msg = msg;
        mmd.origAN = origAN;
        mmd.origNP = origNP;
        mmd.origIsdnNumber = origIsdnNumber;

        return doSendSri(destAN, destNP, destIsdnNumber, this.getServiceCenterAddress(), mmd);
    }

    public String performMtForwardSM(String msg, String destImsi, String vlrNumber,
                                     AddressNatureType origAN, NumberingPlanMapType origNP,
                                     String origIsdnNumber) {
        if (!isStarted)
            return "The tester is not started";
        if (msg == null || msg.equals(""))
            return "Msg is empty";
        if (destImsi == null || destImsi.equals(""))
            return "DestImsi is empty";
        if (vlrNumber == null || vlrNumber.equals(""))
            return "VlrNumber is empty";
        if (origIsdnNumber == null || origIsdnNumber.equals(""))
            return "OrigIsdnNumber is empty";
        int maxMsgLen = this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getSmsCodingType().getSupportesMaxMessageLength(0);
        if (msg.length() > maxMsgLen)
            return "Simulator does not support message length for current encoding type more than " + maxMsgLen;

        currentRequestDef = "";

        return doMtForwardSM(msg, destImsi, vlrNumber,
                             origAN, origNP, origIsdnNumber,
                             this.getServiceCenterAddress());
    }

    private String doMtForwardSM(String msg, String destImsi, String vlrNumber,
                                 AddressNatureType origAN, NumberingPlanMapType origNP, String origIsdnNumber,
                                 String serviceCentreAddr) {

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

        MAPApplicationContextVersion vers;
        MAPApplicationContextName acn = MAPApplicationContextName.shortMsgMTRelayContext;
        switch (this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getMapProtocolVersion().intValue()) {
        case MapProtocolVersion.VAL_MAP_V1:
            vers = MAPApplicationContextVersion.version1;
            acn = MAPApplicationContextName.shortMsgMORelayContext;
            break;
        case MapProtocolVersion.VAL_MAP_V2:
            vers = MAPApplicationContextVersion.version2;
            break;
        default:
            vers = MAPApplicationContextVersion.version3;
            break;
        }
        MAPApplicationContext mapAppContext = MAPApplicationContext.getInstance(acn, vers);

        IMSI imsi = mapProvider.getMAPParameterFactory().createIMSI(destImsi);
        SM_RP_DA da = mapProvider.getMAPParameterFactory().createSM_RP_DA(imsi);
        AddressString serviceCentreAddress = mapProvider.getMAPParameterFactory().createAddressString(
                this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getAddressNature(),
                this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlan(), serviceCentreAddr);
        SM_RP_OA oa = mapProvider.getMAPParameterFactory().createSM_RP_OA_ServiceCentreAddressOA(serviceCentreAddress);

        try {
            AddressField originatingAddress = new AddressFieldImpl(
                    this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getTypeOfNumber(), this.testerHost.getConfigurationData()
                            .getTestSmsServerConfigurationData().getNumberingPlanIdentification(), origIsdnNumber);
            Calendar cld = new GregorianCalendar();
            int year = cld.get(Calendar.YEAR);
            int mon = cld.get(Calendar.MONTH);
            int day = cld.get(Calendar.DAY_OF_MONTH);
            int h = cld.get(Calendar.HOUR);
            int m = cld.get(Calendar.MINUTE);
            int s = cld.get(Calendar.SECOND);
            int tz = cld.get(Calendar.ZONE_OFFSET);
            AbsoluteTimeStamp serviceCentreTimeStamp = new AbsoluteTimeStampImpl(year - 2000, mon, day, h, m, s, tz / 1000 / 60 / 15);
            DataCodingScheme dcs = new DataCodingSchemeImpl(this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getSmsCodingType()
                    .intValue() == SmsCodingType.VAL_GSM7 ? 0 : 8);
            UserData userData = new UserDataImpl(msg, dcs, null, null);
            ProtocolIdentifier pi = new ProtocolIdentifierImpl(0);
            SmsDeliverTpdu tpdu = new SmsDeliverTpduImpl(false, false, false, false, originatingAddress, pi, serviceCentreTimeStamp, userData);
            SmsSignalInfo si = mapProvider.getMAPParameterFactory().createSmsSignalInfo(tpdu, null);

            MAPDialogSms curDialog = mapProvider.getMAPServiceSms().createNewDialog(mapAppContext, this.mapMan.createOrigAddress(), null,
                    this.mapMan.createDestAddress(vlrNumber, this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getVlrSsn()), null);

            if (si.getData().length < 110 || vers == MAPApplicationContextVersion.version1) {
                if (this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getMapProtocolVersion().intValue() <= 2)
                    curDialog.addForwardShortMessageRequest(da, oa, si, false);
                else
                    curDialog.addMtForwardShortMessageRequest(da, oa, si, false, null);
                curDialog.send();

                String mtData = createMtData(curDialog.getLocalDialogId(), destImsi, vlrNumber, origIsdnNumber, serviceCentreAddr);
                currentRequestDef += "Sent mtReq;";
                this.countMtFsmReq++;
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: mtReq: " + msg, mtData, Level.DEBUG);
            } else {
                ResendMessageData md = new ResendMessageData();
                md.da = da;
                md.oa = oa;
                md.si = si;
                md.destImsi = destImsi;
                md.vlrNumber = vlrNumber;
                md.origIsdnNumber = origIsdnNumber;
                md.serviceCentreAddr = serviceCentreAddr;
                md.msg = msg;

                HostMessageData hmd = (HostMessageData) curDialog.getUserObject();
                if (hmd == null) {
                    hmd = new HostMessageData();
                    curDialog.setUserObject(hmd);
                }
                hmd.resendMessageData = md;

                curDialog.send();
                currentRequestDef += "Sent emptTBegin;";
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: emptTBegin", "", Level.DEBUG);
            }

            return "MtForwardShortMessageRequest has been sent";
        } catch (MAPException ex) {
            return "Exception when sending MtForwardShortMessageRequest: " + ex.toString();
        }
    }

    private String createMtData(long dialogId, String destImsi, String vlrNumber, String origIsdnNumber, String serviceCentreAddr) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(", destImsi=\"");
        sb.append(destImsi);
        sb.append(", vlrNumber=\"");
        sb.append(vlrNumber);
        sb.append(", origIsdnNumber=\"");
        sb.append(origIsdnNumber);
        sb.append("\", serviceCentreAddr=\"");
        sb.append(serviceCentreAddr);
        sb.append("\"");
        return sb.toString();
    }

    public void onForwardShortMessageRequest(ForwardShortMessageRequest ind) {
        if (!isStarted)
            return;

        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = ind.getInvokeId();
        SM_RP_DA da = ind.getSM_RP_DA();
        SM_RP_OA oa = ind.getSM_RP_OA();
        SmsSignalInfo si = ind.getSM_RP_UI();

        if (da.getServiceCentreAddressDA() != null) { // mo message
            this.onMoRequest(da, oa, si, curDialog);

            try {
                curDialog.addForwardShortMessageResponse(invokeId);
                this.needSendClose = true;

                this.countMoFsmResp++;
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: moResp", "", Level.DEBUG);
            } catch (MAPException e) {
                this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking addMoForwardShortMessageResponse : " + e.getMessage(), e, Level.ERROR);
            }
        }
    }

    public void onForwardShortMessageResponse(ForwardShortMessageResponse ind) {
        if (!isStarted)
            return;

        this.countMtFsmResp++;

        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = curDialog.getLocalDialogId();
        currentRequestDef += "Rsvd mtResp;";
        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: mtResp", "", Level.DEBUG);

        if (ind.getMAPDialog().getTCAPMessageType() == MessageType.Continue) {
            needSendClose = true;
        }
    }

    public void onMoForwardShortMessageRequest(MoForwardShortMessageRequest ind) {
        if (!isStarted)
            return;

        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = ind.getInvokeId();
        SM_RP_DA da = ind.getSM_RP_DA();
        SM_RP_OA oa = ind.getSM_RP_OA();
        SmsSignalInfo si = ind.getSM_RP_UI();

        this.onMoRequest(da, oa, si, curDialog);

        try {
            curDialog.addMoForwardShortMessageResponse(invokeId, null, null);
            this.needSendClose = true;

            this.countMoFsmResp++;
            this.testerHost.sendNotif(SOURCE_NAME, "Sent: moResp", "", Level.DEBUG);
        } catch (MAPException e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking addMoForwardShortMessageResponse : " + e.getMessage(), e, Level.ERROR);
        }
    }

    private void onMoRequest(SM_RP_DA da, SM_RP_OA oa, SmsSignalInfo si, MAPDialogSms curDialog) {

        this.countMoFsmReq++;

        String serviceCentreAddr = null;
        if (da != null) {
            AddressString as = da.getServiceCentreAddressDA();
            if (as != null)
                serviceCentreAddr = as.getAddress();
        }

        String origIsdnNumber = null;
        if (oa != null) {
            ISDNAddressString isdn = oa.getMsisdn();
            if (isdn != null)
                origIsdnNumber = isdn.getAddress();
        }

        try {
            String msg = null;
            String destIsdnNumber = null;
            if (si != null) {
                SmsTpdu tpdu = si.decodeTpdu(true);
                if (tpdu instanceof SmsSubmitTpdu) {
                    SmsSubmitTpdu dTpdu = (SmsSubmitTpdu) tpdu;
                    AddressField af = dTpdu.getDestinationAddress();
                    if (af != null)
                        destIsdnNumber = af.getAddressValue();
                    UserData ud = dTpdu.getUserData();
                    if (ud != null) {
                        ud.decode();
                        msg = ud.getDecodedMessage();
                    }
                }
            }
            String uData = this.createMoData(curDialog.getLocalDialogId(), destIsdnNumber, origIsdnNumber, serviceCentreAddr);
            this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: moReq: " + msg, uData, Level.DEBUG);
        } catch (MAPException e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when decoding MoForwardShortMessageRequest tpdu : " + e.getMessage(), e, Level.ERROR);
        }
    }

    private String createMoData(long dialogId, String destIsdnNumber, String origIsdnNumber, String serviceCentreAddr) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(", destIsdnNumber=\"");
        sb.append(destIsdnNumber);
        sb.append(", origIsdnNumber=\"");
        sb.append(origIsdnNumber);
        sb.append("\", serviceCentreAddr=\"");
        sb.append(serviceCentreAddr);
        sb.append("\"");
        return sb.toString();
    }

    public void onMoForwardShortMessageResponse(MoForwardShortMessageResponse moForwSmRespInd) {
        // TODO Auto-generated method stub

    }

    public void onMtForwardShortMessageRequest(MtForwardShortMessageRequest mtForwSmInd) {
        // TODO Auto-generated method stub

    }

    public void onMtForwardShortMessageResponse(MtForwardShortMessageResponse ind) {
        if (!isStarted)
            return;

        this.countMtFsmResp++;

        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = curDialog.getLocalDialogId();
        currentRequestDef += "Rsvd mtResp;";
        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: mtResp", "", Level.DEBUG);

        if (ind.getMAPDialog().getTCAPMessageType() == MessageType.Continue) {
            needSendClose = true;
        }
    }

    public void onSendRoutingInfoForSMRequest(SendRoutingInfoForSMRequest sendRoutingInfoForSMInd) {
        // TODO Auto-generated method stub

    }

    public void onSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse ind) {
        if (!isStarted)
            return;

        this.countSriResp++;

        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = curDialog.getLocalDialogId();
        LocationInfoWithLMSI li = ind.getLocationInfoWithLMSI();
        String vlrNum = "";
        if (li != null && li.getNetworkNodeNumber() != null)
            vlrNum = li.getNetworkNodeNumber().getAddress();
        currentRequestDef += "Rsvd SriResp;";
        String destImsi = "";
        if (ind.getIMSI() != null)
            destImsi = ind.getIMSI().getData();
        String uData = this.createSriRespData(invokeId, ind);
        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: sriResp", uData, Level.DEBUG);

        if (curDialog.getUserObject() != null && vlrNum != null && !vlrNum.equals("") && destImsi != null && !destImsi.equals("")) {
            HostMessageData hmd = (HostMessageData) curDialog.getUserObject();
            MtMessageData mmd = hmd.mtMessageData;
            if (mmd != null) {
                mmd.vlrNum = vlrNum;
                mmd.destImsi = destImsi;
            }
        }
    }

    private String createSriRespData(long dialogId, SendRoutingInfoForSMResponse ind) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(", ind=\"");
        sb.append(ind);
        sb.append("\"");
        return sb.toString();
    }

    private String createIscReqData(long dialogId, MWStatus mwStatus) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(",\n mwStatus=");
        sb.append(mwStatus);
        sb.append(",\n");
        return sb.toString();
    }

    public void onReportSMDeliveryStatusRequest(ReportSMDeliveryStatusRequest reportSMDeliveryStatusInd) {
        // TODO Auto-generated method stub

    }

    public void onReportSMDeliveryStatusResponse(ReportSMDeliveryStatusResponse ind) {
        if (!isStarted)
            return;

        this.countRsmdsResp++;

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = ind.getInvokeId();

        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: rsmdsResp", ind.toString(), Level.DEBUG);
    }

    public void onInformServiceCentreRequest(InformServiceCentreRequest ind) {
        if (!isStarted)
            return;

        this.countSriResp++;
        currentRequestDef += "Rsvd IscReq;";

        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = curDialog.getLocalDialogId();
        MWStatus mwStatus = ind.getMwStatus();
        String uData = this.createIscReqData(invokeId, mwStatus);
        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: iscReq", uData, Level.DEBUG);
    }

    public void onAlertServiceCentreRequest(AlertServiceCentreRequest ind) {
        if (!isStarted)
            return;

        this.countAscReq++;

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        MAPDialogSms curDialog = ind.getMAPDialog();
        long invokeId = ind.getInvokeId();

        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: ascReq", ind.toString(), Level.DEBUG);

        try {
            if (curDialog.getApplicationContext().getApplicationContextVersion() == MAPApplicationContextVersion.version1) {
                curDialog.release();
            } else {
                curDialog.addAlertServiceCentreResponse(invokeId);

                this.countAscResp++;
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: ascResp", "", Level.DEBUG);

                this.needSendClose = true;
            }

        } catch (MAPException e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking addAlertServiceCentreResponse() : " + e.getMessage(), e, Level.ERROR);
        }
    }

    public void onAlertServiceCentreResponse(AlertServiceCentreResponse alertServiceCentreInd) {
        // TODO Auto-generated method stub

    }

    public void onDialogRequest(MAPDialog arg0, AddressString arg1, AddressString arg2, MAPExtensionContainer arg3) {
        int i1 = 0;
    }

    public void onDialogDelimiter(MAPDialog mapDialog) {

        if (mapDialog.getApplicationContext().getApplicationContextName() == MAPApplicationContextName.shortMsgMTRelayContext
                || mapDialog.getApplicationContext().getApplicationContextName() == MAPApplicationContextName.shortMsgMORelayContext) {
            if (mapDialog.getUserObject() != null) {
                HostMessageData hmd = (HostMessageData) mapDialog.getUserObject();
                ResendMessageData md = hmd.resendMessageData;
                if (md != null) {
                    try {
                        MAPDialogSms dlg = (MAPDialogSms) mapDialog;

                        if (dlg.getApplicationContext().getApplicationContextVersion().getVersion() <= 2)
                            dlg.addForwardShortMessageRequest(md.da, md.oa, md.si, false);
                        else
                            dlg.addMtForwardShortMessageRequest(md.da, md.oa, md.si, false, null);
                        mapDialog.send();

                        String mtData = createMtData(mapDialog.getLocalDialogId(), md.destImsi, md.vlrNumber, md.origIsdnNumber, md.serviceCentreAddr);
                        currentRequestDef += "Rcvd emptTCont;Sent mtReq;";
                        this.countMoFsmReq++;
                        this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: emptTCont", "", Level.DEBUG);
                        this.testerHost.sendNotif(SOURCE_NAME, "Sent: mtReq: " + md.msg, mtData, Level.DEBUG);
                    } catch (Exception e) {
                        this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking close() : " + e.getMessage(), e, Level.ERROR);
                        return;
                    }
                    hmd.resendMessageData = null;
                    return;
                }
            }
        }

        try {
            if (needSendSend) {
                needSendSend = false;
                mapDialog.send();
                return;
            }
        } catch (Exception e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking send() : " + e.getMessage(), e, Level.ERROR);
            return;
        }
        try {
            if (needSendClose) {
                needSendClose = false;
                mapDialog.close(false);
                return;
            }
        } catch (Exception e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking close() : " + e.getMessage(), e, Level.ERROR);
            return;
        }

        if (mapDialog.getApplicationContext().getApplicationContextName() == MAPApplicationContextName.shortMsgMTRelayContext
                || mapDialog.getApplicationContext().getApplicationContextName() == MAPApplicationContextName.shortMsgMORelayContext) {
            // this is an empty first TC-BEGIN for MO SMS
            try {
                mapDialog.send();
                currentRequestDef += "Rcvd emptTBeg;Sent emptTCont;";
                this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: emptTBeg", "", Level.DEBUG);
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: emptTCont", "", Level.DEBUG);
            } catch (Exception e) {
                this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking send() : " + e.getMessage(), e, Level.ERROR);
            }
            return;
        }
    }

    public void onDialogClose(MAPDialog mapDialog) {
        if (mapDialog.getUserObject() != null) {
            HostMessageData hmd = (HostMessageData) mapDialog.getUserObject();
            MtMessageData mmd = hmd.mtMessageData;
            if (mmd != null && mmd.vlrNum != null && mmd.destImsi != null) {
                // sending SMS
                doMtForwardSM(mmd.msg, mmd.destImsi, mmd.vlrNum,
                              mmd.origAN, mmd.origNP, mmd.origIsdnNumber,
                              this.testerHost.getConfigurationData().getTestSmsServerConfigurationData()
                        .getServiceCenterAddress());
            }
        }

        try {
            if (needSendSend) {
                needSendSend = false;
                mapDialog.send();
            }
        } catch (Exception e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking send() : " + e.getMessage(), e, Level.ERROR);
        }
        try {
            if (needSendClose) {
                needSendClose = false;
                mapDialog.close(false);
            }
        } catch (Exception e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking close() : " + e.getMessage(), e, Level.ERROR);
        }
    }

    public void onErrorComponent(MAPDialog dlg, Long invokeId, MAPErrorMessage msg) {
        // if an error for (mt)ForwardSM or SRI requests
        if (dlg.getApplicationContext().getApplicationContextName() != MAPApplicationContextName.shortMsgMTRelayContext
                || dlg.getApplicationContext().getApplicationContextName() != MAPApplicationContextName.shortMsgMORelayContext
                || (dlg.getUserObject() != null && ((HostMessageData) dlg.getUserObject()).mtMessageData != null && ((HostMessageData) dlg.getUserObject()).mtMessageData.msg != null)) {
            if (this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().isSendSrsmdsIfError() && curDestIsdnNumber != null) {
                try {
                    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
                    MAPApplicationContextVersion vers = dlg.getApplicationContext().getApplicationContextVersion();
                    MAPApplicationContext mapAppContext = MAPApplicationContext.getInstance(MAPApplicationContextName.shortMsgGatewayContext, vers);

                    MAPDialogSms curDialog = mapProvider.getMAPServiceSms().createNewDialog(
                            mapAppContext,
                            this.mapMan.createOrigAddress(),
                            null,
                            this.mapMan.createDestAddress(curDestIsdnNumber,
                                                          testerHost.getConfigurationData().getTestSmsServerConfigurationData().getHlrSsn()),
                            null);

                    ISDNAddressString msisdn = mapProvider.getMAPParameterFactory().createISDNAddressString(curDestAddrNat, curDestNumPlan, curDestIsdnNumber);
                    AddressString serviceCentreAddress = mapProvider.getMAPParameterFactory().createAddressString(
                            this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getAddressNature(),
                            this.testerHost.getConfigurationData().getTestSmsServerConfigurationData().getNumberingPlan(), this.getServiceCenterAddress());
                    curDestAddrNat = null;
                    curDestNumPlan = null;
                    curDestIsdnNumber = null;

                    SMDeliveryOutcome sMDeliveryOutcome = null;
                    if (vers.getVersion() >= 2) {
                        if (msg.isEmSMDeliveryFailure()
                                && msg.getEmSMDeliveryFailure().getSMEnumeratedDeliveryFailureCause() == SMEnumeratedDeliveryFailureCause.memoryCapacityExceeded)
                            sMDeliveryOutcome = SMDeliveryOutcome.memoryCapacityExceeded;
                        else
                            sMDeliveryOutcome = SMDeliveryOutcome.absentSubscriber;
                    }

                    curDialog.addReportSMDeliveryStatusRequest(msisdn, serviceCentreAddress, sMDeliveryOutcome, null, null, false, false, null, null);
                    curDialog.send();

                    currentRequestDef += "Sent RsmdsReq;";
                    this.countRsmdsReq++;
                    String rsmdsData = "msisdn=" + msisdn + ", serviceCentreAddress=" + serviceCentreAddress + ", sMDeliveryOutcome=" + sMDeliveryOutcome;
                    this.testerHost.sendNotif(SOURCE_NAME, "Sent: rsmdsReq", rsmdsData, Level.DEBUG);
                } catch (MAPException e) {
                    this.testerHost.sendNotif(SOURCE_NAME, "Exception when invoking reportSMDeliveryStatusRequest : " + e.getMessage(), e, Level.ERROR);
                }
            }
        }

        super.onErrorComponent(dlg, invokeId, msg);

        // needSendClose = true;
    }

    public void onRejectComponent(MAPDialog mapDialog, Long invokeId, Problem problem, boolean isLocalOriginated) {
        super.onRejectComponent(mapDialog, invokeId, problem, isLocalOriginated);
        if (isLocalOriginated)
            needSendClose = true;
    }

    private class HostMessageData {
        public MtMessageData mtMessageData;
        public ResendMessageData resendMessageData;
    }

    private class MtMessageData {
        public String msg;
        public AddressNatureType origAN;
        public NumberingPlanMapType origNP;
        public String origIsdnNumber;
        public String vlrNum;
        public String destImsi;
    }

    private class ResendMessageData {
        public SM_RP_DA da;
        public SM_RP_OA oa;
        public SmsSignalInfo si;
        public String msg;
        public String destImsi;
        public String vlrNumber;
        public String origIsdnNumber;
        public String serviceCentreAddr;
    }
}
