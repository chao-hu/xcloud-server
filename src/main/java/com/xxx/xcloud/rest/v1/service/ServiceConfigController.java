package com.xxx.xcloud.rest.v1.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.application.entity.HttpData;
import com.xxx.xcloud.module.application.entity.Service;
import com.xxx.xcloud.module.application.entity.ServiceAffinity;
import com.xxx.xcloud.module.application.entity.ServiceContainerLifecycle;
import com.xxx.xcloud.module.application.entity.ServiceHealth;
import com.xxx.xcloud.module.application.entity.ServiceHostpath;
import com.xxx.xcloud.module.application.exception.ConfigMapException;
import com.xxx.xcloud.module.application.repository.ServiceAffinityRepository;
import com.xxx.xcloud.module.application.repository.ServiceRepository;
import com.xxx.xcloud.module.application.service.IAppConfigService;
import com.xxx.xcloud.module.application.service.IAppDetailService;
import com.xxx.xcloud.module.ceph.entity.ServiceAndCephFile;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.configmap.entity.ServiceConfig;
import com.xxx.xcloud.rest.v1.service.model.AffinityUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.EnvUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceBatchCephUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceBatchConfigUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceCephFileUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceCephRbdUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceConfigUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceContainerLifecycleDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceHealthUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceHpaDTO;
import com.xxx.xcloud.rest.v1.service.model.ServiceLocalUpdateDTO;
import com.xxx.xcloud.rest.v1.service.model.ServicePortUpdateDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: ServiceConfigController
 * @Description: ServiceConfigController
 * @author zyh
 * @date 2019年10月25日
 *
 */
@Controller
@RequestMapping("/v1/service")
@Validated
public class ServiceConfigController {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfigController.class);

    @Autowired
    private IAppDetailService appDetailService;
    @Autowired
    private IAppConfigService appConfigService;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceAffinityRepository serviceAffinityRepository;

    /**
     * 批量保存端口
     * @Title: batchSavePorts
     * @Description: 批量保存端口
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/ports" }, method = RequestMethod.PUT)
    @ApiOperation(value = "批量保存端口", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult batchSavePorts(@PathVariable("serviceId") String serviceId,
            @RequestBody ServicePortUpdateDTO json) {

        Service service = null;

        try {
            service = appDetailService.getServiceById(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        if (null != service) {
            /*
             * if (!(Global.OPERATION_UNSTART == service.getStatus() ||
             * Global.OPERATION_STOPPED == service.getStatus() ||
             * Global.OPERATION_START_FAILED == service.getStatus())) {
             * LOG.info("服务 " + service.getServiceName() + " 不在停止状态下,不允许进行操作");
             * return new
             * ApiResult(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED,
             * "服务不在停止状态下,不允许进行操作"); }
             */

            Map<String, String> portAndProtocol = json.getPorts();
            try {
                checkPorts(portAndProtocol);
            } catch (ErrorMessageException e) {
                return new ApiResult(e.getCode(), e.getMessage());
            }
            service.setPortAndProtocol(JSON.toJSONString(portAndProtocol));
            LOG.info(JSON.toJSONString(service));
            try {
                appConfigService.updateServicePorts(service);
                serviceRepository.save(service);
            } catch (Exception e) {
                LOG.error("保存端口失败：", e);
                return new ApiResult(ReturnCode.CODE_K8S_UPDATE_SERVICE_FAILED, "保存端口失败");
            }
        } else {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "服务不存在");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "批量保存端口成功");
    }

    private void checkPorts(Map<String, String> portAndProtocol) {
        if (null != portAndProtocol && portAndProtocol.isEmpty()) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "因端口信息为空,不允许进行操作");
        }

        for (String key : portAndProtocol.keySet()) {
            try {
                Integer.parseInt(key);
            } catch (Exception e) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "端口不符合规范");
            }
            String protocol = portAndProtocol.get(key).toUpperCase();
            if (!Global.SERVICE_PORT_TCP.equals(protocol) && !Global.SERVICE_PORT_UDP.equals(protocol)) {
                throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "协议不符合规范");
            }
        }
    }

    /**
     * updateAffinity
     * @Title: updateAffinity
     * @Description: 修改服务的亲和属性(包括服务亲和以及节点亲和)
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/affinity" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务的亲和属性", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateAffinity(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody AffinityUpdateDTO json) {

        String id = json.getId();
        ServiceAffinity serviceAffinity = new ServiceAffinity();
        serviceAffinity.setId(id);
        serviceAffinity.setServiceId(serviceId);

        Integer affinityType = json.getServiceAffinityType();
        String affinity = json.getServiceAffinity();
        if (null != affinityType) {
            if (Global.NOT_USE_AFFINITY != affinityType && Global.AFFINITY != affinityType
                    && Global.ANTI_AFFINITY != affinityType) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "服务亲和状态不符合规范");
            }
            if (affinityType == Global.AFFINITY || affinityType == Global.ANTI_AFFINITY) {
                if (StringUtils.isEmpty(affinity)) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "亲和/反亲和服务名称为空");
                }
            }
            serviceAffinity.setServiceAffinity(affinity);
            serviceAffinity.setServiceAffinityType(affinityType.byteValue());
        }

        Integer nodeAffinityType = json.getNodeAffinityType();
        String nodeAffinity = json.getNodeAffinity();
        if (null != nodeAffinityType) {
            if (Global.NOT_USE_AFFINITY != nodeAffinityType && Global.AFFINITY != nodeAffinityType
                    && Global.ANTI_AFFINITY != nodeAffinityType) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "节点亲和状态不符合规范");
            }
            if (nodeAffinityType == Global.AFFINITY || nodeAffinityType == Global.ANTI_AFFINITY) {
                if (StringUtils.isEmpty(nodeAffinity)) {
                    return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "节点列表为空");
                }
            }
            serviceAffinity.setNodeAffinity(nodeAffinity);
            serviceAffinity.setNodeAffinityType(nodeAffinityType.byteValue());
        }

        Service service = null;
        try {
            service = appDetailService.getServiceById(serviceAffinity.getServiceId());
            if (Global.OPERATION_RUNNING == service.getStatus() || Global.OPERATION_STARTING == service.getStatus()
                    || Global.OPERATION_UPDATING == service.getStatus()
                    || Global.OPERATION_UPDATE_FAILED == service.getStatus()) {
                service.setIsRestartEffect(true);
            }
        } catch (Exception e) {
            LOG.error("获取当前服务失败, serviceId=" + serviceId, e);
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "获取当前服务失败");
        }

        try {
            serviceAffinity = serviceAffinityRepository.save(serviceAffinity);
            serviceRepository.save(service);
        } catch (ErrorMessageException e) {
            LOG.error("修改服务的亲和属性失败, serviceId=" + serviceId, e);
            return new ApiResult(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "修改服务的亲和属性失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceAffinity, "修改服务的亲和属性成功");
    }

    /**
     * batchServiceCephUpdate
     * @Title: batchServiceCephUpdate
     * @Description: 批量修改存储
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephs" }, method = RequestMethod.PUT)
    @ApiOperation(value = "批量修改存储", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult batchServiceCephUpdate(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceBatchCephUpdateDTO json) {

        // 解析最新的配置文件信息
        List<ServiceCephFileUpdateDTO> newMounts = json.getMountCephs();
        LOG.info("mountList----" + newMounts);
        // 删除原有配置文件信息
        List<ServiceAndCephFile> mountList = appConfigService.getServiceCephFile(serviceId);
        LOG.info("mountList----" + mountList);
        if (null != mountList && !mountList.isEmpty()) {
            for (ServiceAndCephFile mountCeph : mountList) {
                appConfigService.deleteServiceCephFile(mountCeph.getCephFileId(), serviceId);
            }
        }
        // 保存最新的配置文件信息
        ErrorMessageException errorMessageException = null;
        if (null != newMounts && !newMounts.isEmpty()) {
            int size = newMounts.size();
            int flag = 0;
            for (ServiceCephFileUpdateDTO mountCephDTO : newMounts) {
                ServiceAndCephFile mountCeph = new ServiceAndCephFile();
                mountCeph.setServiceId(serviceId);
                mountCeph.setId(mountCephDTO.getId());
                mountCeph.setMountPath(mountCephDTO.getMountPath());
                mountCeph.setCephFileId(mountCephDTO.getCephFileId());
                try {
                    appConfigService.updateServiceCephFile(mountCeph);
                } catch (ErrorMessageException e) {
                    flag++;
                    errorMessageException = e;
                }
            }
            if (size == flag) {
                return new ApiResult(errorMessageException.getCode(), errorMessageException.getMessage());
            }
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "批量修改存储成功");
    }

    /**
     * 获取服务存储卷
     * @Title: getServiceCephFs
     * @Description: 获取服务存储卷
     * @param serviceId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephfs" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务存储卷", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceCephFs(@PathVariable("serviceId") String serviceId) {
        List<ServiceAndCephFile> serviceAndCephFileList = null;
        try {
            serviceAndCephFileList = appConfigService.getServiceCephFile(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceAndCephFileList, "获取服务存储卷成功");
    }

    /**
     * 新增/修改服务存储卷
     * @Title: updateServiceCephfs
     * @Description: 新增/修改服务存储卷
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephfs" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务存储卷", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateServiceCephfs(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceCephFileUpdateDTO json) {

        ServiceAndCephFile serviceCephFile = new ServiceAndCephFile();
        serviceCephFile.setCephFileId(json.getCephFileId());
        serviceCephFile.setId(json.getId());
        serviceCephFile.setMountPath(json.getMountPath());
        serviceCephFile.setServiceId(serviceId);

        try {
            appConfigService.updateServiceCephFile(serviceCephFile);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务存储卷成功");
    }

    /**
     * 删除服务存储卷
     * @Title: deleteServiceCephfs
     * @Description: 删除服务存储卷
     * @param serviceId
     * @param cephFileId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephfs" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务存储卷", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "cephFileId", value = "应用服务关联文件存储ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult deleteServiceCephfs(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "cephFileId", required = true) String cephFileId) {

        try {
            appConfigService.deleteServiceCephFile(cephFileId, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务存储卷成功");
    }

    /**
     * 获取服务本地存储
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/localstorage" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务本地存储", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceLocalStorage(@PathVariable("serviceId") String serviceId) {
        List<ServiceHostpath> serviceHostpathList = null;
        try {
            serviceHostpathList = appConfigService.getServiceHostpath(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceHostpathList, "获取服务本地存储成功");
    }

    /**
     * 修改服务本地存储
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/localstorage" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务本地存储", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateServiceLocalStorage(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceLocalUpdateDTO json) {

        ServiceHostpath serviceHostpath = new ServiceHostpath();
        serviceHostpath.setHostPath(json.getHostPath());
        serviceHostpath.setId(json.getId());
        serviceHostpath.setMountPath(json.getMountPath());
        serviceHostpath.setServiceId(serviceId);

        try {
            appConfigService.updateServiceHostpath(serviceHostpath);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务本地存储成功");
    }

    /**
     * 删除服务本地存储
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/localstorage" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务本地存储", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceHostpathId", value = "应用服务关联本地存储ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult deleteServiceLocalStorage(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "serviceHostpathId", required = true) String serviceHostpathId) {

        try {
            appConfigService.deleteServiceHostpath(serviceHostpathId, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务本地存储成功");
    }

    /**
     * 修改服务块存储
     * @Title: updateServiceCephRbd
     * @Description: 修改服务块存储
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephrbd" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务块存储", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateServiceCephRbd(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceCephRbdUpdateDTO json) {

        // 参数校验
        String id = json.getId();
        String cephRbdId = json.getCephRbdId();
        String mountPath = json.getMountPath();

        ServiceCephRbd serviceCephRbd = new ServiceCephRbd();
        serviceCephRbd.setCephRbdId(cephRbdId);
        serviceCephRbd.setId(id);
        serviceCephRbd.setMountPath(mountPath);
        serviceCephRbd.setServiceId(serviceId);

        try {
            appConfigService.updateServiceCephRbd(serviceCephRbd);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务块存储成功");
    }

    /**
     * 删除服务块存储
     * @Title: deleteServiceCephRbd
     * @Description: 删除服务块存储
     * @param serviceId
     * @param serviceCephRbdId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephrbd" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务块存储", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceCephRbdId", value = "应用服务关联块存储ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult deleteServiceCephRbd(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "serviceCephRbdId", required = true) String serviceCephRbdId) {

        try {
            appConfigService.deleteServiceCephRbd(serviceCephRbdId, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务块存储成功");
    }

    /**
     * 获取服务块存储
     * @Title: getServiceCephRbd
     * @Description: 获取服务块存储
     * @param serviceId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/cephrbd" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务块存储", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceCephRbd(@PathVariable("serviceId") String serviceId) {
        List<ServiceCephRbd> serviceCephRbdList = null;
        try {
            serviceCephRbdList = appConfigService.getServiceCephRbd(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceCephRbdList, "获取服务块存储成功");
    }

    /**
     * 批量修改配置文件
     * @Title: batchConfigSave
     * @Description: 批量修改配置文件
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/configs" }, method = RequestMethod.PUT)
    @ApiOperation(value = "批量修改配置文件", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult batchConfigSave(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceBatchConfigUpdateDTO json) {

        // 删除原有配置文件信息
        List<ServiceConfig> configList = appConfigService.getServiceConfig(serviceId);
        if (null != configList && !configList.isEmpty()) {
            LOG.info("-------configList-----" + configList);
            for (ServiceConfig config : configList) {
                appConfigService.deleteServiceConfig(config.getId(), serviceId);
            }
        }
        // 保存最新的配置文件信息
        List<ServiceConfigUpdateDTO> configs = json.getConfig();
        ConfigMapException eConfigMapException = null;
        if (null != configs && !configs.isEmpty()) {
            int size = configs.size();
            int flag = 0;

            for (ServiceConfigUpdateDTO configDTO : configs) {
                ServiceConfig config = new ServiceConfig();
                config.setServiceId(serviceId);
                config.setId(configDTO.getId());
                config.setPath(configDTO.getPath());
                config.setConfigTemplateId(configDTO.getConfigTemplateId());
                try {
                    appConfigService.updateServiceConfig(config);
                } catch (ConfigMapException e) {
                    flag++;
                    eConfigMapException = e;
                }
            }
            if (size == flag) {
                return new ApiResult(eConfigMapException.getCode(), eConfigMapException.getMessage());
            }
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "批量修改配置文件成功");
    }

    /**
     * 获取服务配置文件
     * @Title: getServiceConfig
     * @Description: 获取服务配置文件
     * @param serviceId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/config" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务配置文件", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceConfig(@PathVariable("serviceId") String serviceId) {
        List<ServiceConfig> serviceConfig = null;
        try {
            serviceConfig = appConfigService.getServiceConfig(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceConfig, "获取服务配置文件成功");
    }

    /**
     * 修改服务配置文件
     * @Title: updateConfig
     * @Description: 修改服务配置文件
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/config" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务配置文件", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateConfig(@PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceConfigUpdateDTO json) {

        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setId(json.getId());
        serviceConfig.setPath(json.getPath());
        serviceConfig.setServiceId(serviceId);
        serviceConfig.setConfigTemplateId(json.getConfigTemplateId());

        try {
            appConfigService.updateServiceConfig(serviceConfig);
        } catch (ConfigMapException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务配置文件成功");
    }

    /**
     * 删除服务配置文件
     * @Title: deleteServiceConfig
     * @Description: 删除服务配置文件
     * @param serviceId
     * @param serviceConfigId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/config" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务配置文件", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceConfigId", value = "应用服务关联配置ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult deleteServiceConfig(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "serviceConfigId", required = true) String serviceConfigId) {

        try {
            appConfigService.deleteServiceConfig(serviceConfigId, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务配置文件成功");
    }

    /**
     * 获取服务环境变量
     * @Title: getServiceEnv
     * @Description: 获取服务环境变量
     * @param serviceId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/env" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务环境变量", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceEnv(@PathVariable("serviceId") String serviceId) {
        Map<String, Object> map = null;
        try {
            map = appConfigService.getServiceEnv(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, map, "获取服务环境变量成功");
    }

    /**
     * 修改服务环境变量
     * @Title: updateEnv
     * @Description: 修改服务环境变量
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/env" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务环境变量", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateEnv(@PathVariable("serviceId") String serviceId, @RequestBody EnvUpdateDTO json) {

        try {
            appConfigService.updateServiceEnv(serviceId, json.getEnvData());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务环境变量成功");
    }

    /**
     * 获取服务健康检查
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/health" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务健康检查", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceHealth(@PathVariable("serviceId") String serviceId) {
        List<ServiceHealth> serviceHealths = null;
        try {
            serviceHealths = appConfigService.getServiceHealth(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceHealths, "获取服务健康检查成功");
    }

    /**
     * 修改服务健康检查
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/health" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务健康检查", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updateServiceHealth(@PathVariable("serviceId") String serviceId,
            @RequestBody ServiceHealthUpdateDTO json) {

        // 参数校验
        String id = json.getId();
        int exec = json.getExec() == null ? 0 : 1;
        int http = json.getHttp() == null ? 0 : 1;
        int tcp = json.getTcp() == null ? 0 : 1;
        if (exec + http + tcp != 1) {
            return new ApiResult(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED, "exec、http、tcp只能选择其一");
        }
        ServiceHealth serviceHealth = new ServiceHealth();
        if (StringUtils.isNotEmpty(id)) {
            serviceHealth.setId(id);
        }
        serviceHealth.setExec(json.getExec());
        serviceHealth.setInitialDelay(json.getInitialDelay());
        serviceHealth.setPeriodDetction(json.getPeriodDetction());
        serviceHealth.setProbeType(json.getProbe().byteValue());
        serviceHealth.setServiceId(serviceId);
        serviceHealth.setSuccessThreshold(json.getSuccessThreshold().byteValue());
        if (json.getTcp() != null) {
            serviceHealth.setTcp(JSONObject.toJSONString(json.getTcp()));
        }
        if (json.getHttp() != null) {
            HttpData httpData = new HttpData();
            httpData.setHttpHeade(json.getHttp().getHttpHeade());
            httpData.setPath(json.getHttp().getPath());
            httpData.setPort(json.getHttp().getPort());
            serviceHealth.setHttpData(JSONObject.toJSONString(httpData));
        }
        serviceHealth.setTimeoutDetction(json.getTimeoutDetction());
        serviceHealth.setIsTurnOn(json.getIsTurnOn());

        try {
            appConfigService.updateServiceHealth(serviceHealth);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务健康检查成功");
    }

    /**
     * 删除服务健康检查
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/health" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务健康检查", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceHealthId", value = "健康检查ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult deleteServiceHealth(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "serviceHealthId", required = true) String serviceHealthId) {

        try {
            appConfigService.deleteServiceHealth(serviceHealthId, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务健康检查成功");
    }

    /**
     * 获取服务的容器生命周期
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/containerLifecycle" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务的容器生命周期", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult getServiceContainerLifecycle(@PathVariable("serviceId") String serviceId) {
        List<ServiceContainerLifecycle> serviceContainerLifecycles = null;
        try {
            serviceContainerLifecycles = appConfigService.getServiceContainerLifecycle(serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, serviceContainerLifecycles, "获取服务健康检查成功");
    }

    /**
     * 修改服务的容器生命周期
     * @Title: updataServiceContainerLifecycle
     * @Description: 修改服务的容器生命周期
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/containerLifecycle" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改服务的容器生命周期", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult updataServiceContainerLifecycle(@PathVariable("serviceId") String serviceId,
            @RequestBody ServiceContainerLifecycleDTO json) {

        // 参数校验
        String id = json.getId();
        String host = json.getHost();
        int exec = json.getExec() == null ? 0 : 1;
        int http = json.getHttp() == null ? 0 : 1;
        int tcp = json.getTcp() == null ? 0 : 1;
        if (exec + http + tcp != 1) {
            return new ApiResult(ReturnCode.CODE_OPT_SERVICE_NOT_ALLOWED_FAILED, "exec、http、tcp只能选择其一");
        }
        ServiceContainerLifecycle serviceContainerLifecycle = new ServiceContainerLifecycle();
        serviceContainerLifecycle.setHost(host);
        if (StringUtils.isNotEmpty(id)) {
            serviceContainerLifecycle.setId(id);
        }
        serviceContainerLifecycle.setExec(json.getExec());
        serviceContainerLifecycle.setLifecycleType(json.getLifecycleType());
        serviceContainerLifecycle.setServiceId(serviceId);
        if (json.getTcp() != null) {
            serviceContainerLifecycle.setTcp(JSONObject.toJSONString(json.getTcp()));
        }
        if (json.getHttp() != null) {
            HttpData httpData = new HttpData();
            httpData.setHttpHeade(json.getHttp().getHttpHeade());
            httpData.setPath(json.getHttp().getPath());
            httpData.setPort(json.getHttp().getPort());
            serviceContainerLifecycle.setHttpData(JSONObject.toJSONString(httpData));
        }
        serviceContainerLifecycle.setIsTurnOn(json.getIsTurnOn());

        try {
            appConfigService.updataServiceContainerLifecycle(serviceContainerLifecycle);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作服务的容器生命周期检查成功");
    }

    /**
     * 删除服务的容器生命周期
     * @Title: deleteServiceContainerLifecycle
     * @Description: 删除服务的容器生命周期
     * @param serviceId
     * @param serviceContainerLifecycleId
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/containerLifecycle" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除服务的容器生命周期", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceContainerLifecycleId", value = "容器生命周期检查ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String") })
    public ApiResult deleteServiceContainerLifecycle(@PathVariable("serviceId") String serviceId,
            @RequestParam(value = "serviceContainerLifecycleId", required = true) String serviceContainerLifecycleId) {

        try {
            appConfigService.deleteServiceContainerLifecycle(serviceContainerLifecycleId, serviceId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除服务的容器生命周期检查成功");
    }

    /**
     * 自动伸缩
     * @Title: hpaService
     * @Description: 自动伸缩
     * @param serviceId
     * @param json
     * @return ApiResult
     * @throws
     */
    @ResponseBody
    @RequestMapping(value = { "/{serviceId}/hpa" }, method = RequestMethod.PUT)
    @ApiOperation(value = "自动伸缩", notes = "")
    @ApiImplicitParam(paramType = "path", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult hpaService(@PathVariable("serviceId") String serviceId, @Valid @RequestBody ServiceHpaDTO json) {

        int minReplicas = json.getMinReplicas();
        int maxReplicas = json.getMaxReplicas();
        int cpuThreshold = json.getCpuThreshold();
        boolean isEnable = json.getIsEnable();
        try {
            appConfigService.serviceAutomaticScale(serviceId, minReplicas, maxReplicas, cpuThreshold, isEnable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "自动伸缩成功");
    }

}
