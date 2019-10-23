package com.xxx.xcloud.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BdosProperties {

    private static Map<String, String> configMap = new ConcurrentHashMap<>();

    public static Map<String, String> getConfigMap() {

        return configMap;
    }

}
