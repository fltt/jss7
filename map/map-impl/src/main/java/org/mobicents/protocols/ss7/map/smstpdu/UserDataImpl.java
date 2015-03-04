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

package org.mobicents.protocols.ss7.map.smstpdu;

import java.util.Vector;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPSmsTpduParameterFactory;
import org.mobicents.protocols.ss7.map.api.datacoding.NationalLanguageIdentifier;
import org.mobicents.protocols.ss7.map.api.smstpdu.CharacterSet;
import org.mobicents.protocols.ss7.map.api.smstpdu.ConcatenatedMessage;
import org.mobicents.protocols.ss7.map.api.smstpdu.DataCodingScheme;
import org.mobicents.protocols.ss7.map.api.smstpdu.Gsm7NationalLanguageIdentifier;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserData;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserDataHeader;
import org.mobicents.protocols.ss7.map.api.smstpdu.UserDataHeaderElement;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharset;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharsetDecoder;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharsetDecodingData;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharsetEncoder;
import org.mobicents.protocols.ss7.map.datacoding.GSMCharsetEncodingData;

/**
 *
 * @author sergey vetyutnev
 *
 */
public class UserDataImpl implements UserData {

    private static GSMCharset gsm7Charset = new GSMCharset("GSM", new String[] {});
    private static Charset ucs2Charset = Charset.forName("UTF-16BE");

    private DataCodingScheme dataCodingScheme;
    private Charset gsm8Charset;
    private byte[] encodedData;
    private int encodedUserDataLength;
    private boolean encodedUserDataHeaderIndicator;
    private UserDataHeader decodedUserDataHeader;
    private String decodedMessage;

    private boolean isDecoded;
    private boolean isEncoded;

    public UserDataImpl(byte[] encodedData, DataCodingScheme dataCodingScheme, int encodedUserDataLength,
            boolean encodedUserDataHeaderIndicator, Charset gsm8Charset) {
        this.encodedData = encodedData;
        this.encodedUserDataLength = encodedUserDataLength;
        this.encodedUserDataHeaderIndicator = encodedUserDataHeaderIndicator;
        if (dataCodingScheme != null)
            this.dataCodingScheme = dataCodingScheme;
        else
            this.dataCodingScheme = new DataCodingSchemeImpl(0);
        this.gsm8Charset = gsm8Charset;

        this.isEncoded = true;
    }

    public UserDataImpl(String decodedMessage, DataCodingScheme dataCodingScheme, UserDataHeader decodedUserDataHeader,
            Charset gsm8Charset) {
        this.decodedMessage = decodedMessage;
        this.decodedUserDataHeader = decodedUserDataHeader;
        if (dataCodingScheme != null)
            this.dataCodingScheme = dataCodingScheme;
        else
            this.dataCodingScheme = new DataCodingSchemeImpl(0);
        this.gsm8Charset = gsm8Charset;

        this.isDecoded = true;
    }

    public DataCodingScheme getDataCodingScheme() {
        return this.dataCodingScheme;
    }

    public byte[] getEncodedData() {
        return this.encodedData;
    }

    public int getEncodedUserDataLength() {
        return encodedUserDataLength;
    }

    public boolean getEncodedUserDataHeaderIndicator() {
        return encodedUserDataHeaderIndicator;
    }

    public UserDataHeader getDecodedUserDataHeader() {
        return decodedUserDataHeader;
    }

    public String getDecodedMessage() {
        return decodedMessage;
    }

    public void encode() throws MAPException {

        if (this.isEncoded)
            return;
        this.isEncoded = true;

        this.encodedData = null;
        this.encodedUserDataLength = 0;
        this.encodedUserDataHeaderIndicator = false;

        if (this.decodedMessage == null)
            this.decodedMessage = "";

        // encoding UserDataHeader if it exists
        byte[] buf2 = null;
        if (this.decodedUserDataHeader != null) {
            buf2 = this.decodedUserDataHeader.getEncodedData();
            if (buf2 != null && buf2.length > 0)
                this.encodedUserDataHeaderIndicator = true;
            else
                buf2 = null;
        }

        if (this.dataCodingScheme.getIsCompressed()) {
            // TODO: implement the case with compressed message
            throw new MAPException("Error encoding a text in Sms UserData: compressed message is not supported yet");
        } else {
            switch (this.dataCodingScheme.getCharacterSet()) {
                case GSM7:
                    // selecting a Charset for encoding
                    Charset cSet = gsm7Charset;
                    Gsm7NationalLanguageIdentifier nationalLanguageLockingShift = null;
                    Gsm7NationalLanguageIdentifier nationalLanguageSingleShift = null;
                    NationalLanguageIdentifier nationalLanguageLockingShiftIdentifier = null;
                    NationalLanguageIdentifier nationalLanguageSingleShiftIdentifier = null;
                    if (this.decodedUserDataHeader != null) {
                        nationalLanguageLockingShift = this.decodedUserDataHeader.getNationalLanguageLockingShift();
                        nationalLanguageSingleShift = this.decodedUserDataHeader.getNationalLanguageSingleShift();
                        if (nationalLanguageLockingShift != null)
                            nationalLanguageLockingShiftIdentifier = nationalLanguageLockingShift
                                    .getNationalLanguageIdentifier();
                        if (nationalLanguageSingleShift != null)
                            nationalLanguageSingleShiftIdentifier = nationalLanguageSingleShift.getNationalLanguageIdentifier();
                    }
                    if (nationalLanguageLockingShift != null || nationalLanguageSingleShift != null) {
                        cSet = new GSMCharset("GSM", new String[] {}, nationalLanguageLockingShiftIdentifier,
                                nationalLanguageSingleShiftIdentifier);
                    }

                    GSMCharsetEncoder encoder = (GSMCharsetEncoder) cSet.newEncoder();
                    encoder.setGSMCharsetEncodingData(new GSMCharsetEncodingData(buf2));
                    ByteBuffer bb = null;
                    try {
                        bb = encoder.encode(CharBuffer.wrap(this.decodedMessage));
                    } catch (Exception e) {
                        // This can not occur
                    }
                    this.encodedUserDataLength = encoder.getGSMCharsetEncodingData().getTotalSeptetCount();
                    if (bb != null) {
                        this.encodedData = new byte[bb.limit()];
                        bb.get(this.encodedData);
                    } else {
                        this.encodedData = new byte[0];
                    }
                    break;

                case GSM8:
                    if (gsm8Charset != null) {
                        bb = gsm8Charset.encode(this.decodedMessage);
                        this.encodedData = new byte[bb.limit()];
                        bb.get(this.encodedData);
                        if (buf2 != null) {
                            byte[] tempBuf = this.encodedData;
                            this.encodedData = new byte[buf2.length + tempBuf.length];
                            System.arraycopy(buf2, 0, this.encodedData, 0, buf2.length);
                            System.arraycopy(tempBuf, 0, this.encodedData, buf2.length, tempBuf.length);
                        }
                        this.encodedUserDataLength = this.encodedData.length;
                    } else {
                        throw new MAPException(
                                "Error encoding a text in Sms UserData: gsm8Charset is not defined for GSM8 dataCodingScheme");
                    }
                    break;

                case UCS2:
                    bb = ucs2Charset.encode(this.decodedMessage);
                    this.encodedData = new byte[bb.limit()];
                    bb.get(this.encodedData);
                    if (buf2 != null) {
                        byte[] tempBuf = this.encodedData;
                        this.encodedData = new byte[buf2.length + tempBuf.length];
                        System.arraycopy(buf2, 0, this.encodedData, 0, buf2.length);
                        System.arraycopy(tempBuf, 0, this.encodedData, buf2.length, tempBuf.length);
                    }
                    this.encodedUserDataLength = this.encodedData.length;
                    break;
            }
        }
    }

    public void decode() throws MAPException {

        if (this.isDecoded)
            return;
        this.isDecoded = true;

        this.decodedUserDataHeader = null;
        this.decodedMessage = null;

        if (this.encodedData == null)
            throw new MAPException("Error decoding a text from Sms UserData: encodedData field is null");

        int offset = 0;
        if (this.encodedUserDataHeaderIndicator) {
            // decode userDataHeader
            if (this.encodedData.length < 1)
                return;
            offset = (this.encodedData[0] & 0xFF) + 1;
            if (offset > this.encodedData.length)
                return;

            this.decodedUserDataHeader = new UserDataHeaderImpl(this.encodedData);
        }

        if (this.dataCodingScheme.getIsCompressed()) {
            // TODO: implement the case with compressed sms message
            // If this is a signal message - this.decodedMessage can be decoded
            // If thus is a segment of concatenated message this.decodedMessage should stay null and this case should be
            // processed by a special static method
            int i1 = 0;
            i1++;
        } else {
            switch (this.dataCodingScheme.getCharacterSet()) {
                case GSM7:
                    GSMCharset cSet = gsm7Charset;
                    Gsm7NationalLanguageIdentifier nationalLanguageLockingShift = null;
                    Gsm7NationalLanguageIdentifier nationalLanguageSingleShift = null;
                    NationalLanguageIdentifier nationalLanguageLockingShiftIdentifier = null;
                    NationalLanguageIdentifier nationalLanguageSingleShiftIdentifier = null;
                    if (this.decodedUserDataHeader != null) {
                        nationalLanguageLockingShift = this.decodedUserDataHeader.getNationalLanguageLockingShift();
                        nationalLanguageSingleShift = this.decodedUserDataHeader.getNationalLanguageSingleShift();
                        if (nationalLanguageLockingShift != null)
                            nationalLanguageLockingShiftIdentifier = nationalLanguageLockingShift
                                    .getNationalLanguageIdentifier();
                        if (nationalLanguageSingleShift != null)
                            nationalLanguageSingleShiftIdentifier = nationalLanguageSingleShift.getNationalLanguageIdentifier();
                    }
                    if (nationalLanguageLockingShift != null || nationalLanguageSingleShift != null) {
                        cSet = new GSMCharset("GSM", new String[] {}, nationalLanguageLockingShiftIdentifier,
                                nationalLanguageSingleShiftIdentifier);
                    }

                    GSMCharsetDecoder decoder = (GSMCharsetDecoder) cSet.newDecoder();
                    if (offset > 0) {
                        int bitOffset = offset * 8;
                        int septetOffset = (bitOffset - 1) / 7 + 1;
                        decoder.setGSMCharsetDecodingData(new GSMCharsetDecodingData(this.encodedUserDataLength, septetOffset));
                    } else
                        decoder.setGSMCharsetDecodingData(new GSMCharsetDecodingData(this.encodedUserDataLength, 0));
                    ByteBuffer bb = ByteBuffer.wrap(this.encodedData);
                    CharBuffer bf = null;
                    try {
                        bf = decoder.decode(bb);
                    } catch (CharacterCodingException e) {
                        // This can not occur
                    }
                    if (bf != null)
                        this.decodedMessage = bf.toString();
                    else
                        this.decodedMessage = "";
                    break;

                case GSM8:
                    if (gsm8Charset != null) {
                        byte[] buf = this.encodedData;
                        int len = this.encodedUserDataLength;
                        if (len > buf.length)
                            len = buf.length;
                        if (offset > 0) {
                            if (offset > len)
                                buf = new byte[0];
                            else {
                                buf = new byte[len - offset];
                                System.arraycopy(this.encodedData, offset, buf, 0, len - offset);
                            }
                        }
                        bb = ByteBuffer.wrap(buf);
                        bf = gsm8Charset.decode(bb);
                        this.decodedMessage = bf.toString();
                    }
                    break;

                case UCS2:
                    byte[] buf = this.encodedData;
                    int len = this.encodedUserDataLength;
                    if (len > buf.length)
                        len = buf.length;
                    if (offset > 0) {
                        if (offset > len)
                            buf = new byte[0];
                        else {
                            buf = new byte[len - offset];
                            System.arraycopy(this.encodedData, offset, buf, 0, len - offset);
                        }
                    }
                    bb = ByteBuffer.wrap(buf);
                    bf = ucs2Charset.decode(bb);
                    this.decodedMessage = bf.toString();
                    break;
            }
        }
    }

    public static UserData[] encodeGSM7SplitMessagesSet(MAPSmsTpduParameterFactory mapSmsTpduParameterFactory,
                                                        String msg, DataCodingScheme dataCodingScheme,
                                                        boolean referenceIs16bit, int messageReferenceNumber,
                                                        int messageSegmentCount, int messageSegmentNumber,
                                                        UserDataHeader userDataHeader) throws MAPException {
        byte[] buf2 = null;
        boolean encodedUserDataHeaderIndicator = false;
        if (userDataHeader != null) {
            buf2 = userDataHeader.getEncodedData();
        } else if ((messageSegmentCount > 0) && (messageSegmentNumber > 0)) {
            userDataHeader = mapSmsTpduParameterFactory.createUserDataHeader();
            UserDataHeaderElement concatenatedShortMessagesIdentifier = mapSmsTpduParameterFactory
                .createConcatenatedShortMessagesIdentifier(referenceIs16bit, messageReferenceNumber,
                                                           messageSegmentCount, messageSegmentNumber);
            userDataHeader.addInformationElement(concatenatedShortMessagesIdentifier);
            buf2 = userDataHeader.getEncodedData();
        }
        if ((buf2 != null) && (buf2.length > 0))
            encodedUserDataHeaderIndicator = true;

        byte[] encodedData = null;
        ByteBuffer bb = ByteBuffer.allocateDirect(140);
        CoderResult res;
        GSMCharsetEncoder encoder = (GSMCharsetEncoder)gsm7Charset.newEncoder();
        Vector<byte[]> encodedDataVector = new Vector<byte[]>();
        Vector<Integer> encodedDataLengthVector = new Vector<Integer>();

        if (encodedUserDataHeaderIndicator || (msg.length() <= 160)) {
            // Try encoding single segment (if encodedUserDataHeaderIndicator == true,
            // don't encode multiple segments in any case)
            encoder.setGSMCharsetEncodingData(new GSMCharsetEncodingData(buf2));
            CharBuffer msgBuffer = CharBuffer.wrap(msg);
            res = encoder.encode(msgBuffer, bb, true);
            if (res.isError())
                return null;
            if (encodedUserDataHeaderIndicator || res.isUnderflow()) {
                // The message fits a single segment or we are forced to
                // encode a single segment (i.e., there is a UserDataHeader)
                encoder.flush(bb);
                bb.flip();
                encodedData = new byte[bb.limit()];
                bb.get(encodedData);
                encodedDataVector.add(encodedData);
                encodedDataLengthVector.add(new Integer(encoder.getGSMCharsetEncodingData().getTotalSeptetCount()));
            }
        }

        if (encodedData == null) {
            // There is no UserDataHeader and the
            // message doesn't fit a single segment

            // Fake UserDataHeader
            if (referenceIs16bit) {
                buf2 = new byte[7];
            } else {
                buf2 = new byte[6];
            }
            encoder.setGSMCharsetEncodingData(new GSMCharsetEncodingData(buf2));
            CharBuffer msgBuffer = CharBuffer.wrap(msg);

            do {
                encoder.reset();
                res = encoder.encode(msgBuffer, bb, true);
                if (res.isError())
                    return null;
                if (res.isUnderflow())
                    encoder.flush(bb);
                bb.flip();
                encodedData = new byte[bb.limit()];
                bb.get(encodedData);
                bb.clear();
                encodedDataVector.add(encodedData);
                encodedDataLengthVector.add(new Integer(encoder.getGSMCharsetEncodingData().getTotalSeptetCount()));

                // Do not encode more than 255 segments
                if (encodedDataVector.size() > 254)
                    break;
            } while (res.isOverflow());
        }

        UserData[] userDataArray = new UserData[encodedDataVector.size()];
        DataCodingScheme newDataCodingScheme =
            new DataCodingSchemeImpl(dataCodingScheme.getDataCodingGroup(),
                                     dataCodingScheme.getMessageClass(),
                                     dataCodingScheme.getDataCodingSchemaIndicationType(),
                                     dataCodingScheme.getSetIndicationActive(),
                                     CharacterSet.GSM7, false);

        if (encodedDataVector.size() > 1) {
            messageSegmentNumber = 0;
            for (byte[] userData : encodedDataVector) {
                userDataHeader = mapSmsTpduParameterFactory.createUserDataHeader();
                UserDataHeaderElement concatenatedShortMessagesIdentifier = mapSmsTpduParameterFactory
                    .createConcatenatedShortMessagesIdentifier(referenceIs16bit, messageReferenceNumber,
                                                               encodedDataVector.size(), messageSegmentNumber + 1);
                userDataHeader.addInformationElement(concatenatedShortMessagesIdentifier);
                buf2 = userDataHeader.getEncodedData();
                System.arraycopy(buf2, 0, userData, 0, buf2.length);
                userDataArray[messageSegmentNumber] = new UserDataImpl(userData, newDataCodingScheme,
                                                                       encodedDataLengthVector.get(messageSegmentNumber).intValue(),
                                                                       true, null);
                messageSegmentNumber++;
            }
        } else {
            byte[] userData = encodedDataVector.firstElement();
            userDataArray[0] = new UserDataImpl(userData, newDataCodingScheme,
                                                encodedDataLengthVector.get(0).intValue(),
                                                encodedUserDataHeaderIndicator, null);
        }

        return userDataArray;
    }

    public static UserData[] encode8BitDataSplitMessagesSet(MAPSmsTpduParameterFactory mapSmsTpduParameterFactory,
                                                            byte[] msg, DataCodingScheme dataCodingScheme,
                                                            boolean referenceIs16bit, int messageReferenceNumber,
                                                            int messageSegmentCount, int messageSegmentNumber,
                                                            UserDataHeader userDataHeader) throws MAPException {
        int bufferLimit = 140;
        byte[] buf2 = null;
        boolean encodedUserDataHeaderIndicator = false;
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferLimit);
        if (userDataHeader != null) {
            buf2 = userDataHeader.getEncodedData();
        } else if ((messageSegmentCount > 0) && (messageSegmentNumber > 0)) {
            userDataHeader = mapSmsTpduParameterFactory.createUserDataHeader();
            UserDataHeaderElement concatenatedShortMessagesIdentifier = mapSmsTpduParameterFactory
                .createConcatenatedShortMessagesIdentifier(referenceIs16bit, messageReferenceNumber,
                                                           messageSegmentCount, messageSegmentNumber);
            userDataHeader.addInformationElement(concatenatedShortMessagesIdentifier);
            buf2 = userDataHeader.getEncodedData();
        }
        if ((buf2 != null) && (buf2.length > 0)) {
            encodedUserDataHeaderIndicator = true;
            bb.put(buf2);
        }

        if ((!encodedUserDataHeaderIndicator) && (msg.length > 140)) {
            if (referenceIs16bit) {
                bufferLimit = 133;
            } else {
                bufferLimit = 134;
            }
            bb.limit(bufferLimit);
        }

        byte[] encodedData;
        Vector<byte[]> encodedDataVector = new Vector<byte[]>();

        int len;
        int offset = 0;
        do {
            len = msg.length - offset;
            if (len > bb.remaining())
                len = bb.remaining();
            bb.put(msg, offset, len);
            offset += len;
            bb.flip();
            encodedData = new byte[bb.limit()];
            bb.get(encodedData);
            bb.clear();
            bb.limit(bufferLimit);
            encodedDataVector.add(encodedData);

            // Do not encode more than 255 segments
            if (encodedDataVector.size() > 254)
                break;
        } while ((offset < msg.length) && !encodedUserDataHeaderIndicator);

        UserData[] userDataArray = new UserData[encodedDataVector.size()];
        DataCodingScheme newDataCodingScheme =
            new DataCodingSchemeImpl(dataCodingScheme.getDataCodingGroup(),
                                     dataCodingScheme.getMessageClass(),
                                     dataCodingScheme.getDataCodingSchemaIndicationType(),
                                     dataCodingScheme.getSetIndicationActive(),
                                     CharacterSet.GSM8, false);

        if (encodedDataVector.size() > 1) {
            messageSegmentNumber = 0;
            for (byte[] userData : encodedDataVector) {
                userDataHeader = mapSmsTpduParameterFactory.createUserDataHeader();
                UserDataHeaderElement concatenatedShortMessagesIdentifier = mapSmsTpduParameterFactory
                    .createConcatenatedShortMessagesIdentifier(referenceIs16bit, messageReferenceNumber,
                                                               encodedDataVector.size(), messageSegmentNumber + 1);
                userDataHeader.addInformationElement(concatenatedShortMessagesIdentifier);
                buf2 = userDataHeader.getEncodedData();
                encodedData = new byte[buf2.length + userData.length];
                System.arraycopy(buf2, 0, encodedData, 0, buf2.length);
                System.arraycopy(userData, 0, encodedData, buf2.length, userData.length);
                userDataArray[messageSegmentNumber++] = new UserDataImpl(encodedData, newDataCodingScheme,
                                                                         encodedData.length, true, null);
            }
        } else {
            byte[] userData = encodedDataVector.firstElement();
            userDataArray[0] = new UserDataImpl(userData, newDataCodingScheme, userData.length,
                                                encodedUserDataHeaderIndicator, null);
        }

        return userDataArray;
    }

    public static UserData[] encodeUCS2SplitMessagesSet(MAPSmsTpduParameterFactory mapSmsTpduParameterFactory,
                                                        String msg, DataCodingScheme dataCodingScheme,
                                                        boolean referenceIs16bit, int messageReferenceNumber,
                                                        int messageSegmentCount, int messageSegmentNumber,
                                                        UserDataHeader userDataHeader) throws MAPException {
        int bufferLimit = 140;
        byte[] buf2 = null;
        boolean encodedUserDataHeaderIndicator = false;
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferLimit);
        if (userDataHeader != null) {
            buf2 = userDataHeader.getEncodedData();
        } else if ((messageSegmentCount > 0) && (messageSegmentNumber > 0)) {
            userDataHeader = mapSmsTpduParameterFactory.createUserDataHeader();
            UserDataHeaderElement concatenatedShortMessagesIdentifier = mapSmsTpduParameterFactory
                .createConcatenatedShortMessagesIdentifier(referenceIs16bit, messageReferenceNumber,
                                                           messageSegmentCount, messageSegmentNumber);
            userDataHeader.addInformationElement(concatenatedShortMessagesIdentifier);
            buf2 = userDataHeader.getEncodedData();
        }
        if ((buf2 != null) && (buf2.length > 0)) {
            if ((buf2.length & 0x01) != 0)
                bb.limit(139);
            encodedUserDataHeaderIndicator = true;
            bb.put(buf2);
        }

        CoderResult res;
        byte[] replacement = new byte[2];
        replacement[0] = 0;
        replacement[1] = 0x20;
        CharsetEncoder encoder = ucs2Charset.newEncoder().replaceWith(replacement);
        encoder = encoder
            .onMalformedInput(CodingErrorAction.IGNORE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);

        if ((!encodedUserDataHeaderIndicator) && (msg.length() > 70)) {
            if (referenceIs16bit) {
                bufferLimit = 132;
            } else {
                bufferLimit = 134;
            }
            bb.limit(bufferLimit);
        }

        byte[] encodedData;
        Vector<byte[]> encodedDataVector = new Vector<byte[]>();
        CharBuffer msgBuffer = CharBuffer.wrap(msg);

        do {
            encoder.reset();
            res = encoder.encode(msgBuffer, bb, true);
            if (res.isError())
                return null;
            if (res.isUnderflow())
                res = encoder.flush(bb);
            bb.flip();
            encodedData = new byte[bb.limit()];
            bb.get(encodedData);
            bb.clear();
            bb.limit(bufferLimit);
            encodedDataVector.add(encodedData);

            // Do not encode more than 255 segments
            if (encodedDataVector.size() > 254)
                break;
        } while (res.isOverflow() && !encodedUserDataHeaderIndicator);

        UserData[] userDataArray = new UserData[encodedDataVector.size()];
        DataCodingScheme newDataCodingScheme =
            new DataCodingSchemeImpl(dataCodingScheme.getDataCodingGroup(),
                                     dataCodingScheme.getMessageClass(),
                                     dataCodingScheme.getDataCodingSchemaIndicationType(),
                                     dataCodingScheme.getSetIndicationActive(),
                                     CharacterSet.UCS2, false);

        if (encodedDataVector.size() > 1) {
            messageSegmentNumber = 0;
            for (byte[] userData : encodedDataVector) {
                userDataHeader = mapSmsTpduParameterFactory.createUserDataHeader();
                UserDataHeaderElement concatenatedShortMessagesIdentifier = mapSmsTpduParameterFactory
                    .createConcatenatedShortMessagesIdentifier(referenceIs16bit, messageReferenceNumber,
                                                               encodedDataVector.size(), messageSegmentNumber + 1);
                userDataHeader.addInformationElement(concatenatedShortMessagesIdentifier);
                buf2 = userDataHeader.getEncodedData();
                encodedData = new byte[buf2.length + userData.length];
                System.arraycopy(buf2, 0, encodedData, 0, buf2.length);
                System.arraycopy(userData, 0, encodedData, buf2.length, userData.length);
                userDataArray[messageSegmentNumber++] = new UserDataImpl(encodedData, newDataCodingScheme,
                                                                         encodedData.length, true, null);
            }
        } else {
            byte[] userData = encodedDataVector.firstElement();
            userDataArray[0] = new UserDataImpl(userData, newDataCodingScheme, userData.length,
                                                encodedUserDataHeaderIndicator, null);
        }

        return userDataArray;
    }

    public static ConcatenatedMessage decodeSplitMessagesSet(UserData[] encodedArray, DataCodingScheme dataCodingScheme)
            throws MAPException {

        // TODO: implement the splitMessage tool: decoding splitted sms messages into a solid message
        throw new MAPException("Not yet implemented");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TP-User-Data [");
        if (this.decodedMessage == null) {
            if (this.encodedData != null)
                sb.append(printDataArr(this.encodedData));
        } else {
            sb.append("Msg:[");
            sb.append(this.decodedMessage);
            sb.append("]");
            if (this.decodedUserDataHeader != null) {
                sb.append("\n");
                sb.append(this.decodedUserDataHeader.toString());
            }
        }
        sb.append("]");

        return sb.toString();
    }

    private String printDataArr(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int b : arr) {
            sb.append(b);
            sb.append(", ");
        }

        return sb.toString();
    }
}
