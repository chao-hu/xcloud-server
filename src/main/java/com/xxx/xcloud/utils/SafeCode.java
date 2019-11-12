package com.xxx.xcloud.utils;

/**
 * @author 李林兵
 * @version 1.0
 * @Copyright: Copyright
 * @Description: asc码转换
 * 
 */
public class SafeCode {

    public SafeCode() {
    }

    /**
     * asc码换成字符串转
     * 
     * @param s
     * @return
     */
    public static final String decode(String s) {
        if (s == null) {
            return "";
        }
        String s3 = "";
        for (int i = 0; i < s.length(); i++) {
            char c;
            switch (c = s.charAt(i)) {
            case 126: // '~'
                String s1 = s.substring(i + 1, (i + 4) - 1);
                s3 = s3 + (char) Integer.parseInt(s1, 16);
                i += 2;
                break;

            case 94: // '^'
                String s2 = s.substring(i + 1, i + 4 + 1);
                s3 = s3 + (char) Integer.parseInt(s2, 16);
                i += 4;
                break;

            default:
                s3 = s3 + c;
                break;
            }
        }

        return s3;
    }

    /**
     * 字符串转换成asc码
     * 
     * @param s
     * @return
     */
    public static final String encode(String s) {
        if (s == null) {
            return "";
        }
        String s3 = "";
        for (int i = 0; i < s.length(); i++) {
            char c;
            if ((c = s.charAt(i)) > '\377') {
                String s1;
                for (int j = (s1 = Integer.toString(c, 16)).length(); j < 4; j++) {
                    s1 = "0" + s1;
                }
                s3 = s3 + "^" + s1;
            } else if (c < '0' || c > '9' && c < 'A' || c > 'Z' && c < 'a' || c > 'z') {
                String s2;
                for (int k = (s2 = Integer.toString(c, 16)).length(); k < 2; k++) {
                    s2 = "0" + s2;
                }
                s3 = s3 + "~" + s2;
            } else {
                s3 = s3 + c;
            }
        }

        return s3;
    }
}
