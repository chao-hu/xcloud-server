package com.xxx.xcloud.client.gitlab;

import com.xxx.xcloud.client.gitlab.exception.GitlabException;
import com.xxx.xcloud.client.rest.ApiClient;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.util.concurrent.ConcurrentHashMap;

/**
 * gitlab接口实例工具
 *
 * @author mengaijun
 * @date: 2019年1月7日 下午6:48:31
 */
public class GitlabClientFactory {
    /**
     * key：gitlab地址（带版本信息） value：gitlabapi接口代理类实例
     */
    public static ConcurrentHashMap<String, GitlabApi> gitlabApiMap = new ConcurrentHashMap<>();

    /**
     * key：gitlab地址 value：gitlab支持api版本
     */
    public static ConcurrentHashMap<String, ApiVersion> gitlabApiVerMap = new ConcurrentHashMap<>();

    /**
     * Giblab接口异常类
     *
     * @author mengaijun
     * @date: 2018年12月21日 上午9:37:22
     */
    static class GitlabErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            return new GitlabException(response.status(), response.reason());
        }
    }

    /**
     * 获取gitlab接口实例
     *
     * @param endpoint
     *            接口地址
     * @return GitlabApi
     * @date: 2018年12月21日 上午9:46:23
     */
    public static GitlabApi getGitlabInstance(String endpoint) {
        if (gitlabApiMap.get(endpoint) == null) {
            synchronized (GitlabClientFactory.class) {
                if (gitlabApiMap.get(endpoint) == null) {
                    gitlabApiMap.put(endpoint,
                            ApiClient.getInstance(endpoint, new GitlabErrorDecoder(), GitlabApi.class, null));
                }
            }
        }

        return gitlabApiMap.get(endpoint);
    }

    /**
     * 移除实例
     *
     * @param endpoint
     *            void
     * @date: 2019年8月8日 下午4:41:54
     */
    public static void removeGitlabInstance(String endpoint) {
        gitlabApiMap.remove(endpoint);
    }

    /**
     * 设置uri对应api版本
     *
     * @param uri
     * @param apiVersion
     *            void
     * @date: 2019年8月8日 下午5:10:07
     */
    public static void setUrlVersion(String uri, ApiVersion apiVersion) {
        gitlabApiVerMap.put(uri, apiVersion);
    }

    /**
     * 拼接apiversion到 uri
     *
     * @param uri
     * @param version
     * @return String
     * @date: 2019年8月8日 下午5:10:28
     */
    public static String getUrlWithVersion(String uri, ApiVersion version) {
        return uri + version.getApiNamespace();
    }

    /**
     * 获取gitlab uri 支持的api版本
     *
     * @param uri
     * @return ApiVersion
     * @date: 2019年8月8日 下午5:10:52
     */
    public static ApiVersion getApiVersionOfUri(String uri) {
        return gitlabApiVerMap.get(uri);
    }

    /**
     * 根据判断好的api版本拼接uri
     *
     * @param uri
     * @return String
     * @date: 2019年8月8日 下午5:11:09
     */
    public static String getUrlWithVersion(String uri) {
        return uri + gitlabApiVerMap.get(uri).getApiNamespace();
    }

    /**
     * gitlab api版本
     *
     * @author mengaijun
     * @date: 2019年8月8日 下午5:49:19
     */
    public enum ApiVersion {
        /**
         * v3
         */
        V3,
        /**
         * v4
         */
        V4;

        public String getApiNamespace() {
            return ("/api/" + name().toLowerCase());
        }
    }
}
