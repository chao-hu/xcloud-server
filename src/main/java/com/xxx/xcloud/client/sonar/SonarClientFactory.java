package com.xxx.xcloud.client.sonar;

import com.xxx.xcloud.client.rest.ApiClient;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import feign.auth.BasicAuthRequestInterceptor;

/**
 * 获取sonar 接口实例工具类
 *
 * @author mengaijun
 * @date: 2019年1月7日 下午6:45:53
 */
public class SonarClientFactory {
    private volatile static SonarApi sonarApi;


    /**
     * Creates a sonar client proxy that performs HTTP basic authentication.
     *
     * @param endpoint
     * @param username
     * @param password
     * @return SonarApi
     * @date: 2018年12月28日 下午7:06:36
     */
    private static SonarApi getSonarInstanceWithBasicAuth(String endpoint, String username, String password) {
        return ApiClient.getInstance(endpoint, SonarApi.class, new BasicAuthRequestInterceptor(username, password));
    }

    /**
     * Creates a sonar client proxy that performs HTTP basic authentication.
     *
     * @return SonarApi
     * @date: 2018年12月28日 下午7:06:36
     */
    public static SonarApi getSonarInstanceWithBasicAuth() {
        if (sonarApi == null) {
            synchronized (SonarClientFactory.class) {
                if (sonarApi == null) {
                    sonarApi = getSonarInstanceWithBasicAuth(XcloudProperties.getConfigMap().get(Global.SONAR_URL),
                            XcloudProperties.getConfigMap().get(Global.SONAR_USER_NAME),
                            XcloudProperties.getConfigMap().get(Global.SONAR_USER_PWD));
                }
            }
        }

        return sonarApi;
    }
}
