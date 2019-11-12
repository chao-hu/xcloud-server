package com.xxx.xcloud.client.sonar;

import com.xxx.xcloud.client.rest.ApiClient;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import feign.auth.BasicAuthRequestInterceptor;

/**
 * 获取sonar 接口实例工具类
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月7日 下午6:45:53
 */
public class SonarClientFactory {
    private volatile static SonarApi sonarApi;

//    /**
//     * sonar错误信息转换为异常
//     *
//     * @author mengaijun
//     * @Description: TODO
//     * @date: 2018年12月20日 上午11:28:16
//     */
//    static class SonarErrorDecoder implements ErrorDecoder {
//        @Override
//        public Exception decode(String methodKey, Response response) {
//            return new SheraException(response.status(), response.reason());
//        }
//    }

//    /**
//     * 获取sonar接口实例
//     *
//     * @param endpoint 接口地址
//     * @return SonarApi
//     * @date: 2018年12月21日 上午9:46:23
//     */
//    private static SonarApi getSonarInstance(String endpoint) {
//        return ApiClient.getInstance(endpoint, new SonarErrorDecoder(), SonarApi.class, null);
//    }

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
