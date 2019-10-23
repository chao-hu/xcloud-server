package com.xxx.xcloud.websocket.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.xxx.xcloud.common.BdosProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.utils.HttpUtil;

import io.fabric8.kubernetes.api.model.Pod;

/**
 * Container Handle.
 *
 * @author xujiangpeng
 */
@Component
public class ContainerWebSocketHandler extends TextWebSocketHandler {

    private static Logger logger = LoggerFactory.getLogger(ContainerWebSocketHandler.class);

    /**
     * Map for all docker connection.
     * <p>
     * key is execId ,value is execSession
     */
    private static Map<String, ExecSession> execSessionMap = new HashMap<>();
    /**
     * Map for all execId relation.
     * <p>
     * key is relationSign ,value is execId
     */
    private static Map<String, String> execSessionRelationMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String hostIp, containerId;
        Map<String, String> map = getParam(session);
        if (null != map && !map.isEmpty()) {
            hostIp = map.get("hostIp");
            containerId = map.get("containerId");
        } else {
            logger.error("param is null");
            return;
        }

        String relationSign = map.get("relationSign");
        String execId = execSessionRelationMap.get(relationSign);

        logger.info("relationSign: " + relationSign);
        if (null == execId) {
            // Create bash terminal by exec cmd.
            String shell = getShell(map.get("tenantName"), map.get("podName"), map.get("apptype"));
            try {
                execId = createExec(hostIp, containerId, shell);
            } catch (Exception e) {
                logger.error("Current hostIP:{},containerID:{}", hostIp, containerId);
                logger.error("Create bash terminal exception", e);
                return;
            }

            // Set execId
            if (StringUtils.isEmpty(execId)) {
                logger.error("Create bash terminal , execId is empty !");
                return;
            }

            // Use prepared connection by execId.
            Socket socket;
            try {
                socket = connectExec(hostIp, execId);
            } catch (IOException e) {
                logger.error("Current hostIP:{},execId:{}", hostIp, execId);
                logger.error("Use prepared connection by execId exception", e);
                return;
            }
            execSessionRelationMap.put(relationSign, execId);
            session.getAttributes().put("execId", execId);

            getExecMessage(session, hostIp, containerId, socket);

        } else {
            execId = execSessionRelationMap.get(relationSign);
            session.getAttributes().put("execId", execId);
            ExecSession execSession = execSessionMap.get(execId);
            execSession.getOutPutThread().setSession(session);
        }
        session.getAttributes().put("relationSign", relationSign);

        // 修改tty大小
        resizeTty(hostIp, map.get("width"), map.get("height"), execId);
    }

    private void resizeTty(String hostIp, String width, String height, String execId) {
        int wsPort = Integer.valueOf(BdosProperties.getConfigMap().get(Global.DOCKER_DAEMON_PORT));
        String version = String.valueOf(BdosProperties.getConfigMap().get(Global.DOCKER_API_VERSION));
        if (StringUtils.isNotEmpty(version) && version.indexOf("v") < 0) {
            version = "v" + version;
        }
        String resize = "resize?h=" + height + "&w=" + width;

        JSONObject jsonResult = HttpUtil
                .httpPost("http://" + hostIp + ":" + wsPort + "/" + version + "/exec/" + execId + "/" + resize);
        logger.info("执行功能resizeTty ,返回结果为:" + jsonResult);
    }

    private Map<String, String> getParam(WebSocketSession session) {
        Map<String, String> map = new HashMap<>(7);

        String hostIp = session.getAttributes().get("ip").toString();
        String containerId = session.getAttributes().get("containerId").toString();
        String width = session.getAttributes().get("width").toString();
        String height = session.getAttributes().get("height").toString();
        String tenantName = session.getAttributes().get("tenantName").toString();
        String podName = session.getAttributes().get("podName").toString();
        String appType = session.getAttributes().get("apptype").toString();
        String relationSign = session.getAttributes().get("relationSign").toString();

        // Get msg from session or k8s.
        if (StringUtils.isEmpty(hostIp) || StringUtils.isEmpty(containerId)) {
            // try {
            // Pod pod =
            // KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
            // hostIp = pod.getStatus().getHostIP();
            // containerId = getContainerId(pod, appType);
            // } catch (Exception e) {
            // logger.error("k8s获取pod失败", e);
            // return null;
            // }
        }

        map.put("hostIp", hostIp);
        map.put("containerId", containerId);
        map.put("tenantName", tenantName);
        map.put("podName", podName);
        map.put("width", width);
        map.put("height", height);
        map.put("appType", appType);
        map.put("relationSign", relationSign);

        return map;
    }

    /**
     * @param pod
     * @param apptype
     * @return
     */
    private String getContainerId(Pod pod, String apptype) {
        String containerId = null;
        // List<ContainerStatus> containerStatuses =
        // pod.getStatus().getContainerStatuses();
        // if (null != containerStatuses && containerStatuses.size() > 1) {
        // for (ContainerStatus status : containerStatuses) {
        // if (null != apptype && apptype.equals(CommonConst.APPTYPE_YARN)
        // && status.getName().equals("kubernetes-hadoop")) {
        // containerId = status.getContainerID().replace("docker://", "");
        // }
        // if (null != apptype && apptype.equals(CommonConst.APPTYPE_ZK) &&
        // status.getName().equals("zk")) {
        // containerId = status.getContainerID().replace("docker://", "");
        // }
        // if (null != apptype &&
        // Arrays.asList(CommonConst.FRAMEWORK_TYPE_SERVER).contains(apptype)
        // && status.getName().equals(apptype + "-server")) {
        // containerId = status.getContainerID().replace("docker://", "");
        // }
        //
        // if (null != apptype &&
        // Arrays.asList(CommonConst.FRAMEWORK_TYPE).contains(apptype)
        // && status.getName().equals(apptype)) {
        // containerId = status.getContainerID().replace("docker://", "");
        // }
        // }
        // } else {
        // containerId =
        // pod.getStatus().getContainerStatuses().get(0).getContainerID().replace("docker://",
        // "");
        // }

        return containerId;
    }

    /**
     *
     * <p>
     * Description: 获取容器可用的SHELL，默认使用 /bin/bash
     * </p>
     *
     * @param tenantName
     *            租户名
     * @param podName
     *            pod名
     * @return
     */
    private String getShell(String tenantName, String podName, String apptype) {
        String shell = "/bin/bash";
        String command = "cat /etc/shells";

        // try {
        // String result = application.execCmdInContainer(tenantName, podName,
        // command, apptype);
        // String[] shells = result.split("\n");
        // boolean useDefaultShell = Arrays.asList(shells).contains(shell);
        // if (useDefaultShell) {
        // return shell;
        // }
        // for (String sh : shells) {
        // if (StringUtils.contains(sh, "bin")) {
        // shell = sh;
        // break;
        // }
        // }
        // } catch (Exception e) {
        // logger.error("获取租户" + tenantName + "下的pod:" + podName +
        // "的可用shell失败，即将使用默认/bin/bash建立连接");
        // }

        return shell;
    }

    /**
     * 创建bash.
     *
     * @param ip
     *            宿主机ip地址
     * @param containerId
     *            容器id
     * @param shell
     *            可用的shell
     * @return 命令id
     * @throws Exception
     */
    private String createExec(String ip, String containerId, String shell) throws Exception {
        return DockerHelper.query(ip, docker -> {
            String[] cmd = new String[1];
            cmd[0] = shell;
            ExecCreateCmdResponse createCmdResponse = docker.execCreateCmd(containerId).withAttachStdin(true)
                    .withAttachStdout(true).withAttachStderr(true).withTty(true).withCmd(cmd).exec();
            return createCmdResponse.getId();
        });
    }

    /**
     * 连接bash.
     *
     * @param ip
     *            宿主机ip地址
     * @param execId
     *            命令id
     * @return 连接的socket
     * @throws IOException
     */
    private Socket connectExec(String ip, String execId) throws IOException {

        int wsPort = Integer.valueOf(BdosProperties.getConfigMap().get(Global.DOCKER_DAEMON_PORT));

        Socket socket = new Socket(ip, wsPort);
        socket.setKeepAlive(true);
        OutputStream out = socket.getOutputStream();
        StringBuffer pw = new StringBuffer();
        pw.append("POST /exec/" + execId + "/start HTTP/1.1\r\n");
        pw.append("Host: " + ip + ":" + wsPort + "\r\n");
        pw.append("User-Agent: Docker-Client\r\n");
        pw.append("Content-Type: application/json\r\n");
        pw.append("Connection: Upgrade\r\n");
        JSONObject obj = new JSONObject();
        obj.put("Detach", false);
        obj.put("Tty", true);
        String json = obj.toJSONString();
        pw.append("Content-Length: " + json.length() + "\r\n");
        pw.append("Upgrade: tcp\r\n");
        pw.append("\r\n");
        pw.append(json);
        out.write(pw.toString().getBytes("UTF-8"));
        out.flush();

        return socket;
    }

    /**
     * 获得输出.
     *
     * @param session
     *            webSocket session
     * @param ip
     *            宿主机ip地址
     * @param containerId
     *            容器id
     * @param socket
     *            命令连接socket
     * @throws IOException
     */
    private void getExecMessage(WebSocketSession session, String ip, String containerId, Socket socket)
            throws IOException {

        InputStream inputStream = socket.getInputStream();

        byte[] bytes = new byte[1024];
        StringBuffer returnMsg = new StringBuffer();

        while (true) {
            int n = inputStream.read(bytes);
            String msg = new String(bytes, 0, n);
            returnMsg.append(msg);
            bytes = new byte[10240];
            if (returnMsg.indexOf("\r\n\r\n") != -1) {
                session.sendMessage(
                        new TextMessage(returnMsg.substring(returnMsg.indexOf("\r\n\r\n") + 4, returnMsg.length())));
                break;
            }
        }

        OutPutThread outPutThread = new OutPutThread(inputStream, session);
        outPutThread.start();

        execSessionMap.put(session.getAttributes().get("execId").toString(),
                new ExecSession(ip, containerId, socket, outPutThread));
    }

    /**
     * close ws terminal thread when WebSocket connection Closed.
     *
     * @param session
     *            this.
     * @param closeStatus
     *            close code.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {

        if (CloseStatus.NO_CLOSE_FRAME.getCode() == closeStatus.getCode()) {
            // 非正常关闭 所以不做任何处理
            return;
        }

        if (null == session.getAttributes().get("execId") || null == session.getAttributes().get("relationSign")) {

            return;
        }

        String execId = session.getAttributes().get("execId").toString();
        String relationSign = session.getAttributes().get("relationSign").toString();
        ExecSession execSession = execSessionMap.get(execId);

        try {

            execSession.getOutPutThread().interrupt();
        } catch (Exception e) {

            logger.error("Close connection exception.", e);
        }
        finally {

            execSessionRelationMap.remove(relationSign);
            execSessionMap.remove(execId);
            logger.info("真正关闭连接 execId: " + execId);
            logger.info("execSessionMap 还剩多少：" + execSessionMap.size());
        }
    }

    /**
     * 获得先输入.
     *
     * @param session
     * @param message
     *            输入信息
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String execId = session.getAttributes().get("execId").toString();
        ExecSession execSession = execSessionMap.get(execId);
        OutputStream out = execSession.getSocket().getOutputStream();
        out.write(message.asBytes());
        out.flush();
    }
}
