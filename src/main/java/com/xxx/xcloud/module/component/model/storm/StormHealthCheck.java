package com.xxx.xcloud.module.component.model.storm;

import com.xxx.xcloud.module.component.model.base.HealthCheck;

public class StormHealthCheck extends HealthCheck {

    private boolean isEnable;

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

}
