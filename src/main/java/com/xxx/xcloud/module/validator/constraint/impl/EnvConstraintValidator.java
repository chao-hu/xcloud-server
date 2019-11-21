package com.xxx.xcloud.module.validator.constraint.impl;

import com.alibaba.fastjson.*;
import com.xxx.xcloud.module.validator.constraint.*;
import com.xxx.xcloud.utils.*;

import javax.validation.*;
import java.util.*;

/**
 * 环境变量信息验证
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年7月25日 下午3:12:00
 */
public class EnvConstraintValidator implements ConstraintValidator<Env, String> {

    /**
     * 过滤字符串
     */
    private String forbidden;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.isEmpty(value)) {
            try {
                JSON.parseObject(value, new TypeReference<Map<String, String>>() {
                });
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

}
