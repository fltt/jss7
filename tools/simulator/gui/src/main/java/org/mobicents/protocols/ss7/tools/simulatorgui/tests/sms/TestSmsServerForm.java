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

package org.mobicents.protocols.ss7.tools.simulatorgui.tests.sms;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.management.Notification;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mobicents.protocols.ss7.tools.simulator.common.AddressNatureType;
import org.mobicents.protocols.ss7.tools.simulator.level3.NumberingPlanMapType;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.TestSmsServerManMBean;
import org.mobicents.protocols.ss7.tools.simulatorgui.M3uaForm;
import org.mobicents.protocols.ss7.tools.simulatorgui.TestingForm;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class TestSmsServerForm extends TestingForm {

    private static final long serialVersionUID = 7219729321344799776L;

    private TestSmsServerManMBean smsServer;

    private JComboBox cbDestAN;
    private JComboBox cbDestNP;
    private JComboBox cbOrigAN;
    private JComboBox cbOrigNP;
    private JTextField tbMessage;
    private JTextField tbDestIsdnNumber;
    private JTextField tbOrigIsdnNumber;
    private JTextField tbImsi;
    private JTextField tbVlrNumber;
    private JLabel lbMessage;
    private JLabel lbResult;
    private JLabel lbState;

    public TestSmsServerForm(JFrame owner) {
        super(owner);

        JPanel panel = new JPanel();
        panel_c.add(panel, BorderLayout.CENTER);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWeights = new double[] { 0.0, 1.0 };
        gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        panel.setLayout(gbl_panel);

        JLabel label = new JLabel("Message text");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        panel.add(label, gbc_label);

        tbMessage = new JTextField();
        tbMessage.setColumns(10);
        GridBagConstraints gbc_tbMessage = new GridBagConstraints();
        gbc_tbMessage.insets = new Insets(0, 0, 5, 0);
        gbc_tbMessage.fill = GridBagConstraints.HORIZONTAL;
        gbc_tbMessage.gridx = 1;
        gbc_tbMessage.gridy = 0;
        panel.add(tbMessage, gbc_tbMessage);

        JLabel lblDestinationAN = new JLabel("Destination AddressNature");
        GridBagConstraints gbc_lblDestinationAN = new GridBagConstraints();
        gbc_lblDestinationAN.anchor = GridBagConstraints.EAST;
        gbc_lblDestinationAN.insets = new Insets(0, 0, 5, 5);
        gbc_lblDestinationAN.gridx = 0;
        gbc_lblDestinationAN.gridy = 1;
        panel.add(lblDestinationAN, gbc_lblDestinationAN);

        cbDestAN = new JComboBox();
        GridBagConstraints gbc_cbDestAN = new GridBagConstraints();
        gbc_cbDestAN.insets = new Insets(0, 0, 5, 0);
        gbc_cbDestAN.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbDestAN.gridx = 1;
        gbc_cbDestAN.gridy = 1;
        panel.add(cbDestAN, gbc_cbDestAN);

        JLabel lblDestinationNP = new JLabel("Destination NumberingPlan");
        GridBagConstraints gbc_lblDestinationNP = new GridBagConstraints();
        gbc_lblDestinationNP.anchor = GridBagConstraints.EAST;
        gbc_lblDestinationNP.insets = new Insets(0, 0, 5, 5);
        gbc_lblDestinationNP.gridx = 0;
        gbc_lblDestinationNP.gridy = 2;
        panel.add(lblDestinationNP, gbc_lblDestinationNP);

        cbDestNP = new JComboBox();
        GridBagConstraints gbc_cbDestNP = new GridBagConstraints();
        gbc_cbDestNP.insets = new Insets(0, 0, 5, 0);
        gbc_cbDestNP.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbDestNP.gridx = 1;
        gbc_cbDestNP.gridy = 2;
        panel.add(cbDestNP, gbc_cbDestNP);

        JLabel lblDestinationIsdmNumber = new JLabel("Destination ISDN number");
        GridBagConstraints gbc_lblDestinationIsdmNumber = new GridBagConstraints();
        gbc_lblDestinationIsdmNumber.anchor = GridBagConstraints.EAST;
        gbc_lblDestinationIsdmNumber.insets = new Insets(0, 0, 5, 5);
        gbc_lblDestinationIsdmNumber.gridx = 0;
        gbc_lblDestinationIsdmNumber.gridy = 3;
        panel.add(lblDestinationIsdmNumber, gbc_lblDestinationIsdmNumber);

        tbDestIsdnNumber = new JTextField();
        tbDestIsdnNumber.setColumns(10);
        GridBagConstraints gbc_tbDestIsdnNumber = new GridBagConstraints();
        gbc_tbDestIsdnNumber.insets = new Insets(0, 0, 5, 0);
        gbc_tbDestIsdnNumber.fill = GridBagConstraints.HORIZONTAL;
        gbc_tbDestIsdnNumber.gridx = 1;
        gbc_tbDestIsdnNumber.gridy = 3;
        panel.add(tbDestIsdnNumber, gbc_tbDestIsdnNumber);

        JLabel lblOriginationAN = new JLabel("Origination AddressNature");
        GridBagConstraints gbc_lblOriginationAN = new GridBagConstraints();
        gbc_lblOriginationAN.anchor = GridBagConstraints.EAST;
        gbc_lblOriginationAN.insets = new Insets(0, 0, 5, 5);
        gbc_lblOriginationAN.gridx = 0;
        gbc_lblOriginationAN.gridy = 4;
        panel.add(lblOriginationAN, gbc_lblOriginationAN);

        cbOrigAN = new JComboBox();
        GridBagConstraints gbc_cbOrigAN = new GridBagConstraints();
        gbc_cbOrigAN.insets = new Insets(0, 0, 5, 0);
        gbc_cbOrigAN.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbOrigAN.gridx = 1;
        gbc_cbOrigAN.gridy = 4;
        panel.add(cbOrigAN, gbc_cbOrigAN);

        JLabel lblOriginationNP = new JLabel("Origination NumberingPlan");
        GridBagConstraints gbc_lblOriginationNP = new GridBagConstraints();
        gbc_lblOriginationNP.anchor = GridBagConstraints.EAST;
        gbc_lblOriginationNP.insets = new Insets(0, 0, 5, 5);
        gbc_lblOriginationNP.gridx = 0;
        gbc_lblOriginationNP.gridy = 5;
        panel.add(lblOriginationNP, gbc_lblOriginationNP);

        cbOrigNP = new JComboBox();
        GridBagConstraints gbc_cbOrigNP = new GridBagConstraints();
        gbc_cbOrigNP.insets = new Insets(0, 0, 5, 0);
        gbc_cbOrigNP.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbOrigNP.gridx = 1;
        gbc_cbOrigNP.gridy = 5;
        panel.add(cbOrigNP, gbc_cbOrigNP);

        JLabel lblOriginationIsdnNumber = new JLabel("Origination ISDN number");
        GridBagConstraints gbc_lblOriginationIsdnNumber = new GridBagConstraints();
        gbc_lblOriginationIsdnNumber.anchor = GridBagConstraints.EAST;
        gbc_lblOriginationIsdnNumber.insets = new Insets(0, 0, 5, 5);
        gbc_lblOriginationIsdnNumber.gridx = 0;
        gbc_lblOriginationIsdnNumber.gridy = 6;
        panel.add(lblOriginationIsdnNumber, gbc_lblOriginationIsdnNumber);

        tbOrigIsdnNumber = new JTextField();
        tbOrigIsdnNumber.setColumns(10);
        GridBagConstraints gbc_tbOrigIsdnNumber = new GridBagConstraints();
        gbc_tbOrigIsdnNumber.insets = new Insets(0, 0, 5, 0);
        gbc_tbOrigIsdnNumber.fill = GridBagConstraints.HORIZONTAL;
        gbc_tbOrigIsdnNumber.gridx = 1;
        gbc_tbOrigIsdnNumber.gridy = 6;
        panel.add(tbOrigIsdnNumber, gbc_tbOrigIsdnNumber);

        JLabel lblImsi = new JLabel("IMSI");
        GridBagConstraints gbc_lblImsi = new GridBagConstraints();
        gbc_lblImsi.anchor = GridBagConstraints.EAST;
        gbc_lblImsi.insets = new Insets(0, 0, 5, 5);
        gbc_lblImsi.gridx = 0;
        gbc_lblImsi.gridy = 7;
        panel.add(lblImsi, gbc_lblImsi);

        tbImsi = new JTextField();
        tbImsi.setColumns(10);
        GridBagConstraints gbc_tbImsi = new GridBagConstraints();
        gbc_tbImsi.insets = new Insets(0, 0, 5, 0);
        gbc_tbImsi.fill = GridBagConstraints.HORIZONTAL;
        gbc_tbImsi.gridx = 1;
        gbc_tbImsi.gridy = 7;
        panel.add(tbImsi, gbc_tbImsi);

        JLabel lblVlrNumber = new JLabel("VLR number");
        GridBagConstraints gbc_lblVlrNumber = new GridBagConstraints();
        gbc_lblVlrNumber.anchor = GridBagConstraints.EAST;
        gbc_lblVlrNumber.insets = new Insets(0, 0, 5, 5);
        gbc_lblVlrNumber.gridx = 0;
        gbc_lblVlrNumber.gridy = 8;
        panel.add(lblVlrNumber, gbc_lblVlrNumber);

        tbVlrNumber = new JTextField();
        tbVlrNumber.setColumns(10);
        GridBagConstraints gbc_tbVlrNumber = new GridBagConstraints();
        gbc_tbVlrNumber.insets = new Insets(0, 0, 5, 0);
        gbc_tbVlrNumber.fill = GridBagConstraints.HORIZONTAL;
        gbc_tbVlrNumber.gridx = 1;
        gbc_tbVlrNumber.gridy = 8;
        panel.add(tbVlrNumber, gbc_tbVlrNumber);

        JPanel panel_1 = new JPanel();
        GridBagLayout gbl_panel_1 = new GridBagLayout();
        gbl_panel_1.columnWeights = new double[] { 1.0, 1.0, 1.0 };
        gbl_panel_1.rowWeights = new double[] { 0.0 };
        panel_1.setLayout(gbl_panel_1);
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.insets = new Insets(0, 0, 5, 0);
        gbc_panel_1.gridx = 1;
        gbc_panel_1.gridy = 9;
        panel.add(panel_1, gbc_panel_1);

        JButton btnSendSriforsm = new JButton("Send SRIForSM");
        btnSendSriforsm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendSRIForSM();
            }
        });
        GridBagConstraints gbc_btnSendSriforsm = new GridBagConstraints();
        gbc_btnSendSriforsm.insets = new Insets(5, 5, 5, 5);
        gbc_btnSendSriforsm.fill = GridBagConstraints.NONE;
        gbc_btnSendSriforsm.gridx = 0;
        gbc_btnSendSriforsm.gridy = 0;
        panel_1.add(btnSendSriforsm, gbc_btnSendSriforsm);

        JButton btnSendSriforsmmtforwardsm = new JButton("Send SRIForSM + MtForwardSM");
        btnSendSriforsmmtforwardsm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendSRIForSM_Mtforwardsm();
            }
        });
        GridBagConstraints gbc_btnSendSriforsmmtforwardsm = new GridBagConstraints();
        gbc_btnSendSriforsmmtforwardsm.insets = new Insets(5, 5, 5, 5);
        gbc_btnSendSriforsmmtforwardsm.fill = GridBagConstraints.NONE;
        gbc_btnSendSriforsmmtforwardsm.gridx = 1;
        gbc_btnSendSriforsmmtforwardsm.gridy = 0;
        panel_1.add(btnSendSriforsmmtforwardsm, gbc_btnSendSriforsmmtforwardsm);

        JButton btnSendMtforwardsm = new JButton("Send MtForwardSM");
        btnSendMtforwardsm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMtforwardsm();
            }
        });
        GridBagConstraints gbc_btnSendMtforwardsm = new GridBagConstraints();
        gbc_btnSendMtforwardsm.insets = new Insets(5, 5, 5, 5);
        gbc_btnSendMtforwardsm.fill = GridBagConstraints.NONE;
        gbc_btnSendMtforwardsm.gridx = 2;
        gbc_btnSendMtforwardsm.gridy = 0;
        panel_1.add(btnSendMtforwardsm, gbc_btnSendMtforwardsm);

        JLabel label_1 = new JLabel("Operation result");
        GridBagConstraints gbc_label_1 = new GridBagConstraints();
        gbc_label_1.insets = new Insets(0, 0, 5, 5);
        gbc_label_1.gridx = 0;
        gbc_label_1.gridy = 10;
        panel.add(label_1, gbc_label_1);

        lbResult = new JLabel("-");
        GridBagConstraints gbc_lbResult = new GridBagConstraints();
        gbc_lbResult.insets = new Insets(0, 0, 5, 0);
        gbc_lbResult.gridx = 1;
        gbc_lbResult.gridy = 10;
        panel.add(lbResult, gbc_lbResult);

        JLabel label_2 = new JLabel("Message received");
        GridBagConstraints gbc_label_2 = new GridBagConstraints();
        gbc_label_2.insets = new Insets(0, 0, 5, 5);
        gbc_label_2.gridx = 0;
        gbc_label_2.gridy = 11;
        panel.add(label_2, gbc_label_2);

        lbMessage = new JLabel("-");
        GridBagConstraints gbc_lbMessage = new GridBagConstraints();
        gbc_lbMessage.insets = new Insets(0, 0, 5, 0);
        gbc_lbMessage.gridx = 1;
        gbc_lbMessage.gridy = 11;
        panel.add(lbMessage, gbc_lbMessage);

        lbState = new JLabel("-");
        GridBagConstraints gbc_lbState = new GridBagConstraints();
        gbc_lbState.insets = new Insets(0, 0, 5, 5);
        gbc_lbState.gridx = 1;
        gbc_lbState.gridy = 12;
        panel.add(lbState, gbc_lbState);

        setSize(getPreferredSize());
    }

    public void setData(TestSmsServerManMBean smsServer) {
        this.smsServer = smsServer;

        M3uaForm.setEnumeratedBaseComboBox(cbDestAN, this.smsServer.getAddressNature());
        M3uaForm.setEnumeratedBaseComboBox(cbDestNP, this.smsServer.getNumberingPlan());

        M3uaForm.setEnumeratedBaseComboBox(cbOrigAN, this.smsServer.getAddressNature());
        M3uaForm.setEnumeratedBaseComboBox(cbOrigNP, this.smsServer.getNumberingPlan());
    }

    private void sendSRIForSM() {
        this.lbMessage.setText("");
        AddressNatureType an = (AddressNatureType)cbDestAN.getSelectedItem();
        NumberingPlanMapType np = (NumberingPlanMapType)cbDestNP.getSelectedItem();
        String destIsdnNumber = this.tbDestIsdnNumber.getText();
        String res = this.smsServer.performSRIForSM(an, np, destIsdnNumber);
        this.lbResult.setText(res);
    }

    private void sendMtforwardsm() {
        this.lbMessage.setText("");
        String msg = this.tbMessage.getText();
        String destImsi = this.tbImsi.getText();
        String vlrNumber = this.tbVlrNumber.getText();
        AddressNatureType oan = (AddressNatureType)cbOrigAN.getSelectedItem();
        NumberingPlanMapType onp = (NumberingPlanMapType)cbOrigNP.getSelectedItem();
        String origIsdnNumber = this.tbOrigIsdnNumber.getText();
        String res = this.smsServer.performMtForwardSM(msg, destImsi, vlrNumber,
                                                       oan, onp, origIsdnNumber);
        this.lbResult.setText(res);
    }

    private void sendSRIForSM_Mtforwardsm() {
        this.lbMessage.setText("");
        String msg = this.tbMessage.getText();
        AddressNatureType dan = (AddressNatureType)cbDestAN.getSelectedItem();
        NumberingPlanMapType dnp = (NumberingPlanMapType)cbDestNP.getSelectedItem();
        String destIsdnNumber = this.tbDestIsdnNumber.getText();
        AddressNatureType oan = (AddressNatureType)cbOrigAN.getSelectedItem();
        NumberingPlanMapType onp = (NumberingPlanMapType)cbOrigNP.getSelectedItem();
        String origIsdnNumber = this.tbOrigIsdnNumber.getText();
        String res = this.smsServer.performSRIForSM_MtForwardSM(msg,
                                                                dan, dnp, destIsdnNumber,
                                                                oan, onp, origIsdnNumber);
        this.lbResult.setText(res);
    }

    public void sendNotif(Notification notif) {
        super.sendNotif(notif);

        // if (notif.getMessage().startsWith("CurDialog: Rcvd: procUnstrSsReq: ")) {
        // String s1 = notif.getMessage().substring(17);
        // this.lbMessage.setText(s1);
        // }
        //
        // if (notif.getMessage().startsWith("CurDialog: Rcvd: unstrSsResp: ")) {
        // String s1 = notif.getMessage().substring(17);
        // this.lbMessage.setText(s1);
        // }
    }

    public void refreshState() {
        super.refreshState();

        String s1 = this.smsServer.getCurrentRequestDef();
        this.lbState.setText(s1);
    }
}
