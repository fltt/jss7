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

package org.mobicents.protocols.ss7.tools.simulator.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import javolution.text.TextBuilder;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPart;
import org.mobicents.protocols.ss7.sccp.SccpStack;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;
import org.mobicents.protocols.ss7.tools.simulator.common.AddressNatureType;
import org.mobicents.protocols.ss7.tools.simulator.common.ConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level1.DialogicConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.level1.DialogicMan;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaMan;
import org.mobicents.protocols.ss7.tools.simulator.level2.NatureOfAddressType;
import org.mobicents.protocols.ss7.tools.simulator.level2.NumberingPlanSccpType;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.CapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.NumberingPlanMapType;
import org.mobicents.protocols.ss7.tools.simulator.tests.cap.TestCapScfMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.cap.TestCapSsfMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.NumberingPlanIdentificationType;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.TestSmsClientConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.TestSmsClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.TestSmsServerConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.TestSmsServerMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.TypeOfNumberType;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerMan;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class TesterHost extends NotificationBroadcasterSupport implements TesterHostMBean, Stoppable {
    private static final Logger logger = Logger.getLogger(TesterHost.class);

    private static final String TESTER_HOST_PERSIST_DIR_KEY = "testerhost.persist.dir";
    private static final String USER_DIR_KEY = "user.dir";
    public static String SOURCE_NAME = "HOST";
    public static String SS7_EVENT = "SS7Event";

    private static final String CLASS_ATTRIBUTE = "type";
    private static final String TAB_INDENT = "\t";
    private static final String PERSIST_FILE_NAME_OLD = "simulator.xml";
    private static final String PERSIST_FILE_NAME = "simulator2.xml";
    private static final String CONFIGURATION_DATA = "configurationData";

    public static String SIMULATOR_HOME_VAR = "SIMULATOR_HOME";

    private final String appName;
    private String persistDir = null;
    private final TextBuilder persistFile = TextBuilder.newInstance();
    private static final XMLBinding binding = new XMLBinding();

    // SETTINGS
    private boolean isStarted = false;
    private boolean needQuit = false;
    private boolean needStore = false;
    private ConfigurationData configurationData = new ConfigurationData();
    private long sequenceNumber = 0;

    // Layers
    private Stoppable instance_L1_B = null;
    private Stoppable instance_L2_B = null;
    private Stoppable instance_L3_B1 = null;
    private Stoppable instance_L3_B2 = null;
    private Stoppable instance_TestTask_B = null;

    // levels
    M3uaMan m3ua;
    DialogicMan dialogic;
    SccpMan sccp;
    MapMan map;
    CapMan cap;
    TestUssdClientMan testUssdClientMan;
    TestUssdServerMan testUssdServerMan;
    TestSmsClientMan testSmsClientMan;
    TestSmsServerMan testSmsServerMan;
    TestCapSsfMan testCapSsfMan;
    TestCapScfMan testCapScfMan;

    // testers

    public TesterHost(String appName, String persistDir) {
        this.appName = appName;
        this.persistDir = persistDir;

        this.m3ua = new M3uaMan(appName);
        this.m3ua.setTesterHost(this);

        this.dialogic = new DialogicMan(appName);
        this.dialogic.setTesterHost(this);

        this.sccp = new SccpMan(appName);
        this.sccp.setTesterHost(this);

        this.map = new MapMan(appName);
        this.map.setTesterHost(this);

        this.cap = new CapMan(appName);
        this.cap.setTesterHost(this);

        this.testUssdClientMan = new TestUssdClientMan(appName);
        this.testUssdClientMan.setTesterHost(this);

        this.testUssdServerMan = new TestUssdServerMan(appName);
        this.testUssdServerMan.setTesterHost(this);

        this.testSmsClientMan = new TestSmsClientMan(appName);
        this.testSmsClientMan.setTesterHost(this);

        this.testSmsServerMan = new TestSmsServerMan(appName);
        this.testSmsServerMan.setTesterHost(this);

        this.testCapSsfMan = new TestCapSsfMan(appName);
        this.testCapSsfMan.setTesterHost(this);

        this.testCapScfMan = new TestCapScfMan(appName);
        this.testCapScfMan.setTesterHost(this);

        this.setupLog4j(appName);

        binding.setClassAttribute(CLASS_ATTRIBUTE);

        this.persistFile.clear();
        TextBuilder persistFileOld = new TextBuilder();

        if (persistDir != null) {
            persistFileOld.append(persistDir).append(File.separator).append(this.appName).append("_")
                    .append(PERSIST_FILE_NAME_OLD);
            this.persistFile.append(persistDir).append(File.separator).append(this.appName).append("_")
                    .append(PERSIST_FILE_NAME);
        } else {
            persistFileOld.append(System.getProperty(TESTER_HOST_PERSIST_DIR_KEY, System.getProperty(USER_DIR_KEY)))
                    .append(File.separator).append(this.appName).append("_").append(PERSIST_FILE_NAME_OLD);
            this.persistFile.append(System.getProperty(TESTER_HOST_PERSIST_DIR_KEY, System.getProperty(USER_DIR_KEY)))
                    .append(File.separator).append(this.appName).append("_").append(PERSIST_FILE_NAME);
        }

        File fn = new File(persistFile.toString());
        this.load(fn);
    }

    public ConfigurationData getConfigurationData() {
        return this.configurationData;
    }

    public M3uaMan getM3uaMan() {
        return this.m3ua;
    }

    public DialogicMan getDialogicMan() {
        return this.dialogic;
    }

    public SccpMan getSccpMan() {
        return this.sccp;
    }

    public MapMan getMapMan() {
        return this.map;
    }

    public CapMan getCapMan() {
        return this.cap;
    }

    public TestUssdClientMan getTestUssdClientMan() {
        return this.testUssdClientMan;
    }

    public TestUssdServerMan getTestUssdServerMan() {
        return this.testUssdServerMan;
    }

    public TestSmsClientMan getTestSmsClientMan() {
        return this.testSmsClientMan;
    }

    public TestSmsServerMan getTestSmsServerMan() {
        return this.testSmsServerMan;
    }

    public TestCapSsfMan getTestCapSsfMan() {
        return this.testCapSsfMan;
    }

    public TestCapScfMan getTestCapScfMan() {
        return this.testCapScfMan;
    }

    private void setupLog4j(String appName) {

        // InputStream inStreamLog4j = getClass().getResourceAsStream("/log4j.properties");

        String propFileName = appName + ".log4j.properties";
        File f = new File("./" + propFileName);
        if (f.exists()) {

            try {
                InputStream inStreamLog4j = new FileInputStream(f);
                Properties propertiesLog4j = new Properties();

                propertiesLog4j.load(inStreamLog4j);
                PropertyConfigurator.configure(propertiesLog4j);
            } catch (Exception e) {
                e.printStackTrace();
                BasicConfigurator.configure();
            }
        } else {
            BasicConfigurator.configure();
        }

        // logger.setLevel(Level.TRACE);
        logger.debug("log4j configured");

    }

    public void sendNotif(String source, String msg, Throwable e, Level logLevel) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement st : e.getStackTrace()) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(st.toString());
        }
        this.doSendNotif(source, msg + " - " + e.toString(), sb.toString());

        logger.log(logLevel, msg, e);
        // if (showInConsole) {
        // logger.error(msg, e);
        // } else {
        // logger.debug(msg, e);
        // }
    }

    public void sendNotif(String source, String msg, String userData, Level logLevel) {

        this.doSendNotif(source, msg, userData);

        logger.log(Level.INFO, msg + "\n" + userData);
//        logger.log(logLevel, msg + "\n" + userData);

        // if (showInConsole) {
        // logger.warn(msg);
        // } else {
        // logger.debug(msg);
        // }
    }

    private synchronized void doSendNotif(String source, String msg, String userData) {
        Notification notif = new Notification(SS7_EVENT + "-" + source, "TesterHost", ++sequenceNumber,
                System.currentTimeMillis(), msg);
        notif.setUserData(userData);
        this.sendNotification(notif);
    }

    public boolean isNeedQuit() {
        return needQuit;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public Instance_L1 getInstance_L1() {
        return configurationData.getInstance_L1();
    }

    public void setInstance_L1(Instance_L1 val) {
        configurationData.setInstance_L1(val);
        this.markStore();
    }

    public Instance_L2 getInstance_L2() {
        return configurationData.getInstance_L2();
    }

    public void setInstance_L2(Instance_L2 val) {
        configurationData.setInstance_L2(val);
        this.markStore();
    }

    public Instance_L3 getInstance_L3() {
        return configurationData.getInstance_L3();
    }

    public void setInstance_L3(Instance_L3 val) {
        configurationData.setInstance_L3(val);
        this.markStore();
    }

    public Instance_TestTask getInstance_TestTask() {
        return configurationData.getInstance_TestTask();
    }

    public void setInstance_TestTask(Instance_TestTask val) {
        configurationData.setInstance_TestTask(val);
        this.markStore();
    }

    public String getInstance_L1_Value() {
        return configurationData.getInstance_L1().toString();
    }

    public String getInstance_L2_Value() {
        return configurationData.getInstance_L2().toString();
    }

    public String getInstance_L3_Value() {
        return configurationData.getInstance_L3().toString();
    }

    public String getInstance_TestTask_Value() {
        return configurationData.getInstance_TestTask().toString();
    }

    public String getState() {
        return TesterHost.SOURCE_NAME + ": " + (this.isStarted() ? "Started" : "Stopped");
    }

    public String getL1State() {
        if (this.instance_L1_B != null)
            return this.instance_L1_B.getState();
        else
            return "";
    }

    public String getL2State() {
        if (this.instance_L2_B != null)
            return this.instance_L2_B.getState();
        else
            return "";
    }

    public String getL3State() {
        if (this.instance_L3_B1 != null)
            return this.instance_L3_B1.getState();
        else {
            if (this.instance_L3_B2 != null)
                return this.instance_L3_B2.getState();
            else
                return "";
        }
    }

    public String getTestTaskState() {
        if (this.instance_TestTask_B != null)
            return this.instance_TestTask_B.getState();
        else
            return "";
    }

    public void start() {

        this.store();
        this.stop();

        // L1
        boolean started = false;
        Mtp3UserPart mtp3UserPart = null;
        switch (this.configurationData.getInstance_L1().intValue()) {
            case Instance_L1.VAL_M3UA:
                this.instance_L1_B = this.m3ua;
                started = this.m3ua.start();
                mtp3UserPart = this.m3ua.getMtp3UserPart();
                break;
            case Instance_L1.VAL_DIALOGIC:
                this.instance_L1_B = this.dialogic;
                started = this.dialogic.start();
                mtp3UserPart = this.dialogic.getMtp3UserPart();
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(TesterHost.SOURCE_NAME, "Instance_L1." + this.configurationData.getInstance_L1().toString()
                        + " has not been implemented yet", "", Level.WARN);
                break;
        }
        if (!started) {
            this.sendNotif(TesterHost.SOURCE_NAME, "Layer 1 has not started", "", Level.WARN);
            this.stop();
            return;
        }

        // L2
        started = false;
        SccpStack sccpStack = null;
        switch (this.configurationData.getInstance_L2().intValue()) {
            case Instance_L2.VAL_SCCP:
                if (mtp3UserPart == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME, "Error initializing SCCP: No Mtp3UserPart is defined at L1", "",
                            Level.WARN);
                } else {
                    this.instance_L2_B = this.sccp;
                    this.sccp.setMtp3UserPart(mtp3UserPart);
                    started = this.sccp.start();
                    sccpStack = this.sccp.getSccpStack();
                }
                break;
            case Instance_L2.VAL_ISUP:
                // TODO Implement L2 = ISUP
                this.sendNotif(TesterHost.SOURCE_NAME, "Instance_L2.VAL_ISUP has not been implemented yet", "", Level.WARN);
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(TesterHost.SOURCE_NAME, "Instance_L2." + this.configurationData.getInstance_L2().toString()
                        + " has not been implemented yet", "", Level.WARN);
                break;
        }
        if (!started) {
            this.sendNotif(TesterHost.SOURCE_NAME, "Layer 2 has not started", "", Level.WARN);
            this.stop();
            return;
        }

        // L3
        started = false;
        MapMan curMap = null;
        MapMan curHLRMap = null;
        CapMan curCap = null;
        switch (this.configurationData.getInstance_L3().intValue()) {
            case Instance_L3.VAL_MAP:
                if (sccpStack == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME, "Error initializing TCAP+MAP: No SccpStack is defined at L2", "",
                            Level.WARN);
                } else {
                    curMap = map;
                    curHLRMap = new MapMan(appName+" HLR");
                    curHLRMap.setTesterHost(this);
                    curHLRMap.setDestReference(curMap.getDestReference());
                    curHLRMap.setDestReferenceAddressNature(curMap.getDestReferenceAddressNature());
                    curHLRMap.setDestReferenceNumberingPlan(curMap.getDestReferenceNumberingPlan());
                    curHLRMap.setOrigReference(curMap.getOrigReference());
                    curHLRMap.setOrigReferenceAddressNature(curMap.getOrigReferenceAddressNature());
                    curHLRMap.setOrigReferenceNumberingPlan(curMap.getOrigReferenceNumberingPlan());
                    curHLRMap.setRemoteAddressDigits(curMap.getRemoteAddressDigits());
                    instance_L3_B1 = curMap;
                    instance_L3_B2 = curHLRMap;
                    curMap.setSccpStack(sccpStack);
                    curHLRMap.setSccpStack(sccpStack);
                    started = curMap.start(false) && curHLRMap.start(true);
                }
                break;
            case Instance_L3.VAL_CAP:
                if (sccpStack == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME, "Error initializing TCAP+CAP: No SccpStack is defined at L2", "",
                            Level.WARN);
                } else {
                    this.instance_L3_B1 = this.cap;
                    this.cap.setSccpStack(sccpStack);
                    started = this.cap.start();
                    curCap = this.cap;
                }
                break;
            case Instance_L3.VAL_INAP:
                // TODO: implement INAP .......
                this.sendNotif(TesterHost.SOURCE_NAME, "Instance_L3.VAL_INAP has not been implemented yet", "", Level.WARN);
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(TesterHost.SOURCE_NAME, "Instance_L3." + this.configurationData.getInstance_L3().toString()
                        + " has not been implemented yet", "", Level.WARN);
                break;
        }
        if (!started) {
            this.sendNotif(TesterHost.SOURCE_NAME, "Layer 3 has not started", "", Level.WARN);
            this.stop();
            return;
        }

        // Testers
        started = false;
        switch (this.configurationData.getInstance_TestTask().intValue()) {
            case Instance_TestTask.VAL_USSD_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME,
                            "Error initializing USSD_TEST_CLIENT: No MAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testUssdClientMan;
                    this.testUssdClientMan.setMapMan(curMap);
                    started = this.testUssdClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_USSD_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME,
                            "Error initializing USSD_TEST_SERVER: No MAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testUssdServerMan;
                    this.testUssdServerMan.setMapMan(curMap);
                    started = this.testUssdServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_SMS_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME, "Error initializing SMS_TEST_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSmsClientMan;
                    this.testSmsClientMan.setHLRMapMan(curHLRMap);
                    this.testSmsClientMan.setMSCMapMan(curMap);
                    started = this.testSmsClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_SMS_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME, "Error initializing SMS_TEST_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSmsServerMan;
                    this.testSmsServerMan.setMapMan(curMap);
                    started = this.testSmsServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_CAP_TEST_SCF:
                if (curCap == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME,
                            "Error initializing VAL_CAP_TEST_SCF: No CAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testCapScfMan;
                    this.testCapScfMan.setCapMan(curCap);
                    started = this.testCapScfMan.start();
                }
                break;

            case Instance_TestTask.VAL_CAP_TEST_SSF:
                if (curCap == null) {
                    this.sendNotif(TesterHost.SOURCE_NAME,
                            "Error initializing VAL_CAP_TEST_SSF: No CAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testCapSsfMan;
                    this.testCapSsfMan.setCapMan(curCap);
                    started = this.testCapSsfMan.start();
                }
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(TesterHost.SOURCE_NAME, "Instance_TestTask."
                        + this.configurationData.getInstance_TestTask().toString() + " has not been implemented yet", "",
                        Level.WARN);
                break;
        }
        if (!started) {
            this.sendNotif(TesterHost.SOURCE_NAME, "Testing task has not started", "", Level.WARN);
            this.stop();
            return;
        }

        this.isStarted = true;
    }

    public void stop() {

        this.isStarted = false;

        // TestTask
        if (this.instance_TestTask_B != null) {
            this.instance_TestTask_B.stop();
            this.instance_TestTask_B = null;
        }

        // L3
        if (this.instance_L3_B1 != null) {
            this.instance_L3_B1.stop();
            this.instance_L3_B1 = null;
        }
        if (this.instance_L3_B2 != null) {
            this.instance_L3_B2.stop();
            this.instance_L3_B2 = null;
        }

        // L2
        if (this.instance_L2_B != null) {
            this.instance_L2_B.stop();
            this.instance_L2_B = null;
        }

        // L1
        if (this.instance_L1_B != null) {
            this.instance_L1_B.stop();
            this.instance_L1_B = null;
        }
    }

    public void execute() {
        if (this.instance_L1_B != null) {
            this.instance_L1_B.execute();
        }
        if (this.instance_L2_B != null) {
            this.instance_L2_B.execute();
        }
        if (this.instance_L3_B1 != null) {
            this.instance_L3_B1.execute();
        }
        if (this.instance_L3_B2 != null) {
            this.instance_L3_B2.execute();
        }
        if (this.instance_TestTask_B != null) {
            this.instance_TestTask_B.execute();
        }
    }

    public void quit() {
        this.stop();
        this.store();
        this.needQuit = true;
    }

    public void putInstance_L1Value(String val) {
        Instance_L1 x = Instance_L1.createInstance(val);
        if (x != null)
            this.setInstance_L1(x);
    }

    public void putInstance_L2Value(String val) {
        Instance_L2 x = Instance_L2.createInstance(val);
        if (x != null)
            this.setInstance_L2(x);
    }

    public void putInstance_L3Value(String val) {
        Instance_L3 x = Instance_L3.createInstance(val);
        if (x != null)
            this.setInstance_L3(x);
    }

    public void putInstance_TestTaskValue(String val) {
        Instance_TestTask x = Instance_TestTask.createInstance(val);
        if (x != null)
            this.setInstance_TestTask(x);
    }

    public String getName() {
        return appName;
    }

    public String getPersistDir() {
        return persistDir;
    }

//    public void setPersistDir(String persistDir) {
//        this.persistDir = persistDir;
//    }

    public void markStore() {
        needStore = true;
    }

    public void checkStore() {
        if (needStore) {
            needStore = false;
            this.store();
        }
    }

    public synchronized void store() {

        try {
            XMLObjectWriter writer = XMLObjectWriter.newInstance(new FileOutputStream(persistFile.toString()));
            writer.setBinding(binding);
            // writer.setReferenceResolver(new XMLReferenceResolver());
            writer.setIndentation(TAB_INDENT);

            writer.write(this.configurationData, CONFIGURATION_DATA, ConfigurationData.class);

            writer.close();
        } catch (Exception e) {
            this.sendNotif(SOURCE_NAME, "Error while persisting the Host state in file", e, Level.ERROR);
        }
    }

    private boolean load(File fn) {

        XMLObjectReader reader = null;
        try {
            if (!fn.exists()) {
                this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file: file not found: " + persistFile, "",
                        Level.WARN);
                return false;
            }

            reader = XMLObjectReader.newInstance(new FileInputStream(fn));

            reader.setBinding(binding);

            this.configurationData = reader.read(CONFIGURATION_DATA, ConfigurationData.class);

            reader.close();

            return true;

        } catch (Exception ex) {
            this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file", ex, Level.WARN);
            return false;
        }
    }
}
