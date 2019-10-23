package com.xxx.xcloud.client.rest;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Map;

import com.xxx.xcloud.client.rest.fastjson.FastJsonDecoder;
import com.xxx.xcloud.client.rest.fastjson.FastJsonEncoder;

import feign.Feign;
import feign.Feign.Builder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;

/**
 *
 *
 * @author mengaijun
 * @Description: 获取接口实例
 * @date: 2019年1月7日 下午6:47:45
 */
public class ApiClient {

    /**
     *
     *
     * @author mengaijun
     * @Description: 请求头转换
     * @date: 2018年12月19日 下午5:54:23
     */
    static class HeadersInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate template) {
            String contentType = "Content-Type";
            Map<String, Collection<String>> headers = template.headers();
            if (null == headers.get(contentType)) {
                template.header("Accept", "application/json");
                template.header("Content-Type", "application/json");
            }
        }
    }

    /**
     * 获取接口client
     *
     * @param endpoint
     *            接口地址
     * @param errorDecoder
     *            异常转换类实例
     * @param apiClass
     *            接口类
     * @param interceptors
     * @return T
     * @date: 2018年12月21日 上午9:50:22
     */
    public static <T> T getInstance(String endpoint, ErrorDecoder errorDecoder, Class<T> apiClass,
            RequestInterceptor... interceptors) {
        Builder b = Feign.builder().encoder(new FastJsonEncoder()).decoder(new FastJsonDecoder())
                .errorDecoder(errorDecoder);
        if (interceptors != null) {
            b.requestInterceptors(asList(interceptors));
        }
        b.requestInterceptor(new HeadersInterceptor());
        return (b.target(apiClass, endpoint));
    }

    /**
     * 获取接口client
     *
     * @param endpoint
     *            接口地址
     * @param errorDecoderClass
     *            异常转换类
     * @param apiClass
     *            接口类
     * @param interceptors
     * @return T
     * @date: 2018年12月21日 上午9:50:22
     */
    public static <T> T getInstance(String endpoint, Class<T> apiClass, RequestInterceptor... interceptors) {
        Builder b = Feign.builder().encoder(new FastJsonEncoder()).decoder(new FastJsonDecoder());

        if (interceptors != null) {
            b.requestInterceptors(asList(interceptors));
        }
        b.requestInterceptor(new HeadersInterceptor());
        return (b.target(apiClass, endpoint));
    }

}
