package com.xxx.xcloud.module.component.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * @ClassName: StatefulServiceUnitVersion
 * @Description: 组件统一安装包版本管理表
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_SERVICE_UNIT_VERSION`", indexes = {
        @Index(name = "`idx_app`", columnList = "`APP_TYPE`") })
public class StatefulServiceUnitVersion implements Serializable {

    private static final long serialVersionUID = 8773919588609066234L;

    /**
     * @Fields: id
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    @Column(name = "`ID`")
    private int id;

    /**
     * @Fields: 组件类型
     */
    @Column(name = "`APP_TYPE`") 
    private String appType;

    /**
     * @Fields: 版本
     */
    @Column(name = "`VERSION`")
    private String version;

    /**
     * @Fields: 镜像地址
     */
    @Column(name = "`VERSION_PATH`")
    private String versionPath;

    /**
     * @Fields: 排序
     */
    @Column(name = "`SORT`")
    private int sort;

    /**
     * @Fields: 备注
     */
    @Column(name = "`REMARK`", length = 3000)
    private String remark;

    /**
     * @Fields: 扩展字段，如exporter
     */
    @Column(name = "`EXTENDED_FIELD`")
    private String extendedField;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionPath() {
        return versionPath;
    }

    public void setVersionPath(String versionPath) {
        this.versionPath = versionPath;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExtendedField() {
        return extendedField;
    }

    public void setExtendedField(String extendedField) {
        this.extendedField = extendedField;
    }

    @Override
    public String toString() {
        return "StatefulServiceUnitVersion [id=" + id + ", appType=" + appType + ", version=" + version
                + ", versionPath=" + versionPath + ", sort=" + sort + ", remark=" + remark + ", extendedField="
                + extendedField + "]";
    }

}
