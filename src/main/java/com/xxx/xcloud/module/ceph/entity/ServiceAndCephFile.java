package com.xxx.xcloud.module.ceph.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

/**
 * 
 * <p>
 * Description: 服务和文件存储关联实体对象
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Data
@Entity
@Table(name = "`service_and_ceph_file`")
public class ServiceAndCephFile implements Serializable {

    private static final long serialVersionUID = 9164292964561015906L;

    @Transient
    private CephFile cephFile;

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`SERVICE_ID`")
    private String serviceId;

    @Column(name = "`CEPH_FILE_ID`")
    private String cephFileId;

    @Column(name = "`MOUNT_PATH`")
    private String mountPath;
    /**
     * 挂载存储卷的服务类型{@link com.xxx.xcloud.consts.Global.MOUNTCEPHFILE_SERVICE}
     */
    @Column(name = "`SERVICE_TYPE`")
    private String serviceType;

}
