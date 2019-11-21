package com.xxx.xcloud.module.component.model.ftp;

import java.util.Map;

import com.xxx.xcloud.module.component.model.base.HealthCheck;

/**
 * @ClassName: FtpConfig
 * @Description: FtpConfig
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class FtpConfig extends HealthCheck {

    /**
     * 配置项
     */
    private Map<String, String> usercnf;

    public Map<String, String> getUsercnf() {
        return usercnf;
    }

    public void setUsercnf(Map<String, String> usercnf) {
        this.usercnf = usercnf;
    }
}
