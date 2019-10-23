package com.xxx.xcloud.websocket.docker;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.xxx.xcloud.common.BdosProperties;
import com.xxx.xcloud.common.Global;

/**
 * Docker Helper
 *
 * @author xujiangpeng
 */
public class DockerHelper {

    private static Logger logger = LoggerFactory.getLogger(DockerHelper.class);

    /**
     * Every connection wil be create and close dockerClient by hostIP, If
     * necessary, follow up with optimization
     */
    private static DockerClient dockerClient = null;

    private static DockerClient openDockerClient(String dockerHostIp) {
        Assert.notNull(dockerHostIp, "dockerHostIp cat not empty!");

        String dockerHostPort = BdosProperties.getConfigMap().get(Global.DOCKER_DAEMON_PORT);
        String dockerHostVersion = BdosProperties.getConfigMap().get(Global.DOCKER_DAEMON_APIVERSION);
        String dockerHost = "tcp://" + dockerHostIp + ":" + dockerHostPort;

        DefaultDockerClientConfig.Builder clientBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        clientBuilder.withDockerHost(dockerHost);
        clientBuilder.withApiVersion(dockerHostVersion);
        DockerClientConfig config = clientBuilder.build();

        try {
            dockerClient = DockerClientBuilder.getInstance(config).build();
        } catch (Exception e) {
            logger.error("Init docker client failed! current config is {} :", JSON.toJSONString(config), e);
            throw e;
        }

        return dockerClient;
    }

    private static void closeDockerClient(DockerClient client) {
        try {
            client.close();
        } catch (IOException e) {
            logger.warn("Docker client close exception", e);
        }
    }

    public static <T> T query(String hostIp, DockerQuery<T> dockerQuery) throws Exception {
        dockerClient = openDockerClient(hostIp);
        T result = dockerQuery.action(dockerClient);
        closeDockerClient(dockerClient);
        return result;
    }

    public interface DockerQuery<T> {

        /**
         * The action witch will has a return .
         *
         * @param docker
         *            client for docker.
         * @return Docker exec terminal id.
         * @throws Exception
         *             If error.
         */
        T action(DockerClient docker) throws Exception;
    }
}
