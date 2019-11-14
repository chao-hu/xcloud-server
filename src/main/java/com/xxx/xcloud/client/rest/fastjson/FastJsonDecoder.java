package com.xxx.xcloud.client.rest.fastjson;

import com.alibaba.fastjson.*;
import feign.*;
import feign.codec.*;
import org.apache.commons.io.*;
import org.apache.http.*;

import java.io.*;
import java.lang.reflect.*;

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
            InputStream is = response.body().asInputStream();
            String str = IOUtils.toString(is, "utf-8");

            return JSON.parse(str);
        } catch (JSONException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw IOException.class.cast(e.getCause());
            }

            throw e;
        }
    }


}
