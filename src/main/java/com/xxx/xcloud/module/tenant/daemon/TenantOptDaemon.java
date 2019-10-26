package com.xxx.xcloud.module.tenant.daemon;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.module.tenant.async.BaseTenantAsyncHelper;
import com.xxx.xcloud.module.tenant.entity.TenantOptLog;
import com.xxx.xcloud.module.tenant.repository.TenantOptLogRepository;
import com.xxx.xcloud.utils.SleepUtil;
import com.xxx.xcloud.utils.SpringUtils;

/**
 * @ClassName: TenantOptInit
 * @Description: 系统启动时， 用于对新建租户或删除租户未做完的步骤接着做，保证最后一定能做完
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
@Order(value = 3)
public class TenantOptDaemon implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(TenantOptDaemon.class);

    private static Integer INTERVAL = 1 * 60 * 1000;

    @Autowired
    TenantOptLogRepository tenantOptLogRepository;

    @Override
    public void run(String... args) throws Exception {

        logger.info("-----------TenantOptInit 开始补偿事务 --------");
        while (true) {

            List<TenantOptLog> list = tenantOptLogRepository.findAll();

            if (null != list && !list.isEmpty()) {

                list.stream().filter(obj -> obj.getRetries() <= 3).forEach(obj -> retry(obj));
            }
            SleepUtil.sleep(INTERVAL);
        }
    }

    public void retry(TenantOptLog obj) {

        String opt = obj.getOpt();

        String[] opts = opt.split(BaseTenantAsyncHelper.OPT_SEPAEATOR);

        String className = opts[0];
        String methodName = opts[1];

        try {
            Object bean = SpringUtils.getBean(className);

            Method m = bean.getClass().getMethod(methodName, String.class);

            m.invoke(bean, obj.getTenantName());

        } catch (NoSuchMethodException e) {

            logger.error("没有找到这个方法!" + opt, e);

        } catch (SecurityException e) {

            logger.error("执行方法 " + opt + " 出权限问题!", e);
        } catch (Exception e) {

            logger.error("执行方法 " + opt + " " + e.getMessage(), e);
        }
    }
}
