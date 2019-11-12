package com.xxx.xcloud.rest.v1.cronjob;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.cronjob.entity.Cronjob;
import com.xxx.xcloud.module.cronjob.service.CronjobService;
import com.xxx.xcloud.rest.v1.cronjob.dto.CronOperatorDTO;
import com.xxx.xcloud.rest.v1.cronjob.dto.CronjobDTO;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * <p>
 * Description: 定时任务控制器
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月29日
 */
@Controller
@RequestMapping("/v1/cron")
@Validated
public class CronController {

    @Autowired
    private CronjobService cronjobService;

    /**
     * 创建定时任务
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "创建定时任务", notes = "")
    public ApiResult createCron(@Valid @RequestBody CronjobDTO json, BindingResult result) {

        String tenantName = json.getTenantName();
        String name = json.getName();
        String imageVersionId = json.getImageVersionId();
        Double cpu = json.getCpu();
        Double memory = json.getMemory();
        String cmd = json.getCmd();
        String scheduleCh = json.getScheduleCh();
        String schedule = json.getSchedule();

        // 包装定时任务
        Cronjob cronjob = generateCron(tenantName, name, imageVersionId, cpu, memory, schedule, cmd, scheduleCh,
                json.getCreatedBy(), json.getProjectId());

        try {
            cronjobService.createCronjob(cronjob);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, cronjob, "定时任务创建成功");
    }

    private Cronjob generateCron(String tenantName, String name, String imageVersionId, Double cpu, Double memory,
            String schedule, String cmd, String scheduleCh, String createdBy, String projectId) {
        Cronjob cronjob = new Cronjob();
        cronjob.setCmd(cmd);
        cronjob.setCpu(cpu);
        cronjob.setCreateTime(new Date());
        cronjob.setImageVerisonId(imageVersionId);
        cronjob.setMemory(memory);
        cronjob.setName(name);
        cronjob.setSchedule(schedule);
        cronjob.setScheduleCh(scheduleCh);
        cronjob.setTenantName(tenantName);
        cronjob.setCreateTime(new Date());
        cronjob.setStatus(Global.OPERATION_UNSTART);
        cronjob.setCreatedBy(createdBy);
        cronjob.setProjectId(projectId);

        return cronjob;
    }


    /**
     * 启动、停止、修改任务
     */
    @ResponseBody
    @RequestMapping(value = { "/{cronId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = " 启动、停止、修改定时任务", notes = "")
    @ApiImplicitParam(paramType = "path", name = "cronId", value = "任务ID", required = true, dataType = "String")
    public ApiResult operatorCron(@PathVariable("cronId") String cronId, @RequestBody CronOperatorDTO json, BindingResult result) {

        ApiResult apiResult = new ApiResult();
        Double cpu = json.getCpu();
        Double memory = json.getMemory();
        String cmd = json.getCmd();
        String schedule = json.getSchedule();
        String operator = json.getOperation();

        Cronjob cronJob = null;
        // 校验
        apiResult = checkUpdateParam(cpu, memory, schedule, operator);
        if (null != apiResult) {
            return apiResult;
        }
        boolean flag = false;
        try {
            // 执行操作
            switch (json.getOperation()) {
            case "stop":
                flag = cronjobService.stopCronjob(cronId);
                break;
            case "start":
                flag = cronjobService.startCronjob(cronId);
                break;

            case "modify":
                Cronjob cronjob = generateUpdateCron(cronId, cpu, memory, schedule, cmd);
                if (null == cronjob) {
                    return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "根据cronjobId: " + cronId + " 查询信息失败");
                }
                cronJob = cronjobService.updateCronjob(cronjob);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, json.getOperation() + " 操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        if (!Global.CRON_MODIFY.equals(operator) && !flag) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, " 操作失败");
        }

        if (Global.CRON_MODIFY.equals(operator) && null == cronJob) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, " 操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作定时任务成功");
    }

    private Cronjob generateUpdateCron(String cronId, Double cpu, Double memory, String schedule, String cmd) {

        Cronjob cronjob = null;

        try {
            cronjob = cronjobService.getCronjobById(cronId);
        } catch (Exception e) {
            return null;
        }
        if (null != cronjob) {
            cronjob.setCmd(cmd);
            cronjob.setCpu(cpu);
            cronjob.setMemory(memory);
            cronjob.setSchedule(schedule);
            cronjob.setUpdateTime(new Date());
        }

        return cronjob;
    }

    private ApiResult checkUpdateParam(Double cpu, Double memory, String scheduleCh, String operator) {
        if (!Global.getCronTypeCodes().contains(operator)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, operator + " 操作不存在");
        }

        if (Global.CRON_MODIFY.equals(operator)) {

            if (StringUtils.isEmpty(scheduleCh)) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "任务时间不能为空");
            }

            if (Double.doubleToLongBits(memory) < 0) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "内存应该大于0");
            }

            if (Double.doubleToLongBits(cpu) < 0) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "CPU应该大于0");
            }
        }

        return null;
    }

    /**
     * 删除任务
     */
    @ResponseBody
    @RequestMapping(value = { "/{cronId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除定时任务", notes = "")
    @ApiImplicitParam(paramType = "path", name = "cronId", value = "任务ID", required = true, dataType = "String")
    public ApiResult deleteCron(@PathVariable("cronId") String cronId) {

        try {
            cronjobService.deleteCronjob(cronId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除任务成功");
    }

    /**
     * 定时任务page查询
     */
    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "定时任务列表查询", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "cronName", value = "定时任务名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页条数", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "当前页码", required = false, dataType = "int"), })
    public ApiResult findCronList(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "cronName", required = false) String cronName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        
        Page<Cronjob> cronJobList = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        try {
            cronJobList = cronjobService.getCronjobList(tenantName, cronName, projectId, pageable);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, cronJobList, "定时任务列表查询成功");
    }

    /**
     * 详情查询
     */
    @ResponseBody
    @RequestMapping(value = { "/{cronId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "定时任务详情查询", notes = "")
    @ApiImplicitParam(paramType = "path", name = "cronId", value = "定时任务ID", required = false, dataType = "String")
    public ApiResult findOneCronById(@PathVariable("cronId") String cronId) {

        Cronjob cronJob = null;
        try {
            cronJob = cronjobService.getCronjobById(cronId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        return new ApiResult(ReturnCode.CODE_SUCCESS, cronJob, "定时任务详情查询成功");
    }

}
