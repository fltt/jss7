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
package org.mobicents.ss7.management.console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.scheduler.Scheduler;
import org.mobicents.protocols.ss7.scheduler.Task;
import org.mobicents.ss7.management.transceiver.ChannelProvider;
import org.mobicents.ss7.management.transceiver.ChannelSelectionKey;
import org.mobicents.ss7.management.transceiver.ChannelSelector;
import org.mobicents.ss7.management.transceiver.Message;
import org.mobicents.ss7.management.transceiver.MessageFactory;
import org.mobicents.ss7.management.transceiver.ShellChannel;
import org.mobicents.ss7.management.transceiver.ShellServerChannel;

/**
 * @author amit bhayani
 *
 */
public class ShellServer extends Task {
    Logger logger = Logger.getLogger(ShellServer.class);

    public static final String CONNECTED_MESSAGE = "Connected to %s %s %s";

    Version version = Version.instance;

    private ChannelProvider provider;
    private ShellServerChannel serverChannel;
    private ChannelSelector selector;
    private ChannelSelectionKey skey;
    private List<ChannelSelectionKey> ckeys;
    private Set<ChannelSelectionKey> closing;

    private MessageFactory messageFactory = null;

    private volatile boolean started = false;

    private String address;

    private int port;

    private final FastList<ShellExecutor> shellExecutors = new FastList<ShellExecutor>();

    public ShellServer(Scheduler scheduler, List<ShellExecutor> shellExecutors) throws IOException {
        super(scheduler);
        this.shellExecutors.addAll(shellExecutors);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        logger.info("Starting SS7 management shell environment");
        provider = ChannelProvider.provider();
        serverChannel = provider.openServerChannel();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        serverChannel.bind(inetSocketAddress);

        selector = provider.openSelector();
        skey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        ckeys = new LinkedList<ChannelSelectionKey>();
        closing = new HashSet<ChannelSelectionKey>();

        messageFactory = ChannelProvider.provider().getMessageFactory();

        this.logger.info(String.format("ShellExecutor listening at %s", inetSocketAddress));

        this.started = true;
        this.activate(false);
        scheduler.submit(this, scheduler.MANAGEMENT_QUEUE);
    }

    public void stop() {
        this.started = false;
        closing.clear();

        try {
            for (ChannelSelectionKey ckey : ckeys) {
                ckey.cancel();
                ((ShellChannel)ckey.channel()).close();
            }
            ckeys.clear();
            skey.cancel();
            serverChannel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.logger.info("Stopped ShellExecutor service");
    }

    public int getQueueNumber() {
        return scheduler.MANAGEMENT_QUEUE;
    }

    public long perform() {
        if (!this.started)
            return 0;

        FastSet<ChannelSelectionKey> keys = null;

        try {
            keys = selector.selectNow();
        } catch (IOException e) {
            logger.error("IO Exception while polling channels. Server CLI will be shutdown now", e);
            stop();
            return 0;
        }

        for (FastSet.Record record = keys.head(), end = keys.tail(); (record = record.getNext()) != end;) {
            ShellChannel chan = null;
            ChannelSelectionKey key = (ChannelSelectionKey) keys.valueOf(record);

            if (key.isAcceptable()) {
                ChannelSelectionKey ckey = null;
                try {
                    chan = serverChannel.accept();
                    ckey = chan.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    ckeys.add(ckey);
                    chan.send(messageFactory.createMessage(String.format(CONNECTED_MESSAGE, this.version.getProperty("name"),
                                                                         this.version.getProperty("version"), this.version.getProperty("vendor"))));
                } catch (IOException e) {
                    logger.error("IO Exception while accepting connection", e);
                    if (ckey != null) {
                        ckey.cancel();
                        ckeys.remove(ckey);
                    }
                    try {
                        if (chan != null)
                            chan.close();
                    } catch (IOException e1) {
                        logger.error("IO Exception while closing Channel", e);
                    }
                }
            } else {
                try {
                    chan = (ShellChannel) key.channel();
                    if (key.isReadable()) {
                        Message msg = (Message) chan.receive();

                        if (msg != null) {
                            String rxMessage = msg.toString();
                            String txMessage;
                            logger.info("received command : " + rxMessage);
                            if (rxMessage.compareTo("disconnect") == 0) {
                                txMessage = "Bye";
                                chan.send(messageFactory.createMessage(txMessage));
                                closing.add(key);
                            } else {
                                String[] options = rxMessage.split(" ");
                                ShellExecutor shellExecutor = null;
                                for (FastList.Node<ShellExecutor> n = this.shellExecutors.head(), end1 = this.shellExecutors
                                         .tail(); (n = n.getNext()) != end1;) {
                                    ShellExecutor value = n.getValue();
                                    if (value.handles(options[0])) {
                                        shellExecutor = value;
                                        break;
                                    }
                                }

                                if (shellExecutor == null) {
                                    logger.warn(String.format("Received command=\"%s\" for which no ShellExecutor is configured ", rxMessage));
                                    chan.send(messageFactory.createMessage("Invalid command"));
                                } else {
                                    txMessage = shellExecutor.execute(options);
                                    chan.send(messageFactory.createMessage(txMessage));
                                }
                            } // if (rxMessage.compareTo("disconnect")
                        } // if (msg != null)

                        // TODO Handle message

                    } else if ((key.isWritable()) && closing.remove(key)) {
                        key.cancel();
                        ckeys.remove(key);
                        chan.close();
                    }
                } catch (IOException e) {
                    logger.error("IO Exception while reading/writing. Client CLI connection will be closed now", e);
                    closing.remove(key);
                    key.cancel();
                    ckeys.remove(key);
                    try {
                        if (chan != null)
                            chan.close();
                    } catch (IOException e1) {
                        logger.error("IO Exception while closing Channel", e);
                    }
                }
            }
        }

        if (this.started)
            scheduler.submit(this, scheduler.MANAGEMENT_QUEUE);

        return 0;
    }
}
