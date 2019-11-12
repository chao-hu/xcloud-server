package com.xxx.xcloud.client.harbor;

import com.bonc.bdos.harbor.client.ApiClient;
import com.bonc.bdos.harbor.client.api.ProductsApi;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.XcloudProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 用于获取harborClient
 *
 * @author HBL
 */
public class HarborClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HarborClientFactory.class);

    private static ApiClient harborApiClient;
    private static ProductsApi productsApi;

    /**
     * get harbor api client
     *
     * @return harborApiClient
     */
    public static ApiClient getHarborApiClient() {

        if (null == harborApiClient) {
            initHarborApiClient();
        }

        return harborApiClient;
    }

    /**
     * 实例化 Harbor Api Client，harbor地址为空则跳过实例化
     */
    private static void initHarborApiClient() {

        String harborUrl = getHarborUrl();

        if (StringUtils.isEmpty(harborUrl)) {
            LOG.warn("实例化harbor客户端异常，获取harbor地址失败");
            return;
        }

        // 初始化客户端
        harborApiClient = new ApiClient();
        harborApiClient.setBasePath(harborUrl);

        // 添加认证
        String path = XcloudProperties.getConfigMap().get(Global.HARBOR_CRT_PATH);
        // File caFile = new File("/opt/bdos/harbor-ca.crt");
        File caFile = new File(path);
        InputStream sslCaCert = null;
        try {
            sslCaCert = new FileInputStream(caFile);
        } catch (FileNotFoundException e) {
            LOG.error("实例化harbor客户端异常，获取harbor-ca.crt文件失败", e);
        }

        harborApiClient.setSslCaCert(sslCaCert);
        harborApiClient.setUsername(XcloudProperties.getConfigMap().get(Global.HARBOR_USERNAME));
        harborApiClient.setPassword(XcloudProperties.getConfigMap().get(Global.HARBOR_PASSWORD));
    }

    /**
     * 实例化 ProductsApi
     *
     * @return productsApi
     */
    public static ProductsApi getProductsApi() {

        if (null == productsApi && null != HarborClientFactory.getHarborApiClient()) {

            productsApi = new ProductsApi();
            productsApi.setApiClient(HarborClientFactory.getHarborApiClient());
        }

        return productsApi;
    }

    /**
     * get harbor url
     *
     * @return
     */
    public static String getHarborUrl() {
        // return "https://172.16.3.50:8443/api";
        LOG.info("-------HarborUrl-------" + "http://"
                + XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/api");
        return "https://" + XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/api";
    }

}
