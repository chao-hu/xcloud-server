package com.xxx.xcloud.client.component;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.module.component.consts.CodisClusterConst;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.EsClusterConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.consts.KafkaClusterConst;
import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.consts.PrometheusClusterConst;
import com.xxx.xcloud.module.component.consts.RedisClusterConst;
import com.xxx.xcloud.module.component.consts.StormClusterConst;
import com.xxx.xcloud.module.component.consts.ZkClusterConst;
import com.xxx.xcloud.module.component.model.codis.CodisCluster;
import com.xxx.xcloud.module.component.model.codis.CodisClusterDoneable;
import com.xxx.xcloud.module.component.model.codis.CodisClusterList;
import com.xxx.xcloud.module.component.model.es.EsCluster;
import com.xxx.xcloud.module.component.model.es.EsClusterDoneable;
import com.xxx.xcloud.module.component.model.es.EsClusterList;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;
import com.xxx.xcloud.module.component.model.ftp.FtpClusterDoneable;
import com.xxx.xcloud.module.component.model.ftp.FtpClusterList;
import com.xxx.xcloud.module.component.model.kafka.KafkaCluster;
import com.xxx.xcloud.module.component.model.kafka.KafkaClusterDoneable;
import com.xxx.xcloud.module.component.model.kafka.KafkaClusterList;
import com.xxx.xcloud.module.component.model.lvm.Lvm;
import com.xxx.xcloud.module.component.model.lvm.LvmDoneable;
import com.xxx.xcloud.module.component.model.lvm.LvmList;
import com.xxx.xcloud.module.component.model.memcached.MemcachedCluster;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterDoneable;
import com.xxx.xcloud.module.component.model.memcached.MemcachedClusterList;
import com.xxx.xcloud.module.component.model.mysql.MysqlCluster;
import com.xxx.xcloud.module.component.model.mysql.MysqlClusterDoneable;
import com.xxx.xcloud.module.component.model.mysql.MysqlClusterList;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlCluster;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlClusterDoneable;
import com.xxx.xcloud.module.component.model.postgresql.PostgresqlClusterList;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusCluster;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusClusterDoneable;
import com.xxx.xcloud.module.component.model.prometheus.PrometheusClusterList;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;
import com.xxx.xcloud.module.component.model.redis.RedisClusterDoneable;
import com.xxx.xcloud.module.component.model.redis.RedisClusterList;
import com.xxx.xcloud.module.component.model.storm.StormCluster;
import com.xxx.xcloud.module.component.model.storm.StormClusterDoneable;
import com.xxx.xcloud.module.component.model.storm.StormClusterList;
import com.xxx.xcloud.module.component.model.zookeeper.ZkCluster;
import com.xxx.xcloud.module.component.model.zookeeper.ZkClusterDoneable;
import com.xxx.xcloud.module.component.model.zookeeper.ZkClusterList;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;


/**
 * @ClassName: ComponentClientFactory
 * @Description: 组件客户端
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class ComponentClientFactory {

    public static MixedOperation<Lvm, LvmList, LvmDoneable, Resource<Lvm, LvmDoneable>> getLvmClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(CommonConst.LVM_CRDS).get();
        MixedOperation<Lvm, LvmList, LvmDoneable, Resource<Lvm, LvmDoneable>> lvmClient = KubernetesClientFactory
                .getClient().customResources(crdObject, Lvm.class, LvmList.class, LvmDoneable.class);
        return lvmClient;
    }

    public static MixedOperation<MysqlCluster, MysqlClusterList, MysqlClusterDoneable, Resource<MysqlCluster, MysqlClusterDoneable>> getMysqlClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(MysqlClusterConst.MYSQL_CLUSTER_CRDS).get();
        MixedOperation<MysqlCluster, MysqlClusterList, MysqlClusterDoneable, Resource<MysqlCluster, MysqlClusterDoneable>> mysqlClient = KubernetesClientFactory
                .getClient()
                .customResources(crdObject, MysqlCluster.class, MysqlClusterList.class, MysqlClusterDoneable.class);
        return mysqlClient;
    }

    public static MixedOperation<RedisCluster, RedisClusterList, RedisClusterDoneable, Resource<RedisCluster, RedisClusterDoneable>> getRedisClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(RedisClusterConst.REDIS_CLUSTER_CRDS).get();
        MixedOperation<RedisCluster, RedisClusterList, RedisClusterDoneable, Resource<RedisCluster, RedisClusterDoneable>> redisClient = KubernetesClientFactory
                .getClient()
                .customResources(crdObject, RedisCluster.class, RedisClusterList.class, RedisClusterDoneable.class);
        return redisClient;
    }


    public static MixedOperation<CodisCluster, CodisClusterList, CodisClusterDoneable, Resource<CodisCluster, CodisClusterDoneable>> getCodisClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(CodisClusterConst.CODIS_CLUSTER_CRDS).get();

        MixedOperation<CodisCluster, CodisClusterList, CodisClusterDoneable, Resource<CodisCluster, CodisClusterDoneable>> codisClient = KubernetesClientFactory
                .getClient()
                .customResources(crdObject, CodisCluster.class, CodisClusterList.class, CodisClusterDoneable.class);
        return codisClient;
    }


    public static MixedOperation<StormCluster, StormClusterList, StormClusterDoneable, Resource<StormCluster, StormClusterDoneable>> getStormClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(StormClusterConst.STORM_CLUSTER_CRDS).get();
        MixedOperation<StormCluster, StormClusterList, StormClusterDoneable, Resource<StormCluster, StormClusterDoneable>> stormClient = KubernetesClientFactory
                .getClient()
                .customResources(crdObject, StormCluster.class, StormClusterList.class, StormClusterDoneable.class);
        return stormClient;
    }

    public static MixedOperation<EsCluster, EsClusterList, EsClusterDoneable, Resource<EsCluster, EsClusterDoneable>> getEsClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(EsClusterConst.ES_CLUSTER_CRDS).get();

        MixedOperation<EsCluster, EsClusterList, EsClusterDoneable, Resource<EsCluster, EsClusterDoneable>> esClient = KubernetesClientFactory
                .getClient().customResources(crdObject, EsCluster.class, EsClusterList.class, EsClusterDoneable.class);

        return esClient;
    }

    public static MixedOperation<KafkaCluster, KafkaClusterList, KafkaClusterDoneable, Resource<KafkaCluster, KafkaClusterDoneable>> getKafkaClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(KafkaClusterConst.KAFKA_CLUSTER_CRDS).get();
        MixedOperation<KafkaCluster, KafkaClusterList, KafkaClusterDoneable, Resource<KafkaCluster, KafkaClusterDoneable>> kafkaClient = KubernetesClientFactory
                .getClient()
                .customResources(crdObject, KafkaCluster.class, KafkaClusterList.class, KafkaClusterDoneable.class);
        return kafkaClient;
    }

    public static MixedOperation<FtpCluster, FtpClusterList, FtpClusterDoneable, Resource<FtpCluster, FtpClusterDoneable>> getFtpClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(FtpClusterConst.FTP_CLUSTER_CRDS).get();
        MixedOperation<FtpCluster, FtpClusterList, FtpClusterDoneable, Resource<FtpCluster, FtpClusterDoneable>> ftpClient = KubernetesClientFactory
                .getClient()
                .customResources(crdObject, FtpCluster.class, FtpClusterList.class, FtpClusterDoneable.class);
        return ftpClient;
    }

    public static MixedOperation<MemcachedCluster, MemcachedClusterList, MemcachedClusterDoneable, Resource<MemcachedCluster, MemcachedClusterDoneable>> getMemcachedClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(MemcachedClusterConst.MEMCACHED_CLUSTER_CRDS).get();

        MixedOperation<MemcachedCluster, MemcachedClusterList, MemcachedClusterDoneable, Resource<MemcachedCluster, MemcachedClusterDoneable>> memcachedClient = KubernetesClientFactory
                .getClient().customResources(crdObject, MemcachedCluster.class, MemcachedClusterList.class,
                        MemcachedClusterDoneable.class);

        return memcachedClient;
    }

    public static MixedOperation<PrometheusCluster, PrometheusClusterList, PrometheusClusterDoneable, Resource<PrometheusCluster, PrometheusClusterDoneable>> getPrometheusClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(PrometheusClusterConst.PROMETHEUS_CLUSTER_CRDS).get();

        MixedOperation<PrometheusCluster, PrometheusClusterList, PrometheusClusterDoneable, Resource<PrometheusCluster, PrometheusClusterDoneable>> prometheusClient = KubernetesClientFactory
                .getClient().customResources(crdObject, PrometheusCluster.class, PrometheusClusterList.class,
                        PrometheusClusterDoneable.class);

        return prometheusClient;
    }

    public static MixedOperation<ZkCluster, ZkClusterList, ZkClusterDoneable, Resource<ZkCluster, ZkClusterDoneable>> getZkClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(ZkClusterConst.ZK_CLUSTER_CRDS).get();
        MixedOperation<ZkCluster, ZkClusterList, ZkClusterDoneable, Resource<ZkCluster, ZkClusterDoneable>> zkClient = KubernetesClientFactory
                .getClient().customResources(crdObject, ZkCluster.class, ZkClusterList.class, ZkClusterDoneable.class);
        return zkClient;
    }

    public static MixedOperation<PostgresqlCluster, PostgresqlClusterList, PostgresqlClusterDoneable, Resource<PostgresqlCluster, PostgresqlClusterDoneable>> getPostgresqlClient() {
        CustomResourceDefinition crdObject = KubernetesClientFactory.getClient().customResourceDefinitions()
                .withName(PostgresqlClusterConst.POSTGRESQL_CLUSTER_CRDS).get();
        MixedOperation<PostgresqlCluster, PostgresqlClusterList, PostgresqlClusterDoneable, Resource<PostgresqlCluster, PostgresqlClusterDoneable>> postgresqlClient = KubernetesClientFactory
                .getClient().customResources(crdObject, PostgresqlCluster.class, PostgresqlClusterList.class,
                        PostgresqlClusterDoneable.class);
        return postgresqlClient;
    }
}
