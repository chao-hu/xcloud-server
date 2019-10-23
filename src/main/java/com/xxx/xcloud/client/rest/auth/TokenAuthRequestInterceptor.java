package com.xxx.xcloud.client.rest.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 *
 * @author mengaijun
 * @Description: 认证
 * @date: 2019年1月8日 上午11:11:23
 */
public class TokenAuthRequestInterceptor implements RequestInterceptor {
    private final String headerValue;

    public TokenAuthRequestInterceptor(String token) {
        headerValue = "token=" + token;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", headerValue);
    }
}
