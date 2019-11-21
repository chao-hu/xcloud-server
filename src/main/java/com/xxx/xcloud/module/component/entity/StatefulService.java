package com.xxx.xcloud.module.component.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.KafkaClusterConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.utils.StringUtils;

/**
 * @ClassName: StatefulService
 * @Description: 组件集群表
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_SERVICE`", indexes = { @Index(name = "`idx_namespace`", columnList = "`NAMESPACE`") })
public class StatefulService implements Serializable {

    private static final long serialVersionUID = -7865176457067568717L;

    /**
     * @Fields: 集群ID
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * @Fields: 集群名称
     */
    @Column(name = "`SERVICE_NAME`")
    private String serviceName;

    /**
     * @Fields: 部署服务版本
     */
    @Column(name = "`VERSION`")
    private String version;

    /**
     * @Fields: 节点个数
     */
    @Column(name = "`NODENUM`")
    private int nodeNum;

    /**
     * @Fields: 框架类型
     */
    @Column(name = "`APP_TYPE`")
    private String appType;

    /**
     * @Fields: 集群状态
     */
    @Column(name = "`SERVICE_STATE`")
    private String serviceState;

    /**
     * @Fields: 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`CREATE_TIME`")
    private Date createTime;

    /**
     * @Fields: 命名空间
     */
    @Column(name = "`NAMESPACE`")
    private String namespace;

    /**
     * @Fields: 创建人
     */
    @Column(name = "`CREATED_BY`")
    private String createdBy;

    /**
     * @Fields: 集群总的cpu
     */
    @Column(name = "`CPU`")
    private Double cpu;

    /**
     * @Fields: 集群总的memory
     */
    @Column(name = "`MEMORY`")
    private Double memory;

    /**
     * @Fields: 集群总的存储
     */
    @Column(name = "`STORAGE`")
    private Double storage;

    /**
     * @Fields: 框架模式
     */
    @Column(name = "`TYPE`")
    private String type;

    /**
     * @Fields: 集群密码
     */
    @Column(name = "`PASSWORD`")
    private String password;

    /**
     * @Fields: 项目id
     */
    @Column(name = "`PROJECT_ID`")
    private String projectId;

    /**
     * @Fields: 订购id
     */
    @Column(name = "`ORDER_ID`")
    private String orderId;

    /**
     * @Fields: 最后一次操作的时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "`LASTOPT_TIME`")
    private Date lastoptTime;

    /**
     * @Fields: 扩展字段
     */
    @Column(name = "`EXTENDED_FIELD`", columnDefinition = "text")
    private String extendedField;

    /**
     * @Fields: 服务配置
     */
    @Column(name = "`CONFIG_UPDATED`", columnDefinition = "text")
    private String configUpdated;

    /**
     * @Fields: 上次操作
     */
    @Column(name = "`LASTOPT`", length = 50)
    private String lastopt; 

    /**
     * @Fields: 不持久字段，用于返回数据库中没有的info给页面
     */
    @Transient
    private JSONObject infoJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public Double getStorage() {
        return storage;
    }

    public void setStorage(Double storage) {
        this.storage = storage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Date getLastoptTime() {
        return lastoptTime;
    }

    public void setLastoptTime(Date lastoptTime) {
        this.lastoptTime = lastoptTime;
    }

    public String getExtendedField() {
        return extendedField;
    }

    public void setExtendedField(String extendedField) {
        this.extendedField = extendedField;
    }

    public String getConfigUpdated() {
        return configUpdated;
    }

    public void setConfigUpdated(String configUpdated) {
        this.configUpdated = configUpdated;
    }

    public JSONObject getInfoJson() {
        if (!StringUtils.isEmpty(this.extendedField)) {
            getNewInfoJson();
        }

        if (null != this.infoJson && !this.infoJson.isEmpty()) {
            return this.infoJson;
        }
        return null;

    }

    private void getNewInfoJson() {
        JSONObject extendedJson = JSON.parseObject(this.extendedField);
        Object kibanaIp = null;
        Object kibanaPort = null;
        Object phpMyadminHost = null;
        Object phpMyadminPort = null;
        this.infoJson = new JSONObject();
        for (Map.Entry<String, Object> extendedEntry : extendedJson.entrySet()) {
            JSONObject infoJsonElement = new JSONObject();
            switch (extendedEntry.getKey()) {
            // zookeeper,storm,prometheus,postgresql,kafka,es,mysql
            case (CommonConst.RESOURCE_EFFECTIVE):
                infoJsonElement.put("name", "资源是否生效");
                String resourceValue = Boolean.parseBoolean(String.valueOf(extendedEntry.getValue())) ? "是" : "否";
                infoJsonElement.put("value", resourceValue);
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            case (CommonConst.CONFIG_EFFECTIVE):
                infoJsonElement.put("name", "配置是否生效");
                String configValue = Boolean.parseBoolean(String.valueOf(extendedEntry.getValue())) ? "是" : "否";
                infoJsonElement.put("value", configValue);
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            // redis,memcached,codis
            case (CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE):
                infoJsonElement.put("name", "资源或配置是否生效");
                String resourceOrConfigValue = Boolean.parseBoolean(String.valueOf(extendedEntry.getValue())) ? "是"
                        : "否";
                infoJsonElement.put("value", resourceOrConfigValue);
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            // storm,kafka,codis
            case ("zkServiceName"):
                infoJsonElement.put("name", "zookeeper服务名");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            // postgresql,mysql
            case ("user"):
                infoJsonElement.put("name", "用户名");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            // case ("replUser"):
            // infoJsonElement.put("name", "复制用户名");
            // infoJsonElement.put("value", extendedEntry.getValue());
            // this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
            // break;
            // case ("replPassword"):
            // infoJsonElement.put("name", "复制用户密码");
            // infoJsonElement.put("value", extendedEntry.getValue());
            // this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
            // break;
            // mysql
            case ("phpMyadminHost"):
                phpMyadminHost = extendedEntry.getValue();
                break;
            case ("phpMyadminPort"):
                phpMyadminPort = extendedEntry.getValue();
                break;
            // zookeeper
            case ("interAddress"):
                if (CommonConst.APPTYPE_ZK.equals(this.appType)) {
                    infoJsonElement.put("name", "zookeeper内部地址");
                    infoJsonElement.put("value", extendedEntry.getValue());
                    this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                }
                // storm
                if (CommonConst.APPTYPE_STORM.equals(this.appType)) {
                    infoJsonElement.put("name", "依赖的zookeeper内部地址");
                    infoJsonElement.put("value", extendedEntry.getValue());
                    this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                }
                break;

            case ("exterAddress"):
                if (CommonConst.APPTYPE_ZK.equals(this.appType)) {
                    infoJsonElement.put("name", "zookeeper外部地址");
                    infoJsonElement.put("value", extendedEntry.getValue());
                    this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                }
                break;
            // storm
            case (StormClusterConst.NIMBUS_UI_URL):
                infoJsonElement.put("name", "nimbus访问网址");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
//            case (StormClusterConst.JAR_UPLOAD_PATH_KEY):
//                infoJsonElement.put("name", "jar包上传路径");
//                infoJsonElement.put("value", extendedEntry.getValue());
//                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
//                break;
                
            // kafka
            case (KafkaClusterConst.ZOOKEEPER_SERVERS):
                infoJsonElement.put("name", "zookeeper连接串");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            // codis
            case ("proxyReplicas"):
                infoJsonElement.put("name", "proxy实例个数");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            // es
            // case ("masterSeparateFlag"):
            // infoJsonElement.put("name", "是否使用master分离模式");
            // String flagValue =
            // Boolean.parseBoolean(String.valueOf(extendedEntry.getValue())) ?
            // "是" : "否";
            // infoJsonElement.put("value", flagValue);
            // this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
            // break;
            case ("masterReplicas"):
                infoJsonElement.put("name", "master节点数");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            case ("dataReplicas"):
                infoJsonElement.put("name", "data节点数");
                infoJsonElement.put("value", extendedEntry.getValue());
                this.infoJson.put(extendedEntry.getKey(), infoJsonElement);
                break;
            case ("kibanaIp"):
                kibanaIp = extendedEntry.getValue();
                break;
            case ("kibanaPort"):
                kibanaPort = extendedEntry.getValue();
                break;
            default:
                break;
            }
        }

        // es
        if (null != kibanaIp && null != kibanaPort) {
            String kibanaPath = "http://" + String.valueOf(kibanaIp) + ":" + String.valueOf(kibanaPort) + "/";
            JSONObject infoJsonElement = new JSONObject();
            infoJsonElement.put("name", "kibana地址");
            infoJsonElement.put("value", kibanaPath);
            this.infoJson.put("kibanaPath", infoJsonElement);
        }

        // mysql
        if (null != phpMyadminHost && null != phpMyadminPort) {
            String phpMyadminPath = "http://" + String.valueOf(phpMyadminHost) + ":" + String.valueOf(phpMyadminPort)
                    + "/";
            JSONObject infoJsonElement = new JSONObject();
            infoJsonElement.put("name", "phpMyadmin地址");
            infoJsonElement.put("value", phpMyadminPath);
            this.infoJson.put("phpMyadminPath", infoJsonElement);
        }
    }

    public void setInfoJson(String infoJson) {
        this.infoJson = JSON.parseObject(infoJson);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastopt() {
        return lastopt;
    }

    public void setLastopt(String lastopt) {
        this.lastopt = lastopt;
    }

    @Override
    public String toString() {
        return "StatefulService [id=" + id + ", serviceName=" + serviceName + ", version=" + version + ", nodeNum="
                + nodeNum + ", appType=" + appType + ", serviceState=" + serviceState + ", createTime=" + createTime
                + ", namespace=" + namespace + ", createdBy=" + createdBy + ", cpu=" + cpu + ", memory=" + memory
                + ", storage=" + storage + ", type=" + type + ", password=" + password + ", projectId=" + projectId
                + ", orderId=" + orderId + ", lastoptTime=" + lastoptTime + ", extendedField=" + extendedField
                + ", configUpdated=" + configUpdated + ", lastopt=" + lastopt + ", infoJson=" + infoJson + "]";
    }

}
