package com.xxx.xcloud.config;

import java.nio.charset.Charset;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * @ClassName: ALiFastJsonConfig
 * @Description: SpringBoot集成 fastjson
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Configuration
public class AliFastJsonConfig {

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        // 1.定义fastJson转换器
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        // 设定日期时间格式
        config.setDateFormat("yyyy-MM-dd HH:mm:ssS");
        // null值也会打印出 key
        config.setSerializerFeatures(SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNonStringValueAsString, SerializerFeature.WriteNullNumberAsZero);

        // 设定UTF-8编码
        config.setCharset(Charset.forName("UTF-8"));
        fastConverter.setFastJsonConfig(config);
        HttpMessageConverter<?> converter = fastConverter;
        return new HttpMessageConverters(converter);
    }
}
