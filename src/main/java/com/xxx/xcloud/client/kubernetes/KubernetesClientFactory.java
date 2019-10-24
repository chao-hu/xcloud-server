package com.xxx.xcloud.client.kubernetes;

import java.io.File;

import org.springframework.stereotype.Component;

import com.xxx.xcloud.common.BdosProperties;
import com.xxx.xcloud.common.Global;

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.batch.DoneableJob;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import io.fabric8.kubernetes.client.dsl.internal.CustomResourceOperationsImpl;

/**
 * 用于获取kubernetesClient
 *
 * @author HBL
 */
@Component
public class KubernetesClientFactory {

    private static final String KUBERNETES_KUBECONFIG_FILE_PATH = "/etc/kubernetes/admin.conf";

    private static XcloudKubernetesClient kubernetesClient;

    private static MixedOperation<Job, JobList, DoneableJob, ScalableResource<Job, DoneableJob>> jobClient;

    /**
     * get kubenetes client
     *
     * @return kubernetesClient
     */
    public static XcloudKubernetesClient getClient() {

        if (null != kubernetesClient) {// 即使有问题也不会为null

            return kubernetesClient;
        }

        // init k8s client
        if (new File(KUBERNETES_KUBECONFIG_FILE_PATH).exists()) {

            // init by admin.conf (https)
            System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, KUBERNETES_KUBECONFIG_FILE_PATH);

            kubernetesClient = new XcloudKubernetesClient();
        } else {

            // init by masterUtl (http)
            String kubenetesMasterUrl = getKubenetesMasterUrl();// 获取kubenetesMasterUrl

            Config config = new ConfigBuilder().withMasterUrl(kubenetesMasterUrl).build();
            kubernetesClient = new XcloudKubernetesClient(config);// 使用自定义的客户端，可以实现级联删除
        }

        return kubernetesClient;
    }

    /**
     * 自定义kubenetes客户端，用于实现级联删除
     *
     * @author HBL
     */
    public static class XcloudKubernetesClient extends DefaultKubernetesClient {

        /**
         * cascading delete default is false
         */
        @SuppressWarnings("rawtypes")
        @Override
        public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> customResources(
                CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass) {

            return new CustomResourceOperationsImpl<T, L, D>(httpClient, getConfiguration(), crd, resourceType,
                    listClass, doneClass);
        }

        public XcloudKubernetesClient(Config config) throws KubernetesClientException {
            super(config);
        }

        public XcloudKubernetesClient() throws KubernetesClientException {
            super();
        }
    }

    /**
     * get kubenetes master url
     *
     * @return kubenetesMsterUrl
     */
    public static String getKubenetesMasterUrl() {

        return BdosProperties.getConfigMap().get(Global.KUBENETES_MASTER_URL);
    }

    public static MixedOperation<Job, JobList, DoneableJob, ScalableResource<Job, DoneableJob>> getJobs() {

        if (null != jobClient) {// 即使有问题也不会为null

            return jobClient;
        }

        jobClient = getClient().batch().jobs();

        return jobClient;
    }
}
