package com.xxx.xcloud.client.rest.fastjson;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

/**
 * @ClassName: FastJsonEncoder
 * @Description: FastJsonEncoder
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class FastJsonEncoder implements Encoder {

    /**
     * @Title: encode
     * @Description:
     * @param object
     * @param bodyType
     * @param template
     * @throws EncodeException
     * @see feign.codec.Encoder#encode(java.lang.Object, java.lang.reflect.Type, feign.RequestTemplate)
     */
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {

        template.body(JSON.toJSONString(object));
    }

}
