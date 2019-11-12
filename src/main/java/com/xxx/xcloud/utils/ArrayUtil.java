/*
 * Created: liushen@Apr 17, 2009 10:38:28 AM
 */
package com.xxx.xcloud.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数组相关操作工具类.<br>
 *
 * @author anychem
 */
public class ArrayUtil {

    public static final String BEGIN_INDEX_KEY = "beginIndex";
    public static final String END_INDEX_KEY = "endIndex";
    private static Logger logger = LoggerFactory.getLogger(ArrayUtil.class);

    private ArrayUtil() {

    }

    /**
     * 计算集合起始index
     *
     * @param listSize
     * @param size
     * @param page
     * @return
     */
    public static synchronized Map<String, Object> getIndex(Integer listSize, Integer size, Integer page) {
        Map<String, Object> map = new HashMap<>(16);
        Integer startIndex = null;
        Integer endIndex = null;
        try {
            startIndex = page * size;
            endIndex = (page + 1) * size;
        } catch (NumberFormatException e) {
            startIndex = 0;
            endIndex = 0;
        }
        if (startIndex > listSize || endIndex > listSize) {
            int remainder = listSize % size;

            if (0 != remainder) {
                startIndex = listSize - remainder;
                endIndex = listSize;
            } else {
                startIndex = listSize - size;
                endIndex = listSize;
            }
        }

        if (startIndex < 0 || endIndex < 0) {
            startIndex = 0;
            endIndex = size;
        }
        map.put(BEGIN_INDEX_KEY, startIndex);
        map.put(END_INDEX_KEY, endIndex);

        return map;
    }

    /**
     * 将int数组转换为Integer数组.
     *
     * @param values
     *            int数组
     * @return Integer数组
     */
    public static Integer[] toIntegerArray(int[] values) {
        if (values == null) {
            return new Integer[0];
        }
        Integer[] result = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = new Integer(values[i]);
        }
        return result;
    }

    /**
     * 将boolean数组转换为Boolean数组.
     *
     * @param values
     *            boolean数组
     * @return Boolean数组
     * @since liushen @ Jun 23, 2010
     */
    public static Object[] toBooleanArray(boolean[] values) {
        if (values == null) {
            return new Object[0];
        }
        Boolean[] result = new Boolean[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Boolean(values[i]);
        }
        return result;
    }

    /**
     * 将Integer数组转换为int数组.
     *
     * @param values
     *            Integer数组
     * @return int数组
     */
    public static int[] toIntArray(Integer[] values) {
        if (values == null) {
            return new int[0];
        }
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].intValue();
        }
        return result;
    }

    /**
     * 将String数组转换(parseInt)为int数组, 如果某String parse失败，则跳过该元素.
     *
     * @param values
     *            String数组
     * @return int数组， 长度是原String数组中能够转换为int的数目.
     */
    public static int[] toIntArray(String[] values) {
        if (values == null) {
            return new int[0];
        }

        List<Integer> temps = new ArrayList<>();
        for (String value : values) {
            try {
                temps.add(Integer.valueOf(value));
            } catch (Exception e) {
                logger.error("错误信息 :" + e.getMessage());
                throw e;
            }
        }
        return toIntArray(temps.toArray(new Integer[0]));
    }

    /**
     * 将Map的Key转换成字符串数组
     *
     * @param map
     * @return
     * @creator changpeng @ 2009-5-31
     */
    public static String[] keysToString(Map<String, ? extends Object> map) {
        return map.keySet().toArray(new String[0]);
    }

    /**
     * 将List<String>转换为字符串数组
     *
     * @param list
     * @return
     * @creator changpeng @ 2009-5-31
     */
    public static String[] listToString(List<String> list) {
        return list.toArray(new String[0]);
    }

    /**
     * 将List<Integer>转换为intArray
     *
     * @param list
     * @return int[]
     * @creator lichuanjiao @ 2009-12-28
     */
    public static int[] toIntArray(List<Integer> list) {
        int[] intArray = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            intArray[i] = list.get(i);
        }
        return intArray;
    }

    /**
     * 比较两个int数组是否相等，顺序无关。
     *
     * @param int[],int[]
     * @return true表示相同，false表示不同
     * @creator lichuanjiao @ 2009-12-28
     */
    public static boolean compareArrayValue(int[] a, int[] b) {
        boolean flag;
        if ((a.length == b.length) && (a.length != 0)) {
            flag = compareArrayVal(a, b);
        } else {
            flag = false;
        }
        return flag;
    }

    private static boolean compareArrayVal(int[] a, int[] b) {
        boolean flag = true;
        for (int element : a) {
            for (int element2 : b) {
                if (element != element2) {
                    flag = false;
                }
            }
        }
        return flag;
    }

    /**
     * 获取数组A中比数组B中多出来的值。
     *
     * @param int[],int[]
     * @return int[]
     * @creator lichuanjiao @ 2009-12-28
     */
    public static int[] getArrayValuesExceeded(int[] a, int[] b) {
        List<Integer> arrayValuesExceededIntegerList = new ArrayList<>();
        boolean flag = true;
        for (int element : a) {
            for (int element2 : b) {
                if (element != element2) {
                    flag = false;
                }
            }
            if (!flag) {
                arrayValuesExceededIntegerList.add(element);
                flag = true;
            }

        }
        return toIntArray(arrayValuesExceededIntegerList);
    }

    /**
     * 给定数组中是否包含所给元素.
     *
     * @param anArray
     *            数组
     * @param anElement
     *            元素
     * @return 包含返回<code>true</code>
     * @since liushen @ May 6, 2010
     */
    public static <T> boolean contain(T[] anArray, T anElement) {
        if (anArray == null) {
            return false;
        }
        for (T element : anArray) {
            if (element.equals(anElement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 所给字符串, 是否是以给定字符串数组的某一个元素为起始.
     *
     * @param anArray
     *            给定字符串数组
     * @param anElement
     *            所给字符串
     * @return
     * @since liushen @ May 31, 2010
     */
    public static boolean prefixMatch(String[] anArray, String anElement) {
        if (anArray == null) {
            return false;
        }
        for (String element : anArray) {
            // 不能排除掉空格等空串.
            if (element == null || anArray.length == 0) {
                continue;
            }
            if (anElement.startsWith(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构造一个由连续自然数(即正整数， 从1开始)构成的数组.
     *
     * @param length
     *            数组长度
     * @return 连续自然数(即正整数， 从1开始)构成的数组
     * @since liushen @ Jun 20, 2010
     */
    public static int[] createSequencedPositiveIntArray(int length) {
        int[] result = new int[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = i + 1;
        }
        return result;
    }

    /**
     * 扩充对象型数组.
     *
     * @param <T>
     *            数组包含的对象的类型
     * @param srcArray
     *            源数组
     * @param deltaArray
     *            要补充的数组
     * @return 扩充后的数组
     * @since liushen @ Jul 15, 2010
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] expand(T[] srcArray, T[] deltaArray) {
        AssertUtil.notNull(srcArray, "srcArray is null.");
        AssertUtil.notNull(deltaArray, "deltaArray is null.");
        int originLength = srcArray.length;
        Object destArray = Array.newInstance(srcArray.getClass().getComponentType(), originLength + deltaArray.length);
        System.arraycopy(srcArray, 0, destArray, 0, originLength);
        System.arraycopy(deltaArray, 0, destArray, originLength, deltaArray.length);
        return (T[]) destArray;
    }

    /**
     * 扩充整型数组.
     *
     * @param srcArray
     *            源数组
     * @param deltaArray
     *            要补充的数组
     * @return 扩充后的数组
     * @since liushen @ Jul 15, 2010
     */
    public static int[] expand(int[] srcArray, int[] deltaArray) {
        AssertUtil.notNull(srcArray, "srcArray is null.");
        AssertUtil.notNull(deltaArray, "deltaArray is null.");
        int originLength = srcArray.length;
        Object destArray = Array.newInstance(srcArray.getClass().getComponentType(), originLength + deltaArray.length);
        System.arraycopy(srcArray, 0, destArray, 0, originLength);
        System.arraycopy(deltaArray, 0, destArray, originLength, deltaArray.length);
        return (int[]) destArray;
    }

    /**
     * 比较两个一维数组是否相同(顺序需相同); 本方法不适用于二维或更高维数组的比较.
     *
     * @param oneArray
     *            一个一维数组
     * @param anotherArray
     * @return
     * @since liushen @ Jul 16, 2010
     */
    public static <T> boolean isEquals(T[] oneArray, T[] anotherArray) {
        return Arrays.equals(oneArray, anotherArray);
    }

    /**
     * @param objArray
     * @return
     * @since liushen @ Jun 2, 2011
     */
    public static boolean isEmpty(Object[] objArray) {
        return objArray == null || objArray.length == 0;
    }
}
