package com.xxx.xcloud.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: XcloudProperties
 * @Description: 系统配置类
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class XcloudProperties {

    /**
     * @Fields: 系统配置 map
     */
    private static Map<String, String> configMap = new ConcurrentHashMap<>();

    /**
     * @Title: getConfigMap
     * @Description: 获取全部系统配置
     * @param @return 参数
     * @return Map<String,String> 返回类型
     * @throws
     */
    public static Map<String, String> getConfigMap() {

        return configMap;
    }

}
