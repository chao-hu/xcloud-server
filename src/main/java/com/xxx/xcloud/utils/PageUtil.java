package com.xxx.xcloud.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {

    private static int pageDef = 0;// 默认的页数
    private static int sizeDef = 2000;// 默认的每页数量

    private PageUtil() {};

    /**
     * 通过参数获取pageable对象
     * 
     * @param page
     *            默认为0
     * @param size
     *            默认为2000
     * @return 参数有一个为空时，返回默认的pageable
     */
    public static Pageable getPageable(Integer page, Integer size) {
        Pageable pageable;

        if (null != page && null != size) {

            pageable = PageRequest.of(page, size);
        } else {

            pageable = PageRequest.of(pageDef, sizeDef);
        }

        return pageable;
    }

    /**
     * 通过参数获取pageable对象
     * 
     * @param page
     *            默认为0
     * @param size
     *            默认为2000
     * @param sort
     * @return 参数有一个为空时，返回默认的pageable
     */
    public static Pageable getPageable(Integer page, Integer size, Sort sort) {
        Pageable pageable;

        if (null != page && null != size) {

            pageable = PageRequest.of(page, size, sort);
        } else {

            pageable = PageRequest.of(pageDef, sizeDef, sort);
        }

        return pageable;
    }

}
