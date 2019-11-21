package com.xxx.xcloud.module.component.model.kafka;

/**
 * @author xujiangpeng
 * @date 2018/6/12
 */
public class KafkaOp {

    /**
     * 集群操作
     */
    private String operator;

    /**
     * 节点名称
     */
    private String nodename;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getNodename() {
        return nodename;
    }

    public void setNodename(String nodename) {
        this.nodename = nodename;
    }
}
