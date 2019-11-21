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
 * @ClassName: StatefulServiceComponentDefaultConfig
 * @Description: 组件默认的配置参数表
 * @author lnn
 * @date 2019年11月14日
 *
 */
@Entity
@Table(name = "`STATEFUL_SERVICE_COMPONENT_DEFAULT_CONFIG`", indexes = {
        @Index(name = "app_version", columnList = "`APP_TYPE`,`VERSION`") })
public class StatefulServiceComponentDefaultConfig implements Serializable {

    private static final long serialVersionUID = -2986610275872959012L;

    /**
     * @Fields: id
     */
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    /**
     * @Fields: 组件类型
     */
    @Column(name = "`APP_TYPE`", length = 50)
    private String appType;

    /**
     * @Fields: 配置参数名称
     */
    @Column(name = "`KEY`")
    private String key;

    /**
     * @Fields: 配置参数中文名称
     */
    @Column(name = "`NAME`")
    private String name;

    /**
     * @Fields: 参数默认值
     */
    @Column(name = "`DEF_VALUE`")
    private String defValue;

    /**
     * @Fields: 参数所在的配置文件名称
     */
    @Column(name = "`CNF_FILE`")
    private String cnfFile;

    /**
     * @Fields: 版本
     */
    @Column(name = "`VERSION`", length = 50)
    private String version;

    /**
     * @Fields: 是否允许更改
     */
    @Column(name = "`UPDATE_ENABLE`")
    private int updateEnable;

    /**
     * @Fields: 参数在配置文件中所属的组
     */
    @Column(name = "`GROUP`")
    private String group;

    /**
     * @Fields: 参数等级，普通还是高级
     */
    @Column(name = "`SHOW_LEVEL`")
    private int showLevel;

    /**
     * @Fields: 参数值类型，如int、String
     */
    @Column(name = "`VALUE_TYPE`")
    private String valueType; 

    /**
     * @Fields: 参数值正则
     */
    @Column(name = "`REGX`", length = 2000)
    private String regx;

    /**
     * @Fields: 正则描述
     */
    @Column(name = "`REGX_REMARK`")
    private String regxRemark;

    /**
     * @Fields: 参数值的展示类型，如input、radio
     */
    @Column(name = "`INPUT_TYPE`")
    private String inputType;

    /**
     * @Fields: 参数值展示与value值映射，如{"是":"yes","否":"no"}
     */
    @Column(name = "`OPTIONAL`", length = 2000)
    private String optional;

    /**
     * @Fields: 参数含义描述
     */
    @Column(name = "`REMARK`", length = 2000)
    private String remark;

    /**
     * @Fields: 排序
     */
    @Column(name = "`SORT`")
    private int sort;

    /**
     * @Fields: 页面展示的单位名称
     */
    @Column(name = "`UNIT_REMARK`", length = 2000)
    private String unitRemark;

    /**
     * @Fields: 换算值，用于有单位的参数进行数值转换
     */
    @Column(name = "`PRODUCT`", columnDefinition = "INT default 1")
    private int product;

    /**
     * @Fields: 存入配置中的参数单位，不同于页面展示的参数值的单位
     */
    @Column(name = "`UNIT`")
    private String unit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public String getCnfFile() {
        return cnfFile;
    }

    public void setCnfFile(String cnfFile) {
        this.cnfFile = cnfFile;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getUpdateEnable() {
        return updateEnable;
    }

    public void setUpdateEnable(int updateEnable) {
        this.updateEnable = updateEnable;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getShowLevel() {
        return showLevel;
    }

    public void setShowLevel(int showLevel) {
        this.showLevel = showLevel;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getRegx() {
        return regx;
    }

    public void setRegx(String regx) {
        this.regx = regx;
    }

    public String getRegxRemark() {
        return regxRemark;
    }

    public void setRegxRemark(String regxRemark) {
        this.regxRemark = regxRemark;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getOptional() {
        return optional;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getUnitRemark() {
        return unitRemark;
    }

    public void setUnitRemark(String unitRemark) {
        this.unitRemark = unitRemark;
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "StatefulServiceComponentDefaultConfig [id=" + id + ", appType=" + appType + ", key=" + key + ", name="
                + name + ", defValue=" + defValue + ", cnfFile=" + cnfFile + ", version=" + version + ", updateEnable="
                + updateEnable + ", group=" + group + ", showLevel=" + showLevel + ", valueType=" + valueType
                + ", regx=" + regx + ", regxRemark=" + regxRemark + ", inputType=" + inputType + ", optional="
                + optional + ", remark=" + remark + ", sort=" + sort + ", unitRemark=" + unitRemark + ", product="
                + product + ", unit=" + unit + "]";
    }

}
