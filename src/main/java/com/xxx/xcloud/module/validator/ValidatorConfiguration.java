package com.xxx.xcloud.module.validator;

import org.hibernate.validator.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.*;

import javax.validation.*;

/**
 * 配置hibernate validator模式为快速失败返回模式
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年7月25日 下午3:00:24
 */
@Configuration
public class ValidatorConfiguration {
    @Bean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure()
                .addProperty("hibernate.validator.fail_fast", "true").buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        return validator;
    }

}
