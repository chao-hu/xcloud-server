package com.xxx.xcloud.client.rest.fastjson;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

public class FastJsonEncoder implements Encoder {

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {

        template.body(JSON.toJSONString(object));
    }

}
