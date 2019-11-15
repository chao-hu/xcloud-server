package com.xxx.xcloud.module.ceph.model;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.amazonaws.services.s3.model.Bucket;


/**
 * @ClassName: CephBucket
 * @Description: 桶对象
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
public class CephBucket extends Bucket {

    private static final long serialVersionUID = 1L;

    private String projectId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
