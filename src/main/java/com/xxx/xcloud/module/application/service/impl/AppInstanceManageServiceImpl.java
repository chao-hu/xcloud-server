package com.xxx.xcloud.module.application.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.FrameReader;
import com.xxx.xcloud.client.docker.DockerClientFactory;
import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.service.IAppDetailService;
import com.xxx.xcloud.module.application.service.IAppInstanceManageService;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.rest.v1.service.model.Event;
import com.xxx.xcloud.rest.v1.service.model.LogStreamContainerResultCallback;
import com.xxx.xcloud.rest.v1.service.model.LogStringContainerResultCallback;
import com.xxx.xcloud.rest.v1.service.model.PodInfo;
import com.xxx.xcloud.utils.DateUtil;
import com.xxx.xcloud.utils.FileUtils;
import com.xxx.xcloud.utils.FtpUtils;
import com.xxx.xcloud.utils.HttpUtil;
import com.xxx.xcloud.utils.StringUtils;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;

/**
 * @ClassName: InstanceManageServiceImpl
 * @Description: 实例管理
 * @author zyh
 * @date 2019年11月12日
 *
 */
@org.springframework.stereotype.Service
public class AppInstanceManageServiceImpl implements IAppInstanceManageService {
    private static final Logger LOG = LoggerFactory.getLogger(AppInstanceManageServiceImpl.class);

    @Autowired
    private IAppDetailService appDetailService;

    /**
     * 服务日志显示的文字最大值
     */
    private Integer dockerLogSize = 524288;

    /**
     * 服务日志显示等候时长
     */
    private Integer dockerLogAwait = 3;

    /**
     * 服务日志下载等候时长
     */
    private static final Integer DOCKER_LOG_DOWNLOAD = 30;

    /**
     * 服务日志错误内容
     */
    private static final String DOCKER_LOG_ERROR_INFO = "Error grabbing logs";

    @Override
    public List<PodInfo> getPodInfo(String serviceId, String tenantName) {
        Service service = appDetailService.getServiceById(serviceId);
        serviceIsRuningState(service);
        if (!tenantName.equals(service.getTenantName())) {
            throw new ErrorMessageException(ReturnCode.CODE_OPT_CROSS_TENANT_NOT_ALLOWED, "不允许跨租户获取服务的实例详情");
        }
        List<PodInfo> podInfos = new ArrayList<PodInfo>();
        io.fabric8.kubernetes.api.model.Service k8sService = null;
        PodList podList = null;
        try {
            k8sService = KubernetesClientFactory.getClient().inNamespace(tenantName).services()
                    .withName(service.getServiceName()).get();
            if (null != k8sService) {
                podList = KubernetesClientFactory.getClient().inNamespace(tenantName).pods()
                        .withLabels(k8sService.getSpec().getSelector()).list();
                if (null != podList) {
                    for (Pod pod : podList.getItems()) {
                        String podName = pod.getMetadata().getName();
                        /*
                         * if (podName.length() > 32) { podName =
                         * service.getServiceName() +
                         * podName.substring(podName.lastIndexOf("-"),
                         * podName.lastIndexOf("-") + 6); }
                         */
                        int podStatus = 1;
                        String runtimeLength = "0天0小时0分钟";
                        if ("Running".equals(pod.getStatus().getPhase())) {
                            // pod中的容器状态
                            List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                            ContainerStateTerminated terminated = null;
                            ContainerStateWaiting waiting = null;
                            if (containerStatuses.size() > 0) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState containerState = containerStatus.getState();
                                terminated = containerState.getTerminated();
                                waiting = containerState.getWaiting();
                            }
                            if (null != terminated) {
                                podStatus = 6;
                            } else if (null != waiting) {
                                podStatus = 2;
                            } else {
                                podStatus = 3;
                            }
                            runtimeLength = DateUtil
                                    .getTimeDifference(DateUtil.parseStandardDate(pod.getStatus().getStartTime()));
                        } else if ("Pending".equals(pod.getStatus().getPhase())) {
                            podStatus = 2;
                        } else if ("Succeeded".equals(pod.getStatus().getPhase())) {
                            podStatus = 4;
                        } else {
                            podStatus = 5;
                        }
                        String restartPolicy = pod.getSpec().getRestartPolicy();
                        String createDate = pod.getStatus().getStartTime();
                        String imageName = pod.getSpec().getContainers().get(0).getImage();
                        imageName = imageName.substring(imageName.indexOf("/") + 1);
                        PodInfo podInfo = new PodInfo();
                        podInfo.setPodName(podName);
                        podInfo.setPodStatus(podStatus);
                        podInfo.setRestartPolicy(restartPolicy);
                        podInfo.setRuningTime(runtimeLength);
                        podInfo.setHostIp(pod.getStatus().getHostIP());
                        podInfo.setPodIp(pod.getStatus().getPodIP());
                        podInfo.setCreateTime(createDate);
                        podInfo.setImageName(imageName);
                        podInfos.add(podInfo);
                    }
                } else {
                    LOG.info("获取服务:" + service.getServiceName() + "实例详情失败");
                }
            } else {
                LOG.info("获取服务:" + service.getServiceName() + "实例详情失败");
            }
        } catch (Exception e) {
            LOG.error("获取服务:" + service.getServiceName() + "实例详情失败", e);
            // throw new
            // ErrorMessageException(ReturnCode.CODE_K8S_GET_POD_FAILED,
            // "获取服务:"+ service.getServiceName() +"实例详情失败");
        }

        return podInfos;
    }

    @Override
    public Map<String, Integer> podNumberDifferentStates(String serviceId) {
        Service service = appDetailService.getServiceById(serviceId);
        serviceIsRuningState(service);
        Map<String, Integer> map = new HashMap<String, Integer>(5);
        io.fabric8.kubernetes.api.model.Service k8sService = null;
        PodList podList = null;
        int runningPodNum = 0;
        int pendingPodNum = 0;
        int succeededPodNum = 0;
        int failedPodNum = 0;
        int unknownPodNum = 0;
        try {
            k8sService = KubernetesClientFactory.getClient().inNamespace(service.getTenantName()).services()
                    .withName(service.getServiceName()).get();
            if (null != k8sService) {
                podList = KubernetesClientFactory.getClient().inNamespace(service.getTenantName()).pods()
                        .withLabels(k8sService.getSpec().getSelector()).list();
                if (null != podList) {
                    for (Pod pod : podList.getItems()) {
                        if ("Running".equals(pod.getStatus().getPhase())) {
                            // pod中的容器状态
                            List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                            ContainerStateTerminated terminated = null;
                            ContainerStateWaiting waiting = null;
                            if (containerStatuses.size() > 0) {
                                ContainerStatus containerStatus = containerStatuses.get(0);
                                ContainerState containerState = containerStatus.getState();
                                terminated = containerState.getTerminated();
                                waiting = containerState.getWaiting();
                            }
                            if (null == terminated && null == waiting) {
                                runningPodNum++;
                            } else {
                                unknownPodNum++;
                            }
                        } else if ("Pending".equals(pod.getStatus().getPhase())) {
                            pendingPodNum++;
                        } else if ("Succeeded".equals(pod.getStatus().getPhase())) {
                            succeededPodNum++;
                        } else if ("Failed".equals(pod.getStatus().getPhase())) {
                            failedPodNum++;
                        } else {
                            unknownPodNum++;
                        }
                    }
                } else {
                    LOG.info("获取服务:" + service.getServiceName() + "获取服务实例的不同状态个数失败");
                }
            } else {
                LOG.info("获取服务:" + service.getServiceName() + "获取服务实例的不同状态个数失败");
            }
            map.put("Succeeded", succeededPodNum);
            map.put("Running", runningPodNum);
            map.put("Pending", pendingPodNum);
            map.put("Failed", failedPodNum);
            map.put("Unknown", unknownPodNum);
        } catch (Exception e) {
            LOG.error("获取服务:" + service.getServiceName() + "获取服务实例的不同状态个数失败", e);
        }
        return map;
    }

    /**
     * 判断服务是否在运行状态下,如果不是则抛异常
     * @Title: serviceIsRuningState
     * @Description: 判断服务是否在运行状态下,如果不是则抛异常
     * @param service void
     * @throws
     */
    private void serviceIsRuningState(Service service) {
        if (Global.OPERATION_RUNNING != service.getStatus()) {
            LOG.info("服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
            throw new ErrorMessageException(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
                    "服务:" + service.getServiceName() + "不在启动状态下,不允许进行操作");
        }
    }

    @Override
    public boolean deletePod(String tenantName, String podName) {
        try {
            Boolean deletePod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName)
                    .cascading(true).delete();
            if (deletePod) {
                LOG.info("实例:" + podName + "删除成功");
                return true;
            } else {
                LOG.error("实例::" + podName + "删除失败");
            }
        } catch (Exception e) {
            LOG.error("实例:" + podName + "删除失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_POD_FAILED, "实例:" + podName + "删除失败");
        }
        return true;
    }

    @Override
    public Map<String, Object> getPods(String tenantName, String serviceName) {

        Map<String, Object> terminatingPods = new HashMap<String, Object>(4);

        Map<String, String> labelSelector = new HashMap<String, String>(16);
        labelSelector.put("app", serviceName);

        try {
            PodList podList = KubernetesClientFactory.getClient().inNamespace(tenantName).pods()
                    .withLabels(labelSelector).list();

            if (podList == null || podList.getItems().size() == 0) {
                return null;
            } else {
                StringBuilder podsName = new StringBuilder();
                for (Pod pod : podList.getItems()) {
                    podsName.append(pod.getMetadata().getName() + ",");
                }
                terminatingPods.put("pods", podsName.toString().substring(0, podsName.toString().length() - 2));
            }

        } catch (Exception e) {
            LOG.error("获取pod信息失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_POD_FAILED, "获取pod信息失败");
        }

        return terminatingPods;
    }

    @Override
    public List<Event> getContainerStartLogs(String podName, String tenantName) {
        List<Event> events = new ArrayList<Event>();
        Map<String, String> field = new HashMap<String, String>(16);
        field.put("involvedObject.kind", "Pod");
        field.put("involvedObject.name", podName);
        try {
            EventList eventList = KubernetesClientFactory.getClient().inNamespace(tenantName).events().withFields(field)
                    .list();
            List<io.fabric8.kubernetes.api.model.Event> items = eventList.getItems();
            for (io.fabric8.kubernetes.api.model.Event k8sEvent : items) {
                Event event = new Event();
                event.setType(k8sEvent.getType());
                event.setMessage(k8sEvent.getMessage());
                event.setTimeStamp(DateUtil.parseStandardDate(k8sEvent.getFirstTimestamp()));
                events.add(event);
            }
        } catch (Exception e) {
            LOG.error("获取实例事件日志失败", e);
        }
        return events;
    }

    @Override
    public String getPodLogs(String tenantName, String podName, String apptype, Integer logTail) {
        String logStr = "";
        try {
            Pod pod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
            String containerId = getContainerId(pod, apptype);
            DockerClient dockerClient = getDockerClient(pod.getStatus().getHostIP());
            LogStringContainerResultCallback callback = new LogStringContainerResultCallback();
            dockerClient.logContainerCmd(containerId).withTail(logTail).withStdOut(true).withStdErr(true).exec(callback)
                    .awaitCompletion(dockerLogAwait, TimeUnit.SECONDS);
            logStr = callback.toString();
            if (logStr.indexOf(DOCKER_LOG_ERROR_INFO) > -1) {
                logStr = "";
            } else {
                if (logStr.length() > dockerLogSize) {
                    logStr = logStr.substring(logStr.length() - dockerLogSize);
                }

                logStr = logStr.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            }

        } catch (Exception e) {
            LOG.error("日志读取错误：", e);
        }
        return logStr;
    }

    @Override
    public String getCurrentPodLogs(String tenantName, String podName, String apptype, Integer logTail) {
        String logStr = "";
        try {
            Pod pod = null;
            try {
                pod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
            } catch (Exception e) {
                LOG.error("未找到该实例的日志：", e);
            }
            if (pod != null) {
                String containerId = getContainerId(pod, apptype);
                DockerClient dockerClient = getDockerClient(pod.getStatus().getHostIP());
                LogStringContainerResultCallback callback = new LogStringContainerResultCallback();
                dockerClient.logContainerCmd(containerId).withTail(logTail).withStdOut(true).withStdErr(true)
                        .exec(callback).awaitCompletion();
                logStr = callback.toString();
                if (logStr.indexOf(DOCKER_LOG_ERROR_INFO) > -1) {
                    logStr = "";
                } else {
                    if (logStr.length() > dockerLogSize) {
                        logStr = logStr.substring(logStr.length() - dockerLogSize);
                    }
                    logStr = logStr.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                }
            }
        } catch (Exception e) {
            LOG.error("日志读取错误：", e);
        }

        return logStr;
    }

    /**
     * 获取containId
     * @Title: getContainerId
     * @Description: 获取containId
     * @param pod
     * @param apptype
     * @return String
     * @throws
     */
    private String getContainerId(Pod pod, String apptype) {
        String containerId = null;

        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        if (null != containerStatuses && containerStatuses.size() > 1) {
            for (ContainerStatus status : containerStatuses) {
                if (null != apptype && apptype.equals(CommonConst.APPTYPE_YARN)
                        && status.getName().equals("kubernetes-hadoop")) {
                    containerId = status.getContainerID().replace("docker://", "");
                }
                if (null != apptype && apptype.equals(CommonConst.APPTYPE_ZK) && status.getName().equals("zk")) {
                    containerId = status.getContainerID().replace("docker://", "");
                }
                if (null != apptype && Arrays.asList(CommonConst.FRAMEWORK_TYPE_SERVER).contains(apptype)
                        && status.getName().equals(apptype + "-server")) {
                    containerId = status.getContainerID().replace("docker://", "");
                }

                if (null != apptype && Arrays.asList(CommonConst.FRAMEWORK_TYPE).contains(apptype)
                        && status.getName().equals(apptype)) {
                    containerId = status.getContainerID().replace("docker://", "");
                }
            }
        } else {
            containerId = pod.getStatus().getContainerStatuses().get(0).getContainerID().replace("docker://", "");
        }

        return containerId;
    }

    private DockerClient getDockerClient(String hostIp) {
        DockerClient dockerClient = null;
        if (hostIp != null) {
            try {
                dockerClient = DockerClientFactory.getDockerClientInstance(hostIp);
            } catch (ErrorMessageException e) {
                LOG.error("获取docker客户端失败!", e);
            }
        }
        return dockerClient;
    }

    @Override
    public String getPodLogByPeriod(String tenantName, String podName, String apptype, String since, String until) {
        StringBuffer logs = new StringBuffer();
        String logStr = null;
        // 验证since和until不能为空
        if (StringUtils.isEmpty(since) || StringUtils.isEmpty(until)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "时间参数不能为空");
        }
        // 验证是否都符合时间格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date sinceDate;
        Date untilDate;
        try {
            sinceDate = simpleDateFormat.parse(since);
            untilDate = simpleDateFormat.parse(until);
        } catch (ParseException e) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "时间参数格式不正确");
        }
        // 验证since不能大于until
        if (sinceDate.compareTo(untilDate) >= 0) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "时间参数格式不正确");
        }

        try {
            Pod pod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
            String containerId = getContainerId(pod, apptype);
            String containerHost = pod.getStatus().getHostIP();

            String url = "http://" + containerHost + ":"
                    + XcloudProperties.getConfigMap().get(Global.DOCKER_DAEMON_PORT) + "/containers/" + containerId
                    + "/logs";
            Map<String, Object> params = new HashMap<>(4);
            params.put("stdout", true);
            params.put("stderr", true);
            params.put("since", sinceDate.getTime() / 1000);
            params.put("until", untilDate.getTime() / 1000);
            try (InputStream inputStream = HttpUtil.doGetStream(url, params);) {
                if (inputStream != null) {
                    FrameReader frameReader = new FrameReader(inputStream);
                    try {
                        Frame frame = frameReader.readFrame();
                        while (frame != null) {
                            logs.append(new String(frame.getPayload(), "UTF8"));
                            if (logs.length() > 1000000) {
                                break;
                            }
                            frame = frameReader.readFrame();
                        }
                    } catch (Throwable t) {}
                    finally {
                        try {
                            frameReader.close();
                        } catch (IOException e) {
                            LOG.error("日志读取错误", e);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logStr = logs.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            if (logStr.indexOf(DOCKER_LOG_ERROR_INFO) > -1) {
                logStr = "";
            }

        } catch (Exception e) {
            LOG.error("日志读取错误：", e);
        }

        return logStr;
    }

    @Override
    public void downloadPodCurrentLog(String tenantName, String podName, String appType, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            response.reset();
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + podName + ".log");
            ServletOutputStream outputStream = response.getOutputStream();

            Pod pod = null;
            try {
                pod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
            } catch (Exception e) {
                LOG.error("租户" + tenantName + "下未找到实例" + podName, e);
            }
            if (pod != null) {
                String containerId = getContainerId(pod, appType);
                DockerClient dockerClient = getDockerClient(pod.getStatus().getHostIP());
                LogStreamContainerResultCallback callback = new LogStreamContainerResultCallback(outputStream);
                dockerClient.logContainerCmd(containerId).withStdOut(true).withStdErr(true).exec(callback)
                        .awaitCompletion(DOCKER_LOG_DOWNLOAD, TimeUnit.SECONDS);
                LOG.info("容器日志下载成功");
            }
        } catch (IOException e) {
            LOG.error("FileController  downloadTemplate:" + e.getMessage());
        } catch (Exception e) {
            LOG.error("容器日志读取错误：", e);
            e.printStackTrace();
        }
    }

    @Override
    public void uploadPodFileViaBrowser(String tenantName, String podName, MultipartFile srcFile, String destPath,
            String appType) {

        String path = "";
        try {
            // 本地存放路径：Global.CI_IMAGE_TEMP_PATH/containerFile/tenantName/serviceName/pod随机串/
            path = XcloudProperties.getConfigMap().get(Global.CI_IMAGE_TEMP_PATH) + "containerFile/" + tenantName + "/"
                    + podName.substring(0, podName.indexOf("-")) + "/"
                    + podName.substring(podName.lastIndexOf("-") + 1);
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path += "/" + srcFile.getOriginalFilename();
            FileUtils.storeFile(srcFile.getInputStream(), path);

            // 执行文件上传
            uploadPodFile(tenantName, podName, path, destPath, appType);
        } catch (ErrorMessageException e) {
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOG.error("通过浏览器方式上传文件" + srcFile.getName() + "到租户" + tenantName + "下的实例" + podName + "失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "文件上传失败");
        }
        finally {
            try {
                File file = new File(path);
                file.delete();
            } catch (Exception e) {
                LOG.error("文件" + path + "删除失败", e);
            }
        }
    }

    /**
     * 容器文件上传功能
     * @Title: uploadPodFile
     * @Description: 容器文件上传功能
     * @param tenantName
     * @param podName
     * @param sourcePath
     * @param destPath
     * @param appType void
     * @throws
     */
    private void uploadPodFile(String tenantName, String podName, String sourcePath, String destPath, String appType) {
        Map<String, Object> dockerInfo = getDockerClient(tenantName, podName, appType);
        DockerClient dockerClient = (DockerClient) dockerInfo.get("dockerClient");
        String containerId = dockerInfo.get("containerId").toString();

        try {
            dockerClient.copyArchiveToContainerCmd(containerId).withRemotePath(destPath).withHostResource(sourcePath)
                    .exec();
        } catch (Exception e) {
            LOG.error("上传文件" + sourcePath + "到租户" + tenantName + "下的实例" + podName + "失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "文件上传失败");
        }
    }

    /**
     * 根据租户名和pod名获取dockerClient相关信息
     * @Title: getDockerClient
     * @Description: 根据租户名和pod名获取dockerClient相关信息
     * @param tenantName
     * @param podName
     * @param appType
     * @return Map<String,Object>
     * @throws
     */
    private Map<String, Object> getDockerClient(String tenantName, String podName, String appType) {
        Map<String, Object> docker = new HashMap<String, Object>(4);

        Pod pod = null;
        try {
            pod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
        } catch (Exception e) {
            LOG.error("租户" + tenantName + "下未找到实例" + podName, e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "租户" + tenantName + "下未找到实例" + podName);
        }

        String containerId = getContainerId(pod, appType);
        DockerClient dockerClient = getDockerClient(pod.getStatus().getHostIP());
        docker.put("containerId", containerId);
        docker.put("dockerClient", dockerClient);

        return docker;
    }

    @Override
    public void uploadPodFileViaFtp(String tenantName, String podName, String ftpPath, String destPath,
            String appType) {

        // 本地存放路径：Global.CI_IMAGE_TEMP_PATH/containerFile/tenantName/serviceName/pod随机串/
        String localPath = XcloudProperties.getConfigMap().get(Global.CI_IMAGE_TEMP_PATH) + "containerFile/"
                + tenantName + "/" + podName.substring(0, podName.indexOf("-")) + "/"
                + podName.substring(podName.lastIndexOf("-") + 1);
        String filePath = ftpPath.substring(0, ftpPath.lastIndexOf(FtpUtils.FTP_SEPARATOR));
        String fileName = ftpPath.substring(ftpPath.lastIndexOf(FtpUtils.FTP_SEPARATOR) + 1);
        boolean deleteFtpFile = false;

        try {
            // 从FTP上下载文件
            FtpUtils.downloadFtpFile(filePath, localPath, fileName);
            // 执行文件上传
            uploadPodFile(tenantName, podName, localPath + "/" + fileName, destPath, appType);
            deleteFtpFile = true;
        } catch (ErrorMessageException e) {
            throw new ErrorMessageException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOG.error("通过FTP方式上传文件" + ftpPath + "到租户" + tenantName + "下的实例" + podName + "失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_ADD_FAILED, "文件上传失败");
        }
        finally {
            try {
                File file = new File(localPath + "/" + fileName);
                file.delete();
            } catch (Exception e) {
                LOG.error("文件" + localPath + "/" + fileName + "删除失败", e);
            }
            if (deleteFtpFile) {
                FtpUtils.removeDirAndSubFile(filePath, fileName);
            }
        }
    }

    @Override
    public void downloadPodFile(String tenantName, String podName, String fullPath, String appType,
            HttpServletRequest request, HttpServletResponse response) {

        Pod pod = null;
        try {
            pod = KubernetesClientFactory.getClient().inNamespace(tenantName).pods().withName(podName).get();
        } catch (Exception e) {
            LOG.error("租户" + tenantName + "下未找到实例" + podName, e);
        }

        if (pod != null) {
            String containerId = getContainerId(pod, appType);
            DockerClient dockerClient = getDockerClient(pod.getStatus().getHostIP());
            String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1) + ".tar";
            try {
                response.setHeader("Content-Disposition",
                        "attachment;fileName=" + new String(fileName.getBytes("GBK"), "ISO8859-1"));
            } catch (Exception e) {
                LOG.error("租户" + tenantName + "下的实例" + podName + " 设置响应头失败", e);
            }
            response.setContentType(request.getServletContext().getMimeType(fileName));

            try (InputStream inputStream = dockerClient.copyArchiveFromContainerCmd(containerId, fullPath).exec();
                    OutputStream outputStream = response.getOutputStream()) {
                byte[] b = new byte[1024];
                int num;
                while ((num = inputStream.read(b)) != -1) {
                    outputStream.write(b, 0, num);
                }
            } catch (Exception e) {
                LOG.error("下载租户" + tenantName + "下的实例" + podName + "中的文件" + fullPath + "失败", e);
            }
        }
    }

    @Override
    public String execCmdInContainer(String tenantName, String podName, String command, String appType) {
        Map<String, Object> dockerInfo = getDockerClient(tenantName, podName, appType);
        DockerClient dockerClient = (DockerClient) dockerInfo.get("dockerClient");
        String containerId = dockerInfo.get("containerId").toString();
        String result = "";

        try {
            String[] cmd = command.split("\\s+");
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId).withAttachStdout(true)
                    .withAttachStderr(true).withCmd(cmd).exec();
            dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(new ExecStartResultCallback(stdout, stderr))
                    .awaitCompletion();

            result = stdout.toString();
            if (StringUtils.isNotEmpty(stderr.toString())) {
                LOG.info("stderr = " + stderr.toString());
                throw new ErrorMessageException(ReturnCode.CODE_DOCKER_EXEC_OPERATION_FAILED, "执行命令失败");
            }
        } catch (Exception e) {
            LOG.error("执行docker命令失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_EXEC_OPERATION_FAILED, "执行命令失败");
        }

        return result;
    }

}
