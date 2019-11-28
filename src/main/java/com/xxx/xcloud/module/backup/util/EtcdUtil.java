package com.xxx.xcloud.module.backup.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global; 
import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;


public class EtcdUtil {

    private static Logger logger = LoggerFactory.getLogger(EtcdUtil.class);

    private static Client client = null;

    /**
     * 链接初始化
     * @Title: getClient
     * @Description: 链接初始化
     * @return Client 
     * @throws
     */
    public static synchronized Client getClient() {

        String addrss = XcloudProperties.getConfigMap().get(Global.ETCD_API_ADDRESS);

        logger.info("port：" + addrss);
        // etcd客户端链接
        if (null == client) {
            client = Client.builder().endpoints(addrss).build();
        }
        return client;

    }

    /**
     * 新增或者修改指定的配置
     * 
     * @param key
     * @param value
     * @return
     */
    public static void putEtcdValueByKey(String key, String value) throws Exception {
        EtcdUtil.getClient().getKVClient().put(ByteSequence.fromString(key),
                ByteSequence.fromBytes(value.getBytes("utf-8")));

    }

    /**
     * 根据指定的配置名称获取对应的value
     * 
     * @param key
     *            配置项
     * @return
     * @throws Exception
     */
    public static String getEtcdValueByKey(String key) throws Exception {
        List<KeyValue> kvs = EtcdUtil.getClient().getKVClient().get(ByteSequence.fromString(key)).get().getKvs();
        if (kvs.size() > 0) {
            String value = kvs.get(0).getValue().toStringUtf8();
            return value;
        } else {
            return null;
        }
    }

    /**
     * 删除指定的配置
     * 
     * @param key
     * @return
     */
    public static void deleteEtcdValueByKey(String key) {
        EtcdUtil.getClient().getKVClient().delete(ByteSequence.fromString(key));

    }

}
