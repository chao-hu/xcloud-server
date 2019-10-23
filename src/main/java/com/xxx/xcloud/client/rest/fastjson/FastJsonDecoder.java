package com.xxx.xcloud.client.rest.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;

public class FastJsonDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {

        if (response.status() == 404) {
            return Util.emptyValueOf(type);
        }
        if (response.body() == null) {
            return null;
        }
        try {

            String text = response.body().toString();
            if (JSON.isValidArray(text)) {

                return JSON.parseArray(text);
            }
            if (JSON.isValidObject(text)) {

                return JSON.parseObject(text);
            }

            return JSON.parse(text);
        } catch (JSONException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw IOException.class.cast(e.getCause());
            }
            throw e;
        }

    }

}
