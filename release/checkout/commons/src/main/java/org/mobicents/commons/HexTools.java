package org.mobicents.commons;

import java.nio.ByteBuffer;

/**
 * <p>
 * Utility class to dump the raw data as hex dump. Also other convenient methods
 * </p>
 * 
 * @author Asbjorn Grandt asbjorn.grandt@gmail.com, asbjorn.grandt@dantelo.com
 * 
 */
public class HexTools {
	public static final int SPACING_NONE = 0;
	public static final int SPACING_OCTET = 3;
	public static final int SPACING_NIBBLE = 4;
	public static final int SPACING_BYTE = 8;
	public static final int SPACING_WORD = 16;

	private static final char[] digits = "0123456789ABCDEF".toCharArray();

	/**
	 * <p>
	 * Generate a hex frame from the provided byte buffer. The Buffer will be
	 * returned to it's original state.
	 * </p>
	 * 
	 * @param buffer
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(ByteBuffer buffer) {
		int len = buffer.remaining();
		int pos = buffer.position();
		byte[] b = new byte[len];
		buffer.get(b);
		buffer.position(pos);
		return HexTools.dump(b);
	}

	/**
	 * <p>
	 * Generate a hex frame from the provided byte buffer. The Buffer will be
	 * returned to it's original state.
	 * </p>
	 * 
	 * <p>
	 * Example: 
	 * <pre>HexTools.dump(bb, 50, true)</pre>
	 * 
	 * Where bb is a ByteBuffer of 256 bytes, filled with bytes from 0 to 255,
	 * where a previous operation have already retrieved 44 bytes will return:
	 * 
	 * <pre>
	 * Start: 44 (0x2C) End: 93 (0x5D) Length: 50 (0x32)
	 * 20: -- -- -- -- -- -- -- -- -- -- -- -- 2C 2D 2E 2F |              ,-./
	 * 30: 30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F | 01234567 89:;<=>?
	 * 40: 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F | @ABCDEFG HIJKLMNO
	 * 50: 50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D -- -- | PQRSTUVW XYZ[\]  
	 * </pre>
	 * 
	 * Where 
	 * <pre>HexTools.dump(bb, 50, false)</pre>
	 * on that same ByteBuffer returns:
	 * 
	 * <pre>
	 * Start: 0 (0x00) End: 49 (0x31) Length: 50 (0x32)
	 * 00: 2C 2D 2E 2F 30 31 32 33 34 35 36 37 38 39 3A 3B | ,-./0123 456789:;
	 * 10: 3C 3D 3E 3F 40 41 42 43 44 45 46 47 48 49 4A 4B | <=>?@ABC DEFGHIJK
	 * 20: 4C 4D 4E 4F 50 51 52 53 54 55 56 57 58 59 5A 5B | LMNOPQRS TUVWXYZ[
	 * 30: 5C 5D -- -- -- -- -- -- -- -- -- -- -- -- -- -- | \]               
	 * </pre>
	 * </p>
	 * 
	 * @param buffer
	 * @param length
	 *            print up to number of bytes. 0 means print all remaining.
	 * @param printAddress
	 *            Should the printout start from the ByteBuffer current
	 *            position.
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(ByteBuffer buffer, int length, boolean printAddress) {
		int len = buffer.remaining();
		int pos = buffer.position();
		if (length > 0 && len > length) {
			len = length;
		}
		byte[] b = new byte[len];
		buffer.get(b);
		buffer.position(pos);
		if (printAddress) {
			return HexTools.dump(b, pos);
		}
		return HexTools.dump(b, 0);
	}

	/**
	 * <p>
	 * Generate a hex frame from the provided byte buffer. The Buffer will be
	 * returned to it's original state. Start address references the initial
	 * address of the provided buffer.
	 * </p>
	 * 
	 * @param buffer
	 * @param startAddress
	 *            address of the provided data chunk.
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(ByteBuffer buffer, int startAddress) {
		int len = buffer.remaining();
		int pos = buffer.position();
		byte[] b = new byte[len];
		buffer.get(b);
		buffer.position(pos);
		return HexTools.dump(b, startAddress);
	}

	/**
	 * <p>
	 * Prints a hex frame from the byte array of the provided String.
	 * </p>
	 * 
	 * @param data
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(String data) {
		return HexTools.dump(data.getBytes());
	}

	/**
	 * <p>
	 * Prints a hex frame from the byte array of the provided String. Start
	 * address references the initial address of the provided string.
	 * </p>
	 * 
	 * @param data
	 * @param startAddress
	 *            address of the provided data chunk.
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(String data, int startAddress) {
		return HexTools.dump(data.getBytes(), startAddress);
	}

	/**
	 * <p>
	 * Prints bytes in hex form, 16 bytes per line.
	 * <pre>
	 * 00: 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F | ........ ........
	 * 10: 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F | ........ ........
	 * 20: 20 21 22 -- -- -- -- -- -- -- -- -- -- -- -- -- | ...              
	 * </pre>
	 * </p>
	 * 
	 * @param data
	 *            String of bytes to dump
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(byte[] data) {
		int dataLen = data.length;

		int hexDigits = Integer.toString(dataLen - 1, 16).length();
		if (hexDigits < 2) {
			hexDigits = 2;
		}

		StringBuffer sb = new StringBuffer();
		StringBuffer chb = new StringBuffer("| ");

		for (int i = 0; i < dataLen; i++) {
			if (i % 16 == 0) {
				sb.append(HexTools.toHex(i, hexDigits * 4, HexTools.SPACING_NONE)).append(":");
			}

			sb.append(" ").append(HexTools.toHex(data[i]));
			char ch = (char) data[i];
			chb.append(data[i] >= 0x20 && data[i] < 0x7F ? ch : ".");

			if (i % 8 == 7) {
				sb.append(" ");
				chb.append(" ");
			}
			if (i % 16 == 15) {
				sb.append(chb);
				sb.append("\n");
				chb = new StringBuffer("| ");
			}
		}

		int last = dataLen % 16;

		if (last > 0) {
			for (int i = last; i < 16; i++) {
				sb.append(" --");
				chb.append(" ");
				if (i % 8 == 7) {
					sb.append(" ");
					chb.append(" ");
				}
			}
			sb.append(chb);
			chb = null;
			sb.append("\n");
		}
		return sb;
	}

	/**
	 * <p>
	 * Prints bytes in hex form, 16 bytes per line. startAddress sets the first
	 * "address" the given array is printed from. In this example the start
	 * address were 0x3456 on a 35 byte array.
	 * 
	 * <pre>
	 * Start: 13398 (0x3456) End: 13432 (0x3478) Length: 35 (0x23)
	 * 50: -- -- -- -- -- -- 00 01 02 03 04 05 06 07 08 09 |       .. ........
	 * 60: 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 | ........ ........
	 * 70: 1A 1B 1C 1D 1E 1F 20 21 22 -- -- -- -- -- -- -- | ........ .       
	 * </pre>
	 * </p>
	 *
	 * @param data
	 * @param startAddress
	 *            address of the provided data chunk.
	 * @return StringBuffer with the hex frame.
	 */
	public static StringBuffer dump(byte[] data, int startAddress) {
		int dataLength = data.length;
		int startAddressMod = startAddress % 16;
		int startAddressMain = startAddress - startAddressMod;

		int hexDigitCount = Integer.toString(dataLength + startAddress - 1, 16).length();
		hexDigitCount += hexDigitCount % 2;
		if (hexDigitCount < 2) {
			hexDigitCount = 2;
		}

		int hexDigitLengthCount = Integer.toString(dataLength, 16).length();
		hexDigitLengthCount += hexDigitLengthCount % 2;
		if (hexDigitLengthCount < 2) {
			hexDigitLengthCount = 2;
		}

		StringBuffer sb = new StringBuffer();
		StringBuffer chb = new StringBuffer("| ");

		sb.append("Start: ").append(startAddress).append(" (0x")
				.append(HexTools.toHex(startAddress, hexDigitCount * 4, HexTools.SPACING_NONE)).append(")  End: ")
				.append(startAddress + dataLength - 1).append(" (0x")
				.append(HexTools.toHex(startAddress + dataLength - 1, hexDigitCount * 4, HexTools.SPACING_NONE))
				.append(")  Length: ").append(dataLength).append(" (0x")
				.append(HexTools.toHex(dataLength, hexDigitLengthCount * 4, HexTools.SPACING_NONE)).append(")\n");

		for (int i = 0; i < dataLength; i++) {
			if ((i + startAddressMod) % 16 == 0 || i == 0) {
				if (i == 0) {
					sb.append(HexTools.toHex(i + startAddressMain, hexDigitCount * 4, HexTools.SPACING_NONE)).append(
							":");

				} else {
					sb.append(HexTools.toHex(i + startAddress, hexDigitCount * 4, HexTools.SPACING_NONE)).append(":");
				}
			}
			if (i == 0 && startAddressMod > 0) {
				for (int j = 0; j < startAddressMod; j++) {
					sb.append(" --");
					chb.append(" ");
					if (j % 8 == 7) {
						sb.append(" ");
						chb.append(" ");
					}
				}
			}

			sb.append(" ").append(HexTools.toHex(data[i]));
			char ch = (char) data[i];
			chb.append(data[i] >= 0x20 && data[i] < 0x7F ? ch : ".");

			if ((i + startAddressMod) % 8 == 7) {
				sb.append(" ");
				chb.append(" ");
			}
			if ((i + startAddressMod) % 16 == 15) {
				sb.append(chb);
				sb.append("\n");
				chb = new StringBuffer("| ");
			}
		}

		int last = (dataLength - (16 - startAddressMod)) % 16;
		if (last < 0) {
			last += 16;
		}

		if (last > 0) {
			for (int i = last; i < 16; i++) {
				sb.append(" --");
				chb.append(" ");
				if (i % 8 == 7) {
					sb.append(" ");
					chb.append(" ");
				}
			}
			sb.append(chb);
			chb = null;
			sb.append("\n");
		}
		return sb;
	}

	/**
	 * <p>
     * Pad a string, right adjusting it to the specified length, adding pad chars to it. 
     * If the given string is longer than the pad, it'll be truncated and prepended with a "<" character.
     * </p>
     * 
     * @param in
     * @param length
     * @param pad
     * @param limitMaxLength
     * @return
     */
	public static String pad(String in, int length, char pad, boolean limitMaxLength) {
		int inLength = in.length();
		if (inLength > length) {
			if (limitMaxLength) {
				return "<" + in.substring(inLength - (length - 1), inLength);
			}
			return in;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length - inLength; i++) {
			sb.append(pad);
		}
		sb.append(in);

		return sb.toString();
	}

	/**
	 * <p>
	 * Convert a byte to hex value with leading zeroes added. The output will
	 * always be 2 characters long. The output is the binary representation of
	 * the digit.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static String toHex(byte digit) {
		return String.valueOf(new char[] { HexTools.digits[digit >> 4 & 0x0F], HexTools.digits[digit & 0x0F] });
	}

	/**
	 * <p>
	 * Convert a char to hex value with leading zeroes added. The output will
	 * always be 4 characters long. The output is the binary representation of
	 * the digit.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static String toHex(char digit) {
		return String.valueOf(new char[] { HexTools.digits[digit >> 12 & 0x0F], HexTools.digits[digit >> 8 & 0x0F],
				HexTools.digits[digit >> 4 & 0x0F], HexTools.digits[digit & 0x0F] });
	}

	/**
	 * <p>
	 * Convert a char to hex value with leading zeroes added. If isSpaced is
	 * true, the output is grouped with a space character between each byte and
	 * will always be 5 characters long. The output is the binary representation
	 * of the digit.
	 * </p>
	 * 
	 * @param digit
	 * @param isSpaced
	 * @return
	 */
	public static String toHex(char digit, boolean isSpaced) {
		if (!isSpaced) {
			return HexTools.toHex(digit);
		}
		return String.valueOf(new char[] { HexTools.digits[digit >> 12 & 0x0F], HexTools.digits[digit >> 8 & 0x0F],
				' ', HexTools.digits[digit >> 4 & 0x0F], HexTools.digits[digit & 0x0F] });
	}

	/**
	 * <p>
	 * Convert a short to hex value with leading zeroes added. The output will
	 * always be 4 characters long. The output is the binary representation of
	 * the digit.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static String toHex(short digit) {
		return String.valueOf(new char[] { HexTools.digits[digit >> 12 & 0x0F], HexTools.digits[digit >> 8 & 0x0F],
				HexTools.digits[digit >> 4 & 0x0F], HexTools.digits[digit & 0x0F] });
	}

	/**
	 * <p>
	 * Convert a short to hex value with leading zeroes added. If isSpaced is
	 * true, the output is grouped with a space character between each byte and
	 * will always be 5 characters long. The output is the binary representation
	 * of the digit.
	 * </p>
	 * 
	 * @param digit
	 * @param isSpaced
	 * @return
	 */
	public static String toHex(short digit, boolean isSpaced) {
		if (!isSpaced) {
			return HexTools.toHex(digit);
		}
		return String.valueOf(new char[] { HexTools.digits[digit >> 12 & 0x0F], HexTools.digits[digit >> 8 & 0x0F],
				' ', HexTools.digits[digit >> 4 & 0x0F], HexTools.digits[digit & 0x0F] });
	}

	/**
	 * <p>
	 * Convert an int to hex value with leading zeroes added. The output will
	 * always be 8 characters long. The output is the binary representation of
	 * the digit.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static String toHex(int digit) {
		return HexTools.toHex((short) (digit >> 16 & 0xFFFF)) + HexTools.toHex((short) (digit & 0xFFFF));
	}

	/**
	 * <p>
	 * Convert an int to hex value with leading zeroes added. If isSpaced is
	 * true, the output is grouped with a space character between each byte and
	 * will always be 11 characters long. The output is the binary
	 * representation of the digit.
	 * </p>
	 * 
	 * @param digit
	 * @param isSpaced
	 * @return
	 */
	public static String toHex(int digit, boolean isSpaced) {
		if (!isSpaced) {
			return HexTools.toHex(digit);
		}
		return HexTools.toHex((short) (digit >> 16 & 0xFFFF), true) + ' '
				+ HexTools.toHex((short) (digit & 0xFFFF), true);
	}

	/**
	 * <p>
	 * Convert a long to hex value with leading zeroes added. The output will
	 * always be 16 characters long. The output is the binary representation of
	 * the digit.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static String toHex(long digit) {
		return HexTools.toHex((short) (digit >> 48 & 0xFFFF)) + HexTools.toHex((short) (digit >> 32 & 0xFFFF))
				+ HexTools.toHex((short) (digit >> 16 & 0xFFFF)) + HexTools.toHex((short) (digit & 0xFFFF));
	}

	/**
	 * <p>
	 * Convert a long to hex value with leading zeroes added. If isSpaced is
	 * true, the output is grouped with a space character between each byte and
	 * will always be 23 characters long. The output is the binary
	 * representation of the digit.
	 * </p>
	 * 
	 * @param digit
	 * @param isSpaced
	 * @return
	 */
	public static String toHex(long digit, boolean isSpaced) {
		if (!isSpaced) {
			return HexTools.toHex(digit);
		}
		return HexTools.toHex((short) (digit >> 48 & 0xFFFF), true) + ' '
				+ HexTools.toHex((short) (digit >> 32 & 0xFFFF), true) + ' '
				+ HexTools.toHex((short) (digit >> 16 & 0xFFFF), true) + ' '
				+ HexTools.toHex((short) (digit & 0xFFFF), true);
	}

	/**
	 * <p>
	 * Byte to bits string, with or without a space between the two nibbles
	 * (4-bits).
	 * </p>
	 * 
	 * @param digit
	 * @param isSpaced
	 * @return
	 */
	public static String toBits(byte digit, boolean isSpaced) {
		return toBits(digit, 8, SPACING_NIBBLE);
	}

	/**
	 * <p>
	 * Byte to bits string.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static String toBits(byte digit) {
		return toBits(digit, 8, SPACING_NONE);
	}

	/**
	 * <p>
	 * Arbitrary length integer value 1-64 bits to bits, with variable spacing
	 * interval.
	 * </p>
	 * 
	 * @param digit
	 * @param bits
	 * @param spacing
	 * @return
	 */
	public static String toBits(long digit, int bits, int spacing) {
		int shift = bits - 1;
		StringBuffer b = new StringBuffer();

		for (int i = 0; i < bits; i++) {
			if (spacing > SPACING_NONE) {
				if (i > 0 && (bits - i) % spacing == 0) {
					b.append(' ');
				}
			}
			b.append((digit >> (shift - i) & 0x01) == 0x01 ? '1' : '0');
		}
		return b.toString();
	}

	/**
	 * <p>
	 * Arbitrary length integer value (1-64 bits) to hex digits, with variable
	 * spacing interval. length of displayed bits is rounded up to nearest
	 * nibble. Spacing is counted in bits and is rounded up to nearest nibble.
	 * </p>
	 * 
	 * <p>
	 * Note: This method is considerably slower than the other toHex methods in
	 * this class.
	 * </p>
	 * 
	 * @param digit
	 * @param bits
	 * @param spacing
	 * @return
	 */
	public static String toHex(long digit, int bits, int spacing) {
		int nibs = (bits / 4);
		int shift = nibs - 1;
		spacing = (spacing / 4);

		StringBuffer b = new StringBuffer();

		for (int i = 0; i < nibs; i++) {
			if (spacing > SPACING_NONE) {
				if (i > 0 && (nibs - i) % spacing == 0) {
					b.append(' ');
				}
			}
			b.append((HexTools.digits[(int) (digit >> (shift - i) * 4) & 0x0F]));
		}
		return b.toString();
	}

	/**
	 * <p>
	 * Cast a byte to int, while treating the byte short as in unsigned value.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static int toUInt(byte digit) {
		return digit & 0xFF;
	}

	/**
	 * <p>
	 * Cast a short to int, while treating the short value as in unsigned short.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static int toUInt(short digit) {
		return digit & 0xFFFF;
	}

	/**
	 * <p>
	 * Cast a int to long, while treating the int value as in unsigned int.
	 * </p>
	 * 
	 * <p>
	 * Example, The integer -1 (0xFFFFFFFF) returns 4294967295 and not -1 as a
	 * normal typecast would.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static long toUInt(int digit) {
		return digit & 0xFFFFFFFFl;
	}

	/**
	 * <p>
	 * Pack a byte (range 0 to 99) to a BCD value with the 10's in the low
	 * nibble.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static byte byteToBcd(byte digit) {
		return (byte) (((digit / 10) | (digit % 10) << 4) & 0xFF);
	}

	/**
	 * <p>
	 * Unpack a BCD value where the 10's are in the low nibble to a byte.
	 * </p>
	 * 
	 * @param digit
	 * @return
	 */
	public static byte bcdToByte(byte digit) {
		return (byte) (((digit & 0x0F) * 10 + ((digit >> 4 & 0x0F))) & 0xFF);
	}
}
