/**
 * Created: liushen@Mar 20, 2009 12:06:13 PM
 */
package com.xxx.xcloud.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URL处理工具类.<br>
 *
 * @author anychem
 */
public class UrlUtil {

    private static Logger logger = LoggerFactory.getLogger(UrlUtil.class);
    
    /**
     * @Fields: ?
     */
    private static final char QUESTION_MARK = '?';
    
    /**
     * @Fields: .
     */
    private static final char POINT_MARK = '.';
    
    /**
     * @Fields: &
     */
    private static final char AND_MARK = '&';

    private UrlUtil() {
    }

    /**
     * 根据指定的url获取各级别域名
     *
     * @param url
     * @param tldLevel
     * @return
     */
    public static String getDomainByLevel(String serverName, int tldLevel) {
        if (StringUtils.isEmpty(serverName) || serverName.indexOf(POINT_MARK) < 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(20);
        String[] arr = serverName.split("\\.");
        int length = arr.length;
        String pattern = "\\b\\d+\\b";
        if (length > 0 && arr[length - 1].matches(pattern)) {
            return "";
        }
        if (tldLevel >= length) {
            sb.append(serverName);
        } else {
            for (int j = 0; j < tldLevel; j++) {
                sb.append(".");
                sb.append(arr[j + length - tldLevel]);
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param url
     * @param paramName
     * @param value
     * @return
     */
    public static String addParam(String url, String paramName, Object value) {
        if (url == null || paramName == null) {
            return url;
        }
        if (url.indexOf(QUESTION_MARK) == -1) {
            return url + '?' + paramName + '=' + value;
        }
        return url + '&' + paramName + '=' + value;
    }

    /**
     *
     * @param url
     * @param paramName
     * @param value
     * @return
     */
    public static String replaceParamValue(String url, String paramName, Object value) {
        if (url == null || paramName == null) {
            return url;
        }
        String target = paramName + '=';
        int startPos = getParamStartPos(url, paramName);
        if (startPos == -1) {
            return url;
        }
        char ch = url.charAt(startPos - 1);
        if (ch != QUESTION_MARK && ch != AND_MARK) {
            return url;
        }

        int endPos = url.indexOf('&', startPos);
        if (endPos == -1) {
            String remain = url.substring(0, startPos - 1);
            return addParam(remain, paramName, value);
        }

        String partStart = url.substring(0, startPos);
        String partEnd = url.substring(endPos);
        return partStart + target + value + partEnd;
    }

    /**
     * @param url
     * @param paramName
     * @return
     */
    public static boolean paramExists(String url, String paramName) {
        return getParamStartPos(url, paramName) != -1;
    }

    static int getParamStartPos(String url, String paramName) {
        String target = paramName + '=';
        int startPos = url.indexOf(target);
        if (startPos < 1) {
            return -1;
        }
        char ch = url.charAt(startPos - 1);
        if (ch != QUESTION_MARK && ch != AND_MARK) {
            return -1;
        }

        return startPos;
    }

    /**
     *
     * @param url
     * @param paramName
     * @param value
     * @return
     */
    public static String addOrReplaceParam(String url, String paramName, Object value) {
        if (paramExists(url, paramName)) {
            return replaceParamValue(url, paramName, value);
        }

        return addParam(url, paramName, value);
    }

    /**
     * 对给定的字符串进行UTF-8方式的URL编码.
     *
     * @see #encode(String, String)
     */
    public static String encode(String str) {
        return encode(str, "UTF-8");
    }

    /**
     * 对给定的字符串进行UTF-8方式的URL解码.
     *
     * @see #decode(String, String)
     */
    public static String decode(String str) {
        return decode(str, "UTF-8");
    }

    /**
     * 对给定的字符串进行URL编码.
     *
     * @param str
     *            待编码的字符串
     * @param encoding
     *            编码方式
     */
    public static String encode(String str, String encoding) {
        try {
            return URLEncoder.encode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("错误信息：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 对给定的字符串进行URL解码.
     *
     * @param str
     *            待解码的字符串
     * @param encoding
     *            编码方式
     */
    public static String decode(String str, String encoding) {
        try {
            return URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("错误信息：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param url
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static boolean isFtp(URL url) {
        return "ftp".equals(url.getProtocol());
    }

    /**
     * @param url
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static String getHost(URL url) {
        String authority = url.getAuthority();
        int lastIndex = authority.lastIndexOf('@');
        if (lastIndex == -1) {
            return authority;
        }
        String hostAndPort = authority.substring(lastIndex + 1);
        int posOfColon = hostAndPort.indexOf(':');
        if (posOfColon == -1) {
            return hostAndPort;
        }
        return hostAndPort.substring(0, posOfColon);
    }

    /**
     * @param url
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static int getPort(URL url) {
        return (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
    }

    /**
     * @param url
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static String getRemoteDir(URL url) {
        String existedPath = url.getPath();
        if (!StringUtils.isEmpty(existedPath)) {
            return existedPath;
        }
        String authority = url.getAuthority();
        String externalForm = url.toExternalForm();
        return StringUtils.substring(externalForm, authority, "?");
    }

    /**
     * @param url
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static String getUserName(URL url) {
        String userInfo = url.getUserInfo();
        if (userInfo == null) {
            return null;
        }
        final int index = userInfo.indexOf(':');
        if (index == -1) {
            return null;
        }
        return userInfo.substring(0, index);
    }

    /**
     * @param url
     * @return
     * @since liushen @ Apr 6, 2011
     */
    public static String getPassword(URL url) {
        String authority = url.getAuthority();
        int lastAt = authority.lastIndexOf('@');
        if (lastAt == -1) {
            return null;
        }
        int colon = authority.indexOf(':');
        if (colon == -1) {
            return null;
        }
        return authority.substring(colon + 1, lastAt);
    }

}
