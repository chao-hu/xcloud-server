package com.xxx.xcloud.module.component.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringApplicationContext
 *
 * @author
 * @version 2018年11月15日
 * @see SpringApplicationContext
 * @since
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {
    /**
     *
     */
    public static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        CONTEXT = context;
    }

    /**
     *
     * Description: getBean
     *
     * @param className
     * @param <T>
     *            bean
     * @return <T>
     * @throws ClassNotFoundException
     * @throws BeansException
     * @see
     */
    public static <T> T getBean(Class<T> requiredType) {
        return CONTEXT.getBean(requiredType);
    }

    /**
     * 通过name获取 Bean.
     * @Title: getBean
     * @Description: 通过name获取 Bean.
     * @param name
     * @return Object 
     * @throws
     */
    public static Object getBean(String name) {
        Object aObject = CONTEXT.getBean(name);
        return aObject;
    }
}
