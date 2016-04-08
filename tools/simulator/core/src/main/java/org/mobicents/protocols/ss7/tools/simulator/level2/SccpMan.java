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

package org.mobicents.protocols.ss7.tools.simulator.level2;

import java.io.IOException;

import org.apache.log4j.Level;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPart;
import org.mobicents.protocols.ss7.sccp.LoadSharingAlgorithm;
import org.mobicents.protocols.ss7.sccp.OriginationType;
import org.mobicents.protocols.ss7.sccp.RemoteSignalingPointCode;
import org.mobicents.protocols.ss7.sccp.RemoteSubSystem;
import org.mobicents.protocols.ss7.sccp.RuleType;
import org.mobicents.protocols.ss7.sccp.SccpProvider;
import org.mobicents.protocols.ss7.sccp.SccpResource;
import org.mobicents.protocols.ss7.sccp.SccpStack;
import org.mobicents.protocols.ss7.sccp.impl.SccpStackImpl;
import org.mobicents.protocols.ss7.sccp.impl.router.RouterImpl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;
import org.mobicents.protocols.ss7.tools.simulator.management.TesterHost;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class SccpMan implements SccpManMBean, Stoppable {

    public static String SOURCE_NAME = "SCCP";

    private final String name;
    private TesterHost testerHost;

    private Mtp3UserPart mtp3UserPart;

    private SccpStackImpl sccpStack;
    private SccpProvider sccpProvider;
    private SccpResource resource;
    private boolean isRspcUp = true;
    private boolean isRspcUp2 = true;
    private boolean isRssUp = true;
    private boolean isRssUp2 = true;

    public SccpMan() {
        this.name = "???";
    }

    public SccpMan(String name) {
        this.name = name;
    }

    public void setTesterHost(TesterHost testerHost) {
        this.testerHost = testerHost;
    }

    public void setMtp3UserPart(Mtp3UserPart val) {
        this.mtp3UserPart = val;
    }

    public SccpStack getSccpStack() {
        return this.sccpStack;
    }

    public boolean isRouteOnGtMode() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().isRouteOnGtMode();
    }

    public void setRouteOnGtMode(boolean val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setRouteOnGtMode(val);
        this.testerHost.markStore();
    }

    public int getRemoteSpc() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc();
    }

    public int getRemoteSpc2() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc2();
    }

    public void setRemoteSpc(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setRemoteSpc(val);
        this.testerHost.markStore();
    }

    public void setRemoteSpc2(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setRemoteSpc2(val);
        this.testerHost.markStore();
    }

    public int getLocalSpc() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc();
    }

    public int getLocalSpc2() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc2();
    }

    public void setLocalSpc(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setLocalSpc(val);
        this.testerHost.markStore();
    }

    public void setLocalSpc2(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setLocalSpc2(val);
        this.testerHost.markStore();
    }

    public int getNi() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getNi();
    }

    public void setNi(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setNi(val);
        this.testerHost.markStore();
    }

    public int getRemoteSsn() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSsn();
    }

    public void setRemoteSsn(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setRemoteSsn(val);
        this.testerHost.markStore();
    }

    public int getLocalSsn() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getLocalSsn();
    }

    public void setLocalSsn(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setLocalSsn(val);
        this.testerHost.markStore();
    }

    public int getHLRSsn() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getHLRSsn();
    }

    public void setHLRSsn(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setHLRSsn(val);
        this.testerHost.markStore();
    }

    public GlobalTitleType getGlobalTitleType() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getGlobalTitleType();
    }

    public String getGlobalTitleType_Value() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getGlobalTitleType().toString();
    }

    public void setGlobalTitleType(GlobalTitleType val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setGlobalTitleType(val);
        this.testerHost.markStore();
    }

    public NatureOfAddressType getNatureOfAddress() {
        return new NatureOfAddressType(this.testerHost.getConfigurationData().getSccpConfigurationData().getNatureOfAddress()
                .getValue());
    }

    public String getNatureOfAddress_Value() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getNatureOfAddress().toString();
    }

    public NatureOfAddressType getNatureOfAddress2() {
        return new NatureOfAddressType(this.testerHost.getConfigurationData().getSccpConfigurationData().getNatureOfAddress2()
                .getValue());
    }

    public String getNatureOfAddress2_Value() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getNatureOfAddress2().toString();
    }

    public void setNatureOfAddress(NatureOfAddressType val) {
        try {
            this.testerHost.getConfigurationData().getSccpConfigurationData()
                    .setNatureOfAddress(NatureOfAddress.valueOf(val.intValue()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.testerHost.markStore();
    }

    public void setNatureOfAddress2(NatureOfAddressType val) {
        try {
            this.testerHost.getConfigurationData().getSccpConfigurationData()
                    .setNatureOfAddress2(NatureOfAddress.valueOf(val.intValue()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.testerHost.markStore();
    }

    public NumberingPlanSccpType getNumberingPlan() {
        return new NumberingPlanSccpType(this.testerHost.getConfigurationData().getSccpConfigurationData().getNumberingPlan()
                .getValue());
    }

    public String getNumberingPlan_Value() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getNumberingPlan().toString();
    }

    public void setNumberingPlan(NumberingPlanSccpType val) {
        try {
            this.testerHost.getConfigurationData().getSccpConfigurationData()
                    .setNumberingPlan(NumberingPlan.valueOf(val.intValue()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.testerHost.markStore();
    }

    public int getTranslationType() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getTranslationType();
    }

    public void setTranslationType(int val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setTranslationType(val);
        this.testerHost.markStore();
    }

    public String getCallingPartyAddressDigits() {
        return this.testerHost.getConfigurationData().getSccpConfigurationData().getCallingPartyAddressDigits();
    }

    public void setCallingPartyAddressDigits(String val) {
        this.testerHost.getConfigurationData().getSccpConfigurationData().setCallingPartyAddressDigits(val);
        this.testerHost.markStore();
    }

    public void putGlobalTitleType(String val) {
        GlobalTitleType x = GlobalTitleType.createInstance(val);
        if (x != null)
            this.setGlobalTitleType(x);
    }

    public void putNatureOfAddress(String val) {
        NatureOfAddressType x = NatureOfAddressType.createInstance(val);
        if (x != null)
            this.setNatureOfAddress(x);
    }

    public void putNatureOfAddress2(String val) {
        NatureOfAddressType x = NatureOfAddressType.createInstance(val);
        if (x != null)
            this.setNatureOfAddress2(x);
    }

    public void putNumberingPlan(String val) {
        NumberingPlanSccpType x = NumberingPlanSccpType.createInstance(val);
        if (x != null)
            this.setNumberingPlan(x);
    }

    public String getState() {
        StringBuilder sb = new StringBuilder();
        sb.append("SCCP: Rspc: ");
        sb.append(this.isRspcUp ? "Enabled" : "Disabled");
        sb.append("  Rss: ");
        sb.append(this.isRssUp ? "Enabled" : "Disabled");
        sb.append(", Rspc2: ");
        sb.append(this.isRspcUp2 ? "Enabled" : "Disabled");
        sb.append("  Rss2: ");
        sb.append(this.isRssUp2 ? "Enabled" : "Disabled");
        return sb.toString();
    }

    public boolean start() {
        try {
            this.isRspcUp = true;
            this.isRspcUp2 = true;
            this.isRssUp = true;
            this.isRssUp2 = true;
            this.initSccp(this.mtp3UserPart,
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSsn(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getLocalSsn(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc2(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc2(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getNi(),
                          this.testerHost.getConfigurationData().getSccpConfigurationData().getCallingPartyAddressDigits(),
                          this.testerHost.getPersistDir());
            this.testerHost.sendNotif(SOURCE_NAME, "SCCP has been started", "", Level.INFO);
            return true;
        } catch (Throwable e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when starting SccpMan", e, Level.ERROR);
            return false;
        }
    }

    public void stop() {
        try {
            this.stopSccp();
            this.testerHost.sendNotif(SOURCE_NAME, "SCCP has been stopped", "", Level.INFO);
        } catch (Exception e) {
            this.testerHost.sendNotif(SOURCE_NAME, "Exception when stopping SccpMan", e, Level.ERROR);
        }
    }

    public void execute() {
        if (this.resource != null) {
            RemoteSignalingPointCode rspc = this.resource.getRemoteSpc(1);
            RemoteSubSystem rss = this.resource.getRemoteSsn(1);
            if (rspc != null) {
                boolean conn = !rspc.isRemoteSpcProhibited();
                if (this.isRspcUp != conn) {
                    this.isRspcUp = conn;
                    this.testerHost.sendNotif(SOURCE_NAME, "SCCP RemoteSignalingPoint is " + (conn ? "enabled" : "disabled"),
                            "Dpc=" + this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc(),
                            Level.INFO);
                }
            }
            if (rss != null) {
                boolean conn = !rss.isRemoteSsnProhibited();
                if (this.isRssUp != conn) {
                    this.isRssUp = conn;
                    this.testerHost.sendNotif(SOURCE_NAME, "SCCP RemoteSubSystem is " + (conn ? "enabled" : "disabled"), "Dpc="
                            + this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc() + " Ssn="
                            + this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSsn(), Level.INFO);
                }
            }
            rspc = this.resource.getRemoteSpc(2);
            rss = this.resource.getRemoteSsn(2);
            if (rspc != null) {
                boolean conn = !rspc.isRemoteSpcProhibited();
                if (this.isRspcUp2 != conn) {
                    this.isRspcUp2 = conn;
                    this.testerHost.sendNotif(SOURCE_NAME, "SCCP RemoteSignalingPoint is " + (conn ? "enabled" : "disabled"),
                            "Dpc=" + this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc2(),
                            Level.INFO);
                }
            }
            if (rss != null) {
                boolean conn = !rss.isRemoteSsnProhibited();
                if (this.isRssUp2 != conn) {
                    this.isRssUp2 = conn;
                    this.testerHost.sendNotif(SOURCE_NAME, "SCCP RemoteSubSystem is " + (conn ? "enabled" : "disabled"), "Dpc="
                            + this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSpc2() + " Ssn="
                            + this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSsn(), Level.INFO);
                }
            }
        }
    }

    private void initSccp(Mtp3UserPart mtp3UserPart, int remoteSsn, int localSsn, int dpc, int opc,
                          int dpc2, int opc2, int ni, String callingPartyAddressDigits, String persistDir)
            throws Exception {

        this.sccpStack = new SccpStackImpl("TestingSccp");
        this.sccpStack.setPersistDir(persistDir);

        this.sccpStack.setMtp3UserPart(1, mtp3UserPart);
        this.sccpStack.start();
        this.sccpStack.removeAllResourses();

        RouterImpl router = (RouterImpl)sccpStack.getRouter();

        router.addMtp3ServiceAccessPoint(1, 1, opc, ni);
        router.addMtp3Destination(1, 1, dpc, dpc, 0, 255, 255);
        if (opc2 > 0) {
            router.addMtp3ServiceAccessPoint(2, 1, opc2, ni);
            if (dpc2 > 0)
                router.addMtp3Destination(2, 2, dpc2, dpc2, 0, 255, 255);
        }

        this.sccpProvider = this.sccpStack.getSccpProvider();
        this.resource = this.sccpStack.getSccpResource();

        this.resource.addRemoteSpc(1, dpc, 0, 0);
        this.resource.addRemoteSsn(1, dpc, remoteSsn, 0, false);
        if (dpc2 > 0) {
            this.resource.addRemoteSpc(2, dpc2, 0, 0);
            this.resource.addRemoteSsn(2, dpc2, remoteSsn, 0, false);
        }

        if (this.testerHost.getConfigurationData().getSccpConfigurationData().isRouteOnGtMode()) {
            router.addRoutingAddress(1, new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                                                        dpc, this.createGlobalTitle("-"), remoteSsn));
            router.addRoutingAddress(2, new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                                                        opc, this.createGlobalTitle("-"), localSsn));
            if (dpc2 > 0)
                router.addRoutingAddress(3, new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                                                            dpc2, this.createGlobalTitle("-"), remoteSsn));
            if (opc2 > 0)
                router.addRoutingAddress(4, new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                                                            opc2, this.createGlobalTitle("-"), localSsn));

            SccpAddress pattern = new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                                                  0, this.createGlobalTitle("*"), 0);
            if (dpc2 > 0) {
                router.addRule(1, RuleType.Loadshared, LoadSharingAlgorithm.Bit0,
                               OriginationType.LocalOriginated, pattern, "K", 1, 3, null);
            } else {
                router.addRule(1, RuleType.Solitary, LoadSharingAlgorithm.Undefined,
                               OriginationType.LocalOriginated, pattern, "K", 1, -1, null);
            }
            if (opc2 > 0) {
                router.addRule(2, RuleType.Loadshared, LoadSharingAlgorithm.Bit1,
                               OriginationType.RemoteOriginated, pattern, "K", 2, 4, null);
            } else {
                router.addRule(2, RuleType.Solitary, LoadSharingAlgorithm.Undefined,
                               OriginationType.RemoteOriginated, pattern, "K", 2, -1, null);
            }
            if (testerHost.getConfigurationData().getSccpConfigurationData().getNatureOfAddress().getValue() !=
                testerHost.getConfigurationData().getSccpConfigurationData().getNatureOfAddress2().getValue()) {
                pattern = new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                                          0, this.createGlobalTitle2("*"), 0);
                if (opc2 > 0) {
                    router.addRule(3, RuleType.Loadshared, LoadSharingAlgorithm.Bit1,
                                   OriginationType.RemoteOriginated, pattern, "K", 2, 4, null);
                } else {
                    router.addRule(3, RuleType.Solitary, LoadSharingAlgorithm.Undefined,
                                   OriginationType.RemoteOriginated, pattern, "K", 2, -1, null);
                }
            }
        }
    }

    private void stopSccp() {

        this.sccpStack.removeAllResourses();
        this.sccpStack.stop();
    }

    public SccpAddress createCallingPartyAddress() {
        if (this.testerHost.getConfigurationData().getSccpConfigurationData().isRouteOnGtMode()) {
            return new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, 0, createGlobalTitle(this.testerHost
                    .getConfigurationData().getSccpConfigurationData().getCallingPartyAddressDigits()), this.testerHost
                    .getConfigurationData().getSccpConfigurationData().getLocalSsn());
        } else {
            int spc = testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc();
            if ((testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc2() > 0) &&
                ((System.currentTimeMillis() % 2) > 0))
                spc = testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc2();
            return new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, spc, null,
                                   testerHost.getConfigurationData().getSccpConfigurationData().getLocalSsn());
        }
    }

    public SccpAddress createCalledPartyAddress() {
        int spc = testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc();
        if ((testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc2() > 0) &&
            ((System.currentTimeMillis() % 2) > 0))
            spc = testerHost.getConfigurationData().getSccpConfigurationData().getLocalSpc2();
        return new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, spc, null,
                               testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSsn());
    }

    public SccpAddress createCalledPartyAddress(String address, int ssn) {
        if (this.testerHost.getConfigurationData().getSccpConfigurationData().isRouteOnGtMode()) {
            return new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, 0, createGlobalTitle(address),
                    (ssn >= 0 ? ssn : this.testerHost.getConfigurationData().getSccpConfigurationData().getRemoteSsn()));
        } else {
            return createCalledPartyAddress();
        }
    }

    public GlobalTitle createGlobalTitle(String address) {
        GlobalTitle gt = null;
        switch (this.testerHost.getConfigurationData().getSccpConfigurationData().getGlobalTitleType().intValue()) {
            case GlobalTitleType.VAL_NOA_ONLY:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNatureOfAddress(), address);
                break;
            case GlobalTitleType.VAL_TT_ONLY:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getTranslationType(), address);
                break;
            case GlobalTitleType.VAL_TT_NP_ES:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getTranslationType(), this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNumberingPlan(), address);
                break;
            case GlobalTitleType.VAL_TT_NP_ES_NOA:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getTranslationType(), this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNumberingPlan(), this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNatureOfAddress(), address);
                break;
        }
        return gt;
    }

    public GlobalTitle createGlobalTitle2(String address) {
        GlobalTitle gt = null;
        switch (this.testerHost.getConfigurationData().getSccpConfigurationData().getGlobalTitleType().intValue()) {
            case GlobalTitleType.VAL_NOA_ONLY:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNatureOfAddress2(), address);
                break;
            case GlobalTitleType.VAL_TT_ONLY:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getTranslationType(), address);
                break;
            case GlobalTitleType.VAL_TT_NP_ES:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getTranslationType(), this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNumberingPlan(), address);
                break;
            case GlobalTitleType.VAL_TT_NP_ES_NOA:
                gt = GlobalTitle.getInstance(this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getTranslationType(), this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNumberingPlan(), this.testerHost.getConfigurationData().getSccpConfigurationData()
                        .getNatureOfAddress2(), address);
                break;
        }
        return gt;
    }

    // public SccpAddress createDestAddress() {
    // return new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, this.remotePc, null, this.remoteSsn);
    // }
}
