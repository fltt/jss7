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

package org.mobicents.protocols.ss7.tools.simulator.tests.ussd;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.AlertingPattern;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.mobicents.protocols.ss7.map.api.service.supplementary.MAPServiceSupplementaryListener;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyResponse;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.mobicents.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.mobicents.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.mobicents.protocols.ss7.map.primitives.AlertingPatternImpl;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;
import org.mobicents.protocols.ss7.tools.simulator.common.AddressNatureType;
import org.mobicents.protocols.ss7.tools.simulator.common.TesterBase;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.NumberingPlanMapType;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class TestUssdClientMan extends TesterBase implements TestUssdClientManMBean, Stoppable, MAPDialogListener,
        MAPServiceSupplementaryListener {

    public static String SOURCE_NAME = "TestUssdClient";

    private final String name;
    // private TesterHost testerHost;
    private MapMan mapMan;

    private int countProcUnstReq = 0;
    private int countProcUnstResp = 0;
    private int countProcUnstReqNot = 0;
    private int countUnstReq = 0;
    private int countUnstResp = 0;
    private int countUnstNotifReq = 0;
    private MAPDialogSupplementary currentDialog = null;
    private Long invokeId = null;
    private boolean isStarted = false;
    private String currentRequestDef = "";
    private boolean needSendSend = false;
    private boolean needSendClose = false;

    private AtomicInteger nbConcurrentDialogs = new AtomicInteger();
    private MessageSender sender = null;

    public TestUssdClientMan() {
        super(SOURCE_NAME);
        this.name = "???";
    }

    public TestUssdClientMan(String name) {
        super(SOURCE_NAME);
        this.name = name;
    }

    public void setMapMan(MapMan val) {
        this.mapMan = val;
    }

    public String getMsisdnAddress() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMsisdnAddress();
    }

    public void setMsisdnAddress(String val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setMsisdnAddress(val);
        this.testerHost.markStore();
    }

    public AddressNatureType getMsisdnAddressNature() {
        return new AddressNatureType(this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                .getMsisdnAddressNature().getIndicator());
    }

    public String getMsisdnAddressNature_Value() {
        return new AddressNatureType(this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                .getMsisdnAddressNature().getIndicator()).toString();
    }

    public void setMsisdnAddressNature(AddressNatureType val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                .setMsisdnAddressNature(AddressNature.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public NumberingPlanMapType getMsisdnNumberingPlan() {
        return new NumberingPlanMapType(this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                .getMsisdnNumberingPlan().getIndicator());
    }

    public String getMsisdnNumberingPlan_Value() {
        return new NumberingPlanMapType(this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                .getMsisdnNumberingPlan().getIndicator()).toString();
    }

    public void setMsisdnNumberingPlan(NumberingPlanMapType val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                .setMsisdnNumberingPlan(NumberingPlan.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public int getDataCodingScheme() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getDataCodingScheme();
    }

    public void setDataCodingScheme(int val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setDataCodingScheme(val);
        this.testerHost.markStore();
    }

    public int getAlertingPattern() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getAlertingPattern();
    }

    public void setAlertingPattern(int val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setAlertingPattern(val);
        this.testerHost.markStore();
    }

    public UssdClientAction getUssdClientAction() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getUssdClientAction();
    }

    public String getUssdClientAction_Value() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getUssdClientAction().toString();
    }

    public void setUssdClientAction(UssdClientAction val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setUssdClientAction(val);
        this.testerHost.markStore();
    }

    public String getAutoRequestString() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getAutoRequestString();
    }

    public void setAutoRequestString(String val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setAutoRequestString(val);
        this.testerHost.markStore();
    }

    public int getMaxConcurrentDialogs() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMaxConcurrentDialogs();
    }

    public void setMaxConcurrentDialogs(int val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setMaxConcurrentDialogs(val);
        this.testerHost.markStore();
    }

    public boolean isOneNotificationFor100Dialogs() {
        return this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().isOneNotificationFor100Dialogs();
    }

    public void setOneNotificationFor100Dialogs(boolean val) {
        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().setOneNotificationFor100Dialogs(val);
        this.testerHost.markStore();
    }

    public void putMsisdnAddressNature(String val) {
        AddressNatureType x = AddressNatureType.createInstance(val);
        if (x != null)
            this.setMsisdnAddressNature(x);
    }

    public void putMsisdnNumberingPlan(String val) {
        NumberingPlanMapType x = NumberingPlanMapType.createInstance(val);
        if (x != null)
            this.setMsisdnNumberingPlan(x);
    }

    public void putUssdClientAction(String val) {
        UssdClientAction x = UssdClientAction.createInstance(val);
        if (x != null)
            this.setUssdClientAction(x);
    }

    public String getCurrentRequestDef() {
        if (this.currentDialog != null)
            return "CurDialog: " + currentRequestDef;
        else
            return "PrevDialog: " + currentRequestDef;
    }

    public String getState() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(SOURCE_NAME);
        sb.append(": CurDialog=");
        MAPDialogSupplementary curDialog = currentDialog;
        if (curDialog != null)
            sb.append(curDialog.getLocalDialogId());
        else
            sb.append("No");
        sb.append("<br>Count: processUnstructuredSSRequest-");
        sb.append(countProcUnstReq);
        sb.append(", processUnstructuredSSResponse-");
        sb.append(countProcUnstResp);
        sb.append("<br>unstructuredSSRequest-");
        sb.append(countUnstReq);
        sb.append(", unstructuredSSResponse-");
        sb.append(countUnstResp);
        sb.append(", unstructuredSSNotify-");
        sb.append(countUnstNotifReq);
        sb.append("</html>");
        return sb.toString();
    }

    public boolean start() {
        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        mapProvider.getMAPServiceSupplementary().acivate();
        mapProvider.getMAPServiceSupplementary().addMAPServiceListener(this);
        mapProvider.addMAPDialogListener(this);
        this.testerHost.sendNotif(SOURCE_NAME, "USSD Client has been started", "", Level.INFO);
        isStarted = true;

        if (this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getUssdClientAction().intValue() == UssdClientAction.VAL_AUTO_SendProcessUnstructuredSSRequest) {
            nbConcurrentDialogs = new AtomicInteger();
            this.sender = new MessageSender();
            Thread thr = new Thread(this.sender);
            thr.start();
        }

        return true;
    }

    public void stop() {
        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        isStarted = false;

        if (this.sender != null) {
            this.sender.stop();
            try {
                this.sender.notify();
            } catch (Exception e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            this.sender = null;
        }

        this.doRemoveDialog();
        mapProvider.getMAPServiceSupplementary().deactivate();
        mapProvider.getMAPServiceSupplementary().removeMAPServiceListener(this);
        mapProvider.removeMAPDialogListener(this);
        this.testerHost.sendNotif(SOURCE_NAME, "USSD Client has been stopped", "", Level.INFO);
    }

    public void execute() {
    }

    public String closeCurrentDialog() {
        if (!isStarted)
            return "The tester is not started";
        if (this.sender != null)
            return "The tester is not in a manual mode";

        MAPDialogSupplementary curDialog = currentDialog;
        if (curDialog != null) {
            try {
                MAPUserAbortChoice choice = new MAPUserAbortChoiceImpl();
                choice.setUserSpecificReason();
                curDialog.abort(choice);
                this.doRemoveDialog();
                return "The current dialog has been closed";
            } catch (MAPException e) {
                this.doRemoveDialog();
                return "Exception when closing the current dialog: " + e.toString();
            }
        } else {
            return "No current dialog";
        }
    }

    private void doRemoveDialog() {
        currentDialog = null;
        // currentRequestDef = "";
    }

    public String performProcessUnstructuredRequest(String msg) {
        if (!isStarted)
            return "The tester is not started";
        if (this.sender != null)
            return "The tester is not in manual mode";

        MAPDialogSupplementary curDialog = currentDialog;
        if (curDialog != null)
            return "The current dialog exists. Finish it previousely";
        if (msg == null || msg.equals(""))
            return "USSD message is empty";

        currentRequestDef = "";

        return this.doPerformProcessUnstructuredRequest(msg, true);
    }

    private String doPerformProcessUnstructuredRequest(String msg, boolean manualMode) {

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
        USSDString ussdString = null;
        try {
            ussdString = mapProvider.getMAPParameterFactory().createUSSDString(
                    msg,
                    new CBSDataCodingSchemeImpl(this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                            .getDataCodingScheme()), null);
        } catch (MAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MAPApplicationContext mapUssdAppContext = MAPApplicationContext.getInstance(
                MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

        try {
            MAPDialogSupplementary curDialog = mapProvider.getMAPServiceSupplementary().createNewDialog(mapUssdAppContext,
                    this.mapMan.createOrigAddress(), this.mapMan.createOrigReference(), this.mapMan.createDestAddress(),
                    this.mapMan.createDestReference());
            if (manualMode)
                currentDialog = curDialog;
            invokeId = null;

            ISDNAddressString msisdn = null;
            if (this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMsisdnAddress() != null
                    && !this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMsisdnAddress()
                            .equals("")) {
                msisdn = mapProvider.getMAPParameterFactory().createISDNAddressString(
                        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMsisdnAddressNature(),
                        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMsisdnNumberingPlan(),
                        this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getMsisdnAddress());
            }

            AlertingPattern alPattern = null;
            if (this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getAlertingPattern() >= 0
                    && this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getAlertingPattern() <= 255)
                alPattern = new AlertingPatternImpl((byte) this.testerHost.getConfigurationData()
                        .getTestUssdClientConfigurationData().getAlertingPattern());
            curDialog.addProcessUnstructuredSSRequest(new CBSDataCodingSchemeImpl(this.testerHost.getConfigurationData()
                    .getTestUssdClientConfigurationData().getDataCodingScheme()), ussdString, alPattern, msisdn);

            curDialog.send();

            if (manualMode)
                currentRequestDef += "Sent procUnstrSsReq=\"" + msg + "\";";
            this.countProcUnstReq++;
            if (this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().isOneNotificationFor100Dialogs()) {
                int i1 = countProcUnstReq / 100;
                if (countProcUnstReqNot < i1) {
                    countProcUnstReqNot = i1;
                    this.testerHost.sendNotif(SOURCE_NAME, "Sent: procUnstrSsReq: " + (countProcUnstReqNot * 100)
                            + " messages sent", "", Level.DEBUG);
                }
            } else {
                String uData = this.createUssdMessageData(curDialog.getLocalDialogId(), this.testerHost.getConfigurationData()
                        .getTestUssdClientConfigurationData().getDataCodingScheme(), msisdn, alPattern);
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: procUnstrSsReq: " + msg, uData, Level.DEBUG);
            }

            return "ProcessUnstructuredSSRequest has been sent";
        } catch (MAPException ex) {
            return "Exception when sending ProcessUnstructuredSSRequest: " + ex.toString();
        }
    }

    private String createUssdMessageData(long dialogId, int dataCodingScheme, ISDNAddressString msisdn,
            AlertingPattern alPattern) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(" DataCodingSchema=");
        sb.append(dataCodingScheme);
        sb.append(" ");
        if (msisdn != null) {
            sb.append(msisdn.toString());
            sb.append(" ");
        }
        if (alPattern != null) {
            sb.append(alPattern.toString());
            sb.append(" ");
        }
        return sb.toString();
    }

    public String performUnstructuredResponse(String msg) {
        if (!isStarted)
            return "The tester is not started";
        if (this.sender != null)
            return "The tester is not ion manual mode";

        MAPDialogSupplementary curDialog = currentDialog;
        if (curDialog == null)
            return "No current dialog exists. Start it previousely";
        if (invokeId == null)
            return "No pending unstructured request";

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
//        if (msg == null || msg.equals(""))
//            return "USSD message is empty";
        USSDString ussdString = null;
        if (msg != null && !msg.equals("")) {
            try {
                ussdString = mapProvider.getMAPParameterFactory().createUSSDString(msg,
                        new CBSDataCodingSchemeImpl(this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().getDataCodingScheme()), null);
            } catch (MAPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            curDialog.addUnstructuredSSResponse(invokeId, new CBSDataCodingSchemeImpl(this.testerHost.getConfigurationData()
                    .getTestUssdClientConfigurationData().getDataCodingScheme()), ussdString);

            curDialog.send();

            invokeId = null;

            currentRequestDef += "Sent unstrSsResp=\"" + msg + "\";";
            this.countUnstResp++;
            String uData = this.createUssdMessageData(curDialog.getLocalDialogId(), this.testerHost.getConfigurationData()
                    .getTestUssdClientConfigurationData().getDataCodingScheme(), null, null);
            this.testerHost.sendNotif(SOURCE_NAME, "Sent: unstrSsResp: " + msg, uData, Level.DEBUG);

            return "UnstructuredSSResponse has been sent";
        } catch (MAPException ex) {
            return "Exception when sending UnstructuredSSResponse: " + ex.toString();
        }
    }

    public void onMAPMessage(MAPMessage mapMessage) {
        // TODO Auto-generated method stub

    }

    public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequest procUnstrReqInd) {
        // TODO Auto-generated method stub

    }

    public void onProcessUnstructuredSSResponse(ProcessUnstructuredSSResponse ind) {
        if (!isStarted)
            return;

        if (this.sender == null) {
            MAPDialogSupplementary curDialog = currentDialog;
            if (curDialog != ind.getMAPDialog())
                return;
            try {
                currentRequestDef += "procUnstrSsResp=\"" + ind.getUSSDString().getString(null) + "\";";
            } catch (MAPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        this.countProcUnstResp++;
        if (!this.testerHost.getConfigurationData().getTestUssdClientConfigurationData().isOneNotificationFor100Dialogs()) {
            String uData = this.createUssdMessageData(ind.getMAPDialog().getLocalDialogId(), ind.getDataCodingScheme()
                    .getCode(), null, null);
            try {
                this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: procUnstrSsResp: " + ind.getUSSDString().getString(null), uData,
                        Level.DEBUG);
            } catch (MAPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        this.doRemoveDialog();
    }

    public void onUnstructuredSSRequest(UnstructuredSSRequest ind) {
        if (!isStarted)
            return;
        MAPDialogSupplementary curDialog = currentDialog;
        if (curDialog != ind.getMAPDialog())
            return;

        invokeId = ind.getInvokeId();

        try {
            currentRequestDef += "Rcvd: unstrSsReq=\"" + ind.getUSSDString().getString(null) + "\";";
        } catch (MAPException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.countUnstReq++;
        String uData = this
                .createUssdMessageData(curDialog.getLocalDialogId(), ind.getDataCodingScheme().getCode(), null, null);
        try {
            this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: unstrSsReq: " + ind.getUSSDString().getString(null), uData,
                    Level.DEBUG);
        } catch (MAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onUnstructuredSSResponse(UnstructuredSSResponse unstrResInd) {
        // TODO Auto-generated method stub

    }

    public void onUnstructuredSSNotifyRequest(UnstructuredSSNotifyRequest ind) {
        if (!isStarted)
            return;

        MAPDialogSupplementary dlg = ind.getMAPDialog();
        invokeId = ind.getInvokeId();

        this.countUnstNotifReq++;
        String uData = this.createUssdMessageData(dlg.getLocalDialogId(), ind.getDataCodingScheme().getCode(), null, null);
        try {
            this.testerHost.sendNotif(SOURCE_NAME, "Rcvd: unstrSsNotify: " + ind.getUSSDString().getString(null), uData,
                    Level.DEBUG);
        } catch (MAPException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            dlg.addUnstructuredSSNotifyResponse(invokeId);
            this.needSendClose = true;
        } catch (MAPException e) {
            this.testerHost.sendNotif(SOURCE_NAME,
                    "Exception when invoking addUnstructuredSSNotifyResponse() : " + e.getMessage(), e, Level.ERROR);
        }
    }

    public void onUnstructuredSSNotifyResponse(UnstructuredSSNotifyResponse unstrNotifyInd) {
        // TODO Auto-generated method stub

    }

    public void onDialogDelimiter(MAPDialog mapDialog) {
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

    public void onDialogRelease(MAPDialog mapDialog) {
        if (this.currentDialog == mapDialog)
            this.doRemoveDialog();

        nbConcurrentDialogs.decrementAndGet();
        if (this.sender != null) {
            if (nbConcurrentDialogs.get() < this.testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                    .getMaxConcurrentDialogs() / 2)
                this.sender.notify();
        }
    }

    private class MessageSender implements Runnable {

        private boolean needStop = false;

        public void stop() {
            needStop = true;
        }

        public void run() {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            while (true) {
                if (needStop)
                    break;

                if (nbConcurrentDialogs.get() < testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                        .getMaxConcurrentDialogs()) {
                    doPerformProcessUnstructuredRequest(testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                            .getAutoRequestString(), false);
                    nbConcurrentDialogs.incrementAndGet();
                }

                if (nbConcurrentDialogs.get() >= testerHost.getConfigurationData().getTestUssdClientConfigurationData()
                        .getMaxConcurrentDialogs()) {
                    try {
                        this.wait(100);
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }
}
