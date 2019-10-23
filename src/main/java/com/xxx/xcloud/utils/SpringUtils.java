package com.xxx.xcloud.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;

/**
 * ApplicationContext管理类
 *
 * @author mengaijun
 * @Description: Spring 获取bean类
 * @date: 2019年10月22日 上午10:00:07
 */
@Service
public class SpringUtils implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;

    /**
     * 获取ApplicationContext对象
     *
     * @return ApplicationContext
     * @date: 2019年4月16日 上午10:00:07
     */
    public static ApplicationContext getApplicationContext() {

        if (applicationContext == null) {

            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "ApplicationContext为空!");
        }

        return applicationContext;
    }

    /**
     * 获取bean对象
     *
     * @param clazz
     * @return T
     * @date: 2019年4月16日 上午10:12:40
     */
    public static <T> T getBean(Class<T> clazz) {

        if (applicationContext == null) {

            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "ApplicationContext为空!");
        }

        return applicationContext.getBean(clazz);
    }

    /**
     * 获取bean对象
     *
     * @param clazz
     * @return T
     * @date: 2019年4月16日 上午10:12:40
     */
    public static Object getBean(String name) {

        if (applicationContext == null) {

            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "ApplicationContext为空!");
        }

        return applicationContext.getBean(name);
    }

    /**
     * Context关闭时清理静态变量
     */
    @Override
    public void destroy() throws Exception {

        applicationContext = null;
    }

    /**
     * 设置ApplicationContext对象
     *
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        SpringUtils.applicationContext = applicationContext;
    }
}
