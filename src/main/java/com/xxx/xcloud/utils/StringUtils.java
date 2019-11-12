/*
 * Created: liushen@Mar 10, 2009 4:16:11 PM
 */
package com.xxx.xcloud.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 字符串处理的工具类, 由于历史原因该类以Helper命名, 而不是以Util命名. <BR>
 *
 * @author anychem
 */
public class StringUtils {

    private static Logger logger = LoggerFactory.getLogger(StringUtils.class);

    private static Pattern NUMBER_PATTERN = Pattern.compile("^[-|+]?(\\d+)(\\.\\d+)?");
    private static Pattern INTEGER_PATTERN = Pattern.compile("^[-|+]?[1-9][0-9]*");
    private static Pattern POSITIVEINTEGER_PATTERN = Pattern.compile("^[1-9][0-9]*");
    private static Pattern PHONE_PATTERN = Pattern
            .compile("^(((\\+|0)[0-9]{2,3})-)?((0[0-9]{2,3})-)?([0-9]{8})(-([0-9]{3,4}))?$");
    private static Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@(\\w+\\.)+[a-zA-Z]{2,3}");

    private static Pattern DOMAIN_PATTERN = Pattern
            .compile("[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+");

    private static Pattern IP_PATTERN = Pattern.compile(
            "((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)(\\.((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)){3}");

    private static Pattern IPV4_PATTERN = Pattern.compile(
            "((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)(\\.((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)){3}");

    private static Pattern HTTPURL_PATTERN = Pattern
            .compile("http://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(:\\d{2,4})?(\\/[^#$]+)*");

    private static Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,15}$");

    private static Pattern URL_PATTERN = Pattern.compile("^[a-zA-z]+://[^\\s]+");

    private static Pattern RTMP_PATTERN = Pattern
            .compile("rtmp://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(:\\d{2,4})?(\\/[^#$]+)*");

    private static Pattern QQ_PATTERN = Pattern.compile("^[1-9]\\d{4,8}$");
    
    private static final String TRUE = "true";
    
    private static final String FALSE = "false";
    
    private static final char QUOTATION_MARK = '"';
    
    private static final char EMPTY_CHAR = ' ';
    
    private static final String SLASH = "/";
    
    private static final String LEFT_BRACKET = "{";
    
    private static final String RIGHT_BRACKET = "}";

    /**
     * 常量，值为空串<code>""</code>.
     */
    public static final String EMPTY = "";

    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    private StringUtils() {
    }

    /**
     * 将指定字符串的指定位置的字符以byte方式返回. 该方法中对返回的字符有一次ascii转换, 即减去一个'0'的ascii值(十进制的48).
     *
     * @param strHead
     *            指定字符串
     * @param index
     *            指定位置
     * @return 指定位置的字符的byte值
     */
    public static byte byteAt(String strHead, int index) {
        byte result = (byte) strHead.charAt(index);
        result -= '0';
        return result;
    }

    /**
     * 转换html标签(<,>)
     *
     * @param src
     * @return
     * @since TRS @ 2012-1-19
     */
    public static String escape(String src) {
        String str = src.replaceAll("<", "&lt;");

        return str.replaceAll(">", "&gt;");
    }

    /**
     * 将给定字符串(origin)以字符串token作为分隔符进行分隔, 得到一个字符串数组. 该函数不依赖于JDK 1.4, 和JDK
     * 1.4中String.split(String regex)的区别是不支持正则表达式.<br>
     * 在不包含有token字符串时, 本函数返回以原字符串构成的数组.
     *
     * @param origin
     *            给定字符串
     * @param token
     *            分隔符
     * @return 字符串数组
     */
    public static String[] split(String origin, String token) {
        if (origin == null) {
            return new String[0];
        }
        StringTokenizer st = (token == null) ? new StringTokenizer(origin) : new StringTokenizer(origin, token);
        final int countTokens = st.countTokens();
        if (countTokens <= 0) {
            return new String[] { origin };
        }
        String[] results = new String[countTokens];
        for (int i = 0; i < countTokens; i++) {
            results[i] = st.nextToken();
        }
        return results;
    }

    /**
     * 得到给定字符串的逆序的字符串. <BR>
     * 用例: <code>
     * <pre>
     *         assertEquals("/cba", StringHelper.reverse("abc/"));
     *         assertEquals("aabbbccccx", StringHelper.reverse("xccccbbbaa"));
     * 		   assertEquals("试测^%6cbA数参", StringHelper.reverse("参数Abc6%^测试"));
     * </pre>
     * </code>
     */
    public static String reverse(String origin) {
        if (origin == null) {
            return null;
        }
        return new StringBuilder(origin).reverse().toString();
    }

    /**
     * 用ISO-8859-1对给定字符串编码.
     *
     * @param string
     *            给定字符串
     * @return 编码后所得的字符串. 如果给定字符串为null或"", 则原样返回.
     */
    public static String encodingByISO88591(String string) {
        if ((string != null) && !("".equals(string))) {
            try {
                return new String(string.getBytes(), "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                logger.error("字符串编码 : " + e.getMessage(), e);
                return string;
            }
        }
        return string;
    }

    // from commons-codec: char[] Hex.encodeHex(byte[])
    /**
     * 将给定的字节数组用十六进制字符串表示.
     */
    public static String toString(byte[] data) {
        if (data == null) {
            return "null!";
        }
        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return new String(out);
    }

    // from commons-codec: byte[] Hex.decodeHex(char[])
    /**
     * 将给定的十六进制字符串转化为字节数组. <BR>
     * 与<code>toString(byte[] data)</code>作用相反.
     *
     * @throws RuntimeException
     *             当给定十六进制字符串的长度为奇数时或给定字符串包含非十六进制字符.
     * @see #toString(byte[])
     */
    public static byte[] toBytes(String str) {
        if (str == null) {
            return new byte[0];
        }
        char[] data = str.toCharArray();
        int len = data.length;

        if ((len & 1) != 0) {
            return new byte[0];
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j]) << 4;
            j++;
            f = f | toDigit(data[j]);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    // [liushen@2005-04-21] from caohui
    /**
     * HTML元素value值过滤处理函数：将 <code> & &lt; &gt; &quot </code> 等特殊字符作转化处理. <BR>
     * 用例: <code>
     *    &lt;input type="text" name="Name" value="<%=StringHelper.filterForHTMLValue(sContent)%>"&gt;
     * </code>
     *
     * @param sContent
     *            指定的文本内容. 如果为null则返回"".
     * @return 处理后的文本内容.
     */
    public static String filterForHTMLValue(String sContent) {
        if (sContent == null) {
            return "";
        }

        char[] srcBuff = sContent.toCharArray();
        int nLen = srcBuff.length;
        if (nLen == 0) {
            return "";
        }

        StringBuilder retBuff = new StringBuilder((int) (nLen * 1.8));

        for (int i = 0; i < nLen; i++) {
            char cTemp = srcBuff[i];
            switch (cTemp) {
            case '&': 
                buildRetBuff(i, nLen, srcBuff, retBuff);
                break;
            case '<':
                retBuff.append("&lt;");
                break;
            case '>':
                retBuff.append("&gt;");
                break;
            case '\"':
                retBuff.append("&quot;");
                break;
            default:
                retBuff.append(cTemp);
            }// case
        } // end for

        return retBuff.toString();
    }

    private static void buildRetBuff(int i, int nLen, char[] srcBuff, StringBuilder retBuff) {
        if ((i + 1) < nLen) {
            boolean cTempFlag = srcBuff[i + 1] == '#' ? true :false;
            if (cTempFlag) {
                retBuff.append("&");
            } else {
                retBuff.append("&amp;");
            }
        } else {
            retBuff.append("&amp;");
        }
    }

    /**
     * 等价于<code>toString(objs, false, ",");</code>.
     */
    public static String toString(Object[] objs) {
        return toString(objs, false, ", ");
    }

    /**
     * 等价于<code>toString(objs, showOrder, ",");</code>.
     *
     * @see #toString(Object[], boolean, String)
     */
    public static String toString(Object[] objs, boolean showOrder) {
        return toString(objs, showOrder, ",");
    }

    /**
     * 输出数组内容. 如果数组为null, 返回null. 如果数组某元素为null则该元素输出为null.
     *
     * @param objs
     *            待输出的数组
     * @param showOrder
     *            是否输出元素的序号
     * @param token
     *            元素间的分割串
     */
    public static String toString(Object[] objs, boolean showOrder, String token) {
        if (objs == null) {
            return "null";
        }
        int len = objs.length;
        StringBuilder sb = new StringBuilder(10 * len);
        for (int i = 0; i < len; i++) {
            if (showOrder) {
                sb.append(i).append(':');
            }
            sb.append(objs[i]);
            if (i < len - 1) {
                sb.append(token);
            }
        }
        return sb.toString();
    }

    public static String avoidNull(String str) {
        return (str == null) ? "" : ("null".equalsIgnoreCase(str) ? "" : str);
    }

    /**
     *
     * @param str
     * @return
     * @since liushen @ Oct 12, 2010
     */
    public static String assertNotEmptyAndTrim(String str) {
        AssertUtil.notNullOrEmpty(str, "the string is empty!");
        return str.trim();
    }

    /**
     * 为空或null时返回缺省值
     *
     * @param str
     * @param defaultStr
     * @return
     * @since liuyou @ 2010-4-19
     * @see #isEmpty(String)
     */
    public static String avoidEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 以","为分隔符连接数组为一个串
     *
     * @param array
     * @return
     * @since liuyou @ 2010-4-19
     * @see StringHelper.toString(Object[] array)
     */
    public static String join(int[] array, String separator) {
        if (array == null) {
            return null;
        }
        String delimiter = separator == null ? EMPTY : separator;

        StringBuilder buf = new StringBuilder(200);

        if (array.length > 0) {
            buf.append(array[0]);
        }
        for (int i = 1; i < array.length; i++) {
            buf.append(delimiter);
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * 以","为分隔符连接数组为一个串
     *
     * @param array
     * @return
     * @since liuyou @ 2010-4-19
     * @see StringHelper.toString(Object[] array)
     */
    public static String join(Object[] array) {
        return join(array, ",");
    }

    /**
     * 指定分隔符用来连接数组为一个串
     *
     * @param array
     * @param separator
     * @return
     * @since liuyou @ 2010-4-19
     */
    public static String join(Object[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * 指定分隔符用来连接数组为一个串
     *
     * @param array
     * @param separator
     * @return
     * @since liuyou @ 2010-4-19
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing
     * the provided list of elements.
     * </p>
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty
     * strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * join(null, *)               = null
     * join([], *)                 = ""
     * join([null], *)             = ""
     * join(["a", "b", "c"], ';')  = "a;b;c"
     * join(["a", "b", "c"], null) = "abc"
     * join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array
     *            the array of values to join together, may be null
     * @param separator
     *            the separator character to use
     * @param startIndex
     *            the first index to start joining from. It is an error to pass
     *            in an end index past the end of the array
     * @param endIndex
     *            the index to stop joining from (exclusive). It is an error to
     *            pass in an end index past the end of the array
     * @return the joined String, <code>null</code> if null array input
     * @see Apache Commons Lang
     */
    static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = endIndex - startIndex;
        if (bufSize <= 0) {
            return EMPTY;
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }

        String delimiter = separator == null ? EMPTY : separator;

        // endIndex - startIndex > 0: Len = NofStrings *(len(firstString) +
        // len(separator))
        // (Assuming that all Strings are roughly equally long)
        int bufSize = endIndex - startIndex;
        if (bufSize <= 0) {
            return EMPTY;
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + delimiter.length());

        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(delimiter);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * Capitalize a <code>String</code>, changing the first letter to upper case
     * as per {@link Character#toUpperCase(char)}. No other letters are changed.
     *
     * @param str
     *            the String to capitalize, may be <code>null</code>
     * @return the capitalized String, <code>null</code> if null
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * Uncapitalize a <code>String</code>, changing the first letter to lower
     * case as per {@link Character#toLowerCase(char)}. No other letters are
     * changed.
     *
     * @param str
     *            the String to uncapitalize, may be <code>null</code>
     * @return the uncapitalized String, <code>null</code> if null
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (str == null || str.length() == 0) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str.length());
        if (capitalize) {
            buf.append(Character.toUpperCase(str.charAt(0)));
        } else {
            buf.append(Character.toLowerCase(str.charAt(0)));
        }
        buf.append(str.substring(1));
        return buf.toString();
    }

    /**
     * 去除字符串首尾的空白字符。
     *
     * @param str
     *            原字符串
     * @return 处理后的字符串；如果原字符串为<code>null</code>则也返回<code>null</code>.
     */
    public static String trim(String str) {
        return (str == null) ? null : str.trim();
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch
     *            A character to convert to an integer digit
     * @param index
     *            The index of the character in the source
     * @return An integer
     * @throws RuntimeException
     *             Thrown if ch is an illegal hex character
     */
    static int toDigit(char ch) {

        return Character.digit(ch, 16);
    }

    /**
     * 按给定分割符对给定字符串作分割, 然后作trim处理.<BR>
     * <li>origin为null时返回null.
     * <li>不包含有token字符串时, 本函数返回以原字符串trim后构成的数组.
     *
     * @param origin
     *            给定字符串
     * @param token
     *            分隔符. 不允许为null.
     * @return 字符串数组
     */
    public static String[] splitAndTrim(String origin, String token) {
        if (origin == null) {
            return new String[0];
        }
        String originStr = origin.trim();
        final StringTokenizer st = new StringTokenizer(originStr, token);
        final int countTokens = st.countTokens();
        if (countTokens <= 0) {
            return new String[] { originStr };
        }
        List<String> strs = new ArrayList<>(countTokens);
        String str;
        for (int i = 0; i < countTokens; i++) {
            str = st.nextToken().trim();
            if (str.length() > 0) {
                strs.add(str);
            }
        }
        return strs.toArray(new String[0]);
    }

    public static String hexToStr(String hex) {
        return new String(toBytes(hex));
    }

    /**
     * 见单元测试(暂时在MAS工程下).
     */
    public static String truncateAndTrim(String str, String delim) {
        if (str == null || delim == null) {
            return str;
        }
        int nStart = str.indexOf(delim);
        if (nStart < 0) {
            return str;
        }
        return str.substring(nStart + delim.length()).trim();
    }

    /**
     * 获得字符串指定编码的字符串，默认的原始编码为ISO8859-1。
     *
     * @param originalStr
     *            原始的串
     * @param encoding
     *            目标编码
     * @return 编码后的字符串
     */
    public static String getStringByEncoding(String originalStr, String encoding) {
        return getStringByEncoding(originalStr, "ISO8859-1", encoding);
    }

    /**
     * 获得字符串指定编码的字符串.
     *
     * @param str
     *            原始的串
     * @param fromEncoding
     *            原始的编码
     * @param toEncoding
     *            目标编码
     * @return 编码后的字符串。如果原始串为<code>null</code>，则也返回<code>null</code>.
     */
    public static String getStringByEncoding(String str, String fromEncoding, String toEncoding) {
        if (str == null) {
            return null;
        }
        if (StringUtils.isEmpty(fromEncoding)) {
            return str;
        }
        try {
            return new String(str.getBytes(fromEncoding), toEncoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("获取编码字符串错误：" + e.getMessage(), e);

            return str;
        }
    }

    /**
     * 判断字符串是否为null或空.
     *
     * <pre>
     * isEmpty(null)      = true
     * isEmpty(&quot;&quot;)        = true
     * isEmpty(&quot; &quot;)       = true
     * isEmpty(&quot;bob&quot;)     = false
     * isEmpty(&quot;  bob  &quot;) = false
     * </pre>
     *
     * @return true if <code>(str == null || str.trim().length() == 0)</code>,
     *         otherwise false.
     * @since ls@07.0624
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 将字符串调整为不超过maxLength长度的字符串, 调整方法为截掉末尾适当长度, 并且以...结束.
     *
     * @param str
     * @param maxLength
     * @return
     * @since liushen @ Jun 8, 2011
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return str;
        }
        if (maxLength <= 0) {
            throw new IllegalArgumentException(
                    "Illegal value of maxLength: " + maxLength + "! It must be a positive integer.");
        }
        int strLength = str.length();
        if (maxLength >= strLength) {
            return str;
        }
        final String delit = "...";
        StringBuilder sb = new StringBuilder(maxLength);
        int splitPos = maxLength - delit.length();
        sb.append(str.substring(0, splitPos));
        sb.append(delit);
        return sb.toString();
    }

    /**
     * 将字符串调整为不超过maxLength长度的字符串, 调整方法为去掉中间的适当长度, 以...区分开头和结束.
     *
     * @param maxLength
     *            指定的长度.
     * @since ls@07.1227
     * @see StringHelperTest#testAdjustLength()
     */
    public static String adjustLength(String str, int maxLength) {
        if (str == null) {
            return str;
        }
        if (maxLength <= 0) {
            throw new IllegalArgumentException(
                    "Illegal value of maxLength: " + maxLength + "! It must be a positive integer.");
        }
        int strLength = str.length();
        if (maxLength > strLength) {
            return str;
        }
        final String delit = "...";
        StringBuilder sb = new StringBuilder(maxLength);
        int splitPos = (maxLength - delit.length()) / 2;
        sb.append(str.substring(0, splitPos));
        sb.append(delit);
        sb.append(str.substring(strLength - splitPos));
        return sb.toString();
    }

    /**
     * 获得安全的URL，避免跨站式攻击 处理方法同
     * {@link RequestUtil#getParameterSafe(javax.servlet.http.HttpServletRequest, String)}
     * ynj@2008-11-03
     *
     * @return
     */
    public static String getURLSafe(String url) {
        if (url == null || "".equals(url)) {
            return "";
        }

        StringBuilder strBuff = new StringBuilder();
        char[] charArray = url.toCharArray();
        for (char element : charArray) {
            if (element == '<' || element == '>') {
                continue;
            }

            strBuff.append(element);
        }
        return strBuff.toString();
    }

    /**
     * 去掉末尾的/.
     *
     * @param str
     * @return
     */
    public static String removeLastSlashChar(String str) {
        if (str == null) {
            return null;
        }
        if (str.endsWith(SLASH)) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * 在末尾添加/, 如果不是以/结尾的话.
     *
     * @param str
     *            原字符串
     * @see #smartAppendSuffix(String, String)
     * @see #removeLastSlashChar(String)
     */
    public static String smartAppendSlashToEnd(String str) {
        return smartAppendSuffix(str, "/");
    }

    /**
     * 在原字符串末尾添加给定的后缀, 如果不是以该后缀结尾的话.
     *
     * @param str
     *            原字符串
     * @param endingStr
     *            后缀(用来结束的字符或字符串)
     * @return 原字符串(如果原字符串为<code>null</code>或已经以该后缀结束)；或者原字符串加上该后缀
     * @since liushen @ Jun 21, 2010
     */
    public static String smartAppendSuffix(String str, String endingStr) {
        if (str == null) {
            return null;
        }
        return str.endsWith(endingStr) ? str : str + endingStr;
    }

    /**
     * 字符串替换, 不基于正则表达式, 并且适用于JDK1.3. 各种情况的返回值参见测试用例:
     * {@link LangUtilTest#testReplaceAll()} in Util_JDK14.
     *
     * @param origin
     *            源字符串
     * @param oldPart
     *            被替换的旧字符串
     * @param replacement
     *            用来替换旧字符串的新字符串
     * @return 替换处理后的字符串
     */
    public static String replaceAll(String origin, String oldPart, String replacement) {
        if (origin == null || replacement == null) {
            return origin;
        }
        // 必须排除oldPart.length() == 0(即oldPart为"")的情况, 因为对任何字符串s, s.indexOf("")
        // == 0而非-1!
        if (oldPart == null || oldPart.length() == 0) {
            return origin;
        }

        int index = origin.indexOf(oldPart);
        if (index < 0) {
            return origin;
        }

        StringBuilder sb = new StringBuilder(origin);
        do {
            sb.replace(index, index + oldPart.length(), replacement);
            origin = sb.toString();
            index = origin.indexOf(oldPart);
        } while (index != -1);
        return origin;
    }

    /**
     * 从该字符串中去掉指定的起始部分.
     *
     * @param origin
     *            该字符串
     * @param beginPart
     *            指定的起始部分
     * @return 该字符串中去掉指定的起始部分后的内容; 如果不以其起始, 则返回原串.
     * @creator liushen @ Apr 2, 2009
     */
    public static String removeBeginPart(String origin, String beginPart) {
        if (origin == null || beginPart == null) {
            return origin;
        }

        int pos = origin.indexOf(beginPart);
        if (pos != 0) {
            return origin;
        }

        return origin.substring(beginPart.length());
    }

    /**
     * 以,为间隔, 分隔为整型数组.
     *
     * @param data
     * @return
     * @creator liushen @ May 3, 2009
     */
    public static int[] split2IntArray(String data) {
        String[] splited = splitAndTrim(data, ",");
        return ArrayUtil.toIntArray(splited);
    }

    /**
     * 将int型转换为字符串，转换失败则返回空字符串
     *
     * @param num
     * @return
     * @creator changpeng @ 2009-5-19
     */
    public static String int2String(int num) {
        try {

            return String.valueOf(num);
        } catch (Exception e) {
            logger.error("int转String出错 ： " + e);
            return "";
        }
    }

    /**
     * 返回原串中位于两个字符串之间的子串，不包括这两个字符串.
     *
     * @param origin
     * @param begin
     * @param end
     * @return
     * @creator liushen @ Jan 23, 2010
     */
    public static String substring(String origin, String begin, String end) {
        if (origin == null) {
            return origin;
        }
        int beginIndex = (begin == null) ? 0 : origin.indexOf(begin) + begin.length();
        int endIndex = (end == null) ? origin.length() : origin.indexOf(end, beginIndex);
        if (endIndex == -1) {
            return origin.substring(beginIndex);
        }
        return origin.substring(beginIndex, endIndex);
    }

    /**
     * 返回原串中位于某字符串之后的子串.
     *
     * @see #substring(String, String, String)
     */
    public static String substring(String origin, String begin) {
        return substring(origin, begin, null);
    }

    /**
     * 提取整数；本方法和 {@link NumberUtil#parseInt(String)} 的区别是，不支持小数(舍入处理)，直接返回
     * <code>-1</code>.
     *
     * @param value
     *            要转换的字符串
     * @return 如果<code>Integer.parseInt</code>不能成功，则返回<code>-1</code>.
     * @creator fangxiang @ Aug 15, 2009
     */
    public static int parseInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * 按指定的格式提取整数.
     *
     * @param source
     *            要转换的字符串
     * @param formatPattern
     *            指定的格式，参见 {@link DecimalFormat}.
     * @return 如果转换不能成功，则返回<code>-1</code>.
     * @since Jan 15, 2010
     */
    public static int parseIntUsingFormat(String source, String formatPattern) {
        DecimalFormat df = new DecimalFormat(formatPattern);
        try {
            return df.parse(source).intValue();
        } catch (ParseException e) {
            return -1;
        }
    }

    /**
     * 将字符串分割成名值对（Key-Value）
     *
     * @param src
     * @param propertyDelimiter
     * @param fieldDelimiter
     * @return
     * @since TRS @ Mar 5, 2011
     */
    public static Map<String, String> parseAsMap(String src, String propertyDelimiter, String fieldDelimiter) {
        if (StringUtils.isEmpty(src)) {
            return null;
        }
        Map<String, String> parsedMap = new HashMap<>(16);
        //
        String[] properties = StringUtils.split(src, propertyDelimiter);
        for (String property : properties) {
            int keyIndex = property.indexOf(fieldDelimiter);
            if (keyIndex != -1) {
                String key = property.substring(0, keyIndex).trim();
                String value = property.substring(keyIndex + fieldDelimiter.length()).trim();
                parsedMap.put(key, value);
            }
        }
        return parsedMap;
    }

    /**
     * 将字符串按照默认的分隔符切割成名值对（Key-Value），默认的属性分隔符是“;“，默认的字段分隔符是”=“
     *
     * @param src
     * @return
     * @since TRS @ Mar 5, 2011
     */
    public static Map<String, String> parseAsMap(String src) {
        return parseAsMap(src, ";", "=");
    }

    public static Map<String, String> parseSimpleJsonStrAsMap(String src, String propertyDelimiter,
            String fieldDelimiter) {
        if (StringUtils.isEmpty(src)) {
            return null;
        }
        Map<String, String> parsedMap = new HashMap<>(16);
        String mapSrc = src.trim();
        if (mapSrc.startsWith(LEFT_BRACKET)) {
            mapSrc = mapSrc.substring(1);
        }
        if (mapSrc.endsWith(RIGHT_BRACKET)) {
            mapSrc = mapSrc.substring(0, mapSrc.length() - 1);
        }
        //
        String[] properties = StringUtils.split(mapSrc, propertyDelimiter);
        for (String property : properties) {
            int keyIndex = property.indexOf(fieldDelimiter);
            if (keyIndex != -1) {
                String key = StringUtils.trimQuote(property.substring(0, keyIndex).trim());
                String value = StringUtils.trimQuote(property.substring(keyIndex + fieldDelimiter.length()).trim());
                parsedMap.put(key, value);
            }
        }
        return parsedMap;
    }

    public static Map<String, String> parseSimpleJsonStrAsMap(String src) {
        return parseSimpleJsonStrAsMap(src, ",", ":");
    }

    public static String parseMapAsSimpleJsonStr(Map<String, String> srcMap) {
        return parseMapAsSimpleJsonStr(srcMap, ",", ":", true);
    }

    public static String parseMapAsSimpleJsonStr(Map<String, String> srcMap, String propertyDelimiter,
            String fieldDelimiter, boolean bHasKH) {

        if (srcMap == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (bHasKH) {
            sb.append("{");
        }

        String[] keys = srcMap.keySet().toArray(new String[0]);
        for (String key : keys) {
            sb.append(StringUtils.quotes(key)).append(fieldDelimiter)
                    .append(StringUtils.quotes(StringUtils.avoidNull(srcMap.get(key)))).append(propertyDelimiter);
        }
        if (bHasKH) {
            sb.append("}");
        }
        return sb.toString();
    }

    public static Map<String, String> parseXMLStrAsMap(String src) {
        if (StringUtils.isEmpty(src)) {
            return null;
        }
        Map<String, String> docMap = new HashMap<>(16);
        String nodeRegex = "<(?<nodeName>[^>]+?)>(?<nodeValue>[\\w\\W]+?)</\\k<nodeName>>";
        Pattern pt = Pattern.compile(nodeRegex, Pattern.CASE_INSENSITIVE);
        Matcher m = pt.matcher(src);
        while (m.find()) {
            String nodeName = StringUtils.trim(m.group("nodeName"));
            String nodeValue = StringUtils.trim(m.group("nodeValue"));
            if (StringUtils.isNotEmpty(nodeName)) {
                docMap.put(nodeName, nodeValue);
            }
        }
        return docMap;
    }

    public static String parseMapAsSimpleXMLStr(Map<String, String> srcMap) {
        if (srcMap == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String[] keys = srcMap.keySet().toArray(new String[0]);
        for (String key : keys) {
            sb.append("<").append(key).append(">").append(StringUtils.avoidNull(srcMap.get(key))).append("</")
                    .append(key).append(">");
        }
        return sb.toString();
    }

    /**
     * 去除首尾的双引号.
     *
     * @param value
     * @return
     * @creator liushen @ Jan 29, 2010
     */
    public static String trimQuote(String value) {
        int two = 2;
        if (value == null) {
            return value;
        }
        String str = value.trim();

        final int length = str.length();
        if (length < two) {
            return str;
        }
        if (str.charAt(0) != QUOTATION_MARK || str.charAt(length - 1) != QUOTATION_MARK) {
            return str;
        }
        return str.substring(1, length - 1);
    }

    /**
     * 给包含空格的字符串增加首尾的双引号.
     *
     * @param str
     * @return
     * @creator liushen @ Jan 29, 2010
     */
    public static String addQuote(String str) {

        String value = str;
        if (value.indexOf(EMPTY_CHAR) > -1) {
            value = "\"" + str + "\"";
        }

        return value;
    }

    /**
     * 判断是否为数字
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isNumeric(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return NUMBER_PATTERN.matcher(input).matches();
    }

    /**
     * 判断是否为整数
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isInteger(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return INTEGER_PATTERN.matcher(input).matches();
    }

    /**
     * 判断是否为正整数.
     *
     * @param input
     * @return
     * @since liushen @ Nov 1, 2010
     */
    public static boolean isPositiveInteger(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return POSITIVEINTEGER_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否手机号码或座机电话号码
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isTelphone(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return PHONE_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否手机号码<br>
     * 支持的格式为：+国际区号-国内区号-电话号码-分机号<br>
     * 其中：<br>
     * + 可选 ；国际区号2-3位数字；国内区号以0开头、3-4位数字；电话号码为7-8位数字；分机号为3-4位数字；
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isMobile(String input) {
        if (!isNumeric(input)) {
            return false;
        }
        return input.length() == 11 && input.charAt(0) == '1';
    }

    /**
     * 判断输入是否符合账号的要求
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isAccountName(String input, int minLength, int maxLength) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        if (!ACCOUNT_PATTERN.matcher(input).matches()) {
            return false;
        }
        return limitedLength(input, minLength, maxLength);
    }

    /**
     * 判断输入是否符合长度限制
     *
     * @param input
     * @param minLength
     * @param maxLength
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean limitedLength(String input, int minLength, int maxLength) {
        if (StringUtils.isEmpty(input)) {
            return minLength <= 0;
        }
        return input.length() >= minLength && input.length() <= maxLength;
    }

    /**
     * 判断输入是否为邮件地址
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isEmail(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否为域名
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isDomainName(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return DOMAIN_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否为IP地址
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     * @deprecated
     */
    // @Deprecated
    // public static boolean isIP(String input) {
    // if (StringUtils.isEmpty(input)) {
    // return false;
    // }
    // return IP_PATTERN.matcher(input).matches();
    // }

    /**
     * 判断是否为IPv4的地址
     *
     * @param input
     * @return 如果是的话返回true，否则返回false。
     * @since TRS @ Feb 12, 2012
     */
    public static boolean isIPv4(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否为IP地址集合
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    // public static boolean isMultiIPs(String input) {
    // if (isEmpty(input)) {
    // return false;
    // }
    // StringTokenizer st = new StringTokenizer(input, ",");
    // boolean isMultiIPs = true;
    // while (st.hasMoreTokens() && isMultiIPs) {
    // String token = st.nextToken();
    // isMultiIPs &= isIP(token);
    // }
    //
    // return isMultiIPs;
    // }

    public static boolean isMultiIPs(List<String> st) {
        if (st.isEmpty()) {
            return false;
        }
        boolean isMultiIPs = true;
        for (String ip : st) {
            isMultiIPs &= isIPv4(ip);
        }
        return isMultiIPs;
    }

    /**
     * 判断输入是否为HTTP URL.liushen@Mar 2, 2011: 该方法需要增加对https的支持。
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isHttpUrl(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return HTTPURL_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否为 URL
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isUrl(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return URL_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否为RTMP协议族URL. liushen@Mar 2, 2011: 该方法需要增加对rtmps、rtmpt等的支持。
     *
     * @param input
     * @return
     * @since liushen@Mar 2, 2011
     */
    public static boolean isRtmpUrl(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        String value = removeLastSlashChar(input);
        return RTMP_PATTERN.matcher(value).matches();
    }

    /**
     *
     * @param input
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static boolean isFtpUrl(String input) {
        if (isEmpty(input)) {
            return false;
        }
        URL url;
        try {
            url = new URL(input);
        } catch (MalformedURLException e) {
            return false;
        }
        return UrlUtil.isFtp(url);
    }

    /**
     * 判断输入是否为音视频的URL；RTMP协议族、HTTP协议族等都可作为音视频的URL.
     *
     * @param input
     * @return
     * @since liushen @ Mar 10, 2011
     */
    public static boolean isMediaUrl(String input) {
        return isRtmpUrl(input) || isHttpUrl(input);
    }

    /**
     * 判断输入是否为QQ号.
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 28, 2010
     */
    public static boolean isQQ(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return QQ_PATTERN.matcher(input).matches();
    }

    /**
     * 判断输入是否为日期
     *
     * @param input
     * @return
     * @since fangxiang @ Oct 29, 2010
     */
    public static boolean isDate(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        String eL = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
        return Pattern.compile(eL).matcher(input).matches();
    }

    /**
     * 利用正则表达式替换字符串内容
     *
     * @param source
     *            源字符串
     * @param findRegex
     *            查找正则表达式
     * @param replaceRegex
     *            替换正则表达式
     * @return 替换后的正则表达式
     * @since TRS @ Sep 27, 2011
     */
    public static String regexReplace(String source, String findRegex, String replaceRegex) {
        return Pattern.compile(findRegex).matcher(source).replaceAll(replaceRegex);
    }

    /**
     * 变为用*表示的显示形式，位数和原串相等。由于密码前后均可能用空格以提高安全性，故本方法不做trim处理.
     *
     * @param value
     * @return
     * @since liushen @ Nov 2, 2010
     */
    public static String toSecurityMaskForm(String value) {
        if (value == null) {
            return null;
        }
        int length = value.length();
        if (length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('*');
        }
        return sb.toString();
    }

    /**
     * 集合使用特定的分隔符转换成字符串
     *
     * @param entities
     * @param separator
     * @return
     * @since fangxiang @ Nov 24, 2010
     */
    public static String join(Map<String, ?> entities, String separator) {
        if (entities == null) {
            return null;
        }
        String delimiter = separator == null ? "," : separator;
        StringBuilder buf = new StringBuilder(200);
        String[] keys = entities.keySet().toArray(new String[0]);
        for (String key : keys) {
            buf.append(key).append("=").append(StringUtils.avoidNull(entities.get(key).toString())).append(delimiter);
        }
        return buf.toString();
    }

    /**
     * 和 {@link #isEmpty(String)} 相反。
     *
     * @since liushen @ Jul 1, 2011
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 将String数组中的值转化为set，避免重复值
     *
     * @param strArray
     * @return
     * @creator yao.nengjun@trs.com.cn @ Apr 10, 2009
     */
    public static Set<String> changeStringArrayToSet(String[] strArray) {
        if (strArray == null || strArray.length == 0) {
            return Collections.emptySet();
        }

        Set<String> set2Return = new HashSet<>();
        for (String str : strArray) {
            set2Return.add(str);
        }
        return set2Return;
    }

    /**
     * 按顺序合并两个字符数组
     *
     * @param firstArray
     * @param secondArray
     * @return
     * @since v3.5
     * @creator yaonengjun @ Jun 8, 2010
     */
    public static String[] mergeArrary(String[] firstArray, String[] secondArray) {
        if (isStringArrayEmpty(firstArray) && isStringArrayEmpty(secondArray)) {
            return new String[0];
        }
        if (isStringArrayEmpty(firstArray) && null != secondArray) {
            return secondArray;
        }
        if (null != firstArray && isStringArrayEmpty(secondArray)) {
            return firstArray;
        }

        return combineArrary(firstArray, secondArray);
    }

    private static String[] combineArrary(String[] firstArray, String[] secondArray) {
        int firstLength = 0;
        int secondLength = 0;
        if (null != firstArray && null != secondArray) {
            firstLength = firstArray.length;
            secondLength = secondArray.length;
        }
        int resultLength = firstLength + secondLength;
        String[] resultArray = new String[resultLength];
        for (int i = 0; i < firstLength; i++) {
            resultArray[i] = firstArray[i];
        }
        for (int j = 0; j < secondLength; j++) {
            resultArray[firstLength + j] = secondArray[j];
        }

        return resultArray;
    }

    public static boolean isStringArrayEmpty(String[] strArray) {
        if (strArray == null || strArray.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * 在忽略字母大小写和前后空白的情况下，判断两个字符串是否相同。
     *
     * @return 以下两种情况返回<code>true</code>，其他情况均返回<code>false</code>:
     *         <ul>
     *         <li>两个字符串都是<code>null</code></li>
     *         <li>两个字符串在在忽略字母大小写和前后空白后内容相同</li>
     *         </ul>
     * @since liushen @ Jan 11, 2012
     */
    public static boolean equalIngoreCaseAndSpace(String one, String another) {
        if (one == null) {
            return another == null;
        }
        if (another == null) {
            return false;
        }
        return one.trim().equalsIgnoreCase(another.trim());
    }

    /**
     * 指定分隔符用来连接数组为一个串
     *
     * @param array
     * @param separator
     * @param quotes
     * @return
     * @since jacob @ 2014-4-19
     */
    public static String join(Object[] array, String separator, boolean quotes) {
        if (array == null) {
            return null;
        }
        List<String> entities = new ArrayList<>();
        if (quotes) {
            for (Object obj : array) {
                entities.add(quotes(obj.toString()));
            }
            return join(entities.toArray(), separator, 0, array.length);
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, String separator, String quotesChar) {
        if (array == null) {
            return null;
        }
        List<String> entities = new ArrayList<>();
        for (Object obj : array) {
            entities.add(quotesChar + obj.toString() + quotesChar);
        }
        return join(entities.toArray(), separator, 0, array.length);
    }

    /**
     *
     * 字符串加双引号
     *
     * @param str
     * @return
     * @since jacob @ 2014年7月25日 下午3:01:52
     */
    public static String quotes(String str) {
        if (isEmail(str)) {
            return str;
        }
        return "\"" + str + "\"";
    }

    /**
     *
     * 获取url中的host
     *
     * @param url
     * @return
     * @since Administrator @ 2014年8月24日 下午11:32:32
     */
    public static String getDomain(String str) {

        String url = str.replaceFirst("https://", "");
        url = url.replaceFirst("http://", "");

        int i = url.indexOf('/');

        return i > 0 ? url.substring(0, i) : url;
    }

    /**
     * 判断一个输入是否全部为字母
     *
     * @param str
     * @return
     */
    public static boolean isLetter(String str) {
        if (isEmpty(str)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            boolean isLetter = (str.charAt(i) <= 'Z' && str.charAt(i) >= 'A')
                    || (str.charAt(i) <= 'z' && str.charAt(i) >= 'a');
            if (!isLetter) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将给定字符串(origin)以字符串token作为分隔符进行分隔,并将结果以List返回
     *
     * @param origin
     *            给定字符串
     * @param token
     *            分隔符
     * @return
     * @creator huangshengbo @ 2009-7-9
     */
    public static List<String> splitToList(String origin, String token) {
        List<String> result = new ArrayList<>();
        String[] arr = split(origin, token);
        if (null != arr) {
            for (String element : arr) {
                result.add(element);
            }
        }
        return result;
    }

    public static List<String> splitToList(String origin) {
        List<String> result = new ArrayList<>();
        if (isNotEmpty(origin)) {
            String[] arr = split(origin.trim(), ",");
            if (null == arr) {

                return result;
            }
            for (String element : arr) {
                String ip = element.trim();
                if (!result.contains(ip)) {
                    result.add(ip);
                }
            }
        }

        return result;
    }

    public static String rtrim(String str, String ch) {
        String ret = "";
        if (str == null || ch == null) {

            return ret;
        }
        ret = str.trim();

        if (ret.endsWith(ch)) {

            int pos = ret.lastIndexOf(ch);

            if (pos != -1) {

                ret = ret.substring(0, pos);
            }
        }

        return ret;
    }

    public static String getWords(String content) {
        return content.replaceAll("[^\\u4e00-\\u9fa5,]", "");
    }

    /**
     * a 在不在 b里面
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean isIn(String a, String b) {

        boolean flag = isEmpty(a) || (!isEmpty(b) && b.indexOf(a) != -1);
        if (flag) {

            return true;
        }

        return false;
    }

    /**
     * 将接收的收据转为json串
     *
     * @param json
     * @return JSONObject
     */
    public static JSONObject toJsonObject(Object object) {

        String json;
        if (object instanceof String) {
            json = object.toString();
        } else {
            json = JSON.toJSONString(object);
        }
        JSONObject jsonObj = new JSONObject();
        if (StringUtils.isEmpty(json) || !json.startsWith(LEFT_BRACKET)) {
            return jsonObj;
        }
        try {
            jsonObj = JSON.parseObject(json);
        } catch (Exception e) {
            logger.error("json转换失败，不是正确的json格式，json ： " + json, e);
        }

        return jsonObj;
    }

    /**
     * 随机生成String
     *
     * @return
     */
    public static String randomString(int num) {
        char[] charSource = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
                'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
                'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '!', '@', '#', '$', '%', '^', '&', '*' };
        String password = "";
        Random rd = new Random();
        int i = 0;
        do {
            int getNum = rd.nextInt() % charSource.length;
            if (getNum < 0) {
                continue;
            }
            char s = charSource[getNum];
            password += s;

            i++;
        } while (i < num);

        return password;
    }

    /**
     * 随机生成8位密码 ;字母数字组合；不能有特殊字符，如%,&,.,之类的
     *
     * @return
     */
    public static String randomPassword(int num) {
        char[] charSource = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
                'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C',
                'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z' };

        StringBuilder password = new StringBuilder();
        Random rd = new Random();
        int i = 1;
        do {
            int getNum = rd.nextInt() % charSource.length;
            if (getNum < 0) {
                continue;
            }

            char s = charSource[getNum];
            password.append(s);
            ++i;
        } while (i <= num);

        return password.toString();
    }

    /**
     * 以字符末尾
     *
     * @param s
     * @return
     * @author ruzz 2018年2月2日
     */
    public static String unitExchange(String str) {
        String substring = str.substring(0, str.length() - 2);
        Float resultSize = Float.parseFloat(substring) * 1024;
        return String.valueOf(resultSize);
    }

    /**
     * @Description 判断参数是否满足星期字符串(0表示星期日，6表示星期六) exp:0,3,5,6（星期日，星期三，星期五，星期六）
     *
     * @param week
     * @return
     */
    public static boolean isWeekStrings(String week) {
        if (isEmpty(week)) {
            return false;
        }
        String[] weeks = week.split(",");
        try {
            for (String w : weeks) {
                int value = Integer.parseInt(w);
                if (value < 0 || value > 6) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * @Description 判断是否满足时刻字符串(0表示零点，1表示凌晨1点，23点表示23点)
     *              exp:0,5,17,22(0点,5点,17点,22点)
     * @param time
     * @return
     */
    public static boolean isTimeStrings(String time) {
        if (isEmpty(time)) {
            return false;
        }
        String[] times = time.split(",");
        try {
            for (String t : times) {
                int value = Integer.parseInt(t);
                if (value < 0 || value > 23) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 将字符串转换成${} 格式的环境变量
     *
     * @param str
     * @return String
     * @date: 2019年4月16日 下午5:29:39
     */
    public static String transferEnvironmentVariable(String str) {
        return "${" + str + "}";
    }

    /**
     * 删除起始字符
     *
     * @param s
     * @return
     * @author ruzz 2018年2月2日
     */
    public static String trimStart(String str, String trim) {
        if (null == str) {
            return null;
        }
        return str.replaceAll("^(" + trim + ")+", "");
    }

    /**
     * 删除末尾字符
     *
     * @param s
     * @return
     * @author ruzz 2018年2月2日
     */
    public static String trimEnd(String str, String trim) {
        if (null == str) {
            return null;
        }
        return str.replaceAll("(" + trim + ")+$", "");
    }

    /**
     * 以字符开头
     *
     * @param s
     * @return
     * @author ruzz 2018年2月2日
     */
    public static boolean startWith(String str, String s) {
        return str.startsWith(s);
    }

    /**
     * 以字符末尾
     *
     * @param s
     * @return
     * @author ruzz 2018年2月2日
     */
    public static boolean endWith(String str, String s) {
        return str.endsWith(s);
    }

    /**
     * 用str替换url的域名(http://xxx.xxx.xxx 部分)
     *
     * @param str
     * @param url
     * @return String
     * @date: 2019年5月7日 下午4:55:59
     */
    public static String replaceUrlDomainName(String str, String url) {
        String doubleSlash = "//";
        String slash = "/";

        int fromIndex = url.indexOf(doubleSlash);
        int index = url.indexOf(slash, fromIndex + doubleSlash.length());
        return str + url.substring(index);

    }
    /**
     * 判断Str是否为boolean值（true：false）
     *
     * @param str
     * @return boolean
     */
    public static boolean isboolean(String str) {
       if(StringUtils.isEmpty(str)) {
    	   return false;
       }
       else if(TRUE.equals(str) || FALSE.equals(str)) {
    	   return true;
       }
       return false;
    }

    /**
     * 换行符
     */
    private static char NEW_LINE_CHAR = '\n';

    /**
     * 获取字符串的后lineNum行（以 \r\n 换行）；str不能为空串；lineNum不能为0；
     *
     * @param str
     * @param lineNum
     * @return String
     * @date: 2019年6月21日 上午10:35:07
     */
    public static String splitStrByLines(String str, int lineNum) {
        if (isEmpty(str) || lineNum <= 0) {
            return "";
        }

        int startIndex = str.length() - 1;

        int line = 0;
        while (startIndex >= 0) {
            // 如果两个连续的换行符，当做一个处理
            if (str.charAt(startIndex) == NEW_LINE_CHAR) {
                line++;
                if (line >= lineNum) {
                    return str.substring(startIndex + 1, str.length());
                }
                startIndex--;
            } else {
                // 如果不是换行符，向前移动一个字符
                startIndex--;
            }
        }

        return str;
    }

}
