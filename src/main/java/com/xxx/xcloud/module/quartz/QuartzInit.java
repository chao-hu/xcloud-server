package com.xxx.xcloud.module.quartz;

import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.entity.Ci;
import com.xxx.xcloud.module.ci.service.ICiService;
import com.xxx.xcloud.module.sonar.entity.CodeCheckTask;
import com.xxx.xcloud.module.sonar.service.SonarService;
import com.xxx.xcloud.utils.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务初始化类
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年1月3日 下午3:59:16
 */
@Service
public class QuartzInit implements ApplicationRunner {

    @Autowired
    private ICiService ciService;

    @Autowired
    private SonarService sonarService;

    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(QuartzInit.class);

    @Override
    public void run(ApplicationArguments app) throws Exception {
        LOG.info("初始化定时任务");
        initAllJobs();
    }

    /**
     * 初始化所有定时任务
     *
     * @throws
     * @date: 2019年1月3日 下午4:01:24
     */
    private void initAllJobs() throws SchedulerException {
        LOG.info("添加代码构建定时任务----开始");
        List<Ci> cis = ciService.getCisCronIsNotNull();
        if (cis != null) {
            for (Ci ci : cis) {
                if (!StringUtils.isEmpty(ci.getCron())
                        && ci.getConstructionStatus() != CiConstant.CONSTRUCTION_STATUS_DISABLED) {
                    ciService.addCodeCiQuartz(ci);
                }
            }
        }
        LOG.info("添加代码构建定时任务----完成");

        // 代码检查先不写
        LOG.info("添加代码检查定时任务----开始");
        List<CodeCheckTask> codeCheckTasks = sonarService.getCodeCheckTasksCronIsNotNull();
        if (codeCheckTasks != null) {
            for (CodeCheckTask codeCheckTask : codeCheckTasks) {
                if (!StringUtils.isEmpty(codeCheckTask.getCron())
                        && codeCheckTask.getStatus() != CiConstant.CONSTRUCTION_STATUS_DISABLED) {
                    sonarService.addCodeCheckQuartz(codeCheckTask);
                }
            }
        }
        LOG.info("添加代码检查定时任务----完成");
    }

}
