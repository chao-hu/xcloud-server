package com.xxx.xcloud.module.component.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.component.consts.CodisClusterConst;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.consts.MemcachedClusterConst;
import com.xxx.xcloud.module.component.consts.MysqlClusterConst;
import com.xxx.xcloud.module.component.consts.PostgresqlClusterConst;
import com.xxx.xcloud.module.component.consts.RedisClusterConst;
import com.xxx.xcloud.module.component.entity.StatefulNode;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.entity.StatefulServiceComponentDefaultConfig;
import com.xxx.xcloud.module.component.model.ftp.FtpUser;
import com.xxx.xcloud.module.component.repository.StatefulNodeRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceComponentDefaultConfigRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceRepository;
import com.xxx.xcloud.module.component.repository.StatefulServiceUnitVersionRepository;
import com.xxx.xcloud.module.component.service.IComponentService;
import com.xxx.xcloud.module.component.util.ComponentOperationsClientUtil;
import com.xxx.xcloud.module.component.util.ComponentOperationsDataBaseUtil;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.repository.TenantRepository;
import com.xxx.xcloud.utils.StringUtils;

public abstract class BaseComponentServiceImpl implements IComponentService {

    private static Logger LOG = LoggerFactory.getLogger(BaseComponentServiceImpl.class);

    @Autowired
    protected ComponentOperationsDataBaseUtil componentOperationsDataBaseUtil;

    @Autowired
    protected ComponentOperationsClientUtil componentOperationsClientUtil;

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected StatefulServiceRepository statefulServiceRepository;

    @Autowired
    protected StatefulNodeRepository statefulNodeRepository;

    @Autowired
    protected StatefulServiceUnitVersionRepository statefulServiceUnitVersionRepository;

    @Autowired
    protected StatefulServiceComponentDefaultConfigRepository statefulServiceComponentDefaultConfigRepository;

    @Override
    public Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> getServiceConfig(String tenantName,
            String serviceId, String appType, String versionAccept) throws ErrorMessageException {
        LOG.info("获取service配置，接收的参数=>serviceId：" + serviceId + ",appType:" + appType + ",version:" + versionAccept);

        // 校验tenantName
        checkTenantNameExist(tenantName);

        // 校验appType,controller获取实现类时校验

        // 校验version
        if (StringUtils.isEmpty(versionAccept) && StringUtils.isEmpty(serviceId)) {
            LOG.error("获取配置失败，创建集群时要求version不能为空，集群创建后要求serviceId不能为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL,
                    "获取配置失败，创建集群时要求version不能为空，集群创建后要求serviceId不能为空");
        }

        String version = "";

        if (StringUtils.isEmpty(version) && StringUtils.isNotEmpty(serviceId)) {
            LOG.info("集群创建后获取配置");
            try {
                StatefulService service = statefulServiceRepository.findByNamespaceAndIdAndServiceStateNot(tenantName,
                        serviceId, CommonConst.STATE_CLUSTER_DELETED);
                if (null == service) {
                    LOG.error("根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                            + serviceId);
                    throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                            "获取配置失败，根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                                    + serviceId);
                }
                version = service.getVersion();
            } catch (Exception e) {
                LOG.error("获取配置失败，根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                        + serviceId);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                        "获取配置失败，根据tenantName和serviceId获取statefulService失败，tenantName：" + tenantName + "，serviceId:"
                                + serviceId + ",error:" + e.getMessage());
            }
        } else {
            LOG.info("集群创建时获取配置");
            version = versionAccept;
        }

        try {
            Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> serviceConfig = buildServiceConfig(
                    tenantName, serviceId, appType, version);
            if (null == serviceConfig || serviceConfig.isEmpty()) {
                LOG.error("获取service配置为空");
                return null;
            }
            return serviceConfig;
        } catch (ErrorMessageException e) {
            LOG.error("获取service配置失败:", e);
            throw e;
        } catch (Exception e) {
            LOG.error("获取service配置失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取service配置失败，error：" + e.getMessage());
        }
    }

    /**
     * 获取集群默认配置
     *
     * @param tenantName
     * @param serviceId
     * @param appType
     * @param version
     * @return
     */
    private Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> buildServiceConfig(String tenantName,
            String serviceId, String appType, String version) throws ErrorMessageException {

        Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> returnMap = new HashMap<>();
        List<String> groupList = null;
        try {
            if (appType.equals(CommonConst.APPTYPE_CODIS) || appType.equals(CommonConst.APPTYPE_REDIS)
                    || appType.equals(CommonConst.APPTYPE_MEMCACHED)
                    || CommonConst.APPTYPE_POSTGRESQL.equals(appType)) {
                groupList = statefulServiceComponentDefaultConfigRepository.getCnfFileList(appType, version);
            } else {
                groupList = statefulServiceComponentDefaultConfigRepository.getGroupList(appType, version);
            }

        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取service配置失败，获取groupList时失败，tenantName:" + tenantName + ",appType:" + appType + ",version:"
                            + version + ",error:" + e.getMessage());
        }

        if (null == groupList || groupList.isEmpty()) {
            LOG.error("获取service配置失败，获取的groupList为null");
            return returnMap;
        }
        try {
            for (String groupName : groupList) {
                Map<String, List<StatefulServiceComponentDefaultConfig>> groupMap = new HashMap<>();
                List<StatefulServiceComponentDefaultConfig> ordinaryList = new ArrayList<>();
                List<StatefulServiceComponentDefaultConfig> seniorList = new ArrayList<>();
                if (appType.equals(CommonConst.APPTYPE_CODIS) || appType.equals(CommonConst.APPTYPE_REDIS)
                        || appType.equals(CommonConst.APPTYPE_MEMCACHED)
                        || CommonConst.APPTYPE_POSTGRESQL.equals(appType)) {
                    ordinaryList = statefulServiceComponentDefaultConfigRepository
                            .findByAppTypeAndVersionAndCnfFileAndShowLevelOrderBySortAsc(appType, version, groupName,
                                    CommonConst.SHOW_LEVEL_ORDINARY);
                    seniorList = statefulServiceComponentDefaultConfigRepository
                            .findByAppTypeAndVersionAndCnfFileAndShowLevelOrderBySortAsc(appType, version, groupName,
                                    CommonConst.SHOW_LEVEL_SENIOR);
                } else {
                    ordinaryList = statefulServiceComponentDefaultConfigRepository
                            .findByAppTypeAndVersionAndGroupAndShowLevelOrderBySortAsc(appType, version, groupName,
                                    CommonConst.SHOW_LEVEL_ORDINARY);
                    seniorList = statefulServiceComponentDefaultConfigRepository
                            .findByAppTypeAndVersionAndGroupAndShowLevelOrderBySortAsc(appType, version, groupName,
                                    CommonConst.SHOW_LEVEL_SENIOR);
                }

                if (null != ordinaryList && !ordinaryList.isEmpty()) {
                    LOG.info("获取service配置，groupName：" + groupName + ",ordinaryList:" + JSON.toJSONString(ordinaryList));
                    groupMap.put(CommonConst.LEVEL_ORDINARY, ordinaryList);
                }
                if (null != seniorList && !seniorList.isEmpty()) {
                    LOG.info("获取service配置，groupName：" + groupName + ",seniorList:" + JSON.toJSONString(seniorList));
                    groupMap.put(CommonConst.LEVEL_SENIOR, seniorList);
                }
                if (groupMap.isEmpty()) {
                    LOG.error("获取service配置失败，获取的groupName:" + groupName + "下的参数为null");
                } else {
                    returnMap.put(groupName, groupMap);
                }
            }
        } catch (Exception e) {
            LOG.error("获取service配置失败，根据groupList获取service默认配置时失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取service配置失败，根据groupList获取service默认配置时失败,error:" + e.getMessage());
        }

        LOG.info("获取默认的service配置，returnMap:" + JSON.toJSONString(returnMap));

        if (null == serviceId) {
            return returnMap;
        }

        returnMap = mergeServiceConfig(serviceId, returnMap);
        LOG.info("合并后的service配置，returnMap:" + JSON.toJSONString(returnMap));
        return returnMap;
    }

    /**
     * 合并配置参数
     *
     * @param serviceId
     * @param defaultConfig
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> mergeServiceConfig(String serviceId,
            Map<String, Map<String, List<StatefulServiceComponentDefaultConfig>>> defaultConfig)
            throws ErrorMessageException {

        StatefulService service = null;
        try {
            service = statefulServiceRepository.findByIdAndServiceStateNot(serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
        } catch (Exception e) {
            LOG.error("获取service配置失败，获取合并的service配置时获取service失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "获取service配置失败，获取合并的service配置时获取service失败,serviceId:" + serviceId + ",error:" + e.getMessage());
        }

        if (null == service) {
            LOG.info("根据serviceId获取StatefulService失败，获取的service为null，serviceId：" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "获取service配置失败，根据serviceId获取service失败，serviceId：" + serviceId);
        }

        String configUpdated = service.getConfigUpdated();
        if (StringUtils.isEmpty(configUpdated)) {
            LOG.info("获取的service的configUpdated为空，service：" + JSON.toJSONString(service));
            return defaultConfig;
        }
        JSONObject updatedConfigJson = JSON.parseObject(configUpdated);
        if (null == updatedConfigJson || updatedConfigJson.isEmpty()) {
            LOG.info("获取的service的configUpdated转为map为空，service：" + JSON.toJSONString(service));
            return defaultConfig;
        }

        for (Map.Entry<String, Object> entryGroup : updatedConfigJson.entrySet()) {
            Map<String, List<StatefulServiceComponentDefaultConfig>> defaultGroupMap = defaultConfig
                    .get(entryGroup.getKey());

            JSONObject updatedGroupJson = JSON.parseObject(JSON.toJSONString(entryGroup.getValue()));

            for (Map.Entry<String, Object> entryLevel : updatedGroupJson.entrySet()) {
                Map<String, String> updatedLevelMap = JSON.parseObject(JSON.toJSONString(entryLevel.getValue()),
                        Map.class);
                List<StatefulServiceComponentDefaultConfig> defaultLevelList = defaultGroupMap.get(entryLevel.getKey());

                for (Map.Entry<String, String> entry : updatedLevelMap.entrySet()) {
                    for (StatefulServiceComponentDefaultConfig serviceComponentDefaultConfig : defaultLevelList) {
                        if (entry.getKey().equals(serviceComponentDefaultConfig.getKey())) {
                            serviceComponentDefaultConfig.setDefValue(entry.getValue());
                        }
                    }
                }
                defaultGroupMap.put(entryLevel.getKey(), defaultLevelList);
            }

            defaultConfig.put(entryGroup.getKey(), defaultGroupMap);
        }

        LOG.info("获取合并后的service配置，mergedMap:" + JSON.toJSONString(defaultConfig));
        return defaultConfig;
    }

    @Override
    public List<String> getUnitVersion(String appType, String extendedField) throws ErrorMessageException {
        LOG.info("获取组件的版本,接收的参数=>appType:" + appType + ",extendedField:" + extendedField);

        // 校验appType,controller获取实现类时校验

        List<String> unitVersionList = null;
        try {
            if (StringUtils.isNotEmpty(extendedField)) {
                unitVersionList = statefulServiceUnitVersionRepository.findByAppTypeAndExtendedField(appType,
                        extendedField);
            } else {
                unitVersionList = statefulServiceUnitVersionRepository.findByAppType(appType);
            }
        } catch (Exception e) {
            LOG.error("获取组件的版本失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取组件的版本失败，appType：" + appType + ",extendedField:" + extendedField + ",error：" + e.getMessage());
        }
        return unitVersionList;
    }

    @Override
    public List<StatefulService> getAvailableService(String tenantName, String projectId, String appType)
            throws ErrorMessageException {
        LOG.info("获取可用的服务,接收的参数=>tenantName:" + tenantName + ",projectId:" + projectId + ",appType:" + appType);

        // 校验tenantName
        checkTenantNameExist(tenantName);

        // 校验appType，controller获取实现类时校验

        List<StatefulService> serviceList = null;
        try {
            if (StringUtils.isNotEmpty(projectId)) {
                serviceList = statefulServiceRepository.findByNamespaceAndProjectIdAndAppTypeAndServiceState(tenantName,
                        projectId, appType, CommonConst.STATE_CLUSTER_RUNNING);
            } else {
                serviceList = statefulServiceRepository.findByNamespaceAndAppTypeAndServiceState(tenantName, appType,
                        CommonConst.STATE_CLUSTER_RUNNING);
            }
        } catch (Exception e) {
            LOG.error("获取可用的服务失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "获取可用的服务失败，tenantName:" + tenantName
                    + ",projectId" + projectId + ",appType" + appType + ",error：" + e.getMessage());
        }
        return serviceList;
    }

    @Override
    public Page<StatefulService> getServiceList(String tenantName, String projectId, String serviceName,
            String serviceState, String appType, Pageable pageable) throws ErrorMessageException {
        LOG.info("获取服务列表，接收的参数=>tenantName:" + tenantName + ",projectId:" + projectId + ",serviceName:" + serviceName
                + ",serviceState:" + serviceState);
        // 校验tenantName
        checkTenantNameExist(tenantName);

        Page<StatefulService> serviceList = null;
        try {
            serviceList = getServiceListByFilter(tenantName, projectId, serviceName, serviceState, appType, pageable);
        } catch (Exception e) {
            LOG.error("获取服务列表失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取服务列表失败，tenantName：" + tenantName + ",projectId:" + projectId + ",serviceName:" + serviceName
                            + ",serviceState:" + serviceState + ",pageable:" + JSON.toJSONString(pageable) + ",error:"
                            + e.getMessage());
        }
        return serviceList;
    }

    /**
     * 获取过滤后的集群列表
     *
     * @param tenantName
     * @param projectId
     * @param serviceName
     * @param serviceState
     * @param pageable
     * @return
     */
    private Page<StatefulService> getServiceListByFilter(String tenantName, String projectId, String serviceName,
            String serviceState, String appType, Pageable pageable) {
        Page<StatefulService> serviceList = null;
        boolean projectIdFilter = StringUtils.isNotEmpty(projectId);
        boolean serviceNameFilter = StringUtils.isNotEmpty(serviceName);
        boolean serviceStateFilter = StringUtils.isNotEmpty(serviceState);
        if (projectIdFilter) {
            if (serviceNameFilter) {
                if (serviceStateFilter) {
                    // 租户过滤+projectId过滤+serviceName过滤+状态过滤
                    serviceList = statefulServiceRepository
                            .findByNamespaceAndProjectIdAndServiceNameContainingAndServiceStateAndAppType(tenantName,
                                    projectId, serviceName, serviceState, appType, pageable);
                } else {
                    // 租户过滤+projectId过滤+serviceName过滤
                    serviceList = statefulServiceRepository
                            .findByNamespaceAndProjectIdAndServiceNameContainingAndAppTypeAndServiceStateNot(tenantName,
                                    projectId, serviceName, appType, CommonConst.STATE_CLUSTER_DELETED, pageable);
                }
            } else {
                if (serviceStateFilter) {
                    // 租户过滤+projectId过滤+状态过滤
                    serviceList = statefulServiceRepository.findByNamespaceAndProjectIdAndServiceStateAndAppType(
                            tenantName, projectId, serviceState, appType, pageable);
                } else {
                    // 租户过滤+projectId过滤
                    serviceList = statefulServiceRepository.findByNamespaceAndProjectIdAndAppTypeAndServiceStateNot(
                            tenantName, projectId, appType, CommonConst.STATE_CLUSTER_DELETED, pageable);
                }
            }
        } else {
            if (serviceNameFilter) {
                // 租户过滤+serviceName过滤+serviceState过滤
                if (serviceStateFilter) {
                    serviceList = statefulServiceRepository
                            .findByNamespaceAndServiceNameContainingAndServiceStateAndAppType(tenantName, serviceName,
                                    serviceState, appType, pageable);
                } else {
                    // 租户过滤+serviceName过滤
                    serviceList = statefulServiceRepository
                            .findByNamespaceAndServiceNameContainingAndAppTypeAndServiceStateNot(tenantName,
                                    serviceName, appType, CommonConst.STATE_CLUSTER_DELETED, pageable);
                }

            } else {
                if (serviceStateFilter) {
                    // 租户过滤+状态过滤
                    serviceList = statefulServiceRepository.findByNamespaceAndServiceStateAndAppType(tenantName,
                            serviceState, appType, pageable);
                } else {
                    // 租户过滤
                    serviceList = statefulServiceRepository.findByNamespaceAndAppTypeAndServiceStateNot(tenantName,
                            appType, CommonConst.STATE_CLUSTER_DELETED, pageable);
                }
            }
        }
        return serviceList;
    }

    @Override
    public StatefulService getServiceInfo(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("获取单个集群信息，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 校验tenantName
        checkTenantNameExist(tenantName);

        if (StringUtils.isEmpty(serviceId)) {
            LOG.error("获取单个集群信息失败，serviceId为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "获取单个集群信息失败，serviceId为空");
        }
        StatefulService service = null;
        try {
            service = statefulServiceRepository.findByNamespaceAndIdAndServiceStateNot(tenantName, serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
        } catch (ErrorMessageException e) {
            LOG.error("获取单个集群信息失败，error：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "获取单个集群信息失败，tenantName：" + tenantName + ",serviceId:" + serviceId + ",error:" + e.getMessage());
        }

        return service;
    }

    @Override
    public List<StatefulNode> getNodeList(String tenantName, String serviceId, String nodeName, String role,
            String nodeState) throws ErrorMessageException {
        LOG.info("获取节点列表，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId + ",nodeName:" + nodeName
                + ",role:" + role + ",nodeState:" + nodeState);

        // 校验tenantName和serviceId
        checkTenantNameAndServiceIdExist(tenantName, serviceId);

        List<StatefulNode> nodeList = null;
        try {
            nodeList = getNodeListByFilter(serviceId, nodeName, role, nodeState);
        } catch (Exception e) {
            LOG.error("获取节点列表失败：", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "获取节点列表失败，tenantName:" + tenantName + ",serviceId:" + serviceId + ",nodeName:" + nodeName + ",role:"
                            + role + ",nodeState:" + nodeState + ",error:" + e.getMessage());
        }
        return nodeList;
    }

    /**
     * 获取过滤后的nodeList
     *
     * @param serviceId
     * @param nodeName
     * @param role
     * @param nodeState
     * @return
     */
    private List<StatefulNode> getNodeListByFilter(String serviceId, String nodeName, String role, String nodeState) {
        List<StatefulNode> nodeList = null;
        boolean nodeNameFilter = StringUtils.isNotEmpty(nodeName);
        boolean roleFilter = StringUtils.isNotEmpty(role);
        boolean nodeStateFilter = StringUtils.isNotEmpty(nodeState);

        if (nodeNameFilter) {
            if (roleFilter) {
                if (nodeStateFilter) {
                    // serviceId+nodeName过滤+role过滤+nodeState过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndNodeNameContainingAndRoleAndNodeState(serviceId,
                            nodeName, role, nodeState);
                } else {
                    nodeList = statefulNodeRepository.findByServiceIdAndNodeNameContainingAndRoleAndNodeStateNot(
                            serviceId, nodeName, role, CommonConst.STATE_CLUSTER_DELETED);
                }
            } else {
                if (nodeStateFilter) {
                    // serviceId+nodeName过滤+nodeState过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndNodeNameContainingAndNodeState(serviceId,
                            nodeName, nodeState);
                } else {
                    // serviceId+nodeName过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndNodeNameContainingAndNodeStateNot(serviceId,
                            nodeName, CommonConst.STATE_CLUSTER_DELETED);
                }
            }
        } else {
            if (roleFilter) {
                if (nodeStateFilter) {
                    // serviceId+role过滤+nodeState过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndRoleAndNodeState(serviceId, role, nodeState);
                } else {
                    // serviceId+role过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndRoleAndNodeStateNot(serviceId, role,
                            CommonConst.STATE_CLUSTER_DELETED);
                }
            } else {
                if (nodeStateFilter) {
                    // serviceId+nodeState过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndNodeState(serviceId, nodeState);
                } else {
                    // serviceId过滤
                    nodeList = statefulNodeRepository.findByServiceIdAndNodeStateNot(serviceId,
                            CommonConst.STATE_CLUSTER_DELETED);
                }
            }
        }
        return nodeList;
    }

    @Override
    public void checkClusterUpdate(String tenantName, String serviceId) throws ErrorMessageException {
        LOG.info("集群启动，停止，删除操作参数校验，接收的参数=>tenantName:" + tenantName + ",serviceId:" + serviceId);

        // 校验tenantName和serviceId
        checkTenantNameAndServiceIdExist(tenantName, serviceId);
    }

    @Override
    public void checkNodeUpdate(String tenantName, String nodeId) throws ErrorMessageException {
        LOG.info("节点启动，停止，删除操作参数校验，接收的参数=>tenantName:" + tenantName + ",nodeId:" + nodeId);

        // 校验tenantName，serviceId和nodeId
        checkTenantNameAndNodeIdExist(tenantName, nodeId);
    }

    @Override
    public Map<String, String> checkClusterCreate(String tenantName, JSONObject jsonObject)
            throws ErrorMessageException {
        LOG.info("集群创建操作参数校验，接收的参数=>tenantName:" + tenantName + ",jsonObject:" + JSON.toJSONString(jsonObject));

        // 校验tenantName
        checkTenantNameExist(tenantName);

        // 校验appType，controller获取实现类时校验

        // 校验serviceName
        String serviceName = jsonObject.getString("serviceName");
        String appType = jsonObject.getString("appType");
        checkServiceNameIsExist(tenantName, appType, serviceName);

        return checkClusterCreatePersonaliseParameters(tenantName, jsonObject);
    }

    @Override
    public Map<String, String> checkClusterExpand(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException {
        // 校验tenantName和serviceId
        checkTenantNameAndServiceIdExist(tenantName, serviceId);

        return checkClusterExpandPersonaliseParameters(tenantName, serviceId, jsonObject);
    }

    @Override
    public Map<String, String> checkChangeResource(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException {
        // 校验tenantName和serviceId
        checkTenantNameAndServiceIdExist(tenantName, serviceId);

        return checkClusterChangeResourcePersonaliseParameters(tenantName, serviceId, jsonObject);
    }

    @Override
    public Map<String, String> checkChangeConfig(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException {
        // 校验tenantName和serviceId
        checkTenantNameAndServiceIdExist(tenantName, serviceId);

        return checkClusterChangeConfigPersonaliseParameters(tenantName, serviceId, jsonObject);
    }

    /**
     * 检查租户是否存在
     *
     * @param tenantName
     * @throws ErrorMessageException
     */
    protected void checkTenantNameExist(String tenantName) throws ErrorMessageException {
        if (StringUtils.isEmpty(tenantName)) {
            LOG.error("tenantName参数校验失败，tenantName为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "tenantName参数校验失败，tenantName为空");
        }

        Tenant tenant = null;
        try {
            tenant = tenantRepository.findByTenantName(tenantName);
        } catch (Exception e) {
            LOG.error("校验tenantName：" + tenantName + "是否存在时，查询数据库失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "校验租户" + tenantName + "是否存在时，查询数据库失败,error:" + e.getMessage());
        }
        if (null == tenant) {
            LOG.error("tenantName参数校验失败，tenantName：" + tenantName + "不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "tenantName参数校验失败，tenantName：" + tenantName + "不存在");
        }
    }

    /**
     * 检查租户以及租户中集群是否存在
     *
     * @param tenantName
     * @param serviceId
     * @throws ErrorMessageException
     */
    @Override
    public void checkTenantNameAndServiceIdExist(String tenantName, String serviceId) throws ErrorMessageException {
        checkTenantNameExist(tenantName);
        if (StringUtils.isEmpty(serviceId)) {
            LOG.error("serviceId参数校验失败，serviceId为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "serviceId参数校验失败，serviceId为空");
        }
        StatefulService statefulService = null;
        try {
            statefulService = statefulServiceRepository.findByNamespaceAndIdAndServiceStateNot(tenantName, serviceId,
                    CommonConst.STATE_CLUSTER_DELETED);
        } catch (Exception e) {
            LOG.error("校验集群ID: " + serviceId + "是否存在时，查询数据库失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "校验集群ID: " + serviceId + "是否存在时，查询数据库失败,error:" + e.getMessage());
        }
        if (null == statefulService) {
            LOG.error("租户" + tenantName + "中，不存在集群" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST,
                    "租户" + tenantName + "中，不存在集群" + serviceId);
        }
    }

    /**
     * 检查租户、租户中节点是否存在
     *
     * @param tenantName
     * @param nodeId
     * @throws ErrorMessageException
     */
    protected void checkTenantNameAndNodeIdExist(String tenantName, String nodeId) throws ErrorMessageException {
        checkTenantNameExist(tenantName);
        if (StringUtils.isEmpty(nodeId)) {
            LOG.error("参数校验失败，nodeId为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "参数校验失败，nodeId为空");
        }
        StatefulNode statefulNode = null;
        try {
            statefulNode = statefulNodeRepository.findByIdAndNodeStateNot(nodeId, CommonConst.STATE_NODE_DELETED);
        } catch (Exception e) {
            LOG.error("校验节点" + nodeId + "是否存在时，查询数据库失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "校验节点" + nodeId + "是否存在时，查询数据库失败,error:" + e.getMessage());
        }
        if (null == statefulNode) {
            LOG.error("参数校验失败，节点" + nodeId + "不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "参数校验失败，节点" + nodeId + "不存在");
        }
        StatefulService service = null;
        try {
            service = componentOperationsDataBaseUtil.getStatefulServiceById(statefulNode.getServiceId());
        } catch (Exception e) {
            LOG.error("校验节点" + nodeId + "的集群是否存在时，查询数据库失败:", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "校验节点" + nodeId + "的集群是否存在时，查询数据库失败,error:" + e.getMessage());
        }
        if (null == service) {
            LOG.error("参数校验失败，节点" + nodeId + "的集群不存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "参数校验失败，节点" + nodeId + "的集群不存在");
        }
    }

    /**
     * 检查集群名称是否已存在
     *
     * @param tenantName
     * @param appType
     * @param serviceName
     * @throws ErrorMessageException
     */
    @Override
    public boolean checkServiceNameIsExist(String tenantName, String appType, String serviceName)
            throws ErrorMessageException {
        if (StringUtils.isEmpty(serviceName)) {
            LOG.error("参数校验失败，serviceName为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，serviceName为空");
        }

        if (!serviceName.matches(CommonConst.CHECK_CLUSTER_NAME)) {
            LOG.error("参数校验失败，serviceName不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，serviceName不符合校验规则");
        }

        List<StatefulService> statefulServiceList = new ArrayList<>();
        try {
            statefulServiceList = statefulServiceRepository.findByNamespaceAndAppTypeAndServiceNameAndServiceStateNot(
                    tenantName, appType, serviceName, CommonConst.STATE_CLUSTER_DELETED);
        } catch (Exception e) {
            LOG.error("校验集群名称" + serviceName + "是否存在时，查询数据库失败");
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED,
                    "校验集群名称" + serviceName + "是否存在时，查询数据库失败");
        }

        if (null != statefulServiceList && !statefulServiceList.isEmpty()) {
            LOG.error("参数校验失败，集群名称" + serviceName + "已存在");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST,
                    "操作参数校验失败，集群名称" + serviceName + "已存在");
        }
        return true;
    }

    /**
     * 检查cpu、内存、version参数是否符合规范
     *
     * @param cpu
     * @param memory
     * @throws ErrorMessageException
     */
    protected void checkCpuAndMemory(String cpu, String memory) throws ErrorMessageException {
        if (StringUtils.isEmpty(cpu)) {
            LOG.error("参数校验失败，cpu为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，cpu为空");
        }
        if (StringUtils.isEmpty(memory)) {
            LOG.error("参数校验失败，memory为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，memory为空");
        }

        if (!cpu.matches(CommonConst.CHECK_RESOURCE_CPU)) {
            LOG.error("参数校验失败，cpu不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，cpu不符合校验规则");
        }

        if (!memory.matches(CommonConst.CHECK_RESOURCE_MEMORY)) {
            LOG.error("参数校验失败，memory不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，memory不符合校验规则");
        }

    }

    /**
     * 校验capacity
     *
     * @param capacity
     * @throws ErrorMessageException
     */
    protected void checkCapacity(String capacity) throws ErrorMessageException {
        if (StringUtils.isEmpty(capacity)) {
            LOG.error("参数校验失败，capacity为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，capacity为空");
        }

        if (!capacity.matches(CommonConst.CHECK_RESOURCE_CAPACITY)) {
            LOG.error("参数校验失败，capacity不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，capacity不符合校验规则");
        }
    }

    /**
     * 校验health check config
     *
     * @param capacity
     * @throws ErrorMessageException
     */
    protected void checkHealthCheckConfig(JSONObject healthCheckConfig) throws ErrorMessageException {
        List<String> healthCheckConfigList = MysqlClusterConst.getHealthCheckConfigList();
        if (!healthCheckConfigList.containsAll(healthCheckConfig.keySet())) {
            LOG.error("集群修改配置时，配置参数不符合规范,jsonObject:" + JSON.toJSONString(healthCheckConfig));
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "集群修改配置时，配置参数不符合规范,jsonObject:" + JSON.toJSONString(healthCheckConfig));
        }
        for (String configKey : healthCheckConfig.keySet()) {
            if (StringUtils.isEmpty(healthCheckConfig.getString(configKey))) {
                LOG.error("参数校验失败，" + configKey + "为空");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，HealthCheckConfig为空");
            }

            if (!healthCheckConfig.getString(configKey).matches(CommonConst.CHECK_HEALTH_CONFIG)) {
                LOG.error("参数校验失败，" + configKey + "不符合校验规则");
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "操作参数校验失败，HealthCheckConfig不符合校验规则");
            }
        }
    }

    /**
     * 校验replicas
     *
     * @param replicas
     * @throws ErrorMessageException
     */
    protected void checkReplicas(String replicas) throws ErrorMessageException {
        if (StringUtils.isEmpty(replicas)) {
            LOG.error("参数校验失败，replicas为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，replicas为空");
        }

        if (!replicas.matches(CommonConst.CHECK_CLUSTER_REPLICAS)) {
            LOG.error("参数校验失败，replicas不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，replicas不符合校验规则");
        }
    }

    /**
     * 校验addReplicas
     *
     * @param addReplicas
     * @throws ErrorMessageException
     */
    protected void checkAddReplicas(String addReplicas) throws ErrorMessageException {
        if (StringUtils.isEmpty(addReplicas)) {
            LOG.error("参数校验失败，addReplicas为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，addReplicas为空");
        }

        if (!addReplicas.matches(CommonConst.CHECK_CLUSTER_REPLICAS)) {
            LOG.error("参数校验失败，addReplicas不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，addReplicas不符合校验规则");
        }
    }

    /**
     * 校验password
     *
     * @param password
     * @throws ErrorMessageException
     */
    protected void checkPassword(String password) throws ErrorMessageException {
        if (StringUtils.isEmpty(password)) {
            LOG.error("参数校验失败，password为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，password为空");
        }

        if (!password.matches(CommonConst.CHECK_USER_PASSSWORD)) {
            LOG.error("参数校验失败，password不符合校验规则");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "操作参数校验失败，password不符合校验规则");
        }
    }

    /**
     * 校验type
     *
     * @param type
     * @throws ErrorMessageException
     */
    protected void checkType(String appType, String type) throws ErrorMessageException {
        // 校验是否为空
        if (StringUtils.isEmpty(type)) {
            LOG.error("参数校验失败，type为空");
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "操作参数校验失败，type为空");
        }
        // 校验正则
        switch (appType) {
        case CommonConst.APPTYPE_CODIS:
            if (!CodisClusterConst.CODIS_TYPE.equals(type.toLowerCase())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "操作参数校验失败，type不符合校验规则, type:" + type);
            }
            break;
        case CommonConst.APPTYPE_REDIS:
            if (!RedisClusterConst.getRedisTypeList().contains(type.toLowerCase())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "操作参数校验失败，type不符合校验规则, type:" + type);
            }
            break;
        case CommonConst.APPTYPE_MEMCACHED:
            if (!MemcachedClusterConst.getMemcachedTypeList().contains(type.toLowerCase())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "操作参数校验失败，type不符合校验规则, type:" + type);
            }
            break;
        case CommonConst.APPTYPE_MYSQL:
            if (!MysqlClusterConst.getMysqlTypeList().contains(type.toLowerCase())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "操作参数校验失败，type不符合校验规则, type:" + type);
            }
            break;
        case CommonConst.APPTYPE_POSTGRESQL:
            if (!PostgresqlClusterConst.getPostgresqlTypeList().contains(type.toLowerCase())) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                        "操作参数校验失败，type不符合校验规则, type:" + type);
            }
        default:
            break;
        }

    }

    /**
     * 集群创建其他个性化参数校验
     *
     * @param tenantName
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    protected abstract Map<String, String> checkClusterCreatePersonaliseParameters(String tenantName,
            JSONObject jsonObject) throws ErrorMessageException;

    /**
     * 集群扩展节点其他个性化参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    protected abstract Map<String, String> checkClusterExpandPersonaliseParameters(String tenantName, String serviceId,
            JSONObject jsonObject) throws ErrorMessageException;

    /**
     * 集群修改资源其他个性化参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */

    protected abstract Map<String, String> checkClusterChangeResourcePersonaliseParameters(String tenantName,
            String serviceId, JSONObject jsonObject) throws ErrorMessageException;

    /**
     * 集群修改参数其他个性化参数校验
     *
     * @param tenantName
     * @param serviceId
     * @param jsonObject
     * @return
     * @throws ErrorMessageException
     */
    protected abstract Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName,
            String serviceId, JSONObject jsonObject) throws ErrorMessageException;

    @Override
    public Map<String, String> checkUserCreate(String tenantName, String serviceId, JSONObject jsonObject)
            throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FtpUser userCreate(Map<String, String> data) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> checkUserUpdate(String tenantName, String serviceId, String userName,
            JSONObject jsonObject) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FtpUser userUpdate(Map<String, String> data) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void checkUserDelete(String tenantName, String serviceId, String userName) throws ErrorMessageException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean userDelete(String tenantName, String serviceId, String userName) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Page<FtpUser> getUserList(String tenantName, String serviceId, String userName, Pageable page)
            throws ErrorMessageException {
        return null;
    }

    @Override
    public FtpUser getUserInfo(String tenantName, String serviceId, String userName) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean checkUserNameIsNotExist(String tenantName, String serviceId, String userName)
            throws ErrorMessageException {
        return false;
    }

    @Override
    public JSONObject getPrometheusConfig(String tenantName, String serviceId) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isDependenceService(String serviceId) {
        boolean isDependence = false;
        try {
            isDependence = componentOperationsDataBaseUtil.isDependenceService(serviceId);
        } catch (ErrorMessageException e) {
            LOG.error("删除节点时，查询依赖表失败，error：", e.getMessage());
            throw e;
        }
        if (isDependence) {
            LOG.error("该服务正在被其他服务使用，不允许删除节点，serviceId：" + serviceId);
            throw new ErrorMessageException(ReturnCode.CODE_OPT_NODE_NOT_ALLOWED_FAILED,
                    "该服务正在被其他服务使用，不允许删除节点，serviceId：" + serviceId);
        }
        return isDependence;
    }

    @Override
    public Map<String, String> checkClusterChangeConfigPersonaliseParameters(String tenantName, String serviceId,
            String opt, JSONObject configuration) throws ErrorMessageException {
        // TODO Auto-generated method stub
        return null;
    }

}
