package com.xxx.xcloud.module.component.model.kafka;

import com.xxx.xcloud.module.component.consts.KafkaClusterConst;

import io.fabric8.kubernetes.client.CustomResource;

/**
 * <p>
 * kafka yaml文件对应的集群实体
 *
 * @author xujiangpeng
 * @date 2018/6/12
 */
public class KafkaCluster extends CustomResource {

    private static final long serialVersionUID = -6030071869889749688L;
    private KafkaSpec spec;
    private KafkaStatus status;

    public KafkaCluster() {
        super();
        super.setApiVersion(KafkaClusterConst.API_VERSION);
        super.setKind(KafkaClusterConst.KIND);
    }

    public KafkaSpec getSpec() {
        return spec;
    }

    public void setSpec(KafkaSpec spec) {
        this.spec = spec;
    }

    public KafkaStatus getStatus() {
        return status;
    }

    public void setStatus(KafkaStatus status) {
        this.status = status;
    }
}
