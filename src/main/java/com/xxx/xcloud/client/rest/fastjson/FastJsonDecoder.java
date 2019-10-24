package com.xxx.xcloud.client.rest.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;

/**
 * @ClassName: FastJsonDecoder
 * @Description: fastjsondecoder
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class FastJsonDecoder implements Decoder {

    /**
     * @Title: decode
     * @Description:
     * @param response
     * @param type
     * @return
     * @throws IOException
     * @throws DecodeException
     * @throws FeignException
     * @see feign.codec.Decoder#decode(feign.Response, java.lang.reflect.Type)
     */
    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {

        if (response.status() == HttpStatus.SC_NOT_FOUND) {
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
