package com.xxx.xcloud.module.component.model.codis;

import java.util.Map;

/**
 * @ClassName: CodisGroupStatus
 * @Description: CodisGroupStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisGroupStatus {

    private int id;
    private boolean onDashboard;
    private Map<Integer, CodisGroupBindingNode> bindings;
    private String status;

    public Map<Integer, CodisGroupBindingNode> getBindings() {
        return bindings;
    }

    public void setBindings(Map<Integer, CodisGroupBindingNode> bindings) {
        this.bindings = bindings;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOnDashboard() {
        return onDashboard;
    }

    public void setOnDashboard(boolean onDashboard) {
        this.onDashboard = onDashboard;
    }

}
