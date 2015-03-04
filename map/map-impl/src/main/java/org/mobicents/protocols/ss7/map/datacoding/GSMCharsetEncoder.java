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

package org.mobicents.protocols.ss7.map.datacoding;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 *
 * @author amit bhayani
 * @author sergey vetyutnev
 *
 */
public class GSMCharsetEncoder extends CharsetEncoder {

    private int bytepos = 0;
    private int bitpos = 0;
    private int carryOver = 0;
    private char lastChar = ' ';
    private GSMCharset cs;
    private GSMCharsetEncodingData encodingData;

    static final byte ESCAPE = 0x1B;

    protected GSMCharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar) {
        super(cs, averageBytesPerChar, maxBytesPerChar);
        implReset();
        this.cs = (GSMCharset) cs;
    }

    public void setGSMCharsetEncodingData(GSMCharsetEncodingData encodingData) {
        this.encodingData = encodingData;
    }

    public GSMCharsetEncodingData getGSMCharsetEncodingData() {
        return this.encodingData;
    }

    protected void implReset() {
        bytepos = 0;
        bitpos = 0;
        carryOver = 0;
        lastChar = ' ';
        if (encodingData != null)
            encodingData.totalSeptetCount = 0;
    }

    protected CoderResult implFlush(ByteBuffer out) {

        if (bitpos != 0) {
            // USSD: replace 7-bit pad with <CR>
            if (this.encodingData != null && this.encodingData.ussdStyleEncoding && bitpos == 7)
                carryOver |= 0x1A;

            // writing a carryOver data
            if (out.remaining() < 1)
                return CoderResult.OVERFLOW;
            out.put((byte) carryOver);
            bitpos = 0;
        } else {

            // USSD: adding extra <CR> if the last symbol is <CR> and no padding
            if (this.encodingData != null && this.encodingData.ussdStyleEncoding && lastChar == '\r') {
                if (out.remaining() < 1)
                    return CoderResult.OVERFLOW;
                out.put((byte) 0x0D);
            }
        }

        return CoderResult.UNDERFLOW;
    }

    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {

        if (this.encodingData != null && this.encodingData.leadingBuffer != null) {
            int septetCount = (this.encodingData.leadingBuffer.length * 8 + 6) / 7;
            bitpos = septetCount % 8;
            this.encodingData.totalSeptetCount = septetCount;
            for (; bytepos < this.encodingData.leadingBuffer.length; bytepos++) {
                if (out.remaining() < 1)
                    return CoderResult.OVERFLOW;
                out.put(this.encodingData.leadingBuffer[bytepos]);
            }
        }

        while (in.hasRemaining()) {

            // Read the first char
            lastChar = in.get();

            boolean found = false;
            // searching a char in the main character table
            for (int i = 0; i < this.cs.mainTable.length; i++) {
                if (this.cs.mainTable[i] == lastChar) {
                    if (putByte(i, out)) {
                        in.position(in.position() - 1);
                        return CoderResult.OVERFLOW;
                    }
                    found = true;
                    break;
                }
            }

            // searching a char in the extension character table
            if (!found && this.cs.extensionTable != null) {
                for (int i = 0; i < this.cs.mainTable.length; i++) {
                    if (this.cs.extensionTable[i] == lastChar) {
                        if (putEscByte(i, out)) {
                            in.position(in.position() - 1);
                            return CoderResult.OVERFLOW;
                        }
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                // found no suitable symbol - encode a space char
                if (putByte(0x20, out)) {
                    in.position(in.position() - 1);
                    return CoderResult.OVERFLOW;
                }
            }
        }

        return CoderResult.UNDERFLOW;
    }

    private boolean putByte(int data, ByteBuffer out) {
        if (out.remaining()*8 < 15 - bitpos) {
            implFlush(out);
            return true;
        }

        if (bitpos == 0) {
            carryOver = data;
        } else {
            int i1 = data << (8 - bitpos);
            out.put((byte) (i1 | carryOver));
            carryOver = data >>> bitpos;
        }

        bitpos++;
        if (bitpos == 8) {
            bitpos = 0;
        }

        if (this.encodingData != null)
            this.encodingData.totalSeptetCount++;
        return false;
    }

    private boolean putEscByte(int data, ByteBuffer out) {
        if (out.remaining()*8 < 22 - bitpos) {
            implFlush(out);
            return true;
        }

        if (bitpos == 0) {
            int i1 = data << 7;
            out.put((byte) (i1 | GSMCharsetEncoder.ESCAPE));
            carryOver = data >>> 1;
        } else {
            int i1 = GSMCharsetEncoder.ESCAPE << (8 - bitpos);
            out.put((byte) (i1 | carryOver));
            if (bitpos == 7) {
                carryOver = data;
            } else {
                carryOver = GSMCharsetEncoder.ESCAPE >>> bitpos;
                i1 = data << (8 - bitpos - 1);
                out.put((byte) (i1 | carryOver));
                carryOver = data >>> (bitpos + 1);
            }
        }

        bitpos += 2;
        if (bitpos >= 8)
            bitpos -= 8;

        if (this.encodingData != null)
            this.encodingData.totalSeptetCount += 2;
        return false;
    }
}
