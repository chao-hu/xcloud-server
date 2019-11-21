package com.xxx.xcloud.module.component.model.base;

/**
 * @ClassName: Resources
 * @Description: Resources
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class Resources {
    private MemoryCpu requests;
    private MemoryCpu limits;

    public MemoryCpu getRequests() {
        return requests;
    }

    public void setRequests(MemoryCpu requests) {
        this.requests = requests;
    }

    public MemoryCpu getLimits() {
        return limits;
    }

    public void setLimits(MemoryCpu limits) {
        this.limits = limits;
    }

}
