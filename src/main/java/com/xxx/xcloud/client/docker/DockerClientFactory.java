package com.xxx.xcloud.client.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;

/**
 *
 * @author mengaijun
 * @date: 2018年12月11日 下午2:07:43
 */
public class DockerClientFactory {

    /**
     * 获取DockerClient
     *
     * @return DockerClient
     * @date: 2018年12月11日 下午2:08:09
     */
    // public static DockerClient getDockerClientInstance() {
    // DockerClient dockerClient = null;
    // try {
    // DockerClientConfig config =
    // DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(url)
    // .withApiVersion(apiVersion).withRegistryUsername(username).withRegistryPassword(password)
    // .withRegistryEmail(email).withRegistryUrl(serverAddress).build();
    // dockerClient = DockerClientBuilder.getInstance(config).build();
    // } catch (Exception e) {
    // e.printStackTrace();
    // throw new
    // ErrorMessageException(ReturnCode.CODE_DOCKER_INIT_CLIENT_FAILED,
    // "创建docker客户端失败");
    // }
    // return dockerClient;
    // }

    /**
     * 获取DockerClient
     *
     * @param username
     *            注册harbor用户名
     * @param password
     *            注册harbor密码
     * @param email
     *            注册harbor邮箱
     * @return DockerClient
     * @date: 2018年12月11日 下午2:08:09
     */
    public static DockerClient getDockerClientInstance(String username, String password, String email) {
        DockerClient dockerClient = null;
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(XcloudProperties.getConfigMap().get(Global.DOCKER_URL))
                    .withApiVersion(XcloudProperties.getConfigMap().get(Global.DOCKER_API_VERSION))
                    .withRegistryUsername(username).withRegistryPassword(password).withRegistryEmail(email)
                    .withRegistryUrl("http://" + XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS))
                    .build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_INIT_CLIENT_FAILED, "创建docker客户端失败");
        }
        return dockerClient;
    }

    /**
     * 获取指定docker节点IP的docker客户端
     *
     * @param nodeIp
     *            连接docker的ip地址
     * @return DockerClient
     * @date: 2018年12月11日 下午2:08:26
     */
    public static DockerClient getDockerClientInstance(String nodeIp) {
        DockerClient dockerClient = null;
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(
                            "tcp" + "://" + nodeIp + ":" + XcloudProperties.getConfigMap().get(Global.DOCKER_DAEMON_PORT))
                    .withApiVersion(XcloudProperties.getConfigMap().get(Global.DOCKER_API_VERSION))
                    .withRegistryUsername(XcloudProperties.getConfigMap().get(Global.HARBOR_USERNAME))
                    .withRegistryPassword(XcloudProperties.getConfigMap().get(Global.HARBOR_PASSWORD))
                    .withRegistryEmail(XcloudProperties.getConfigMap().get(Global.HARBOR_EMAIL))
                    .withRegistryUrl("http://" + XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS))
                    .build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_INIT_CLIENT_FAILED, "创建docker客户端失败");
        }
        return dockerClient;
    }

    /**
     * get Docker Registry url
     *
     * @return DockerRegistryUrl
     */
    public static String getDockerRegistryUrl() {

        return "http://" + XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS);
    }

}
