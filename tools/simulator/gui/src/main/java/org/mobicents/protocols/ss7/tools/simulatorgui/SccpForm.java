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

package org.mobicents.protocols.ss7.tools.simulatorgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.tools.simulator.level2.GlobalTitleType;
import org.mobicents.protocols.ss7.tools.simulator.level2.NatureOfAddressType;
import org.mobicents.protocols.ss7.tools.simulator.level2.NumberingPlanSccpType;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpManMBean;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class SccpForm extends JDialog {

    private SccpManMBean sccp;

    private static final long serialVersionUID = 7571177143420596631L;
    private JTextField tbRemoteSpc;
    private JTextField tbRemoteSpc2;
    private JTextField tbLocalSpc;
    private JTextField tbLocalSpc2;
    private JTextField tbNi;
    private JTextField tbRemoteSsn;
    private JTextField tbHLRSsn;
    private JTextField tbLocalSsn;
    private JTextField tbTranslationType;
    private JComboBox cbGlobalTitleType;
    private JComboBox cbAddressNature;
    private JComboBox cbAddressNature2;
    private JComboBox cbNumberingPlan;
    private JTextField tbCallingPartyAddressDigits;
    private JRadioButton rbRouteDpcSsn;
    private JRadioButton rbRouteGt;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JPanel panel_1;

    public SccpForm(JFrame owner) {
        super(owner, true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setTitle("SCCP settings");
        setBounds(100, 100, 590, 682);

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        JLabel lblDpc = new JLabel("Remote Spc");
        lblDpc.setBounds(10, 14, 134, 14);
        panel.add(lblDpc);

        tbRemoteSpc = new JTextField();
        tbRemoteSpc.setColumns(10);
        tbRemoteSpc.setBounds(154, 11, 129, 20);
        panel.add(tbRemoteSpc);

        tbRemoteSpc2 = new JTextField();
        tbRemoteSpc2.setColumns(10);
        tbRemoteSpc2.setBounds(294, 11, 129, 20);
        panel.add(tbRemoteSpc2);

        tbLocalSpc = new JTextField();
        tbLocalSpc.setColumns(10);
        tbLocalSpc.setBounds(154, 42, 129, 20);
        panel.add(tbLocalSpc);

        tbLocalSpc2 = new JTextField();
        tbLocalSpc2.setColumns(10);
        tbLocalSpc2.setBounds(294, 42, 129, 20);
        panel.add(tbLocalSpc2);

        JLabel lblOpc = new JLabel("Local Spc");
        lblOpc.setBounds(10, 45, 134, 14);
        panel.add(lblOpc);

        tbNi = new JTextField();
        tbNi.setColumns(10);
        tbNi.setBounds(154, 73, 129, 20);
        panel.add(tbNi);

        tbRemoteSsn = new JTextField();
        tbRemoteSsn.setColumns(10);
        tbRemoteSsn.setBounds(154, 104, 129, 20);
        panel.add(tbRemoteSsn);

        JLabel lblNetworkIndicatpr = new JLabel("Network indicator");
        lblNetworkIndicatpr.setBounds(10, 76, 112, 14);
        panel.add(lblNetworkIndicatpr);

        JLabel lblRemoteSsn = new JLabel("Remote Ssn");
        lblRemoteSsn.setBounds(10, 107, 112, 14);
        panel.add(lblRemoteSsn);

        JButton button = new JButton("Load default values for side A");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadDataA();
            }
        });
        button.setBounds(10, 586, 245, 23);
        panel.add(button);

        JButton button_1 = new JButton("Load default values for side B");
        button_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadDataB();
            }
        });
        button_1.setBounds(265, 586, 234, 23);
        panel.add(button_1);

        JButton button_2 = new JButton("Reload");
        button_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadData();
            }
        });
        button_2.setBounds(10, 620, 144, 23);
        panel.add(button_2);

        JButton button_3 = new JButton("Save");
        button_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (saveData()) {
                    getJFrame().dispose();
                }
            }
        });
        button_3.setBounds(255, 620, 117, 23);
        panel.add(button_3);

        JButton button_4 = new JButton("Cancel");
        button_4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getJFrame().dispose();
            }
        });
        button_4.setBounds(382, 620, 117, 23);
        panel.add(button_4);

        tbLocalSsn = new JTextField();
        tbLocalSsn.setColumns(10);
        tbLocalSsn.setBounds(154, 135, 129, 20);
        panel.add(tbLocalSsn);

        JLabel lblLocalSsn = new JLabel("Local Ssn");
        lblLocalSsn.setBounds(10, 138, 112, 14);
        panel.add(lblLocalSsn);

        tbHLRSsn = new JTextField();
        tbHLRSsn.setColumns(10);
        tbHLRSsn.setBounds(154, 166, 129, 20);
        panel.add(tbHLRSsn);

        JLabel lblHLRSsn = new JLabel("HLR Ssn");
        lblHLRSsn.setBounds(10, 169, 112, 14);
        panel.add(lblHLRSsn);

        panel_1 = new JPanel();
        panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
        panel_1.setLayout(null);
        panel_1.setBounds(10, 366, 564, 209);
        panel.add(panel_1);

        JLabel lblParametersForCreating = new JLabel("Parameters for creating SccpAddress (when routing on GT)");
        lblParametersForCreating.setBounds(10, 0, 468, 14);
        panel_1.add(lblParametersForCreating);

        JLabel lblTranslationType = new JLabel("Translation type");
        lblTranslationType.setBounds(10, 150, 136, 14);
        panel_1.add(lblTranslationType);

        JLabel label_2 = new JLabel("AddressNature");
        label_2.setBounds(10, 57, 136, 14);
        panel_1.add(label_2);

        JLabel label_3 = new JLabel("AddressNature 2");
        label_3.setBounds(10, 88, 136, 14);
        panel_1.add(label_3);

        JLabel label_4 = new JLabel("NumberingPlan");
        label_4.setBounds(10, 119, 136, 14);
        panel_1.add(label_4);

        tbTranslationType = new JTextField();
        tbTranslationType.setColumns(10);
        tbTranslationType.setBounds(156, 147, 130, 20);
        panel_1.add(tbTranslationType);

        cbAddressNature = new JComboBox();
        cbAddressNature.setBounds(156, 54, 294, 20);
        panel_1.add(cbAddressNature);

        cbAddressNature2 = new JComboBox();
        cbAddressNature2.setBounds(156, 85, 294, 20);
        panel_1.add(cbAddressNature2);

        cbNumberingPlan = new JComboBox();
        cbNumberingPlan.setBounds(156, 116, 294, 20);
        panel_1.add(cbNumberingPlan);

        JLabel lblGlobaltitleType = new JLabel("GlobalTitle type");
        lblGlobaltitleType.setBounds(10, 29, 136, 14);
        panel_1.add(lblGlobaltitleType);

        cbGlobalTitleType = new JComboBox();
        cbGlobalTitleType.setBounds(156, 26, 398, 20);
        panel_1.add(cbGlobalTitleType);

        JLabel lblCallingpartyaddressDigits = new JLabel("CallingPartyAddress digits");
        lblCallingpartyaddressDigits.setBounds(10, 181, 176, 14);
        panel_1.add(lblCallingpartyaddressDigits);

        tbCallingPartyAddressDigits = new JTextField();
        tbCallingPartyAddressDigits.setColumns(10);
        tbCallingPartyAddressDigits.setBounds(219, 178, 231, 20);
        panel_1.add(tbCallingPartyAddressDigits);

        rbRouteDpcSsn = new JRadioButton("Route on DPC and SSN mode");
        buttonGroup.add(rbRouteDpcSsn);
        rbRouteDpcSsn.setBounds(6, 195, 332, 23);
        panel.add(rbRouteDpcSsn);

        rbRouteGt = new JRadioButton("Route on GlobalTitle mode");
        rbRouteGt.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setRouteOnGtEnabled(e.getStateChange() == 1);
            }
        });
        buttonGroup.add(rbRouteGt);
        rbRouteGt.setBounds(10, 278, 332, 23);
        panel.add(rbRouteGt);

        JLabel lblNewLabel = new JLabel(
                "<html>\r\nCallingPartyAddress:  RoutingIndicator=RoutingOnDpcAndSsn, PC=localSpc, GT=null, SSN=localSsn<br>\r\nCalledPartyAddress:  RoutingIndicator=RoutingOnDpcAndSsn PC=remoteSpc, GT=null, SSN=remoteSsn\r\n</html>");
        lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
        lblNewLabel.setBounds(10, 221, 564, 50);
        panel.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel(
                "<html>\r\nCallingPartyAddress:  RoutingIndicator=RoutingOnGt, PC=0, GT=CallingPartyAddress digits, SSN=localSsn<br>\r\nCalledPartyAddress:  RoutingIndicator=RoutingOnGt, PC=0, GT and SSN is supplied by upper levels<br>\r\nAll messages will be routed to remoteSpc except messages with CallingPartyAddress digits or extra local addresses\r\n</html>");
        lblNewLabel_1.setVerticalAlignment(SwingConstants.TOP);
        lblNewLabel_1.setBounds(10, 308, 564, 78);
        panel.add(lblNewLabel_1);
    }

    public void setData(SccpManMBean sccp) {
        this.sccp = sccp;

        this.reloadData();
    }

    private JDialog getJFrame() {
        return this;
    }

    private void reloadData() {
        if (!this.sccp.isRouteOnGtMode()) {
            this.rbRouteDpcSsn.setSelected(true);
            setRouteOnGtEnabled(false);
        } else {
            this.rbRouteGt.setSelected(true);
            setRouteOnGtEnabled(true);
        }

        M3uaForm.setEnumeratedBaseComboBox(cbGlobalTitleType, this.sccp.getGlobalTitleType());
        M3uaForm.setEnumeratedBaseComboBox(cbAddressNature, this.sccp.getNatureOfAddress());
        M3uaForm.setEnumeratedBaseComboBox(cbAddressNature2, this.sccp.getNatureOfAddress2());
        M3uaForm.setEnumeratedBaseComboBox(cbNumberingPlan, this.sccp.getNumberingPlan());

        tbRemoteSpc.setText(((Integer) this.sccp.getRemoteSpc()).toString());
        tbRemoteSpc2.setText(((Integer) this.sccp.getRemoteSpc2()).toString());
        tbLocalSpc.setText(((Integer) this.sccp.getLocalSpc()).toString());
        tbLocalSpc2.setText(((Integer) this.sccp.getLocalSpc2()).toString());
        tbNi.setText(((Integer) this.sccp.getNi()).toString());
        tbRemoteSsn.setText(((Integer) this.sccp.getRemoteSsn()).toString());
        tbHLRSsn.setText(((Integer) this.sccp.getHLRSsn()).toString());
        tbLocalSsn.setText(((Integer) this.sccp.getLocalSsn()).toString());
        tbTranslationType.setText(((Integer) this.sccp.getTranslationType()).toString());

        tbCallingPartyAddressDigits.setText(this.sccp.getCallingPartyAddressDigits());
        // tbExtraLocalAddressDigits.setText(this.sccp.getExtraLocalAddressDigits());
    }

    private void loadDataA() {
        SsnDefaultSelectionForm fmA = new SsnDefaultSelectionForm();
        fmA.setVisible(true);

        int res = fmA.getResult();
        if (res == 0)
            return;
        switch (res) {
            case 1:
                tbRemoteSsn.setText("8");
                tbLocalSsn.setText("8");
                break;
            case 2:
                tbRemoteSsn.setText("146");
                tbLocalSsn.setText("146");
                break;
        }

        this.rbRouteDpcSsn.setSelected(true);
        setRouteOnGtEnabled(false);

        M3uaForm.setEnumeratedBaseComboBox(cbGlobalTitleType, new GlobalTitleType(GlobalTitleType.VAL_TT_NP_ES_NOA));
        M3uaForm.setEnumeratedBaseComboBox(cbAddressNature, new NatureOfAddressType(NatureOfAddress.INTERNATIONAL.getValue()));
        M3uaForm.setEnumeratedBaseComboBox(cbAddressNature2, new NatureOfAddressType(NatureOfAddress.NATIONAL.getValue()));
        M3uaForm.setEnumeratedBaseComboBox(cbNumberingPlan, new NumberingPlanSccpType(NumberingPlan.ISDN_TELEPHONY.getValue()));

        tbRemoteSpc.setText("2");
        tbRemoteSpc2.setText("4");
        tbLocalSpc.setText("1");
        tbLocalSpc2.setText("3");
        tbNi.setText("2");
        tbTranslationType.setText("0");

        tbCallingPartyAddressDigits.setText("");
        // tbExtraLocalAddressDigits.setText("");
    }

    private void loadDataB() {
        SsnDefaultSelectionForm fmA = new SsnDefaultSelectionForm();
        fmA.setVisible(true);

        int res = fmA.getResult();
        if (res == 0)
            return;
        switch (res) {
            case 1:
                tbRemoteSsn.setText("8");
                tbLocalSsn.setText("8");
                break;
            case 2:
                tbRemoteSsn.setText("146");
                tbLocalSsn.setText("146");
                break;
        }

        this.rbRouteDpcSsn.setSelected(true);
        setRouteOnGtEnabled(false);

        M3uaForm.setEnumeratedBaseComboBox(cbGlobalTitleType, new GlobalTitleType(GlobalTitleType.VAL_TT_NP_ES_NOA));
        M3uaForm.setEnumeratedBaseComboBox(cbAddressNature, new NatureOfAddressType(NatureOfAddress.INTERNATIONAL.getValue()));
        M3uaForm.setEnumeratedBaseComboBox(cbAddressNature2, new NatureOfAddressType(NatureOfAddress.NATIONAL.getValue()));
        M3uaForm.setEnumeratedBaseComboBox(cbNumberingPlan, new NumberingPlanSccpType(NumberingPlan.ISDN_TELEPHONY.getValue()));

        tbRemoteSpc.setText("1");
        tbRemoteSpc2.setText("3");
        tbLocalSpc.setText("2");
        tbLocalSpc2.setText("4");
        tbNi.setText("2");
        tbTranslationType.setText("0");

        tbCallingPartyAddressDigits.setText("");
        // tbExtraLocalAddressDigits.setText("");
    }

    private boolean saveData() {
        int remoteSpc = 0;
        int remoteSpc2 = 0;
        int localSpc = 0;
        int localSpc2 = 0;
        int ni = 0;
        int remoteSsn = 0;
        int hlrSsn = 6;
        int localSsn = 0;
        int translationType = 0;
        try {
            remoteSpc = Integer.parseInt(tbRemoteSpc.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing Dpc value: " + e.toString());
            return false;
        }
        try {
            remoteSpc2 = Integer.parseInt(tbRemoteSpc2.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing Dpc2 value: " + e.toString());
            return false;
        }
        try {
            localSpc = Integer.parseInt(tbLocalSpc.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing Opc value: " + e.toString());
            return false;
        }
        try {
            localSpc2 = Integer.parseInt(tbLocalSpc2.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing Opc2 value: " + e.toString());
            return false;
        }
        try {
            ni = Integer.parseInt(tbNi.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing Network indicator value: " + e.toString());
            return false;
        }
        try {
            remoteSsn = Integer.parseInt(tbRemoteSsn.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing RemoteSsn value: " + e.toString());
            return false;
        }
        try {
            hlrSsn = Integer.parseInt(tbHLRSsn.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing hlrSsn value: " + e.toString());
            return false;
        }
        try {
            localSsn = Integer.parseInt(tbLocalSsn.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing LocalSsn value: " + e.toString());
            return false;
        }
        try {
            translationType = Integer.parseInt(tbTranslationType.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Exception when parsing TranslationType value: " + e.toString());
            return false;
        }

        this.sccp.setRouteOnGtMode(this.rbRouteGt.isSelected());

        this.sccp.setRemoteSpc(remoteSpc);
        this.sccp.setRemoteSpc2(remoteSpc2);
        this.sccp.setLocalSpc(localSpc);
        this.sccp.setLocalSpc2(localSpc2);
        this.sccp.setNi(ni);
        this.sccp.setRemoteSsn(remoteSsn);
        this.sccp.setHLRSsn(hlrSsn);
        this.sccp.setLocalSsn(localSsn);
        this.sccp.setTranslationType(translationType);

        this.sccp.setGlobalTitleType((GlobalTitleType) cbGlobalTitleType.getSelectedItem());
        this.sccp.setNatureOfAddress((NatureOfAddressType) cbAddressNature.getSelectedItem());
        this.sccp.setNatureOfAddress2((NatureOfAddressType) cbAddressNature2.getSelectedItem());
        this.sccp.setNumberingPlan((NumberingPlanSccpType) cbNumberingPlan.getSelectedItem());

        this.sccp.setCallingPartyAddressDigits(tbCallingPartyAddressDigits.getText());
        // this.sccp.setExtraLocalAddressDigits(tbExtraLocalAddressDigits.getText());

        return true;
    }

    private void setRouteOnGtEnabled(boolean val) {
        this.cbGlobalTitleType.setEnabled(val);
        this.cbAddressNature.setEnabled(val);
        this.cbAddressNature2.setEnabled(val);
        this.cbNumberingPlan.setEnabled(val);
        this.tbTranslationType.setEnabled(val);
        this.tbCallingPartyAddressDigits.setEnabled(val);
        // this.tbExtraLocalAddressDigits.setEnabled(val);
    }
}
