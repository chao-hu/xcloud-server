package com.xxx.xcloud.module.component.consts;

import java.util.Arrays;

import java.util.List;

/**
 * @ClassName: PrometheusClusterConst
 * @Description: Prometheus常量
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class PrometheusClusterConst {

    public static final String PROMETHEUS_KIND = "Prometheus";
    public static final String PROMETHEUS_API_VERSION = "prometheus.bonc.com/v1beta1";
    public static final String PROMETHEUS_CLUSTER_CRDS = "prometheuses.prometheus.bonc.com";

    public static final String PROMETHEUS_TYPE = "single";

    public static final String PROMETHEUS_YAML_CONF_NAME = "prometheus.yaml";
    public static final String PROMETHEUS_TARGETS_CONF_NAME = "targets.json";

    public static final String STORAGE_TSDB_RETENTION_UNIT = "d";

    public static final String PROMETHEUS_CLUSTER_OPT_CREATE = "ClusterCreate";
    public static final String PROMETHEUS_CLUSTER_OPT_STOP = "ClusterStop";
    public static final String PROMETHEUS_CLUSTER_OPT_START = "ClusterStart";
    public static final String PROMETHEUS_CLUSTER_OPT_CHANGE_RESOURCE = "ClusterChangeResource";
    public static final String PROMETHEUS_CLUSTER_OPT_CHANGE_CONFIG = "ClusterChangeConfig";
    public static final String PROMETHEUS_CLUSTER_OPT_DELETE = "ClusterDelete";

    public static final String PROMETHEUS_CLUSTER_CONFIG_OPT_ADDTARGET = "AddTarget";
    public static final String PROMETHEUS_CLUSTER_CONFIG_OPT_DELETETARGET = "DeleteTarget";
    public static final String PROMETHEUS_CLUSTER_CONFIG_OPT_UPDATEPROMETHEUSYAML = "UpdatePrometheusYaml";
    public static final String PROMETHEUS_CLUSTER_CONFIG_OPT_SUPER_ADDTARGET = "SuperAddTarget";

    public final static String PROMETHEUS_CLUSTER_CREATE_WORKER = "PrometheusClusterCreateWorker";
    public final static String PROMETHEUS_CLUSTER_DELETE_WORKER = "PrometheusClusterDeleteWorker";
    public final static String PROMETHEUS_CLUSTER_START_WORKER = "PrometheusClusterStartWorker";
    public final static String PROMETHEUS_CLUSTER_STOP_WORKER = "PrometheusClusterStopWorker";
    public final static String PROMETHEUS_CLUSTER_CHANGE_RESOURCE_WORKER = "PrometheusClusterChangeResourceWorker";

    public static final List<String> PROMETHEUS_CLUSTER_CONFIG_OPT_LIST = Arrays.asList(
            PROMETHEUS_CLUSTER_CONFIG_OPT_ADDTARGET, PROMETHEUS_CLUSTER_CONFIG_OPT_DELETETARGET,
            PROMETHEUS_CLUSTER_CONFIG_OPT_UPDATEPROMETHEUSYAML, PROMETHEUS_CLUSTER_CONFIG_OPT_SUPER_ADDTARGET);

    public static final List<String> PROMETHEUS_NODE_STATE_WAITING_LIST = Arrays.asList(CommonConst.STATE_NODE_PENDING,
            CommonConst.STATE_NODE_STARTING, CommonConst.STATE_NODE_STOPPING, CommonConst.STATE_NODE_TERMINATING);

    public static final List<String> PROMETHEUS_CLUSTER_STATE_WAITING_LIST = Arrays.asList(
            CommonConst.STATE_CLUSTER_PENDING, CommonConst.STATE_CLUSTER_STARTING, CommonConst.STATE_CLUSTER_STOPPING,
            CommonConst.STATE_CLUSTER_TERMINATING, CommonConst.STATE_CLUSTER_SCALING,
            CommonConst.STATE_CLUSTER_WAITING);

    public static List<String> getPrometheusNodeStateWaitingList() {
        return PROMETHEUS_NODE_STATE_WAITING_LIST;
    }

    public static List<String> getPrometheusClusterStateWaitingList() {
        return PROMETHEUS_CLUSTER_STATE_WAITING_LIST;
    }

    public static List<String> getPrometheusClusterConfigOptList() {
        return PROMETHEUS_CLUSTER_CONFIG_OPT_LIST;
    }

}
