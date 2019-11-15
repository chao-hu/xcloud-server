package com.xxx.xcloud.rest.v1.service.model;

import java.util.List;

import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceAffinity;
import com.xxx.xcloud.module.application.entity.ServiceHealth;

/**
 *
 * <p>
 * Description: service对象，用于外部接口创建服务时使用
 * </p>
 *
 * @author wangkebiao
 * @date 2018年12月3日
 */
public class ServiceRequest extends Service {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8353815292388047450L;

    /**
     * 配置文件json串
     */
    private String config;

    /**
     * 文件存储json串
     */
    private String storageFile;

    /**
     * 块存储json串
     */
    private String storageRbd;

    /**
     * 本地存储json串
     */
    private String storageLocal;

    /**
     * 亲和属性 -- 节点亲和状态，请参见 {@link com.xxx.xcloud.consts.Global}
     */
    private Byte nodeAffinityType;

    /**
     * 亲和属性 -- 节点列表，以半英逗号分隔
     */
    private String nodeAffinity;

    /**
     * 亲和属性 -- 服务亲和状态，请参见 {@link com.xxx.xcloud.consts.Global}
     */
    private Byte serviceAffinityType;

    /**
     * 亲和属性 -- 亲和/反亲和的服务名称
     */
    private String serviceAffinity;

    /**
     * 健康检查设置
     */
    private List<ServiceHealth> healthCheck;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(String storageFile) {
        this.storageFile = storageFile;
    }

    public String getStorageRbd() {
        return storageRbd;
    }

    public void setStorageRbd(String storageRbd) {
        this.storageRbd = storageRbd;
    }

    public String getStorageLocal() {
        return storageLocal;
    }

    public void setStorageLocal(String storageLocal) {
        this.storageLocal = storageLocal;
    }

    public Byte getNodeAffinityType() {
        return nodeAffinityType;
    }

    public void setNodeAffinityType(Byte nodeAffinityType) {
        this.nodeAffinityType = nodeAffinityType;
    }

    public String getNodeAffinity() {
        return nodeAffinity;
    }

    public void setNodeAffinity(String nodeAffinity) {
        this.nodeAffinity = nodeAffinity;
    }

    public Byte getServiceAffinityType() {
        return serviceAffinityType;
    }

    public void setServiceAffinityType(Byte serviceAffinityType) {
        this.serviceAffinityType = serviceAffinityType;
    }

    public String getServiceAffinity() {
        return serviceAffinity;
    }

    public void setServiceAffinity(String serviceAffinity) {
        this.serviceAffinity = serviceAffinity;
    }

    public List<ServiceHealth> getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(List<ServiceHealth> healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     * 
     * <p>
     * Description:根据入参，拼接ServiceAffinity
     * </p>
     *
     * @return ServiceAffinity 服务亲和对象
     */
    public ServiceAffinity getAffinity() {
        ServiceAffinity affinity = new ServiceAffinity();
        affinity.setNodeAffinity(nodeAffinity);
        affinity.setNodeAffinityType(nodeAffinityType);
        affinity.setServiceAffinity(serviceAffinity);
        affinity.setServiceAffinityType(serviceAffinityType);
        return affinity;
    }

    public Service getService() {
        Service service = new Service();
        service.setId(getId());
        service.setServiceName(getServiceName());
        service.setStatus(getStatus());
        service.setCpu(getCpu());
        service.setMemory(getMemory());
        service.setInstance(getInstance());
        service.setTenantName(getTenantName());
        service.setImageVersionId(getImageVersionId());
        service.setCmd(getCmd());
        service.setPortAndProtocol(getPortAndProtocol());
        service.setEnv(getEnv());
        service.setIsPodMutex(getIsPodMutex());
        service.setHpa(getHpa());
        service.setDescription(getDescription());
        service.setCreateTime(getCreateTime());
        service.setUpdateTime(getUpdateTime());
        service.setProjectId(getProjectId());
        service.setCreatedBy(getCreatedBy());
        service.setIsUsedApm(getIsUsedApm());
        service.setImageName(getImageName());
        service.setRegistryImageName(getRegistryImageName());
        service.setIsRestartEffect(false);
        service.setGpu(getGpu());
        service.setHostAliases(getHostAliases());
        service.setInitContainer(getInitContainer());
        return service;
    }

    @Override
    public String toString() {
        return "ServiceRequest [config=" + config + ", storageFile=" + storageFile + ", storageRbd=" + storageRbd
                + ", storageLocal=" + storageLocal + ", nodeAffinityType=" + nodeAffinityType + ", nodeAffinity="
                + nodeAffinity + ", serviceAffinityType=" + serviceAffinityType + ", serviceAffinity=" + serviceAffinity
                + ", healthCheck=" + healthCheck + "]";
    }

}
