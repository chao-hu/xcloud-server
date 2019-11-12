package com.xxx.xcloud.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Base64编解码的工具类. <BR>
 * [08.12.12]增加byte[] decodeBytes(String dest)支持将字符串直接解码成字节码
 *
 * @author anychem
 */
public class Base64Util {

    private static final String UTF8 = "UTF-8";
    private static Logger logger = LoggerFactory.getLogger(Base64Util.class);

    private Base64Util() {

    }

    private static String getSalt() {
        String[] validStr = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h",
                "i", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D",
                "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
                "Z" };
        int number = validStr.length;
        String salt = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            String rand = String.valueOf(validStr[random.nextInt(number)]);
            salt += rand;
        }
        return salt;
    }

    public static String encrypt(String origin) {
        // origin = origin + getSalt();
        return getSalt() + encode(origin);
    }

    public static String decrypt(String origin) {
        // String dest = decode(origin);
        // return dest.substring(0, dest.length() - 6);
        return decode(origin.substring(6, origin.length()));
    }

    /**
     * base64编码；字符串和字节间的转换使用UTF-8编码.
     *
     * @param origin
     *            待编码的字符串
     * @return 解码后的字符串. 如果origin参数为null, 则返回"".
     */
    public static String encode(String origin) {
        if (origin == null) {
            return "";
        }
        byte[] bytes = null;
        try {
            bytes = origin.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            logger.error("encode方法出问题", e);
        }
        // liushen@Aug 23, 2011: 已经都是Base64字符集了，用什么编码结果都一样，因此不再需要指定编码
        return new String(Base64.encodeBase64(bytes));
    }

    /**
     * base64解码；字符串和字节间的转换使用UTF-8编码.
     *
     * @param dest
     *            待解码的字符串
     * @return 解码得到的原始字符串
     */
    public static String decode(String dest) {
        if (dest == null) {
            return "";
        }
        // liushen@Aug 23, 2011: Base64编码后的串，用什么编码去获取字节序列结果都一样，因此不需要指定编码
        byte[] bytes = dest.getBytes();
        return decodeAndToUTF8String(bytes);
    }

    /**
     * base64编码.
     *
     * @param origin
     *            待编码的原字符串
     * @param charsetName
     *            原字符串的字符集编码
     * @return 解码后的字符串. 如果origin参数为null, 则返回"".
     */
    public static String encode(String origin, String charsetName) {
        if (origin == null) {
            return "";
        }
        String base64Str = "";
        try {
            base64Str = new String(Base64.encodeBase64(origin.getBytes(charsetName)));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode方法出问题", e);
        }
        return base64Str;
    }

    /**
     * base64解码.
     *
     * @param base64Str
     *            待解码的字符串
     * @param charsetName
     *            原字符串的字符集编码
     * @return 解码得到的原始字符串
     */
    public static String decode(String base64Str, String charsetName) {
        if (StringUtils.isEmpty(base64Str)) {
            return "";
        }
        if (StringUtils.isEmpty(charsetName)) {
            return decode(base64Str);
        }
        String originStr = "";
        try {
            originStr = new String(Base64.decodeBase64(base64Str.getBytes()), charsetName);
        } catch (UnsupportedEncodingException e) {
            logger.error("decode方法出问题", e);
        }
        return originStr;
    }

    /**
     * Base64解码
     *
     * @param dest
     *            待解码的字符串
     * @return 字节码
     */
    public static byte[] decodeBytes(String dest) {
        if (dest == null) {
            return new byte[0];
        }
        return Base64.decodeBase64(dest.getBytes());
    }

    static String decodeAndToUTF8String(byte[] encoded) {
        try {
            return new String(Base64.decodeBase64(encoded), UTF8);
        } catch (UnsupportedEncodingException e) {
            logger.error("decodeAndToUTF8String方法出问题", e);
            return null;
        }
    }

    static InputStream encode(InputStream inputStream) {
        // [liushen@2005-02-06] 未测试.
        try {
            int bytesToRead = inputStream.available(); // 对文件流可靠, 对网络流不可靠.
            byte[] input = new byte[bytesToRead];
            int bytesRead = 0;
            while (bytesRead < bytesToRead) {
                int actualRead = inputStream.read(input, bytesRead, bytesToRead - bytesRead);
                if (actualRead == -1) {
                    break;
                }
                bytesRead += actualRead;
            }
            String encodeBytes = encode(input);
            return new ByteArrayInputStream(encodeBytes.getBytes());
        } catch (IOException e) {
            logger.error("错误信息:" + e);
        }
        return new ByteArrayInputStream("".getBytes()); // 返回empty的流
    }

    public static String encode(byte[] data) {
        return new String(Base64.encodeBase64(data));
    }

    /**
     * 将二进制字节数组编码成为base64数组.
     *
     * @since ls@08.0106
     */
    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        return Base64.encodeBase64(binaryData, isChunked);
    }

    /**
     * 将base64数组解码成为二进制字节数组.
     *
     * @since ls@08.0106
     */
    public static byte[] decodeBase64(byte[] base64Data) {
        return Base64.decodeBase64(base64Data);
    }

    /**
     * 判断是否是Base64编码
     *
     * Base64编码规则: 1)字串只由A～Z a~z 0~9 + / = 构成。 2)字串长度是4的倍数。 3) = 只出现在字串尾端，最多2个。
     *
     * @param strEncoded
     * @return
     * @since v3.5
     * @creator shixin @ 2010-4-5
     */
    public static boolean isBase64Encoded(String strEncoded, String charsetName) {
        // 长度不是4的倍数，返回false
        if (strEncoded.length() % 4 != 0) {
            return false;
        }
        try {
            byte[] base64Data = strEncoded.getBytes(charsetName);
            if (Base64.isArrayByteBase64(base64Data)) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("错误信息:" + e);
        }
        return false;
    }
}

/**
 * Provides Base64 encoding and decoding as defined by RFC 2045.
 *
 * <p>
 * This class implements section <cite>6.8. Base64
 * Content-Transfer-Encoding</cite> from RFC 2045 <cite>Multipurpose Internet
 * Mail Extensions (MIME) Part One: Format of Internet Message Bodies</cite> by
 * Freed and Borenstein.
 * </p>
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
 * @author Apache Software Foundation
 * @since 1.0-dev
 * @version v 1.20 2004/05/24 00:21:24 ggregory
 */
class Base64 {

    /**
     * Chunk size per RFC 2045 section 6.8.
     *
     * <p>
     * The {@value} character limit does not count the trailing CRLF, but counts
     * all other characters, including any equal signs.
     * </p>
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section
     *      6.8</a>
     */
    static final int CHUNK_SIZE = 76;

    /**
     * Chunk separator per RFC 2045 section 2.1.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section
     *      2.1</a>
     */
    private static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();

    /**
     * The base length.
     */
    static final int BASELENGTH = 255;

    /**
     * Lookup length.
     */
    static final int LOOKUPLENGTH = 64;

    /**
     * Used to calculate the number of bits in a byte.
     */
    static final int EIGHTBIT = 8;

    /**
     * Used when encoding something which has fewer than 24 bits.
     */
    static final int SIXTEENBIT = 16;

    /**
     * Used to determine how many bits data contains.
     */
    static final int TWENTYFOURBITGROUP = 24;

    /**
     * Used to get the number of Quadruples.
     */
    static final int FOURBYTE = 4;

    /**
     * Used to test the sign of a byte.
     */
    static final int SIGN = -128;

    /**
     * Byte used to pad output.
     */
    static final byte PAD = (byte) '=';

    // Create arrays to hold the base64 characters and a
    // lookup for base64 chars
    private static byte[] base64Alphabet = new byte[BASELENGTH];
    private static byte[] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

    // Populating the lookup and character arrays
    static {
        for (int i = 0; i < BASELENGTH; i++) {
            base64Alphabet[i] = (byte) -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            base64Alphabet[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i >= 'a'; i--) {
            base64Alphabet[i] = (byte) (i - 'a' + 26);
        }
        for (int i = '9'; i >= '0'; i--) {
            base64Alphabet[i] = (byte) (i - '0' + 52);
        }

        base64Alphabet['+'] = 62;
        base64Alphabet['/'] = 63;

        for (int i = 0; i <= 25; i++) {
            lookUpBase64Alphabet[i] = (byte) ('A' + i);
        }

        for (int i = 26, j = 0; i <= 51; i++, j++) {
            lookUpBase64Alphabet[i] = (byte) ('a' + j);
        }

        for (int i = 52, j = 0; i <= 61; i++, j++) {
            lookUpBase64Alphabet[i] = (byte) ('0' + j);
        }

        lookUpBase64Alphabet[62] = (byte) '+';
        lookUpBase64Alphabet[63] = (byte) '/';
    }

    private static boolean isBase64(byte octect) {
        if (octect == PAD) {
            return true;
        } else if (base64Alphabet[octect] == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Tests a given byte array to see if it contains only valid characters
     * within the Base64 alphabet.
     *
     * @param arrayOctect
     *            byte array to test
     * @return true if all bytes are valid characters in the Base64 alphabet or
     *         if the byte array is empty; false, otherwise
     */
    public static boolean isArrayByteBase64(byte[] arrayOctect) {

        byte[] arrayObject;
        arrayObject = discardWhitespace(arrayOctect);

        int length = arrayObject.length;
        if (length == 0) {

            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!isBase64(arrayObject[i])) {

                return false;
            }
        }
        return true;
    }

    /**
     * Encodes binary data using the base64 algorithm but does not chunk the
     * output.
     *
     * @param binaryData
     *            binary data to encode
     * @return Base64 characters
     */
    public static byte[] encodeBase64(byte[] binaryData) {
        return encodeBase64(binaryData, false);
    }

    /**
     * Encodes binary data using the base64 algorithm and chunks the encoded
     * output into 76 character blocks
     *
     * @param binaryData
     *            binary data to encode
     * @return Base64 characters chunked in 76 character blocks
     */
    public static byte[] encodeBase64Chunked(byte[] binaryData) {
        return encodeBase64(binaryData, true);
    }

    /**
     * Decodes a byte[] containing containing characters in the Base64 alphabet.
     *
     * @param pArray
     *            A byte array containing Base64 character data
     * @return a byte array containing binary data
     */
    public byte[] decode(byte[] pArray) {
        return decodeBase64(pArray);
    }

    /**
     * Encodes binary data using the base64 algorithm, optionally chunking the
     * output into 76 character blocks.
     *
     * @param binaryData
     *            Array containing binary data to encode.
     * @param isChunked
     *            if isChunked is true this encoder will chunk the base64 output
     *            into 76 character blocks
     * @return Base64-encoded data.
     */
    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        int lengthDataBits = binaryData.length * EIGHTBIT;
        int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
        int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;

        int encodedDataLength;
        int nbrChunks = 0;
        byte[] encodedData;
        if (fewerThan24bits != 0) {
            // data not divisible by 24 bit
            encodedDataLength = (numberTriplets + 1) * 4;
        } else {
            // 16 or 8 bit
            encodedDataLength = numberTriplets * 4;
        }

        // If the output is to be "chunked" into 76 character sections,
        // for compliance with RFC 2045 MIME, then it is important to
        // allow for extra length to account for the separator(s)
        if (isChunked) {

            nbrChunks = CHUNK_SEPARATOR.length == 0 ? 0 : (int) Math.ceil((float) encodedDataLength / CHUNK_SIZE);
            encodedDataLength += nbrChunks * CHUNK_SEPARATOR.length;
        }

        encodedData = new byte[encodedDataLength];

        byte k;
        byte l;
        byte b1;
        byte b2;
        byte b3;

        int encodedIndex = 0;
        int dataIndex;
        int i;
        int nextSeparatorIndex = CHUNK_SIZE;
        int chunksSoFar = 0;

        for (i = 0; i < numberTriplets; i++) {
            dataIndex = i * 3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
            byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);

            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2 & 0xff | (k << 4)];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[(l << 2) | val3 & 0xff];
            encodedData[encodedIndex + 3] = lookUpBase64Alphabet[b3 & 0x3f];

            encodedIndex += 4;

            // If we are chunking, let's put a chunk separator down. this
            // assumes that CHUNK_SIZE % 4 == 0
            if (isChunked && encodedIndex == nextSeparatorIndex) {

                System.arraycopy(CHUNK_SEPARATOR, 0, encodedData, encodedIndex, CHUNK_SEPARATOR.length);
                chunksSoFar++;
                nextSeparatorIndex = (CHUNK_SIZE * (chunksSoFar + 1)) + (chunksSoFar * CHUNK_SEPARATOR.length);
                encodedIndex += CHUNK_SEPARATOR.length;

            }
        }

        // form integral number of 6-bit groups
        dataIndex = i * 3;

        if (fewerThan24bits == EIGHTBIT) {
            b1 = binaryData[dataIndex];
            k = (byte) (b1 & 0x03);
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[k << 4];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if (fewerThan24bits == SIXTEENBIT) {

            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);

            encodedData[encodedIndex] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[val2 & 0xff | (k << 4)];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[l << 2];
            encodedData[encodedIndex + 3] = PAD;
        }

        if (isChunked && (chunksSoFar < nbrChunks)) {
            // we also add a separator to the end of the final chunk.

            System.arraycopy(CHUNK_SEPARATOR, 0, encodedData, encodedDataLength - CHUNK_SEPARATOR.length,
                    CHUNK_SEPARATOR.length);

        }

        return encodedData;
    }

    /**
     * Decodes Base64 data into octets
     *
     * @param base64Datas
     *            Byte array containing Base64 data
     * @return Array containing decoded data.
     */
    public static byte[] decodeBase64(byte[] base64Datas) {
        // RFC 2045 requires that we discard ALL non-Base64 characters
        byte[] base64Data;
        base64Data = discardNonBase64(base64Datas);

        // handle the edge case, so we don't have to worry about it later
        if (base64Data.length == 0) {
            return new byte[0];
        }

        int numberQuadruple = base64Data.length / FOURBYTE;
        byte[] decodedData;
        byte b1;
        byte b2;
        byte b3;
        byte b4;
        byte marker0;
        byte marker1;

        // Throw away anything not in base64Data

        int encodedIndex = 0;
        int dataIndex;

        decodedData = getDecodedData(base64Data, numberQuadruple);

        for (int i = 0; i < numberQuadruple; i++) {
            dataIndex = i * 4;
            marker0 = base64Data[dataIndex + 2];
            marker1 = base64Data[dataIndex + 3];

            b1 = base64Alphabet[base64Data[dataIndex]];
            b2 = base64Alphabet[base64Data[dataIndex + 1]];

            if (marker0 == PAD) {
                // Two PAD e.g. 3c[Pad][Pad]
                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
            } else if (marker1 == PAD) {// One PAD e.g. 3cQ[Pad]
                b3 = base64Alphabet[marker0];

                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
            }

            if (marker0 != PAD && marker1 != PAD) {
                // No PAD e.g 3cQl
                b3 = base64Alphabet[marker0];
                b4 = base64Alphabet[marker1];

                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4 & 0xff);
            }

            encodedIndex += 3;
        }
        return decodedData;
    }

    private static byte[] getDecodedData(byte[] base64Data, int numberQuadruple) {
        byte[] decodedData;
        // this sizes the output array properly - rlw
        int lastData = base64Data.length;
        // ignore the '=' padding
        while (base64Data[lastData - 1] == PAD) {
            if (--lastData == 0) {
                return new byte[0];
            }
        }
        decodedData = new byte[lastData - numberQuadruple];
        return decodedData;
    }

    /**
     * Discards any whitespace from a base-64 encoded block.
     *
     * @param data
     *            The base-64 encoded data to discard the whitespace from.
     * @return The data, less whitespace (see RFC 2045).
     */
    static byte[] discardWhitespace(byte[] data) {
        byte[] groomedData = new byte[data.length];
        int bytesCopied = 0;

        for (byte element : data) {
            switch (element) {
            case (byte) ' ':
            case (byte) '\n':
            case (byte) '\r':
            case (byte) '\t':
                break;
            default:
                groomedData[bytesCopied++] = element;
            }
        }

        byte[] packedData = new byte[bytesCopied];

        System.arraycopy(groomedData, 0, packedData, 0, bytesCopied);

        return packedData;
    }

    /**
     * Discards any characters outside of the base64 alphabet, per the
     * requirements on page 25 of RFC 2045 - "Any characters outside of the
     * base64 alphabet are to be ignored in base64 encoded data."
     *
     * @param data
     *            The base-64 encoded data to groom
     * @return The data, less non-base64 characters (see RFC 2045).
     */
    static byte[] discardNonBase64(byte[] data) {
        byte[] groomedData = new byte[data.length];
        int bytesCopied = 0;

        for (byte element : data) {
            if (isBase64(element)) {
                groomedData[bytesCopied++] = element;
            }
        }

        byte[] packedData = new byte[bytesCopied];

        System.arraycopy(groomedData, 0, packedData, 0, bytesCopied);

        return packedData;
    }

    // Implementation of the Encoder Interface

    /**
     * Encodes a byte[] containing binary data, into a byte[] containing
     * characters in the Base64 alphabet.
     *
     * @param pArray
     *            a byte array containing binary data
     * @return A byte array containing only Base64 character data
     */
    public byte[] encode(byte[] pArray) {
        return encodeBase64(pArray, false);
    }

}
