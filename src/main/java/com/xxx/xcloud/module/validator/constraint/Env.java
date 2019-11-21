package com.xxx.xcloud.module.validator.constraint;

import com.xxx.xcloud.module.validator.constraint.impl.*;

import javax.validation.*;
import java.lang.annotation.*;

/**
 * @author xjp
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnvConstraintValidator.class)
public @interface Env {

    /**
     * 验证不通过错误提示
     *
     * @return String
     * @date: 2019年7月25日 下午3:27:23
     */
    String message() default "环境变量数据不符合{key:value,key2:value2}格式！";

    /**
     * 拦截字符串
     *
     * @return String
     * @date: 2019年7月25日 下午3:28:00
     */
    String forbidden() default "";

    // groups 和 payload 这两个parameter 必须包含
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
