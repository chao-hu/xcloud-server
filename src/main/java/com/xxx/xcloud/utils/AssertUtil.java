/*
 * Created: liushen@Mar 19, 2010 1:59:57 PM
 */
package com.xxx.xcloud.utils;

import java.util.Collection;

/**
 * 
 * @ClassName: AssertUtil
 * @Description: 常用断言方法
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
public class AssertUtil {

    private AssertUtil() {

    }

    /**
     * 断言对象不为null.
     * 
     * @param obj
     *            断言的对象
     * @param message
     *            提示信息
     * @throws IllegalArgumentException
     * @since liushen @ Mar 19, 2010
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(StringUtils.isEmpty(message) ? "the object is null!" : message);
        }
    }

    /**
     * 断言字符串不为null，或者长度不为0（做trim）.
     * 
     * @param str
     *            字符串
     * @param message
     *            提示信息
     * @since liushen @ Jun 24, 2010
     */
    public static void notNullOrEmpty(String str, String message) {
        if (str == null || str.trim().length() == 0) {
            throw new IllegalArgumentException(StringUtils.isEmpty(message) ? "the string is empty!" : message);
        }
    }

    /**
     * 断言集合不为null，或者size不为0.
     * 
     * @param objs
     *            集合
     * @param message
     *            提示信息
     * @since liushen @ Apr 15, 2010
     */
    public static void notNullOrEmpty(Collection<?> objs, String message) {
        notNull(objs, StringUtils.isEmpty(message) ? "the collection is null!" : message);
        if (objs.isEmpty()) {
            throw new IllegalArgumentException(StringUtils.isEmpty(message) ? "the collection is empty!" : message);
        }
    }

}
