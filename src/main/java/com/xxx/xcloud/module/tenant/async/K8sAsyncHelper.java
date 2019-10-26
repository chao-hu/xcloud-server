package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;

import io.fabric8.kubernetes.api.model.Namespace;

/**
 * @ClassName: K8sAsyncHelper
 * @Description: k8s相关租户操作
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
public class K8sAsyncHelper extends BaseTenantAsyncHelper {

    private static final Logger logger = LoggerFactory.getLogger(K8sAsyncHelper.class);

    private static String OPT_K8S_NAMESPACE_KEY = "k8sNamespace";

    @Async
    public void addK8sNamespace(String tenantName) {

        String opt = "k8sAsyncHelper#addK8sNamespace";

        try {

            Namespace namespace = KubernetesClientFactory.getClient().namespaces().withName(tenantName).get();
            if (null == namespace) {
                namespace = KubernetesClientFactory.getClient().namespaces().createNew().withNewMetadata()
                        .withName(tenantName).endMetadata().done();
            }

            if (null == namespace) {

                saveTenantOpt(tenantName, opt);
                logger.error("租户 " + tenantName + " k8s创建namespace " + tenantName + " 失败!");
            } else {

                saveTenant(tenantName, OPT_K8S_NAMESPACE_KEY, true);
                deleteTenantOpt(tenantName, opt);

                logger.error("租户 " + tenantName + " k8s创建namespace " + tenantName + " 成功!");
            }

        } catch (Exception e) {

            logger.error("k8s 操作失败", e);
            saveTenantOpt(tenantName, opt);
            logger.error("租户 " + tenantName + " k8s创建namespace " + tenantName + " 失败!");
        }

    }

    @Async
    public void delK8sNamespace(String tenantName) {
        String opt = "k8sAsyncHelper#delK8sNamespace";
        try {

            KubernetesClientFactory.getClient().namespaces().withName(tenantName).cascading(true).delete();

            deleteTenantOpt(tenantName, opt);

            logger.debug("租户 " + tenantName + " k8s删除namespace " + tenantName + " 成功!");
        } catch (Exception e) {

            saveTenantOpt(tenantName, opt);
            logger.debug("租户 " + tenantName + " k8s删除namespace " + tenantName + " 失败!");
        }
    }

}
