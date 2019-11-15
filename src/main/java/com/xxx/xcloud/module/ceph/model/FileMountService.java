package com.xxx.xcloud.module.ceph.model;

import java.io.Serializable;

import lombok.Data;


/**
 * @ClassName: FileMountService
 * @Description: 服务文件挂载
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
@Data
public class FileMountService implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceId;

    private String serviceName;

    private String serviceType;

}
