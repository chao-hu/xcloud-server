package com.xxx.xcloud.module.ceph.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.xxx.xcloud.client.ceph.CephClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.pojo.AccessControlListEnum;
import com.xxx.xcloud.module.ceph.service.AbstractCephObjectService;
import com.xxx.xcloud.utils.StringUtils;

/**
 * 
 * <p>
 * Description: 对象存储功能实现类
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Service
public class CephObjectServiceImpl extends AbstractCephObjectService {

    private static final String BUCKET_NAME_ILLEGAL = "桶名称不合法，应由5到15位小写字母、数字和下划线组成，以字母开头";
    private static final String BUCKET_NOT_EXIST = "桶:%s不存在或不属于租户:%s";
    private static final String BUCKET_ACL_ILLEGAL = "acl参数不合法，应为private/publicread/publicreadwrite";
    private static final String BUCKET_STORAGECLASS_ILLEGAL = "storageClass参数不合法，应为STANDARD/GLACIER/STANDARD_IA";
    private static final String BUCKET_ALREADY_EXIST = "桶:%s已经存在";
    private static final String BUCKET_CREATE_FAILED = "桶:%s创建失败";
    private static final String BUCKET_NOT_EMPTY = "桶:%s非空，删除失败";
    private static final String BUCKET_DELETE_FAILED = "桶不存在或删除失败";
    private static final String UPLOAD_FAILED = "上传文件失败";
    private static final String UPLOAD_FILE_EMPTY = "上传文件为空";
    private static final String UPLOAD_FILE_ALREADY_EXIST = "文件已存在";
    private static final String DOWNLOAD_FAILED = "文件:%s下载失败";
    private static final String TENANT_NAME_ILLEGAL = "租户名为空";
    private static final String FILE_NAME_EMPTY = "文件名为空";
    private static final String DOWNLOAD_FILE_NOT_EXIST = "对象:%s不存在";
    private static final String DOWNLOAD_FILE_NAME_ILLEGAL = "下载文件名为空";
    private static final String CEPH_OBJ_CLIENT = "对象存储客户端异常";
    private static final String CHARSET_UTF = "utf-8";
    private static final String CHARSET_ISO = "ISO8859-1";

    @Override
    protected AWSCredentials getCredentials(String tenantName) {
        return new BasicAWSCredentials(tenantName, tenantName);
    }

    @Override
    public boolean createBucket(String tenantName, String bucketName, String accessControlList) {

        // check bucketName
        if (StringUtils.isEmpty(bucketName) || !bucketName.toLowerCase().equals(bucketName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, BUCKET_NAME_ILLEGAL);
        }

        // check acl
        AccessControlListEnum acl = AccessControlListEnum.getAcl(accessControlList);
        if (acl == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, BUCKET_ACL_ILLEGAL);
        }

        // check if conn is connected and the bucket exists
        boolean bucketExist = false;
        try {
            bucketExist = CephClientFactory.getCephObjectConn().doesBucketExistV2(bucketName);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, CEPH_OBJ_CLIENT);
        }

        if (bucketExist) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_EXIST,
                    String.format(BUCKET_ALREADY_EXIST, bucketName));
        }

        AWSCredentials awsCredentials = getCredentials(tenantName);
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        createBucketRequest.setCannedAcl(acl.acl);
        createBucketRequest.setRequestCredentialsProvider(new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return awsCredentials;
            }
        });
        try {
            CephClientFactory.getCephObjectConn().createBucket(createBucketRequest);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CREATE,
                    String.format(BUCKET_CREATE_FAILED, bucketName));
        }

        return true;
    }

    @Override
    public boolean deleteBucket(String tenantName, String bucketName) {
        AWSCredentials awsCredentials = getCredentials(tenantName);
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return awsCredentials;
            }
        };
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucketName);
        deleteBucketRequest.setRequestCredentialsProvider(awsCredentialsProvider);

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setRequestCredentialsProvider(awsCredentialsProvider);
        try {
            ObjectListing objectListing = CephClientFactory.getCephObjectConn().listObjects(listObjectsRequest);
            List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
            if (s3ObjectSummaries.size() > 0) {
                throw new ErrorMessageException(ReturnCode.CODE_CEPH_OCCUPIED,
                        String.format(BUCKET_NOT_EMPTY, bucketName));
            }

            CephClientFactory.getCephObjectConn().deleteBucket(deleteBucketRequest);
        } catch (SdkClientException e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, BUCKET_DELETE_FAILED);
        }

        return true;
    }

    @Override
    public Bucket getBucket(String tenantName, String bucketName) {
        AWSCredentials awsCredentials = getCredentials(tenantName);
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return awsCredentials;
            }
        };
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
        listBucketsRequest.setRequestCredentialsProvider(awsCredentialsProvider);
        List<Bucket> buckets = null;
        try {
            buckets = CephClientFactory.getCephObjectConn().listBuckets(listBucketsRequest);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, CEPH_OBJ_CLIENT);
        }

        for (Bucket bucket : buckets) {
            if (bucket.getName().equals(bucketName)) {
                return bucket;
            }
        }

        return null;
    }

    @Override
    public List<Bucket> listBuckets(String tenantName) {
        AWSCredentials awsCredentials = getCredentials(tenantName);
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return awsCredentials;
            }
        };
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
        listBucketsRequest.setRequestCredentialsProvider(awsCredentialsProvider);
        List<Bucket> buckets = null;
        try {
            buckets = CephClientFactory.getCephObjectConn().listBuckets(listBucketsRequest);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, CEPH_OBJ_CLIENT);
        }

        return buckets;
    }

    @Override
    public List<S3ObjectSummary> listObjects(String tenantName, String bucketName) {
        AWSCredentials awsCredentials = getCredentials(tenantName);
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return awsCredentials;
            }
        };
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setRequestCredentialsProvider(awsCredentialsProvider);
        List<S3ObjectSummary> objectSummaries = null;
        try {
            if (getBucket(tenantName, bucketName) == null) {
                throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                        String.format(BUCKET_NOT_EXIST, bucketName, tenantName));
            }
            objectSummaries = CephClientFactory.getCephObjectConn().listObjects(listObjectsRequest)
                    .getObjectSummaries();
        } catch (SdkClientException e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_CLIENT, CEPH_OBJ_CLIENT);
        }

        return objectSummaries;
    }

    @Override
    public boolean deleteObject(String tenantName, String bucketName, String objName) {
        AWSCredentials awsCredentials = getCredentials(tenantName);
        AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return awsCredentials;
            }
        };
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, objName);
        deleteObjectRequest.setRequestCredentialsProvider(awsCredentialsProvider);

        if (StringUtils.isEmpty(bucketName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, BUCKET_NAME_ILLEGAL);
        }

        if (StringUtils.isEmpty(objName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, FILE_NAME_EMPTY);
        }

        // check if the bucket exists and belongs to the tenant
        if (getBucket(tenantName, bucketName) == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                    String.format(BUCKET_NOT_EXIST, bucketName, tenantName));
        }

        try {
            CephClientFactory.getCephObjectConn().deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, CEPH_OBJ_CLIENT);
        }

        return true;
    }

    @Override
    public boolean upLoad(MultipartFile file, String tenantName, String acl, String storageClass, String bucketName) {
        AccessControlListEnum accessControlListEnum = AccessControlListEnum.getAcl(acl);
        if (accessControlListEnum == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, BUCKET_ACL_ILLEGAL);
        }

        StorageClass sClass = null;
        try {
            sClass = StorageClass.fromValue(storageClass);
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, BUCKET_STORAGECLASS_ILLEGAL);
        }

        if (file == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, UPLOAD_FILE_EMPTY);
        }

        if (StringUtils.isEmpty(tenantName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, TENANT_NAME_ILLEGAL);
        }

        if (StringUtils.isEmpty(bucketName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, BUCKET_NAME_ILLEGAL);
        }

        if (getBucket(tenantName, bucketName) == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                    String.format(BUCKET_NOT_EXIST, bucketName, tenantName));
        }

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setRequestCredentialsProvider(new AWSCredentialsProvider() {
            @Override
            public void refresh() {}

            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(tenantName, tenantName);
            }
        });

        String fileName = file.getOriginalFilename();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());

        PutObjectRequest putObjectRequest = null;
        try {
            // check file exists
            ObjectListing objectListing = CephClientFactory.getCephObjectConn().listObjects(listObjectsRequest);
            List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
            for (S3ObjectSummary s3 : s3ObjectSummaries) {
                if (s3.getKey().equals(fileName)) {
                    throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, UPLOAD_FILE_ALREADY_EXIST);
                }
            }

            putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), objectMetadata);
            AWSCredentials awsCredentials = getCredentials(tenantName);
            AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
                @Override
                public void refresh() {}

                @Override
                public AWSCredentials getCredentials() {
                    return awsCredentials;
                }
            };
            putObjectRequest.setRequestCredentialsProvider(awsCredentialsProvider);
            putObjectRequest.withStorageClass(sClass);
            putObjectRequest.withCannedAcl(accessControlListEnum.acl);
            CephClientFactory.getCephObjectConn().putObject(putObjectRequest);
        } catch (SdkClientException | IOException e) {
            log.error(UPLOAD_FAILED, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_UPLOAD, UPLOAD_FAILED);
        }

        return true;
    }

    @Override
    public void downLoad(String tenantName, String bucketName, String objName, HttpServletResponse response) {
        // check objName
        if (StringUtils.isEmpty(objName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_INVALID_PARAM, DOWNLOAD_FILE_NAME_ILLEGAL);
        }

        S3Object s3Object = null;
        try {
            // check if bk belongs to the tn
            if (getBucket(tenantName, bucketName) == null) {
                throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                        String.format(BUCKET_NOT_EXIST, bucketName, tenantName));
            }

            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objName);
            AWSCredentials awsCredentials = getCredentials(tenantName);
            AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
                @Override
                public void refresh() {}

                @Override
                public AWSCredentials getCredentials() {
                    return awsCredentials;
                }
            };
            getObjectRequest.setRequestCredentialsProvider(awsCredentialsProvider);
            s3Object = CephClientFactory.getCephObjectConn().getObject(getObjectRequest);
        } catch (SdkClientException e) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND, DOWNLOAD_FAILED);
        }

        // check if the obj exists
        if (s3Object == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_NOT_FOUND,
                    String.format(DOWNLOAD_FILE_NOT_EXIST, objName));
        }

        response.reset();
        try (OutputStream output = response.getOutputStream()) {
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(objName.getBytes(CHARSET_UTF), CHARSET_ISO) + "\"");

            byte[] b = new byte[1024];
            int len = -1;
            while ((len = s3Object.getObjectContent().read(b)) != -1) {
                output.write(b, 0, len);
            }
            s3Object.getObjectContent().close();
        } catch (IOException e) {
            String msg = String.format(DOWNLOAD_FAILED, objName);
            log.error(msg, e);
            throw new ErrorMessageException(ReturnCode.CODE_CEPH_DOWNLOAD, msg);
        }
    }

}
