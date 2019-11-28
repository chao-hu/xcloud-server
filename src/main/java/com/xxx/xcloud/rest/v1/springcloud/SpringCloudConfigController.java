package com.xxx.xcloud.rest.v1.springcloud;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.springcloud.consts.SpringCloudCommonConst;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudConfigFile;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudConfigFileService;
import com.xxx.xcloud.module.springcloud.service.ISpringCloudService;
import com.xxx.xcloud.rest.v1.springcloud.model.SpringCloudConfigDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @ClassName: SpringCloudConfigController
 * @Description: SpringCloudConfigController
 * @author lnn
 * @date 2019年11月26日
 *
 */
@Controller
@RequestMapping("/v1/configbus")
public class SpringCloudConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(SpringCloudConfigController.class);

    @Autowired
    private ISpringCloudConfigFileService springCloudConfigFileService;

    @Autowired
    private ISpringCloudService configBusServiceImpl;

    /**
     * 新增配置
     *
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "新增配置", notes = "")
    public ApiResult createSpringCloudConfigFile(@RequestBody SpringCloudConfigDTO json) {

        ApiResult result = null;
        SpringCloudConfigFile configFile = null;
        // 解析参数;
        String serviceId = json.getServiceId();
        String configName = json.getConfigName();
        String configContent = json.getConfigContent();
        int enable = json.getEnable();

        result = checkConfigFile(serviceId, configName, configContent, enable);
        if (ReturnCode.CODE_SUCCESS != result.getCode()) {
            return result;
        }
        JSONObject serviceDate = JSON.parseObject(result.getData().toString());
        String cephFileId = serviceDate.getString("cephfileId");
        LOG.info("---------serviceDate-----------" + serviceDate);
        LOG.info("---------cephFileId-----------" + cephFileId);
        // 创建配置文件
        try {
            configFile = createConfigFile(serviceId, configName, configContent, enable, cephFileId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "配置内容转换文件失败");
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return new ApiResult(ReturnCode.CODE_CEPH_UPLOAD, "配置内容转换文件失败");
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, configFile, "创建配置成功");
    }

    private SpringCloudConfigFile createConfigFile(String serviceId, String configName, String configContent,
            int enable, String cephFileId) throws FileNotFoundException, IOException {

        // 在该服务下是否有同名配置
        SpringCloudConfigFile cloudConfigFile = null;
        SpringCloudConfigFile configFile = new SpringCloudConfigFile();
        try {
            // 同名且启用的配置
            cloudConfigFile = springCloudConfigFileService
                    .findTopByServiceIdAndConfigNameAndEnableOrderByCreateTimeDesc(serviceId, configName,
                            SpringCloudCommonConst.SERVICE_CONFIG_ENABLE);
            if (null == cloudConfigFile) {
                // 没有启用的，则选择最新的配置
                cloudConfigFile = springCloudConfigFileService
                        .findTopByServiceIdAndConfigNameOrderByCreateTimeDesc(serviceId, configName);
            }
        } catch (Exception e) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED,
                    "查询服务" + serviceId + "的配置文件" + configName + "失败");
        }
        // 初始化配置信息
        configFile.setConfigContent(configContent);
        configFile.setConfigName(configName);
        configFile.setEnable(enable);
        configFile.setServiceId(serviceId);
        configFile.setCreateTime(new Date());
        // 同名配置不存在
        if (null == cloudConfigFile) {
            return springCloudConfigFileService.saveConfig(configFile, null, cephFileId);
        }

        // 配置存在
        if (!configContent.equals(cloudConfigFile.getConfigContent())) {
            // 内容不相同
            springCloudConfigFileService.saveConfig(configFile, cloudConfigFile, cephFileId);
        } else if (cloudConfigFile.getEnable() != enable) {
            // 启用标记不同，且内容相同
            cloudConfigFile.setEnable(enable);
            springCloudConfigFileService.saveConfig(cloudConfigFile, null, cephFileId);
            configFile = cloudConfigFile;
        } else {
            // 没有变化
            configFile = cloudConfigFile;
        }
        return configFile;

    }

    private ApiResult checkConfigFile(String serviceId, String configName, String configContent, int enable) {

        ApiResult result = null;

        if (StringUtils.isEmpty(configName)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "配置文件名称为空");
        }

        if (StringUtils.isEmpty(configContent)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "配置文件内容为空");
        }

        if (0 != enable && 1 != enable) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "是否启用参数不正确");
        }

        result = checkServiceId(serviceId);
        if (ReturnCode.CODE_SUCCESS != result.getCode()) {
            return result;
        }

        return result;
    }

    private ApiResult checkServiceId(String serviceId) {
        if (StringUtils.isEmpty(serviceId)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "服务ID为空");
        }
        SpringCloudService service = null;
        try {
            service = configBusServiceImpl.findById(serviceId);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "根据服务ID查询失败");
        }
        if (null == service) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "服务不存在");
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, service, "服务存在");
    }

    /**
     * 删除单个配置版本
     */
    @ResponseBody
    @RequestMapping(value = { "/{configId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除单个配置版本", notes = "")
    @ApiImplicitParam(paramType = "path", name = "configId", value = "配置ID", required = true, dataType = "String")
    public ApiResult deleteConfigById(@PathVariable("configId") String configId) {

        // 校验租户
        try {
            SpringCloudConfigFile configFile = springCloudConfigFileService.findById(configId);
            Map<String, Integer> confignameList = springCloudConfigFileService
                    .findConfigNameList(configFile.getServiceId());
            Integer configNum = confignameList.get(configFile.getConfigName());
            if (configNum == 1) {
                springCloudConfigFileService.deleteByServiceIdAndConfigName(configFile.getServiceId(),
                        configFile.getConfigName());
            } else if (configNum == 0) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "配置文件" + configFile.getConfigName() + "不存在");
            } else {
                springCloudConfigFileService.delete(configId);
            }
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "删除单个配置失败");
        }
        LOG.info("---------单个配置版本完成----------");
        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除单个配置成功");
    }

    /**
     * 删除配置所有版本
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除配置所有版本版本", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "configName", value = "配置名称", required = true, dataType = "String") })
    public ApiResult deleteConfigByName(@RequestParam(value = "serviceId", required = true) String serviceId,
            @RequestParam(value = "configName", required = true) String configName) {

        // ApiResult result = null;
        // 校验校验服务ID是否存在
        // result = checkServiceId(serviceId);
        // if (ReturnCode.CODE_SUCCESS != result.getCode()) {
        // return result;
        // }
        try {
            springCloudConfigFileService.deleteByServiceIdAndConfigName(serviceId, configName);

        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "删除配置失败");
        }
        LOG.info("---------删除配置完成----------");
        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除配置成功");
    }

    /**
     * 获取配置单个版本
     */
    @ResponseBody
    @RequestMapping(value = { "/{configId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取配置单个版本", notes = "")
    @ApiImplicitParam(paramType = "path", name = "configId", value = "配置ID", required = true, dataType = "String")
    public ApiResult findConfigById(@PathVariable("configId") String configId) {

        SpringCloudConfigFile configinfo = null;
        // 校验租户
        try {
            configinfo = springCloudConfigFileService.findById(configId);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "获取配置单个版本失败");
        }
        LOG.info("---------删除应用完成----------");
        return new ApiResult(ReturnCode.CODE_SUCCESS, configinfo, "获取配置单个版本成功");
    }

    /**
     * 单个配置所有版本
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "单个配置所有版本", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "configName", value = "配置名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult findConfigByServiceIdAndConfigName(
            @RequestParam(value = "serviceId", required = true) String serviceId,
            @RequestParam(value = "configName", required = true) String configName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        Page<SpringCloudConfigFile> configFilePage = null;
        // 校验租户
        try {
            configFilePage = springCloudConfigFileService.findByServiceIdAndConfigName(serviceId, configName, pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "获取配置所有版本失败");
        }
        LOG.info("---------获取单个配置所有版本----------");
        return new ApiResult(ReturnCode.CODE_SUCCESS, configFilePage, "获取配置所有版本成功");
    }

    /**
     * 配置文件列表
     */
    @ResponseBody
    @RequestMapping(value = { "/info" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取服务下所有配置文件列表", notes = "")
    @ApiImplicitParam(paramType = "query", name = "serviceId", value = "服务ID", required = true, dataType = "String")
    public ApiResult findConfigNameList(@RequestParam(value = "serviceId", required = true) String serviceId) {

        // Map<String, Integer> configFileInfo = null;
        Map<String, SpringCloudConfigFile> configFileContent = null;
        // 校验租户
        try {
            // configFileInfo =
            // springCloudConfigFileService.findConfigNameList(serviceId);
            configFileContent = springCloudConfigFileService.findConfigContentList(serviceId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(ReturnCode.CODE_SQL_DELETE_FAILED, "配置文件列表失败");
        }
        LOG.info("---------获取单个配置所有版本----------");
        return new ApiResult(ReturnCode.CODE_SUCCESS, configFileContent, "配置文件列表成功");
    }
}
