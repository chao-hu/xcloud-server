package com.xxx.xcloud.module.devops.util;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.credentials.pojo.BaseCredential;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.client.JenkinsHttpConnection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author daien
 * @date 2019年2月28日
 */
public class DevopsClient {

    private static Logger logger = LoggerFactory.getLogger(DevopsClient.class);
    private static final String DEFAULT_DOMAIN_NAME = "_";

    private static DevopsClient devopsClient;
    private JenkinsServer jenkinsServer;
    private JenkinsHttpConnection jenkinsHttpConnection;

    private DevopsClient() throws URISyntaxException {
        jenkinsHttpConnection = new JenkinsHttpClient(new URI(XcloudProperties.getConfigMap().get(Global.DEVOPS_URL)),
                XcloudProperties.getConfigMap().get(Global.DEVOPS_ADMIN),
                XcloudProperties.getConfigMap().get(Global.DEVOPS_TOKEN));
        jenkinsServer = new JenkinsServer(jenkinsHttpConnection);
    }

    public static DevopsClient getClient() {
        if (devopsClient == null) {
            try {
                devopsClient = new DevopsClient();
            } catch (URISyntaxException e) {
                logger.error("jenkins客户端初始化失败", e);
            }
        }
        return devopsClient;
    }

    public JenkinsServer getJenkinsServer() {
        return jenkinsServer;
    }

    public void deleteBuild(String jobName, int buildNumber) throws DevopsException {
        try {
            jenkinsHttpConnection.post("job/" + jobName + "/" + buildNumber + "/doDelete", true);
        } catch (IOException e) {
            String msg = "任务:" + jobName + "序号:" + buildNumber + "删除失败";
            logger.error(msg, e);
            throw new DevopsException(500, msg);
        }
    }

    public void createCredential(BaseCredential crd, String domainName) throws IOException {
        String defaultDomain = "GLOBAL";
        if (StringUtils.isEmpty(domainName) || defaultDomain.equalsIgnoreCase(domainName)) {
            domainName = DEFAULT_DOMAIN_NAME;
        }

        String path = "credentials/store/system/domain/" + domainName + "/createCredentials";
        try {
            String xmlString = getXmlFromCrd(crd);
            jenkinsHttpConnection.post_xml(path, xmlString, true);
        } catch (IOException e) {
            logger.error("Create one credential exception.", e);
            throw e;
        }

    }

    public void deleteCredential(String credentialsId, String domainName) throws IOException {
        String defaultDomain = "GLOBAL";
        if (StringUtils.isEmpty(domainName) || defaultDomain.equalsIgnoreCase(domainName)) {
            domainName = DEFAULT_DOMAIN_NAME;
        }

        String path = "credentials/store/system/domain/" + domainName + "/credentials/" + credentialsId + "/doDelete";
        try {
            jenkinsHttpConnection.post(path, true);
        } catch (IOException e) {
            logger.error("Delete one credential exception.", e);
            throw e;
        }
    }

    public String execScript(String script) throws IOException {
        String ret;

        try {
            ret = jenkinsServer.runScript(script, true);
        } catch (IOException e) {
            logger.error("execScript occur exception.", e);
            throw e;
        }

        System.out.println(ret);
        return ret;
    }

    /**
     * Transform java obj to xml string.
     *
     * @param crd
     *            The java object you want to convert.
     * @return The covert xml String.
     */
    private String getXmlFromCrd(BaseCredential crd) {
        String xmlString = JaxbUtil.convertToXml(crd);
        StringBuffer stringBuffer = new StringBuffer(xmlString);

        // cut title
        int i = stringBuffer.indexOf("com");
        stringBuffer.delete(0, i - 1);

        return stringBuffer.toString();
    }
}
