package com.xxx.xcloud.config;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.system.entity.SysClusterInfo;
import com.xxx.xcloud.module.system.repository.SysClusterInfoRepository;

/**
 * @ClassName: SystemConfigInit
 * @Description: 系统启动时， 加载 系统配置信息
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
@Order(value = 1)
public class SystemConfigInit implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(SystemConfigInit.class);

    private static String sqlPath = "/opt/server/system.sql";

    @Autowired
    private SysClusterInfoRepository sysClusterInfoRepository;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {

        logger.info("-----------SystemConfigInit--------导入系统配置开始-----------");
        List<SysClusterInfo> sysConfigList = sysClusterInfoRepository.findAll();
        if (null != sysConfigList && !sysConfigList.isEmpty()) {
            logger.info("-----------sysConfigList--------配置不为空-----------");
            logger.info("-----------sysConfigList----" + JSON.toJSONString(sysConfigList));

            for (SysClusterInfo config : sysConfigList) {
                XcloudProperties.getConfigMap().put(config.getCfgKey(), config.getCfgValue());
            }
        } else {
            // 执行SQL脚本插入并再次导入
            logger.info("-----------sysConfigList--------配置为空-----------");

            logger.info("-------sqlPath------------" + sqlPath);
            FileSystemResource rc = new FileSystemResource(sqlPath);

            if (!rc.exists()) {
                logger.info("-------sqlPath------------" + sqlPath + " 不存在!");
                return;
            }
            EncodedResource er = new EncodedResource(rc, "GBK");
            ScriptUtils.executeSqlScript(dataSource.getConnection(), er);

            sysConfigList = sysClusterInfoRepository.findAll();
            if (null != sysConfigList && !sysConfigList.isEmpty()) {
                for (SysClusterInfo config : sysConfigList) {
                    XcloudProperties.getConfigMap().put(config.getCfgKey(), config.getCfgValue());
                }
            } else {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "初始化数据库脚本失败");
            }
        }
        logger.info("-----------SystemConfigInit--------导入系统配置结束-----------");
    }
}
