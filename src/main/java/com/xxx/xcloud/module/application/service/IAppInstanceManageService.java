package com.xxx.xcloud.module.application.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.xxx.xcloud.rest.v1.service.model.Event;
import com.xxx.xcloud.rest.v1.service.model.PodInfo;

/**
 * @ClassName: IInstanceManageService
 * @Description: 实例管理相关接口定义
 * @author zyh
 * @date 2019年10月25日
 *
 */
public interface IAppInstanceManageService {
    /**
     * 获取服务的实例详情
     * @Title: getPodInfo
     * @Description: 获取服务的实例详情(服务必须在运行状态)
     * @param serviceId
     * @param tenantName
     * @return List<PodInfo>
     * @throws
     */
    List<PodInfo> getPodInfo(String serviceId, String tenantName);

    /**
     * 获取服务实例的不同状态个数
     * @Title: podNumberDifferentStates
     * @Description: 获取服务实例的不同状态个数
     * @param serviceId
     * @return Map<String,Integer>
     * @throws
     */
    Map<String, Integer> podNumberDifferentStates(String serviceId);

    /**
     * 删除单个pod
     * @Title: deletePod
     * @Description: 删除单个pod(删除以后会重新拉起一个pod)
     * @param tenantName
     * @param podName
     * @return boolean
     * @throws
     */
    boolean deletePod(String tenantName, String podName);

    /**
     * 根据租户名称和服务名称查询pod信息
     * @Title: getPods
     * @Description: 根据租户名称和服务名称查询pod信息
     * @param tenantName
     * @param serviceName
     * @return Map<String,Object>
     * @throws
     */
    Map<String, Object> getPods(String tenantName, String serviceName);

    /**
     * 获取实例事件日志
     * @Title: getContainerStartLogs
     * @Description: 获取实例事件日志
     * @param podName
     * @param tenantName
     * @return List<Event>
     * @throws
     */
    List<Event> getContainerStartLogs(String podName, String tenantName);

    /**
     * 获取当前Pod的日志
     * @Title: getPodLogs
     * @Description: 获取当前Pod的日志
     * @param tenantName
     * @param podName
     * @param apptype
     * @param logTail
     * @return String
     * @throws
     */
    String getPodLogs(String tenantName, String podName, String apptype, Integer logTail);

    /**
     * 获取当前Pod的实时日志
     * @Title: getCurrentPodLogs
     * @Description: 获取当前Pod的实时日志
     * @param tenantName
     * @param podName
     * @param apptype
     * @param logTail
     * @return String
     * @throws
     */
    String getCurrentPodLogs(String tenantName, String podName, String apptype, Integer logTail);

    /**
     * 根据起止时间获取日志
     * @Title: getPodLogByPeriod
     * @Description: 根据起止时间获取日志
     * @param tenantName
     * @param podName
     * @param apptype
     * @param since
     * @param until
     * @return String
     * @throws
     */
    String getPodLogByPeriod(String tenantName, String podName, String apptype, String since, String until);

    /**
     * 容器日志下载
     * @Title: downloadPodCurrentLog
     * @Description: 容器日志下载
     * @param tenantName
     * @param podName
     * @param appType
     * @param request
     * @param response void
     * @throws
     */
    void downloadPodCurrentLog(String tenantName, String podName, String appType, HttpServletRequest request,
            HttpServletResponse response);

    /**
     * 容器文件上传(通过浏览器)
     * @Title: uploadPodFileViaBrowser
     * @Description: 容器文件上传(通过浏览器)
     * @param tenantName
     * @param podName
     * @param file
     * @param destPath
     * @param appType void
     * @throws
     */
    void uploadPodFileViaBrowser(String tenantName, String podName, MultipartFile file, String destPath,
            String appType);

    /**
     * 容器文件上传(借助FTP)
     * @Title: uploadPodFileViaFtp
     * @Description: 容器文件上传(借助FTP)
     * @param tenantName
     * @param podName
     * @param ftpPath
     * @param destPath
     * @param appType void
     * @throws
     */
    void uploadPodFileViaFtp(String tenantName, String podName, String ftpPath, String destPath, String appType);

    /**
     * 容器文件下载
     * @Title: downloadPodFile
     * @Description: 容器文件下载
     * @param tenantName
     * @param podName
     * @param fullPath
     * @param appType
     * @param request
     * @param response void
     * @throws
     */
    void downloadPodFile(String tenantName, String podName, String fullPath, String appType, HttpServletRequest request,
            HttpServletResponse response);

    /**
     * 容器内执行命令
     * @Title: execCmdInContainer
     * @Description: 容器内执行命令
     * @param tenantName
     * @param podName
     * @param command
     * @param appType
     * @return String
     * @throws
     */
    String execCmdInContainer(String tenantName, String podName, String command, String appType);

}
