package com.xxx.xcloud.client.github;

import com.xxx.xcloud.client.rest.ApiClient;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;

/**
 * github接口
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月7日 下午6:40:47
 */
public class GithubClientFactory {

    private volatile static GithubApi githubApi;

    /**
     * 获取github api接口实例
     *
     * @return GithubApi
     * @date: 2019年1月2日 下午5:46:33
     */
    public static GithubApi getGithubInstance() {
        if (null == githubApi) {
            synchronized (GithubClientFactory.class) {
                if (null == githubApi) {
                    githubApi = ApiClient.getInstance(XcloudProperties.getConfigMap().get(Global.GITHUB_API_URL),
                            GithubApi.class, null);
                }
            }
        }
        return githubApi;
    }
}
