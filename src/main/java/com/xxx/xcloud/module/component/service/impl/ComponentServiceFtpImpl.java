package com.xxx.xcloud.module.component.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.FtpClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.Resources;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;
import com.xxx.xcloud.utils.StringUtils;

@Service
public class ComponentServiceFtpImpl extends BaseComponentServiceImpl {

    private static Logger LOG = LoggerFactory.getLogger(ComponentServiceFtpImpl.class);

    @Override
    protected Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {
        Map<String, String> map = new HashMap<>();

        LOG.info("ftp创建集群参数校验，tenantName：" + tenantName + ",jsonObject:" + JSON.toJSONString(jsonObject));
        // 1、参数校验

        String serviceName = jsonObject.getString("serviceName");
        String version = jsonObject.getString("version");

        String userName = jsonObject.getString("userName");
        String password = jsonObject.getString("password");

        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        String projectId = jsonObject.getString("projectId");
        String orderId = jsonObject.getString("orderId");
        String createdBy = jsonObject.getString("createdBy");
        String performance = jsonObject.getString("performance");

        checkFtpCreateParams(version, userName, password, cpu, memory, capacity);

        map.put("tenantName", tenantName);
        map.put("serviceName", serviceName);
        map.put("version", version);
        map.put("userName", userName);
        map.put("password", password);
        map.put("cpu", cpu);
        map.put("memory", memory);
        map.put("capacity", capacity);
        map.put("projectId", projectId);
        map.put("orderId", orderId);
        map.put("performance", performance);
        map.put("createdBy", createdBy);

        return map;
    }

    /**
     * 创建集群参数校验
     * 
     * @param version
     * @param userName
     * @param password
     * @param cpu
     * @param memory
     * @param capacity
     */
    private void checkFtpCreateParams(String version, String userName, String password, String cpu, String memory,
            String capacity) throws ErrorMessageException {

        // 校验version
        componentOperationsDataBaseUtil.checkVersion(CommonConst.APPTYPE_FTP, version);

        // 校验cpu，memory,capacity
        checkCpuAndMemory(cpu, memory);
        checkCapacity(capacity);

        // 校验其他参数
        if (StringUtils.isEmpty(userName)) {
            LOG.error("参数校验失败，userName为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，userName为空");
        }

        if (!userName.matches(CommonConst.CHECK_USER_NAME)) {
            LOG.error("参数校验失败，userName不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，userName不符合校验规则");
        }

        checkPassword(password);
    }

    @Override
    public StatefulService clusterCreate(Map<String, String> data) throws ErrorMessageException {
        LOG.info("创建集群，接收的参数=>data:" + JSON.toJSONString(data));

        // 1、接收参数
        String tenantName = data.get("tenantName");
        String serviceName = data.get("serviceName");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));

        String projectId = data.get("projectId");
        String orderId = data.get("orderId");
        String createdBy = data.get("createdBy");
        String version = data.get("version");
        String userName = data.get("userName");
        String password = data.get("password");
        Double capacity = Double.parseDouble(data.get("capacity"));
        int replicas = FtpClusterConst.FTP_REPLICAS_DEFAULT;

        Double cpuTotal = cpu * replicas;
        Double memoryTotal = memory * replicas;
        Double capacityTotal = capacity * replicas;

        // 2、 修改tenant表

        // boolean result = tenantService.updateUsedResource(tenantName,
        // cpuTotal, memoryTotal, capacityTotal);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + cpuTotal +
        // ",updateMemory:" + memoryTotal
        // + ",updateCapacity:" + capacityTotal);

        // 3、存表：StatefulService
        StatefulService service = null;
        JSONObject extendedField = new JSONObject();
        JSONObject userMap = new JSONObject();
        FtpUser ftpUser = new FtpUser();
        ftpUser.setUserName(userName);
        ftpUser.setPassword(password);
        ftpUser.setPermission(FtpClusterConst.USER_PERMISSION_DEFAULT);
        ftpUser.setStatus(FtpClusterConst.USER_STATUS_DEFAULT);
        // ftp user directory，默认生成且不允许更改
        String ftpDirectory = "/ftp/" + serviceName + "/" + userName + "/";
        ftpUser.setDirectory(ftpDirectory);
        ftpUser.setEffective(CommonConst.EFFECTIVE_TRUE);
        userMap.put(userName, ftpUser);
        extendedField.put(FtpClusterConst.EXTENDED_FIELD_FTPUSER, userMap);
        extendedField.put(CommonConst.RESOURCE_EFFECTIVE, CommonConst.EFFECTIVE_TRUE);

        try {
            service = componentOperationsDataBaseUtil.buildStatefulService(tenantName, createdBy, projectId, orderId,
                    CommonConst.APPTYPE_FTP, serviceName, version, replicas, password, null, cpuTotal, memoryTotal,
                    capacityTotal, null, JSON.toJSONString(extendedField), CommonConst.ACTION_CLUSTER_CREATE);
        } catch (ErrorMessageException e) {
            LOG.error("创建集群失败", e.getMessage());
            // 集群创建失败tenent回滚
            // result = tenantService.updateUsedResource(tenantName, cpuTotal *
            // (-1), memoryTotal * (-1),
            // capacityTotal * (-1));
            // LOG.info("ftp集群创建存表失败回滚，修改tenant表结果：result:" + result +
            // ",updateCpu:" + cpuTotal * (-1) + ",updateMemory:"
            // + memoryTotal * (-1) + ",updateCapacity:" + capacityTotal *
            // (-1));
            throw e;
        }

        if (null == service) {
            LOG.error("创建集群时存StatefulService表返回为null，service:" + JSON.toJSONString(service));
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "创建集群时存StatefulService表返回为null,service:" + JSON.toJSONString(service));
        }

        // 4、拼接创建ftpCluster的参数
        data.put("serviceId", service.getId());
        data.put("ftpUser", JSON.toJSONString(ftpUser));
        data.put("replicas", String.valueOf(replicas));
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.CLUSTER_CREATE_WORKER, data);

        return service;
    }

    @Override
    public boolean clusterStop(String tenantName, String serviceId) throws ErrorMessageException {

        // 1、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("停止集群时修改集群和节点状态为waiting失败,serviceId:" + serviceId + ",error:", e);
            throw e;
        }

        // 2、 拼接ftpCluster停止的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.CLUSTER_STOP_WORKER, params);
        return true;
    }

    @Override
    public boolean clusterStart(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("启动集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 1、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_START);
        } catch (ErrorMessageException e) {
            LOG.error("启动集群时修改集群和节点状态为waiting失败,serviceId:" + serviceId + ",error:", e);
            throw e;
        }

        // 3、 拼接FTPCluster启动的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.CLUSTER_START_WORKER, params);
        return true;
    }

    @Override
    public boolean clusterDelete(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("删除集群，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                    CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_DELETE);
        } catch (ErrorMessageException e) {
            LOG.error("删除集群时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        // 2、 拼接ftpCluster删除的参数
        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("serviceId", serviceId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.CLUSTER_DELETE_WORKER, params);
        return true;
    }

    @Override
    public boolean nodeStart(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("启动节点，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);

        // 1、获取statefulNode
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("启动节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "启动节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
        }

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_START);
        } catch (ErrorMessageException e) {
            LOG.error("启动节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.NODE_START_WORKER, params);
        return true;
    }

    @Override
    public boolean nodeStop(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("停止节点，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);
        // 1、获取statefulNode
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("停止节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "停止节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
        }

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_STOP);
        } catch (ErrorMessageException e) {
            LOG.error("启动节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.NODE_STOP_WORKER, params);
        return true;
    }

    @Override
    public boolean nodeDelete(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("删除节点，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);
        // 1、获取statefulNode
        StatefulNode node = componentOperationsDataBaseUtil.getStatefulNodeById(nodeId);
        if (null == node) {
            LOG.error("删除节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "删除节点，根据nodeId获取statefulNode为空,nodeId:" + nodeId);
        }

        // 2、修改数据库中集群和节点状态
        try {
            componentOperationsDataBaseUtil.updateClusterAndNodeState(node.getServiceId(), CommonConst.STATE_CLUSTER_WAITING,
                    nodeId, CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_NODE_DELETE);
        } catch (ErrorMessageException e) {
            LOG.error("删除节点时修改集群和节点状态为waiting失败,error:", e);
            throw e;
        }

        Map<String, String> params = new HashMap<>();
        params.put("tenantName", tenantName);
        params.put("nodeId", nodeId);

        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.NODE_DELETE_WORKER, params);
        return true;
    }

    @Override
    public Map<String, String> checkUserCreate(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException {
        Map<String, String> map = new HashMap<>();

        LOG.info("ftp新增用户参数校验，tenantName：" + tenantName + ",serviceId" + serviceId + ",jsonObject:"
                + JSON.toJSONString(jsonObject));
        // 1、参数校验
        String userName = jsonObject.getString("userName");
        String password = jsonObject.getString("password");

        // 校验其他参数
        if (StringUtils.isEmpty(userName)) {
            LOG.error("参数校验失败，userName为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，userName为空");
        }

        if (!userName.matches(CommonConst.CHECK_USER_NAME)) {
            LOG.error("参数校验失败，userName不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，userName不符合校验规则");
        }
        checkPassword(password);

        // 校验用户名是否重复
        if (!checkUserNameIsNotExist(tenantName, serviceId, userName)) {
            LOG.info("ftp集群中用户名" + userName + "已存在,serviceId:" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "用户名" + userName + "已存在");
        }

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("userName", userName);
        map.put("password", password);
        return map;
    }

    @Override
    public FtpUser userCreate(Map<String, String> data) throws ErrorMessageException {
        LOG.info("ftp新增用户，接收的参数=>data:" + JSON.toJSONString(data));

        // 1、获取参数
        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        String userName = data.get("userName");
        String password = data.get("password");

        // 2、获取serviceName
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("新增用户时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "新增用户时根据serviceId获取statefulService为null");
        }

        // 3、获取旧的ftpCluster
        FtpCluster oldCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("新增用户时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "新增用户时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_USER_ADD);

        // 4、生成新的ftpUser
        FtpUser ftpUser = new FtpUser();
        ftpUser.setUserName(userName);
        ftpUser.setPassword(password);
        ftpUser.setPermission(FtpClusterConst.USER_PERMISSION_DEFAULT);
        ftpUser.setStatus(FtpClusterConst.USER_STATUS_DEFAULT);
        // ftp user directory，默认生成且不允许更改
        String ftpDirectory = "/ftp/" + service.getServiceName() + "/" + userName + "/";
        ftpUser.setDirectory(ftpDirectory);
        ftpUser.setEffective(CommonConst.EFFECTIVE_FALSE);

        String extendedFieldStr = service.getExtendedField();
        JSONObject extendedFieldJson = JSON.parseObject(extendedFieldStr);
        if (extendedFieldJson.containsKey(FtpClusterConst.EXTENDED_FIELD_FTPUSER)) {
            JSONObject extendedFieldUserJson = extendedFieldJson.getJSONObject(FtpClusterConst.EXTENDED_FIELD_FTPUSER);
            extendedFieldUserJson.put(userName, ftpUser);
            extendedFieldJson.put(FtpClusterConst.EXTENDED_FIELD_FTPUSER, extendedFieldUserJson);
        } else {
            JSONObject extendedFieldUserJson = new JSONObject();
            extendedFieldUserJson.put(userName, ftpUser);
            extendedFieldJson.put(FtpClusterConst.EXTENDED_FIELD_FTPUSER, JSON.toJSONString(extendedFieldUserJson));
        }
        LOG.info("ftp新增用户，tenantName：" + tenantName + ",serviceName:" + service.getServiceName() + ",ftpUser:"
                + JSON.toJSONString(ftpUser) + ",extendedField:" + JSON.toJSONString(extendedFieldJson));

        optUserCreateOrUpdate(tenantName, serviceId, service.getServiceName(), ftpUser, oldCluster,
                FtpClusterConst.OPERATOR_USER_ADD, extendedFieldJson);
        return ftpUser;
    }

    @Override
    public Map<String, String> checkUserUpdate(String tenantName, String serviceId, String userName,
            JSONObject jsonObject) throws ErrorMessageException {
        Map<String, String> map = new HashMap<>();

        LOG.info("ftp修改用户参数校验，tenantName：" + tenantName + ",serviceId" + serviceId + ",userName" + userName
                + ",jsonObject:" + JSON.toJSONString(jsonObject));
        // 1、参数校验
        String password = jsonObject.getString("password");
        String permission = jsonObject.getString("permission");
        String status = jsonObject.getString("status");

        checkPassword(password);

        if (StringUtils.isEmpty(permission)) {
            LOG.error("参数校验失败，permission为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "修改用户参数校验失败，permission为空");
        }

        if (StringUtils.isEmpty(status)) {
            LOG.error("参数校验失败，status为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "修改用户参数校验失败，status为空");
        }

        // 校验用户名是否不存在
        if (checkUserNameIsNotExist(tenantName, serviceId, userName)) {
            LOG.info("ftp集群中用户名" + userName + "不存在,serviceId:" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "用户名" + userName + "不存在");
        }

        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("userName", userName);
        map.put("password", password);
        map.put("status", status);
        map.put("permission", permission);

        return map;
    }

    @Override
    public FtpUser userUpdate(Map<String, String> data) throws ErrorMessageException {
        LOG.info("ftp修改用户，接收的参数=>data:" + JSON.toJSONString(data));

        // 1、获取参数
        String tenantName = data.get("tenantName");
        String serviceId = data.get("serviceId");
        String userName = data.get("userName");
        String password = data.get("password");
        int status = Integer.parseInt(data.get("status"));
        String permission = data.get("permission");

        // 2、获取serviceName
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改用户时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改用户时根据serviceId获取statefulService为null");
        }

        // 3、获取旧的ftpCluster
        FtpCluster oldCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改用户时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改用户时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_USER_UPDATE);

        // 4、修改ftpUser
        FtpUser ftpUser = null;
        String extendedFieldStr = service.getExtendedField();
        JSONObject extendedFieldJson = JSON.parseObject(extendedFieldStr);
        if (extendedFieldJson.containsKey(FtpClusterConst.EXTENDED_FIELD_FTPUSER)) {
            JSONObject extendedFieldUserJson = extendedFieldJson.getJSONObject(FtpClusterConst.EXTENDED_FIELD_FTPUSER);
            if (extendedFieldUserJson.containsKey(userName)) {
                ftpUser = new FtpUser();
                ftpUser.setUserName(userName);
                ftpUser.setPassword(password);
                ftpUser.setPermission(permission);
                ftpUser.setStatus(status);
                ftpUser.setEffective(CommonConst.EFFECTIVE_FALSE);
                JSONObject ftpUserJson = extendedFieldUserJson.getJSONObject(userName);
                ftpUser.setDirectory(ftpUserJson.getString("directory"));
                extendedFieldUserJson.put(userName, ftpUser);
                extendedFieldJson.put(FtpClusterConst.EXTENDED_FIELD_FTPUSER, extendedFieldUserJson);
            }
        }
        LOG.info("ftp修改用户，tenantName：" + tenantName + ",serviceName:" + service.getServiceName() + ",ftpUser:"
                + JSON.toJSONString(ftpUser) + ",extendedField:" + JSON.toJSONString(extendedFieldJson));

        if (null == ftpUser) {
            LOG.error("修改用户时，service的userName不存在，tenantName" + tenantName + ",serviceName：" + service.getServiceName()
                    + ",userName:" + userName);
            componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改用户时从statefulService中获取的extendedField中不包含此用户，serviceId：" + serviceId + ",userName：" + userName);
        }
        optUserCreateOrUpdate(tenantName, serviceId, service.getServiceName(), ftpUser, oldCluster,
                FtpClusterConst.OPERATOR_USER_UPDATE, extendedFieldJson);

        return ftpUser;
    }

    @Override
    public void checkUserDelete(String tenantName, String serviceId, String userName) throws ErrorMessageException {
        LOG.info("ftp删除用户参数校验，tenantName：" + tenantName + "，serviceId" + serviceId + ",userName:" + userName);

        // 校验用户名是否不存在
        if (checkUserNameIsNotExist(tenantName, serviceId, userName)) {
            LOG.info("ftp集群中用户名" + userName + "不存在,serviceId:" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "用户名" + userName + "不存在");
        }
    }

    @Override
    public boolean userDelete(String tenantName, String serviceId, String userName) throws ErrorMessageException {
        LOG.info("ftp删除用户，接收的参数=>tenantName：" + tenantName + ",serviceId" + serviceId + ",userName:" + userName);

        // 1、获取serviceName
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("删除用户时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改用户时根据serviceId获取statefulService为null");
        }

        // 2、获取旧的ftpCluster
        FtpCluster oldCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("删除用户时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "删除用户时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_USER_DELETE);

        // 4、修改extendedField
        FtpUser ftpUser = null;
        String extendedFieldStr = service.getExtendedField();
        JSONObject extendedFieldJson = JSON.parseObject(extendedFieldStr);
        if (extendedFieldJson.containsKey(FtpClusterConst.EXTENDED_FIELD_FTPUSER)) {
            JSONObject extendedFieldUserJson = extendedFieldJson.getJSONObject(FtpClusterConst.EXTENDED_FIELD_FTPUSER);
            Object ftpUserRemove = extendedFieldUserJson.remove(userName);
            ftpUser = JSON.parseObject(JSON.toJSONString(ftpUserRemove), FtpUser.class);
            extendedFieldJson.put(FtpClusterConst.EXTENDED_FIELD_FTPUSER, extendedFieldUserJson);
        }
        LOG.info("ftp删除用户，tenantName：" + tenantName + ",serviceName:" + service.getServiceName() + ",ftpUser:"
                + JSON.toJSONString(ftpUser) + ",extendedField:" + JSON.toJSONString(extendedFieldJson));

        if (ftpUser == null) {
            LOG.info("删除用户时，service的userName不存在或已经被删除，tenantName" + tenantName + ",serviceName："
                    + service.getServiceName() + ",userName:" + userName);
            componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, serviceId,
                    service.getServiceName());
            return true;
        }
        optUserCreateOrUpdate(tenantName, serviceId, service.getServiceName(), ftpUser, oldCluster,
                FtpClusterConst.OPERATOR_USER_DELETE, extendedFieldJson);
        return true;
    }

    @Override
    public FtpUser getUserInfo(String tenantName, String serviceId, String userName) throws ErrorMessageException {
        LOG.info("获取user列表，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);
        try {
            checkTenantNameAndServiceIdExist(tenantName, serviceId);
        } catch (Exception e) {
            LOG.error("获取user列表时根据tenantName和serviceId获取service失败，error：", e);
            return null;
        }

        FtpUser ftpUser = null;
        Map<String, String> resultMap = checkUserNameIsNotExistOrGet(tenantName, serviceId, userName);
        if (null != resultMap && !resultMap.isEmpty()) {
            if ("true".equals(resultMap.get("isNotExist"))) {
                LOG.info("ftp集群中用户名" + userName + "不存在,serviceId:" + serviceId);
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "用户名" + userName + "不存在");
            } else if ("false".equals(resultMap.get("isNotExist")) && resultMap.containsKey("ftpUser")) {
                ftpUser = JSON.parseObject(resultMap.get("ftpUser"), FtpUser.class);
            }
        }
        return ftpUser;
    }

    @Override
    public boolean checkUserNameIsNotExist(String tenantName, String serviceId, String userName)
            throws ErrorMessageException {
        Map<String, String> resultMap = checkUserNameIsNotExistOrGet(tenantName, serviceId, userName);
        boolean isNotExist = false;
        if (null != resultMap && !resultMap.isEmpty()) {
            isNotExist = Boolean.parseBoolean(resultMap.get("isNotExist"));
        }
        return isNotExist;
    }

    private Map<String, String> checkUserNameIsNotExistOrGet(String tenantName, String serviceId, String userName)
            throws ErrorMessageException {
        Map<String, String> map = new HashMap<>();
        // 不包含用户名返回true，包含用户名返回false
        LOG.info("校验用户名是否重复，接收的参数=>tenantName：" + tenantName + ",serviceId:" + serviceId + ",userName:" + userName);
        checkTenantNameAndServiceIdExist(tenantName, serviceId);
        // 获取service
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("检查用户名是否重复时，根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "检查用户名是否重复时，根据serviceId获取statefulService为null");
        }

        // 获取扩展字段
        String extendedFieldStr = service.getExtendedField();
        LOG.info("service:" + service.getServiceName() + "的扩展字段为：" + extendedFieldStr);
        JSONObject extendedFieldJson = JSON.parseObject(extendedFieldStr);
        if (!extendedFieldJson.containsKey(FtpClusterConst.EXTENDED_FIELD_FTPUSER)) {
            LOG.error("ftp集群扩展字段中不包含key：" + FtpClusterConst.EXTENDED_FIELD_FTPUSER);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "检查ftp集群用户名是否重复失败，获取service的扩展字段失败");
        }

        // 校验用户名是否已存在
        JSONObject extendedFieldUserJson = extendedFieldJson.getJSONObject(FtpClusterConst.EXTENDED_FIELD_FTPUSER);
        if (null != extendedFieldUserJson && extendedFieldUserJson.containsKey(userName)) {
            LOG.info("ftp集群" + service.getServiceName() + "中用户名" + userName + "存在");
            map.put("isNotExist", "false");
            map.put("ftpUser", JSON.toJSONString(extendedFieldUserJson.getJSONObject(userName)));
            return map;

        }
        map.put("isNotExist", "true");
        return map;
    }

    /**
     * 处理ftp用户新增、修改、删除
     * 
     * @param tenantName
     * @param serviceId
     * @param serviceName
     * @param ftpUser
     * @param oldCluster
     * @param userOptType
     * @param extendedFieldMap
     */
    private void optUserCreateOrUpdate(String tenantName, String serviceId, String serviceName, FtpUser ftpUser,
            FtpCluster oldCluster, String userOptType, JSONObject extendedFieldJson) {
        // 拼接新的ftpCluster
        FtpCluster newCluster = buildClusterCreateUser(tenantName, serviceName, userOptType, ftpUser, oldCluster);

        // 调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateFtpCluster(tenantName, newCluster);

        componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, serviceId, serviceName);

        if (!updateResult) {
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                    "集群用户操作时，更新cluster失败，userOpt:" + userOptType + ",tenantName：" + tenantName + ",serviceId:"
                            + serviceId + ",newCluster:" + JSON.toJSONString(newCluster));
        }

        StatefulService service = null;
        try {
            service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
            if (null != service) {
                service.setExtendedField(JSON.toJSONString(extendedFieldJson));
                statefulServiceRepository.save(service);
            }
        } catch (Exception e) {
            LOG.error("集群用户操作时修改StatefulService表失败，userOpt：" + userOptType + ",serviceId：" + serviceId
                    + ",extendedField:" + JSON.toJSONString(extendedFieldJson), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群用户操作时修改statefulService失败，userOpt：" + userOptType + ",serviceId：" + serviceId + ",extendedField:"
                            + JSON.toJSONString(extendedFieldJson) + ",error:" + e.getMessage());
        }
    }

    /**
     * 新增，修改ftp user
     * 
     * @param tenantName
     * @param serviceName
     * @param operatorType
     * @param ftpUser
     * @return
     * @throws ErrorMessageException
     */
    private FtpCluster buildClusterCreateUser(String tenantName, String serviceName, String operatorType,
            FtpUser ftpUser, FtpCluster ftpCluster) throws ErrorMessageException {

        Map<String, String> usercnf = new HashMap<>();
        switch (operatorType) {
        case FtpClusterConst.OPERATOR_USER_ADD:
        case FtpClusterConst.OPERATOR_USER_UPDATE:
            usercnf = componentOperationsClientUtil.buildUserConfig(ftpUser, serviceName);
            ftpCluster.getSpec().getConfig().setUsercnf(usercnf);
            break;
        default:
            break;
        }

        ftpCluster.getSpec().getFtpop().setOperator(operatorType);
        ftpCluster.getSpec().getFtpop().setName(ftpUser.getUserName());
        return ftpCluster;
    }

    @Override
    protected Map<String, String> checkClusterExpandPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean clusterExpand(Map<String, String> data) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Map<String, String> checkClusterChangeResourcePersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        LOG.info("ftp集群修改资源个性化参数校验,tenantName:" + tenantName + ",serviceId:" + serviceId + ",jsonObject:" + jsonObject);
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        checkCpuAndMemory(cpu, memory);
        checkCapacity(capacity);

        Map<String, String> map = new HashMap<>();
        map.put("tenantName", tenantName);
        map.put("serviceId", serviceId);
        map.put("cpu", cpu);
        map.put("memory", memory);
        map.put("capacity", capacity);
        return map;
    }

    @Override
    public boolean changeResource(Map<String, String> data) throws ErrorMessageException {
        LOG.info("集群修改资源，接收的参数=>data:" + JSON.toJSONString(data));

        String serviceId = data.get("serviceId");
        Double cpu = Double.parseDouble(data.get("cpu"));
        Double memory = Double.parseDouble(data.get("memory"));
        Double capacity = Double.parseDouble(data.get("capacity"));
        String tenantName = data.get("tenantName");

        // 1、获取旧的ftpCluster
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改资源时根据serviceId获取statefulService为null");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "修改资源时根据serviceId获取statefulService为null");
        }

        FtpCluster oldCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
        if (null == oldCluster) {
            LOG.error("修改资源时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                    + service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_COMPONENT_YAML_FAILED,
                    "修改资源时根据tenantName和serviceName获取ftpcluster为空，tenantName：" + tenantName + ",serviceName:"
                            + service.getServiceName());
        }

        // 2、 修改tenant表
        // Double updateCpu = cpu * service.getNodeNum() - service.getCpu();
        // Double updateMemory = memory * service.getNodeNum() -
        // service.getMemory();
        // Double updateCapacity = capacity * service.getNodeNum() -
        // service.getStorage();
        // boolean result = tenantService.updateUsedResource(tenantName,
        // updateCpu, updateMemory, updateCapacity);
        // LOG.info("修改tenant表结果：result:" + result + ",updateCpu:" + updateCpu +
        // ",updateMemory:" + updateMemory
        // + ",updateCapacity:" + updateCapacity);
        // if (!result) {
        // return false;
        // }

        // 3、修改数据库中集群和节点状态
        componentOperationsDataBaseUtil.updateClusterAndNodesState(serviceId, CommonConst.STATE_CLUSTER_WAITING,
                CommonConst.STATE_NODE_WAITING, CommonConst.ACTION_CLUSTER_CHANGERESOURCE);

        FtpCluster newCluster = buildClusterChangeResource(tenantName, service, cpu, memory, capacity, oldCluster);

        // 4、调用k8sclient修改集群
        boolean updateResult = componentOperationsClientUtil.updateFtpCluster(tenantName, newCluster);

        if (!updateResult) {
            // 修改yaml失败tenant表回滚
            // result = tenantService.updateUsedResource(tenantName, updateCpu *
            // (-1), updateMemory * (-1),
            // updateCapacity * (-1));
            // LOG.info("修改资源时修改yaml失败，修改tenant表结果：result:" + result +
            // ",updateCpu:" + updateCpu * (-1) + ",updateMemory:"
            // + updateMemory * (-1) + ",updateCapacity:" + updateCapacity *
            // (-1));
            // 同步集群节点状态
            componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, service.getId(),
                    service.getServiceName());
            throw new ErrorMessageException(ReturnCode.CODE_K8S_UPDATE_COMPONENT_YAML_FAILED,
                    "集群修改资源时，更新cluster失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",newCluster:"
                            + JSON.toJSONString(newCluster));
        }

        if (StringUtils.isNotEmpty(data.get("capacity"))) {
            componentOperationsClientUtil.repleaceFtpClusterLvm(tenantName, newCluster);
        }

        boolean updateServiceFlag = false;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
            if (null != service) {
                int nodeNum = service.getNodeNum();
                if (StringUtils.isNotEmpty(String.valueOf(cpu))) {
                    service.setCpu(cpu * nodeNum);
                }
                if (StringUtils.isNotEmpty(String.valueOf(memory))) {
                    service.setMemory(memory * nodeNum);
                }
                if (StringUtils.isNotEmpty(String.valueOf(capacity))) {
                    service.setStorage(capacity * nodeNum);
                }
                statefulServiceRepository.save(service);
                updateServiceFlag = true;
            }
        } catch (ErrorMessageException e) {
            LOG.error("集群修改资源失败:", e);
            throw e;
        } catch (Exception e) {
            LOG.error("集群修改资源时修改集群资源和状态失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群修改资源时修改集群资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }
        finally {
            if (!updateServiceFlag) {
                LOG.error("集群修改资源时失败，同步集群和节点状态，tenantName：" + tenantName);
                // 同步集群节点状态
                componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, service.getId(),
                        service.getServiceName());
            }
        }

        List<StatefulNode> nodeList = null;
        boolean updateNodeFlag = false;
        try {
            nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId, CommonConst.STATE_NODE_DELETED);
            if (null == nodeList || nodeList.isEmpty()) {
                LOG.error("修改资源时根据serviceId获取nodeList为空，serviceId：" + serviceId);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                        "修改资源时根据serviceId获取nodeList为空，serviceId：" + serviceId);
            }
            for (StatefulNode statefulNode : nodeList) {
                statefulNode.setCpu(cpu);
                statefulNode.setMemory(memory);
                statefulNode.setStorage(capacity);
                statefulNodeRepository.save(statefulNode);
            }
            updateNodeFlag = true;
        } catch (Exception e) {
            LOG.error("集群修改资源时修改节点资源和状态失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED,
                    "集群修改资源时修改节点资源和状态失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }
        finally {
            if (!updateNodeFlag) {
                LOG.error("集群修改资源时失败，同步集群和节点状态，tenantName：" + tenantName);
                // 同步集群节点状态
                componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, service.getId(),
                        service.getServiceName());
            }
        }

        // 5、 拼接ftpCluster修改资源的参数
        componentOperationsDataBaseUtil.addPool(CommonConst.APPTYPE_FTP, FtpClusterConst.CLUSTER_CHANGE_RESOURCE_WORKER,
                data);
        return true;
    }

    private FtpCluster buildClusterChangeResource(String tenantName, StatefulService service, Double cpu, Double memory,
            Double capacity, FtpCluster ftpCluster) {

        ftpCluster.getSpec().getFtpop().setOperator(FtpClusterConst.OPERATOR_CHANGE_RESOURCE);

        Resources resources = componentOperationsClientUtil.getResources(cpu, memory, CommonConst.UNIT_GI);
        ftpCluster.getSpec().setResources(resources);

        if (StringUtils.isNotEmpty(String.valueOf(capacity))) {
            ftpCluster.getSpec().setCapacity(capacity + CommonConst.UNIT_GI);
        }
        return ftpCluster;
    }

    @Override
    protected Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean changeConfig(Map<String, String> data) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Page<FtpUser> getUserList(String tenantName, String serviceId, String userName, Pageable page)
            throws ErrorMessageException {

        List<FtpUser> ftpUserList = getUserArray(tenantName, serviceId, userName);
        Page<FtpUser> ftpUserPage = userArrayToPage(ftpUserList, page);
        return ftpUserPage;
    }

    private List<FtpUser> getUserArray(String tenantName, String serviceId, String userName)
            throws ErrorMessageException {
        LOG.info("获取user列表，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);
        try {
            checkTenantNameAndServiceIdExist(tenantName, serviceId);
        } catch (Exception e) {
            LOG.error("获取user列表时根据tenantName和serviceId获取service失败，error：", e);
            return null;
        }

        StatefulService service = null;
        service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId, CommonConst.STATE_CLUSTER_DELETED);

        String extendedFieldStr = service.getExtendedField();
        LOG.info("StatefulService的extendField为：" + extendedFieldStr);
        JSONObject extendedFieldJson = JSON.parseObject(extendedFieldStr);
        try {
            if (extendedFieldJson.containsKey(FtpClusterConst.EXTENDED_FIELD_FTPUSER)) {
                JSONObject extendedFieldUserJson = extendedFieldJson
                        .getJSONObject(FtpClusterConst.EXTENDED_FIELD_FTPUSER);
                Map<String, FtpUser> extendedFieldUserMap = JSON.parseObject(JSON.toJSONString(extendedFieldUserJson),
                        new TypeReference<Map<String, FtpUser>>() {});
                List<FtpUser> ftpUserList = new ArrayList<FtpUser>();
                if (StringUtils.isEmpty(userName)) {
                    ftpUserList.addAll(extendedFieldUserMap.values());
                } else {
                    for (Map.Entry<String, FtpUser> entry : extendedFieldUserMap.entrySet()) {
                        if (entry.getKey().contains(userName)) {
                            ftpUserList.add(entry.getValue());
                        }
                    }
                }
                Collections.sort(ftpUserList);
                return ftpUserList;
            }
        } catch (Exception e) {
            LOG.error("获取ftp用户列表异常：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询ftp用户列表异常：" + e.getMessage());
        }

        return null;
    }

    private Page<FtpUser> userArrayToPage(List<FtpUser> ftpUserList, Pageable pageable) throws ErrorMessageException {
        if (null != ftpUserList) {

            List<FtpUser> ftpUserPage = new ArrayList<FtpUser>();

            // 分页(pageNumber为0时, 为统计第一页的数据)
            int start = pageable.getPageNumber() * pageable.getPageSize();
            int end = start + pageable.getPageSize() - 1;
            int total = ftpUserList.size();
            end = end <= total - 1 ? end : total - 1;
            while (start <= end) {
                ftpUserPage.add(ftpUserList.get(start++));
            }

            // 生成分页对象
            Page<FtpUser> page = new PageImpl<>(ftpUserPage, pageable, total);
            return page;
        }
        return null;
    }
}
