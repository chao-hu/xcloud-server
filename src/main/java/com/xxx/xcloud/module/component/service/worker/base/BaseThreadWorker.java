package com.xxx.xcloud.module.component.service.worker.base;

/**
 * @ClassName: BaseThreadWorker
 * @Description: BaseThreadWorker
 * @author lnn
 * @date 2019年11月21日
 *
 * @param <Map>
 */
public abstract class BaseThreadWorker<Map> implements Runnable {

    protected Map data = null;

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    @Override
    public void run() {
        execute();
    }

    /**
     * 执行函数 
     */
    public abstract void execute();

}
