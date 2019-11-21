package com.xxx.xcloud.client.kubernetes;

import java.io.File;

import org.springframework.stereotype.Component;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;

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
import io.fabric8.kubernetes.client.dsl.internal.CustomResourceOperationContext;
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
     * get kubernetes client
     *
     * @return kubernetesClient
     */
    public static XcloudKubernetesClient getClient() {

        if (null != kubernetesClient) {
            // 即使有问题也不会为null
            return kubernetesClient;
        }

        if (new File(KUBERNETES_KUBECONFIG_FILE_PATH).exists()) {

            // init by admin.conf (https)
            System.setProperty(Config.KUBERNETES_KUBECONFIG_FILE, KUBERNETES_KUBECONFIG_FILE_PATH);

            kubernetesClient = new XcloudKubernetesClient();
        } else {

            String kubenetesMasterUrl = getKubenetesMasterUrl();

            Config config = new ConfigBuilder().withMasterUrl(kubenetesMasterUrl).build();
            kubernetesClient = new XcloudKubernetesClient(config);
        }

        return kubernetesClient;
    }

    /**
     * 自定义kubernetes客户端，用于实现级联删除
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

            return new CustomResourceOperationsImpl<T, L, D>(new CustomResourceOperationContext()
                    .withOkhttpClient(httpClient).withConfig(getConfiguration()).withCrd(crd)
                    .withApiGroupName(crd.getSpec().getGroup()).withApiGroupVersion(crd.getSpec().getVersion())
                    .withPlural(crd.getSpec().getNames().getPlural()).withCascading(true).withReloadingFromServer(false)
                    .withType(resourceType).withListType(listClass).withDoneableType(doneClass));
            // return new CustomResourceOperationsImpl<T, L, D>(httpClient,
            // getConfiguration(), crd.getSpec().getGroup(),
            // crd.getSpec().getVersion(), crd.getSpec().getNames().getPlural(),
            // null, null, true, null, null,
            // false, resourceType, listClass, doneClass);
        }

        public XcloudKubernetesClient(Config config) throws KubernetesClientException {
            super(config);
        }

        public XcloudKubernetesClient() throws KubernetesClientException {
            super();
        }
    }

    /**
     * get kubernetes master url
     *
     * @return kubenetesMsterUrl
     */
    public static String getKubenetesMasterUrl() {

        return XcloudProperties.getConfigMap().get(Global.KUBENETES_MASTER_URL);
    }

    public static MixedOperation<Job, JobList, DoneableJob, ScalableResource<Job, DoneableJob>> getJobs() {

        if (null != jobClient) {
            // 即使有问题也不会为null

            return jobClient;
        }

        jobClient = getClient().batch().jobs();

        return jobClient;
    }
}
