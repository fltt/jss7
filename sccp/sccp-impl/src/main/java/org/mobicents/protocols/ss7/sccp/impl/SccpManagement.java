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

package org.mobicents.protocols.ss7.sccp.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.mtp.Mtp3StatusCause;
import org.mobicents.protocols.ss7.sccp.ConcernedSignalingPointCode;
import org.mobicents.protocols.ss7.sccp.Mtp3ServiceAccessPoint;
import org.mobicents.protocols.ss7.sccp.RemoteSccpStatus;
import org.mobicents.protocols.ss7.sccp.RemoteSubSystem;
import org.mobicents.protocols.ss7.sccp.SccpListener;
import org.mobicents.protocols.ss7.sccp.SccpManagementEventListener;
import org.mobicents.protocols.ss7.sccp.SignallingPointStatus;
import org.mobicents.protocols.ss7.sccp.impl.message.SccpDataMessageImpl;
import org.mobicents.protocols.ss7.sccp.impl.message.SccpMessageImpl;
import org.mobicents.protocols.ss7.sccp.message.SccpDataMessage;
import org.mobicents.protocols.ss7.sccp.message.SccpMessage;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

/**
 * @author amit bhayani
 * @author sergey vetyutnev
 *
 */
public class SccpManagement {
    private final Logger logger;

    protected static final int MTP3_PAUSE = 3;
    protected static final int MTP3_RESUME = 4;
    protected static final int MTP3_STATUS = 5;

    protected static final int SSA = 1;
    protected static final int SSP = 2;
    protected static final int SST = 3;
    protected static final int SOR = 4;
    protected static final int SOG = 5;
    protected static final int SSC = 6;

    private static final String S_SSA = "SSA";
    private static final String S_SSP = "SSP";
    private static final String S_SST = "SST";
    private static final String S_SOR = "SOR";
    private static final String S_SOG = "SOG";
    private static final String S_SSC = "SSC";
    private static final String S_DEFAULT = "UNIDENTIFIED";

    protected static final int UNAVAILABILITY_CAUSE_UNKNOWN = 0;
    protected static final int UNAVAILABILITY_CAUSE_UNEQUIPED = 1;
    protected static final int UNAVAILABILITY_CAUSE_INACCESSIBLE = 2;

    private static final int ALL_POINT_CODE = -1;

    // private static final int SST_TIMER_DURATION_MIN = 10000;
    // private static final int SST_TIMER_DURATION_MAX = 600000;

    private SccpProviderImpl sccpProviderImpl;
    private SccpStackImpl sccpStackImpl;
    private SccpRoutingControl sccpRoutingControl;

    private ScheduledExecutorService managementExecutors;

    // Keeps track of how many SST are running for given DPC
    private final FastMap<Integer, FastList<SubSystemTest>> dpcVsSst = new FastMap<Integer, FastList<SubSystemTest>>();
    // Keeps the time when the last SSP (after recdMsgForProhibitedSsn()) has
    // been sent
    private final FastMap<DpcSsn, Long> dpcSspSent = new FastMap<DpcSsn, Long>();

    private final String name;

    public SccpManagement(String name, SccpProviderImpl sccpProviderImpl, SccpStackImpl sccpStackImpl) {
        this.name = name;
        this.logger = Logger.getLogger(SccpManagement.class.getCanonicalName() + "-" + this.name);
        this.sccpProviderImpl = sccpProviderImpl;
        this.sccpStackImpl = sccpStackImpl;
    }

    public SccpRoutingControl getSccpRoutingControl() {
        return sccpRoutingControl;
    }

    public void setSccpRoutingControl(SccpRoutingControl sccpRoutingControl) {
        this.sccpRoutingControl = sccpRoutingControl;
    }

    public void onManagementMessage(SccpDataMessage message) {
        byte[] data = message.getData();
        int messgType = data[0] & 0xff;
        int affectedSsn = data[1] & 0xff;
        int affectedPc = (data[2] & 0xff) | ((data[3] & 0xff) << 8);
        int subsystemMultiplicity = data[3] & 0xff;

        switch (messgType) {
            case SSA:

                if (logger.isInfoEnabled()) {
                    logger.info(String.format(
                            "Rx : SSA, Affected SSN=%d, Affected PC=%d, Subsystem Multiplicity Ind=%d SeqControl=%d",
                            affectedSsn, affectedPc, subsystemMultiplicity, message.getSls()));
                }

                // Stop the SST if already started
                this.cancelSst(affectedPc, affectedSsn);

                if (affectedSsn == 1) {
                    this.allowRsp(affectedPc, false, RemoteSccpStatus.available);
                } else {
                    // Mark remote SSN Allowed
                    this.allowSsn(affectedPc, affectedSsn);
                }
                break;
            case SSP:
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn(String.format(
                            "Rx : SSP, Affected SSN=%d, Affected PC=%d, Subsystem Multiplicity Ind=%d SeqControl=%d",
                            affectedSsn, affectedPc, subsystemMultiplicity, message.getSls()));
                }

                if (affectedSsn == 1) {
                    // A subsystem prohibited message with SSN = 1 is not allowed
                } else {
                    this.prohibitSsn(affectedPc, affectedSsn);
                    this.startSst(affectedPc, affectedSsn);
                }

                break;
            case SST:
                if (affectedSsn == 1) {
                    // In the case where the Subsystem-Status-Test message is
                    // testing the status of SCCP management (SSN = 1), if the SCCP
                    // at the destination node is functioning, then a Subsystem
                    // Allowed message with SSN = 1 is sent to SCCP management at
                    // the node conducting the test. If the SCCP is not functioning,
                    // then the MTP cannot deliver the SST message to the SCCP. A
                    // UPU message is returned to the SST initiating node by the
                    // MTP.

                    if (logger.isInfoEnabled()) {
                        logger.info(String.format(
                                "Rx : SST, Affected SSN=%d, Affected PC=%d, Subsystem Multiplicity Ind=%d SeqControl=%d",
                                affectedSsn, affectedPc, subsystemMultiplicity, message.getSls()));
                    }

                    this.sendSSA(message, affectedSsn);
                } else {

                    SccpListener listener = this.sccpProviderImpl.getSccpListener(affectedSsn);
                    if (listener != null) {
                        this.sendSSA(message, affectedSsn);
                        return;
                    }

                    if (logger.isInfoEnabled()) {
                        logger.info(String.format("Received SST for unavailable SSN=%d", affectedSsn));
                    }
                }

                break;
            case SOR:
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn("Received SOR. SOR not yet implemented, dropping message");
                }
                break;
            case SOG:
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn("Received SOG. SOG not yet implemented, dropping message");
                }
                break;
            case SSC:
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn("Received SSC. SSC not yet implemented, dropping message");
                }
                break;
            default:
                logger.error("Received SCMG with unknown MessageType.");
                break;
        }
    }

    private void sendManagementMessage(int dpc, int opc, int messageTypeCode, int affectedSsn, int subsystemMultiplicityIndicator) {

        Mtp3ServiceAccessPoint sap = this.sccpStackImpl.router.findMtp3ServiceAccessPoint(dpc, 0);
        if (sap == null) {
            logger.warn(String.format("Failed sendManagementMessage : Mtp3ServiceAccessPoint has not found for dpc=%d", dpc));
            return;
        }
        int affectedPc;
        if (messageTypeCode == SST || messageTypeCode == SOG) {
            affectedPc = dpc;
        } else if (opc > 0) {
            affectedPc = opc;
        } else {
            affectedPc = sap.getOpc();
        }

        SccpAddress calledAdd = new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, dpc, null, 1);
        SccpAddress callingAdd = new SccpAddress(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, affectedPc, null, 1);

        byte[] data = new byte[5];
        data[0] = (byte) messageTypeCode;
        data[1] = (byte) affectedSsn; // affected SSN
        data[2] = (byte) (affectedPc & 0x000000ff);
        data[3] = (byte) ((affectedPc & 0x0000ff00) >> 8);
        data[4] = (byte) subsystemMultiplicityIndicator;
        SccpDataMessageImpl msg = (SccpDataMessageImpl) sccpProviderImpl.getMessageFactory().createDataMessageClass0(calledAdd,
                callingAdd, data, -1, false, null, null);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Tx :SCMG Type=%d, Affected SSN=%d, AffectedPc=%d", messageTypeCode, affectedSsn,
                    affectedPc));
        }

        try {
            this.sccpRoutingControl.sendMessageToMtp(msg);
        } catch (IOException e) {
            logger.error(String.format("Exception while trying to send SSP message=%s", msg), e);
        }
    }

    private void sendSSA(SccpMessage msg, int affectedSsn) {
        this.sendManagementMessage(((SccpMessageImpl) msg).getIncomingOpc(),
                                   ((SccpMessageImpl) msg).getIncomingDpc(),
                                   SSA, affectedSsn, 0);
    }

    protected void broadcastChangedSsnState(int affectedSsn, boolean inService) {
        this.broadcastChangedSsnState(affectedSsn, inService, ALL_POINT_CODE);
    }

    private void broadcastChangedSsnState(int affectedSsn, boolean inService, int concernedPointCode) {

        FastMap<Integer, ConcernedSignalingPointCode> lst = this.sccpStackImpl.sccpResource.concernedSpcs;
        for (FastMap.Entry<Integer, ConcernedSignalingPointCode> e = lst.head(), end = lst.tail(); (e = e.getNext()) != end;) {
            ConcernedSignalingPointCode concernedSubSystem = e.getValue();

            int dpc = concernedSubSystem.getRemoteSpc();

            if (concernedPointCode == ALL_POINT_CODE || concernedPointCode == dpc) {
                // Send SSA/SSP to only passed concerned point code
                if (inService)
                    this.sendManagementMessage(dpc, -1, SSA, affectedSsn, 0);
                else
                    this.sendManagementMessage(dpc, -1, SSP, affectedSsn, 0);
            }
        }
    }

    protected void recdMsgForProhibitedSsn(SccpMessage msg, int ssn) {

        // we do not send new SSP's to the same DPC+SSN during the one second
        // interval
        int dpc = msg.getIncomingOpc();
        DpcSsn key = new DpcSsn(dpc, ssn);
        long now = System.currentTimeMillis();
        synchronized (this.dpcSspSent) {
            Long dt = this.dpcSspSent.get(key);
            if (dt != null && now - dt < 1000)
                return;
            this.dpcSspSent.put(key, now);
        }

        // Send SSP (when message is mtp3-originated)
        if (msg.getIsMtpOriginated()) {
            this.sendManagementMessage(dpc, -1, SSP, ssn, 0);
        }
    }

    protected void handleMtp3Pause(int affectedPc) {
        // Look at Q.714 Section 5.2.2
        this.cancelAllSst(affectedPc, true);
        this.prohibitRsp(affectedPc, true, RemoteSccpStatus.inaccessible);

    }

    protected void handleMtp3Resume(int affectedPc) {
        // Look at Q.714 Section 5.2.2
        this.allowRsp(affectedPc, true, RemoteSccpStatus.available);

        // Send SSA for all SS registered to affectedPc if it's included in
        // concerned point-code
        FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();
        for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1.getNext()) != end1;) {
            int affectedSsn = e1.getKey();

            this.broadcastChangedSsnState(affectedSsn, true, affectedPc);
        }

    }

    protected void handleMtp3Status(Mtp3StatusCause cause, int affectedPc, int congStatus) {

        switch (cause) {
            case SignallingNetworkCongested:
                // Signaling Network Congestion
                // TODO: implement congestion management
                break;

            case UserPartUnavailability_Unknown:
            case UserPartUnavailability_InaccessibleRemoteUser:
                this.prohibitRsp(affectedPc, false,
                        (cause == Mtp3StatusCause.UserPartUnavailability_Unknown ? RemoteSccpStatus.unavailableReasonUnknown
                                : RemoteSccpStatus.inaccessible));

                SubSystemTest sstForSsn1 = this.cancelAllSst(affectedPc, false);
                if (sstForSsn1 != null) {
                    sstForSsn1.setRecdMtpStatusResp(true);
                } else {
                    // ITU-T Q.714 5.3.4.2 Actions at the initiating node

                    // A subsystem status test for SSN = 1 is initiated when an
                    // MTP-STATUS indication primitive is received with
                    // "remote user inaccessibility" or "unknown" information
                    // for the SCCP at a remote signalling point

                    // Start sending the SST for SSN1
                    this.startSst(affectedPc, 1);
                }
                break;

            case UserPartUnavailability_UnequippedRemoteUser:
                // See ITU-T Q.714 5.2.2 Signalling point prohibited

                // In the case where the SCCP has received an MTP-STATUS
                // indication primitive relating to an unavailable SCCP, the
                // SCCP marks the status of the SCCP and each SSN for the
                // relevant destination to "prohibited" and initiates a
                // subsystem status test with SSN = 1. If the cause in the
                // MTP-STATUS indication primitive indicates "unequipped user",
                // then no subsystem status test is initiated.
                this.prohibitRsp(affectedPc, false, RemoteSccpStatus.unequipped);

                // Discontinues all subsystem status tests (including SSN = 1)
                // if an MTP-PAUSE or MTP-STATUS indication primitive is
                // received with a cause of "unequipped SCCP"
                this.cancelAllSst(affectedPc, true);
                break;
        }
    }

    private void prohibitAllSsn(int affectedPc) {
        FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();
        FastMap<Integer, RemoteSubSystem> remoteSsns = this.sccpStackImpl.sccpResource.remoteSsns;
        for (FastMap.Entry<Integer, RemoteSubSystem> e = remoteSsns.head(), end = remoteSsns.tail(); (e = e.getNext()) != end;) {
            RemoteSubSystemImpl remoteSsn = (RemoteSubSystemImpl) e.getValue();
            if (remoteSsn.getRemoteSpc() == affectedPc) {
                if (!remoteSsn.isRemoteSsnProhibited()) {
                    remoteSsn.setRemoteSsnProhibited(true);

                    setRemoteSsnState(remoteSsn, false);

                    // for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1
                    // .getNext()) != end1;) {
                    // try {
                    // e1.getValue().onState(affectedPc, remoteSsn.getRemoteSsn(), false, 0);
                    // } catch (Exception ee) {
                    // logger.error("Exception while invoking onState", ee);
                    // }
                    // }
                }
            }
        }
    }

    private void allowAllSsn(int affectedPc) {

        FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();
        FastMap<Integer, RemoteSubSystem> remoteSsns = this.sccpStackImpl.sccpResource.remoteSsns;
        for (FastMap.Entry<Integer, RemoteSubSystem> e = remoteSsns.head(), end = remoteSsns.tail(); (e = e.getNext()) != end;) {
            RemoteSubSystemImpl remoteSsn = (RemoteSubSystemImpl) e.getValue();
            if (remoteSsn.getRemoteSpc() == affectedPc) {

                if (remoteSsn.getMarkProhibitedWhenSpcResuming()) {
                    if (!remoteSsn.isRemoteSsnProhibited()) {
                        remoteSsn.setRemoteSsnProhibited(true);
                        this.startSst(affectedPc, remoteSsn.getRemoteSsn());

                        setRemoteSsnState(remoteSsn, false);

                        // for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1
                        // .getNext()) != end1;) {
                        // try {
                        // e1.getValue().onState(affectedPc, remoteSsn.getRemoteSsn(), false, 0);
                        // } catch (Throwable ee) {
                        // logger.error("Exception while invoking onState", ee);
                        // }
                        // }
                    }

                } else {
                    if (remoteSsn.isRemoteSsnProhibited()) {
                        remoteSsn.setRemoteSsnProhibited(false);

                        setRemoteSsnState(remoteSsn, true);

                        // for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1
                        // .getNext()) != end1;) {
                        // try {
                        // e1.getValue().onState(affectedPc, remoteSsn.getRemoteSsn(), true, 0);
                        // } catch (Exception ee) {
                        // logger.error("Exception while invoking onState", ee);
                        // }
                        // }
                    }
                }
            }
        }
    }

    private void prohibitRsp(int affectedPc, boolean spcChanging, RemoteSccpStatus remoteSccpStatus) {

        RemoteSignalingPointCodeImpl remoteSpc = (RemoteSignalingPointCodeImpl) this.sccpStackImpl.getSccpResource()
                .getRemoteSpcByPC(affectedPc);
        if (remoteSpc != null) {
            boolean oldRemoteSpcProhibited = remoteSpc.isRemoteSpcProhibited();
            boolean oldRemoteSccpProhibited = remoteSpc.isRemoteSccpProhibited();
            if (spcChanging)
                remoteSpc.setRemoteSpcProhibited(true);
            if (remoteSccpStatus != null && remoteSccpStatus != RemoteSccpStatus.available)
                remoteSpc.setRemoteSccpProhibited(true);

            FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();
            for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1.getNext()) != end1;) {
                try {
                    e1.getValue().onPcState(
                            affectedPc,
                            (remoteSpc.isRemoteSpcProhibited() ? SignallingPointStatus.inaccessible
                                    : SignallingPointStatus.accessible), 0, remoteSccpStatus);
                } catch (Exception ee) {
                    logger.error("Exception while invoking onPcState", ee);
                }
            }

            for (SccpManagementEventListener lstr : this.sccpProviderImpl.managementEventListeners) {
                try {
                    if (remoteSpc.isRemoteSpcProhibited() != oldRemoteSpcProhibited) {
                        lstr.onRemoteSpcDown(remoteSpc);
                    }
                } catch (Throwable ee) {
                    logger.error("Exception while invoking onRemoteSpcDown", ee);
                }

                try {
                    if (remoteSpc.isRemoteSccpProhibited() != oldRemoteSccpProhibited) {
                        lstr.onRemoteSccpDown(remoteSpc);
                    }
                } catch (Throwable ee) {
                    logger.error("Exception while invoking onRemoteSccpDown", ee);
                }
            }
        }

        this.prohibitAllSsn(affectedPc);
    }

    private void allowRsp(int affectedPc, boolean spcChanging, RemoteSccpStatus remoteSccpStatus) {

        RemoteSignalingPointCodeImpl remoteSpc = (RemoteSignalingPointCodeImpl) this.sccpStackImpl.getSccpResource()
                .getRemoteSpcByPC(affectedPc);
        if (remoteSpc != null) {
            boolean oldRemoteSpcProhibited = remoteSpc.isRemoteSpcProhibited();
            boolean oldRemoteSccpProhibited = remoteSpc.isRemoteSccpProhibited();
            if (spcChanging)
                remoteSpc.setRemoteSpcProhibited(false);
            if (remoteSccpStatus != null && remoteSccpStatus == RemoteSccpStatus.available)
                remoteSpc.setRemoteSccpProhibited(false);

            FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();
            for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1.getNext()) != end1;) {
                try {
                    e1.getValue().onPcState(affectedPc, SignallingPointStatus.accessible, 0, remoteSccpStatus);
                } catch (Exception ee) {
                    logger.error("Exception while invoking onPcState", ee);
                }
            }

            for (SccpManagementEventListener lstr : this.sccpProviderImpl.managementEventListeners) {
                try {
                    if (remoteSpc.isRemoteSpcProhibited() != oldRemoteSpcProhibited) {
                        lstr.onRemoteSpcUp(remoteSpc);
                    }
                } catch (Throwable ee) {
                    logger.error("Exception while invoking onRemoteSpcUp", ee);
                }

                try {
                    if (remoteSpc.isRemoteSccpProhibited() != oldRemoteSccpProhibited) {
                        lstr.onRemoteSccpUp(remoteSpc);
                    }
                } catch (Throwable ee) {
                    logger.error("Exception while invoking onRemoteSccpUp", ee);
                }
            }
        }

        this.allowAllSsn(affectedPc);
    }

    private void prohibitSsn(int affectedPc, int ssn) {

        FastMap<Integer, RemoteSubSystem> remoteSsns = this.sccpStackImpl.sccpResource.remoteSsns;
        for (FastMap.Entry<Integer, RemoteSubSystem> e = remoteSsns.head(), end = remoteSsns.tail(); (e = e.getNext()) != end;) {
            RemoteSubSystemImpl remoteSsn = (RemoteSubSystemImpl) e.getValue();
            if (remoteSsn.getRemoteSpc() == affectedPc && remoteSsn.getRemoteSsn() == ssn) {
                if (!remoteSsn.isRemoteSsnProhibited()) {
                    setRemoteSsnState(remoteSsn, false);
                }
                break;
            }
        }
    }

    private void setRemoteSsnState(RemoteSubSystemImpl remoteSsn, boolean isEnabled) {
        remoteSsn.setRemoteSsnProhibited(!isEnabled);

        FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();

        for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1.getNext()) != end1;) {
            try {
                e1.getValue().onState(remoteSsn.getRemoteSpc(), remoteSsn.getRemoteSsn(), isEnabled, 0);
            } catch (Exception ee) {
                logger.error("Exception while invoking onState", ee);
            }
        }

        for (SccpManagementEventListener lstr : this.sccpProviderImpl.managementEventListeners) {
            try {
                if (isEnabled)
                    lstr.onRemoteSubSystemUp(remoteSsn);
                else
                    lstr.onRemoteSubSystemDown(remoteSsn);
            } catch (Throwable ee) {
                logger.error("Exception while invoking onRemoteSubSystemUp/Down", ee);
            }
        }
    }

    private void allowSsn(int affectedPc, int ssn) {

        FastMap<Integer, SccpListener> lstrs = this.sccpProviderImpl.getAllSccpListeners();
        FastMap<Integer, RemoteSubSystem> remoteSsns = this.sccpStackImpl.sccpResource.remoteSsns;
        for (FastMap.Entry<Integer, RemoteSubSystem> e = remoteSsns.head(), end = remoteSsns.tail(); (e = e.getNext()) != end;) {
            RemoteSubSystemImpl remoteSsn = (RemoteSubSystemImpl) e.getValue();
            if (remoteSsn.getRemoteSpc() == affectedPc && (ssn == 1 || remoteSsn.getRemoteSsn() == ssn)) {
                if (remoteSsn.isRemoteSsnProhibited()) {
                    remoteSsn.setRemoteSsnProhibited(false);

                    setRemoteSsnState(remoteSsn, true);

                    // for (FastMap.Entry<Integer, SccpListener> e1 = lstrs.head(), end1 = lstrs.tail(); (e1 = e1
                    // .getNext()) != end1;) {
                    // try {
                    // e1.getValue().onState(affectedPc, remoteSsn.getRemoteSsn(), true, 0);
                    // } catch (Exception ee) {
                    // logger.error("Exception while invoking onState", ee);
                    // }
                    // }
                }
                break;
            }
        }
    }

    private void startSst(int affectedPc, int affectedSsn) {

        FastList<SubSystemTest> ssts = this.getSubSystemTestListForAffectedDpc(affectedPc, true);
        synchronized (ssts) {
            SubSystemTest sst = getSubSystemTestBySsn(ssts, affectedSsn);
            if (sst == null) {
                sst = new SubSystemTest(affectedSsn, affectedPc, ssts);
                sst.startTest();
            } else {
                sst.resetTimerDuration();
                sst.stopTest();
                sst.startTest();
            }
        }
    }

    private void cancelSst(int affectedPc, int affectedSsn) {
        FastList<SubSystemTest> ssts1 = this.getSubSystemTestListForAffectedDpc(affectedPc, false);
        if (ssts1 != null) {
            SubSystemTest sst1 = getSubSystemTestBySsn(ssts1, affectedSsn);
            if (sst1 != null) {
                sst1.stopTest();
            }
        }
    }

    private SubSystemTest cancelAllSst(int affectedPc, boolean cancelSstForSsn1) {
        SubSystemTest sstForSsn1 = null;
        // cancel all SST if any
        FastList<SubSystemTest> ssts = this.getSubSystemTestListForAffectedDpc(affectedPc, false);
        if (ssts != null) {
            ArrayList<SubSystemTest> arr = new ArrayList<SubSystemTest>();
            synchronized (ssts) {
                // TODO : Amit: Added n.getValue() != null check. Evaluate
                // javolution.FastList as why for loop continues even after
                // removing
                // last element?
                for (FastList.Node<SubSystemTest> n = ssts.head(), endSst = ssts.tail(); ((n = n.getNext()) != endSst)
                        && n.getValue() != null;) {
                    arr.add(n.getValue());
                }
            }
            for (SubSystemTest sst : arr) {
                // If SSN = 1 but flag ssn1 is false, means we don't stop this
                // SST and return back the reference to it
                if (sst.getSsn() == 1 && !cancelSstForSsn1) {
                    sstForSsn1 = sst;
                    continue;
                }
                sst.stopTest();
            }
        }

        return sstForSsn1;
    }

    private FastList<SubSystemTest> getSubSystemTestListForAffectedDpc(int affectedPc, boolean createIfAbsent) {
        synchronized (dpcVsSst) {
            FastList<SubSystemTest> ssts = dpcVsSst.get(affectedPc);
            if (ssts != null || !createIfAbsent)
                return ssts;

            ssts = new FastList<SubSystemTest>();
            dpcVsSst.put(affectedPc, ssts);
            return ssts;
        }
    }

    private SubSystemTest getSubSystemTestBySsn(FastList<SubSystemTest> ssts, int affectedSsn) {
        synchronized (ssts) {
            SubSystemTest sst = null;
            for (FastList.Node<SubSystemTest> n = ssts.head(), end = ssts.tail(); (n = n.getNext()) != end;) {
                sst = n.getValue();
                if (sst.getSsn() == affectedSsn) {
                    break;
                }
            }
            return sst;
        }
    }

    private class SubSystemTest implements Runnable {
        // FIXME: remove "Thread", so we eat less resources.

        private volatile boolean started = false;

        // Flag to check if received an MTP-STATUS indication primitive stating
        // User Part Unavailable.
        private volatile boolean recdMtpStatusResp = true;

        private Future testFuture;
        private FastList<SubSystemTest> testsList; // just a ref to list of
                                                   // testse for DPC, instances
                                                   // of this classes should be
                                                   // there.

        private int ssn = 0;
        private int affectedPc = 0;

        private int currentTimerDelay = sccpStackImpl.sstTimerDuration_Min;

        SubSystemTest(int ssn, int affectedPc, FastList<SubSystemTest> testsList) {
            this.ssn = ssn;
            this.affectedPc = affectedPc;
            this.testsList = testsList;
        }

        public int getSsn() {
            return ssn;
        }

        public void setRecdMtpStatusResp(boolean recdMtpStatusResp) {
            this.recdMtpStatusResp = recdMtpStatusResp;
        }

        void stopTest() {
            synchronized (this.testsList) {
                started = false;
                Future f = this.testFuture;
                if (f != null) {
                    this.testsList.remove(this);
                    this.testFuture = null;
                    f.cancel(false);
                }
            }

            try {
                notify();
            } catch (Exception e) {
            }
        }

        void startTest() {
            synchronized (this.testsList) {
                if (!started) {
                    this.testFuture = managementExecutors.schedule(this, currentTimerDelay, TimeUnit.MILLISECONDS);

                    // increase the "T(stat info)" timer delay up to 10 minutes
                    // for the next step
                    currentTimerDelay = (int) (currentTimerDelay * sccpStackImpl.sstTimerDuration_IncreaseFactor);
                    if (currentTimerDelay > sccpStackImpl.sstTimerDuration_Max)
                        currentTimerDelay = sccpStackImpl.sstTimerDuration_Max;

                    started = true;
                    this.testsList.add(this);
                }
            }
        }

        private void resetTimerDuration() {
            currentTimerDelay = sccpStackImpl.sstTimerDuration_Min;
        }

        public void run() {

            if (started) {

                synchronized (this.testsList) {
                    if (this.ssn == 1 && !this.recdMtpStatusResp) {
                        // If no MTP STATUS received, means we consider
                        // previously
                        // unavailable (SCCP) has recovered

                        this.stopTest();

                        // Stop the SST if already started
                        FastList<SubSystemTest> ssts1 = getSubSystemTestListForAffectedDpc(affectedPc, false);
                        if (ssts1 != null) {
                            SubSystemTest sst1 = getSubSystemTestBySsn(ssts1, ssn);
                            if (sst1 != null) {
                                sst1.stopTest();
                            }
                        }

                        if (ssn == 1) {
                            allowRsp(affectedPc, false, RemoteSccpStatus.available);
                        } else {
                            // Mark remote SSN Allowed
                            allowSsn(affectedPc, ssn);
                        }

                        return;

                    }
                    // Set it false again so we wait for response again after
                    // sending SST for SSN = 1 bellow
                    this.recdMtpStatusResp = false;

                    // TODO : How much to sleep?
                    this.stopTest();
                    this.startTest();
                }

                sendManagementMessage(affectedPc, -1, SST, ssn, 0);

            }// while

        }// run

    }// SubSystemTest

    public void start() {

        synchronized (dpcVsSst) {
            this.dpcVsSst.clear();
        }
        managementExecutors = Executors.newScheduledThreadPool(1);

    }

    public void stop() {
        // no need to stop, it will clean on start, and scheduler is dead.
        managementExecutors.shutdownNow();

    }

    private String getMessageType(int msgType) {
        switch (msgType) {
            case SSA:
                return S_SSA;
            case SSP:
                return S_SSP;
            case SST:
                return S_SST;
            case SOR:
                return S_SOR;
            case SOG:
                return S_SOG;
            case SSC:
                return S_SSC;
            default:
                return S_DEFAULT;
        }
    }

    private class DpcSsn {

        private int dpc;
        private int ssn;

        public DpcSsn(int aDpc, int aSsn) {
            dpc = aDpc;
            ssn = aSsn;
        }

        public int getDpc() {
            return dpc;
        }

        public int getSsn() {
            return ssn;
        }

        public boolean equals(Object obj) {
            if (obj == null && !(obj instanceof DpcSsn))
                return false;

            DpcSsn y = (DpcSsn) obj;
            if (this.dpc == y.dpc && this.ssn == y.ssn)
                return true;
            else
                return false;
        }

        public int hashCode() {
            return this.dpc + 256 * 256 * this.ssn;
        }
    }
}
