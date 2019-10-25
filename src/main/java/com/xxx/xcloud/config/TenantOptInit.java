package com.xxx.xcloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.utils.SleepUtil;

/**
 * @ClassName: TenantOptInit
 * @Description: 系统启动时， 用于对新建租户或删除租户未做完的步骤接着做，保证最后一定能做完
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
@Order(value = 3)
public class TenantOptInit implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(TenantOptInit.class);

    private static Integer INTERVAL = 1 * 60 * 1000;

    @Override
    public void run(String... args) throws Exception {

        logger.info("-----------TenantOptInit 开始补偿事务 --------");
        while (true) {

            SleepUtil.sleep(INTERVAL);
        }

    }
}
