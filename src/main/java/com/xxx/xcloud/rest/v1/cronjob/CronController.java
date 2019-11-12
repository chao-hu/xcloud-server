package com.xxx.xcloud.rest.v1.cronjob;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
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
    public ApiResult createCron(@Valid @RequestBody CronjobDTO dto) {

        Cronjob cronjob = new Cronjob();
        try {
            cronjob = cronjobService.createCronjob(dto.getCronJob());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, cronjob, "定时任务创建成功");
    }


    /**
     * 启动、停止、修改任务
     */
    @ResponseBody
    @RequestMapping(value = { "/{cronId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = " 启动、停止、修改定时任务", notes = "")
    @ApiImplicitParam(paramType = "path", name = "cronId", value = "任务ID", required = true, dataType = "String")
    public ApiResult operatorCron(@PathVariable("cronId") String cronId, @RequestBody CronOperatorDTO dto) {

        String operator = dto.getOperation();
        Cronjob cronJob = cronjobService.getCronjobById(cronId);;
        
        boolean flag = false;
        try {
            switch (operator) {
            case "stop":
                flag = cronjobService.stopCronjob(cronId);
                break;
                
            case "start":
                flag = cronjobService.startCronjob(cronId);
                break;

            case "modify":
                cronJob = cronjobService.updateCronjob(cronJob);
                break;

            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, operator + " 操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }
        
        // 启动或停止操作
        if (!Global.CRON_MODIFY.equals(operator) && !flag) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, " 操作失败");
        }
        // 更新操作
        if (Global.CRON_MODIFY.equals(operator) && null == cronJob) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, " 操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "操作定时任务成功");
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
