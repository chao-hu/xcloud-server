package com.xxx.xcloud.module.tenant.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @ClassName: K8sAsyncHelper
 * @Description: k8s相关租户操作
 * @author huchao
 * @date 2019年10月24日
 *
 */
@Component
public class K8sAsyncHelper extends BaseTenantAsyncHelper {

    private static final Logger LOG = LoggerFactory.getLogger(K8sAsyncHelper.class);

    private static String OPT_ADD_K8S_NAMESPACE_PATH = "addK8sNamespace";
    private static String OPT_DEL_K8S_NAMESPACE_PATH = "delK8sNamespace";

    private static String OPT_K8S_NAMESPACE_KEY = "k8sNamespace";

    public void addK8sNamespace(String tenantName) {

    }

    public void delK8sNamespace(String tenantName) {

    }

}
