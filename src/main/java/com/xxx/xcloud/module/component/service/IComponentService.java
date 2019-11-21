package com.xxx.xcloud.module.component.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;

/**
 * @ClassName: IComponentService
 * @Description:  组件service接口
 * @author lnn
 * @date 2019年11月20日
 *
 */
public interface IComponentService {

    /**
     * 集群创建参数校验
     *
     * @param tenantName
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    Map<String, String> checkClusterCreate(String tenantName, JSONObject jsonObject) throws ErrorMessageException;

    /**
     * 集群创建
     *
     * @param data
     * @return
     * @throws ErrorMessageException
     */
    
    StatefulService clusterCreate(Map<String, String> data) throws ErrorMessageException;

    /**
     * 集群更新参数校验
     * @param tenantName
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */
    void checkClusterUpdate(String tenantName, String serviceId) throws ErrorMessageException;

    /**
     * 集群停止
     *
     * @param tenantName
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */
    boolean clusterStop(String tenantName, String serviceId) throws ErrorMessageException;

    /**
     * 集群启动
     *
     * @param tenantName
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */
    boolean clusterStart(String tenantName, String serviceId) throws ErrorMessageException;

    /**
     * 集群删除
     *
     * @param tenantName
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */
    boolean clusterDelete(String tenantName, String serviceId) throws ErrorMessageException;

    /**
     * 集群新增实例参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    Map<String, String> checkClusterExpand(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException;

    /**
     * 集群新增实例
     *
     * @param data
     * @return
     * @throws ErrorMessageException
     */
    boolean clusterExpand(Map<String, String> data) throws ErrorMessageException;

    /**
     * 用户创建，参数检查
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    Map<String, String> checkUserCreate(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException;

    /**
     * 创建用户：ftp组件
     *
     * @param data
     * @return
     * @throws ErrorMessageException
     */
    FtpUser userCreate(Map<String, String> data) throws ErrorMessageException;

    /**
     * 修改用户，参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param userName
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    Map<String, String> checkUserUpdate(String tenantName, String serviceId, String userName, JSONObject jsonObject)
            throws ErrorMessageException;

    /**
     * 修改用户：ftp组件
     *
     * @param data
     * @return
     * @throws ErrorMessageException
     */
    FtpUser userUpdate(Map<String, String> data) throws ErrorMessageException;

    /**
     * 删除用户，参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param userName
     * @return
     * @throws ErrorMessageException
     */
    void checkUserDelete(String tenantName, String serviceId, String userName) throws ErrorMessageException;

    /**
     * 删除用户： ftp组件
     *
     * @param tenantName
     * @param serviceId
     * @param userName
     * @return
     * @throws ErrorMessageException
     */
    boolean userDelete(String tenantName, String serviceId, String userName) throws ErrorMessageException;

    /**
     * 资源修改，参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    Map<String, String> checkChangeResource(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException;

    /**
     * 修改资源
     *
     * @param data
     * @return
     * @throws ErrorMessageException
     */
    boolean changeResource(Map<String, String> data) throws ErrorMessageException;

    /**
     * 检查修改配置
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */

    /**
     * 修改配置
     *
     * @param data
     * @return
     * @throws ErrorMessageException
     */
    boolean changeConfig(Map<String, String> data) throws ErrorMessageException;

    /**
     * 节点启动，停止，删除参数校验
     *
     * @param tenantName
     * @param nodeId
     * @return
     * @throws ErrorMessageException
     */
    void checkNodeUpdate(String tenantName, String nodeId) throws ErrorMessageException;

    /**
     * 节点启动
     *
     * @param tenantName
     * @param nodeId
     * @return
     * @throws ErrorMessageException
     */
    boolean nodeStart(String tenantName, String nodeId) throws ErrorMessageException;

    /**
     * 节点停止
     *
     * @param tenantName
     * @param nodeId
     * @return
     * @throws ErrorMessageException
     */
    boolean nodeStop(String tenantName, String nodeId) throws ErrorMessageException;

    /**
     * 节点删除
     *
     * @param tenantName
     * @param nodeId
     * @return
     * @throws ErrorMessageException
     */
    boolean nodeDelete(String tenantName, String nodeId) throws ErrorMessageException;

    /**
     * 获取集群列表
     *
     * @param tenantName
     * @param projectId
     * @param serviceName 模糊查询
     * @param serviceState
     * @param pageable
     * @param appType
     * @return
     * @throws ErrorMessageException
     */

    Page<StatefulService> getServiceList(String tenantName, String projectId, String serviceName, String serviceState,
            String appType, Pageable pageable) throws ErrorMessageException;

    /**
     * 获取节点列表
     *
     * @param tenantName
     * @param serviceId
     * @param nodeName：
     *            模糊查询
     * @param role
     * @param nodeState
     * @return
     * @throws ErrorMessageException
     */

    List<StatefulNode> getNodeList(String tenantName, String serviceId, String nodeName, String role, String nodeState)
            throws ErrorMessageException;

    /**
     * 获取单个集群的详细信息
     *
     * @param tenantName
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */
    StatefulService getServiceInfo(String tenantName, String serviceId) throws ErrorMessageException;

    /**
     * 获取集群的配置参数
     *
     * @param tenantName
     * @param serviceId
     * @param appType
     * @param version
     * @return
     * @throws ErrorMessageException
     */
    Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> getServiceConfig(String tenantName,
            String serviceId, String appType, String version) throws ErrorMessageException;
    
    
    /**
     * 校验集群的配置参数
     *
     * @param tenantName
     * @param serviceId
     * @param opt
     * @param configuration
     * @return
     * @throws ErrorMessageException
     */
    Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId, String opt,
            JSONObject configuration);


    /**
     * 获取某个组件所支持的version
     *
     * @param appType
     * @param extendedField
     * @return
     * @throws ErrorMessageException
     */
    List<String> getUnitVersion(String appType, String extendedField) throws ErrorMessageException;

    /**
     * 获取可用的服务，服务状态为Running
     *
     * @param tenantName
     * @param projectId
     * @param appType
     * @return
     * @throws ErrorMessageException
     */
    List<StatefulService> getAvailableService(String tenantName, String projectId, String appType)
            throws ErrorMessageException;

    /**
     * ftp用户列表分页,支持模糊查询
     * 
     * @param tenantName
     * @param serviceId
     * @param userName
     * @param pageable
     * @return
     * @throws ErrorMessageException
     */
    Page<FtpUser> getUserList(String tenantName, String serviceId, String userName, Pageable pageable)
            throws ErrorMessageException;

    /**
     * 获取用户信息
     *
     * @param tenantName
     * @param serviceId
     * @param userName
     * @return
     * @throws ErrorMessageException
     */
    FtpUser getUserInfo(String tenantName, String serviceId, String userName) throws ErrorMessageException;

    /**
     * 校验集群名称是否重复
     *
     * @param tenantName
     * @param appType
     * @param serviceName
     * @return
     * @throws ErrorMessageException
     */
    boolean checkServiceNameIsExist(String tenantName, String appType, String serviceName) throws ErrorMessageException;

    /**
     * 检查ftp用户是否存在
     *
     * @param tenantName
     * @param serviceId
     * @param userName
     * @return
     * @throws ErrorMessageException
     */
    boolean checkUserNameIsNotExist(String tenantName, String serviceId, String userName) throws ErrorMessageException;

    /**
     * 获取prometheus参数
     *
     * @param tenantName
     * @param serviceId
     * @return
     * @throws ErrorMessageException
     */

    JSONObject getPrometheusConfig(String tenantName, String serviceId) throws ErrorMessageException;

    /**
     * 判断是否有依赖的组件
     *
     * @param serviceId
     * @return
     */
    boolean isDependenceService(String serviceId);

    /**
     * 校验serviceId跟租户名称是否存在
     *
     * @param tenantName
     * @param serviceId
     * @return
     */
    void checkTenantNameAndServiceIdExist(String tenantName, String serviceId);

    /**
     * 校验参数
     * 
     * @param tenantName
     * @param serviceId
     * @param info
     * @return
     */
    Map<String, String> checkChangeConfig(String tenantName, String serviceId, JSONObject info);

}
