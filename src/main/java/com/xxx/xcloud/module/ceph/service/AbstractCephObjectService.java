package com.xxx.xcloud.module.ceph.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.xxx.xcloud.client.ceph.CephClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;

/**
 * 
 * <p>
 * Description: 对象存储抽象类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
public abstract class AbstractCephObjectService implements CephObjectService {

    private static final String TENANT_CREDENTIAL_CREATE_FAILED = "租户:%s对象存储认证创建失败";
    private static final String TENANT_CREDENTIAL_DELETE_FAILED = "租户:%s对象存储认证清空失败";

    protected static final Logger log = LoggerFactory.getLogger(AbstractCephObjectService.class);

    @Override
    public boolean createCephObjUser(String tenantName) {
        try {
            CephClientFactory.getCephObjectClient().createUser(tenantName);
            CephClientFactory.getCephObjectClient().createS3Credential(tenantName, tenantName, tenantName);
        } catch (Exception e) {
            String msg = String.format(TENANT_CREDENTIAL_CREATE_FAILED, tenantName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INIT, msg);
        }
        return true;
    }

    @Override
    public boolean destroyCephObjUser(String tenantName) {
        AWSCredentials awsCredentials = getCredentials(tenantName);

        try {
            List<Bucket> buckets = null;
            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
                @Override
                public void refresh() {

                }

                @Override
                public AWSCredentials getCredentials() {
                    return awsCredentials;
                }
            };

            listBucketsRequest.setRequestCredentialsProvider(awsCredentialsProvider);
            buckets = CephClientFactory.getCephObjectConn().listBuckets(listBucketsRequest);

            for (Bucket bucket : buckets) {
                DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucket.getName());
                deleteBucketRequest.setRequestCredentialsProvider(awsCredentialsProvider);
                CephClientFactory.getCephObjectConn().deleteBucket(deleteBucketRequest);
            }

            CephClientFactory.getCephObjectClient().removeS3Credential(tenantName, tenantName);
            CephClientFactory.getCephObjectClient().removeUser(tenantName);
        } catch (Exception e) {
            String msg = String.format(TENANT_CREDENTIAL_DELETE_FAILED, tenantName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DESTROY, msg);
        }

        return true;
    }

    /**
     * 获取Credentials
     * @Title: getCredentials
     * @Description: 获取Credentials
     * @param tenantName 租户名称
     * @return AWSCredentials 
     * @throws
     */
    protected abstract AWSCredentials getCredentials(String tenantName);
}
