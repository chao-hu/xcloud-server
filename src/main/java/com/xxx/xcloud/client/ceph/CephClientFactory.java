package com.xxx.xcloud.client.ceph;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.ceph.fs.CephMount;
import com.ceph.rados.Rados;
import com.xxx.xcloud.common.BdosProperties;
import com.xxx.xcloud.common.Global;

/**
 * 用于获取CephClient
 *
 * @author ruzz
 */
@Component
public class CephClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CephClientFactory.class);

    private static CephMount cephMount;

    private static RgwAdmin rgwAdmin;

    private static Rados cluster;

    private static AmazonS3 conn;

    /**
     * get ceph Object client
     *
     * @return RgwAdmin
     */
    public static RgwAdmin getCephObjectClient() {

        // 即使有问题也不会为null
        if (null != rgwAdmin) {
            return rgwAdmin;
        }

        try {
            // init rgwadmin
            rgwAdmin = new RgwAdminBuilder()
                    .accessKey(BdosProperties.getConfigMap().get(Global.CEPH_RGW_ADMIN_ACCESSKEY))
                    .secretKey(BdosProperties.getConfigMap().get(Global.CEPH_RGW_ADMIN_SECRETKEY))
                    .endpoint("http://" + BdosProperties.getConfigMap().get(Global.CEPH_RGW_ENDPOINT) + "/admin")
                    .build();

            // init AmazonS3
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProtocol(Protocol.HTTP);
            conn = new AmazonS3Client(clientConfiguration);
            conn.setEndpoint(BdosProperties.getConfigMap().get(Global.CEPH_RGW_ENDPOINT));
        } catch (Exception e) {
            LOG.error("对象存储客户端初始化异常", e);
        }
        return rgwAdmin;
    }

    /**
     * get ceph Object client
     *
     * @return RgwAdmin
     */
    public static AmazonS3 getCephObjectConn() {
        // 即使有问题也不会为null
        if (null != conn) {
            return conn;
        }

        try {
            // init rgwadmin
            rgwAdmin = getCephObjectClient();

        } catch (Exception e) {
            LOG.error("对象存储客户端初始化异常", e);
        }
        return conn;
    }

    /**
     * get ceph file client
     *
     * @return CephMount
     */
    public static CephMount getCephFileClient() {
        // 即使有问题也不会为null
        if (null != cephMount) {
            return cephMount;
        }

        // init Cephfile client
        try {
            cephMount = new CephMount(BdosProperties.getConfigMap().get(Global.CEPH_NAME));
            cephMount.conf_read_file(BdosProperties.getConfigMap().get(Global.CEPH_SSH_CEPHDIR)
                    + BdosProperties.getConfigMap().get(Global.CEPH_CONF));
            cephMount.mount("/");
            LOG.info("CephFile客户端初始化成功！");
        } catch (FileNotFoundException e) {
            LOG.error("找不到ceph配置文件", e);
        } catch (Exception | Error e) {
            LOG.error("CephFile客户端异常", e);
        }

        return cephMount;
    }

    /**
     * get ceph Rbd client
     *
     * @return Rados
     */
    public static Rados getCephRbdClient() {
        // 即使有问题也不会为null
        if (null != cluster) {
            return cluster;
        }
        try {

            cluster = new Rados(BdosProperties.getConfigMap().get(Global.CEPH_NAME));
            File f = new File(BdosProperties.getConfigMap().get(Global.CEPH_SSH_CEPHDIR)
                    + BdosProperties.getConfigMap().get(Global.CEPH_CONF));
            cluster.confReadFile(f);
            cluster.connect();
        } catch (Exception | Error e) {
            LOG.error("CephRbd客户端异常");
        }
        return cluster;
    }

}
