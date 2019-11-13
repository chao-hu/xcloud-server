package com.xxx.xcloud.module.ceph.pojo;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.amazonaws.services.s3.model.S3ObjectSummary;


/**
 * @ClassName: ObjectInBucket
 * @Description: 对象
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
public class ObjectInBucket extends S3ObjectSummary {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3660738353937355673L;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public ObjectInBucket(S3ObjectSummary s3ObjectSummary) {
        String[] nameS = s3ObjectSummary.getBucketName().split("bucket");
        this.bucketName = nameS.length > 1 ? nameS[1] : s3ObjectSummary.getBucketName();
        this.eTag = s3ObjectSummary.getETag();
        this.key = s3ObjectSummary.getKey();
        this.storageClass = s3ObjectSummary.getStorageClass();
        this.lastModified = s3ObjectSummary.getLastModified();
        this.size = s3ObjectSummary.getSize();
        this.owner = s3ObjectSummary.getOwner();
        this.createTime = s3ObjectSummary.getLastModified();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
