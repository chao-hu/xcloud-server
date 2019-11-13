package com.xxx.xcloud.rest.v1.ceph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ceph.entity.CephFile;
import com.xxx.xcloud.module.ceph.entity.CephRbd;
import com.xxx.xcloud.module.ceph.entity.CephSnap;
import com.xxx.xcloud.module.ceph.entity.SnapStrategy;
import com.xxx.xcloud.module.ceph.pojo.CephBucket;
import com.xxx.xcloud.module.ceph.pojo.FileInfo;
import com.xxx.xcloud.module.ceph.pojo.ObjectInBucket;
import com.xxx.xcloud.module.ceph.service.CephFileService;
import com.xxx.xcloud.module.ceph.service.CephObjectService;
import com.xxx.xcloud.module.ceph.service.CephRbdService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.ITenantService;
import com.xxx.xcloud.rest.v1.ceph.dto.CephFileAddDirDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephFileDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephFileFormatDirDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephObjDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephRbdDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephRbdExpandDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephRbdSnapDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephRbdSnapRollbackDTO;
import com.xxx.xcloud.rest.v1.ceph.dto.CephRbdSnapshotDTO;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.utils.StringUtils;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * <p>
 * Description: ceph存储控制器
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
@Controller
@RequestMapping("/v1/storage")
@Validated
public class CephController {

    public static final String OPERATION_FORMAT = "format";
    public static final String OPERATION_EXPAND = "expand";
    public static final String OPERATION_ROLLBACK = "rollback";
    public static final String BUCKET_NAME_SPLIT = "bucket";
    public static Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,15}$");

    @Autowired
    private CephFileService cephFileService;

    @Autowired
    private CephRbdService cephRbdService;

    @Autowired
    private CephObjectService cephObjectService;

    @Autowired
    @Qualifier("tenantServiceImpl")
    private ITenantService tenantService;

    /**
     * 新建文件存储卷
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/file" }, method = RequestMethod.POST)
    @ApiOperation(value = "新建文件存储卷", notes = "")
    public ApiResult createCephFs(@Valid @RequestBody CephFileDTO json) {

        // 解析参数
        String tenantName = json.getTenantName();
        String storageFileName = json.getStorageFileName();
        Double storageFileSize = json.getStorageFileSize();
        String description = json.getDescription();
        String projectId = json.getProjectId();
        String createdBy = json.getCreatedBy();

        // 创建文件存储卷
        CephFile cephFile = new CephFile();
        try {
            cephFile = cephFileService.add(tenantName, createdBy, projectId, storageFileName, storageFileSize, description);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, cephFile, "创建文件存储卷成功");
    }

    /**
     * 获取文件存储列表
     */
    @ResponseBody
    @RequestMapping(value = { "/file" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取文件存储列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "storageFileName", value = "存储卷名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "页大小", required = false, defaultValue = "2000", dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "项目ID", required = false, defaultValue = "0", dataType = "int") })
    public ApiResult findCephFileList(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "storageFileName", required = false) String storageFileName,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page) {

        Page<CephFile> cephFileList = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        try {
            cephFileList = cephFileService.list(tenantName, storageFileName, projectId, pageable);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_CEPH_NOT_FOUND, e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, cephFileList, "获取文件存储列表成功");
    }

    /**
     * 获取文件存储卷详情
     */
    @ResponseBody
    @RequestMapping(value = { "/file/{storageFileId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取文件存储卷详情", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageFileId", value = "文件存储卷ID", required = true, dataType = "String")
    public ApiResult findCephFileInfo(@PathVariable("storageFileId") String storageFileId) {

        CephFile cephFile = null;

        try {
            cephFile = cephFileService.get(storageFileId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, cephFile, "获取文件存储卷详情成功");
    }

    /**
     * 文件存储卷内增加文件夹
     */
    @ResponseBody
    @RequestMapping(value = { "/file/{storageFileId}/directory" }, method = RequestMethod.POST)
    @ApiOperation(value = "文件存储卷内增加文件夹", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageFileId", value = "文件存储卷ID", required = true, dataType = "String")
    public ApiResult addCephFileDir(@PathVariable("storageFileId") String storageFileId,
            @Valid @RequestBody CephFileAddDirDTO json) {

        String folderName = json.getDirectoryName();
        String path = json.getDirectoryPath();

        try {
            cephFileService.addFolder(storageFileId, folderName, path);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "文件存储卷内增加文件夹成功");
    }

    /**
     * 文件存储卷内上传、下载文件
     */
    @ResponseBody
    @RequestMapping(value = {
            "/file/{storageFileId}/file" }, headers = "content-type=multipart/form-data", method = RequestMethod.POST)
    @ApiOperation(value = "文件存储卷内上传、下载文件", notes = "")
    public ApiResult cephFileUpOrDownloadFile(@PathVariable("storageFileId") String storageFileId,
            @RequestParam(value = "operation", required = true) @ApiParam(value = "操作类型,上传:upload,下载:download", required = true) String operation,
            @RequestParam(value = "filePath", required = false) @ApiParam(value = "文件相对目录(上传必填)", required = false) String filePath,
            @RequestParam(value = "fileName", required = false) @ApiParam(value = "文件名(下载必填)", required = false) String fileName,
            @ApiParam(value = "文件(上传必填)", required = false) MultipartFile file, HttpServletRequest request,
            HttpServletResponse response) {

        ApiResult result = null;

        try {
            switch (operation) {
            case "upload":
                cephFileService.upLoadFile(storageFileId, filePath, file);
                result = new ApiResult(ReturnCode.CODE_SUCCESS, "文件存储卷内上传文件成功");
                break;
            case "download":
                cephFileService.downLoadFile(storageFileId, fileName, request, response);
                result = new ApiResult(ReturnCode.CODE_SUCCESS, "文件存储卷内下载文件成功");
                break;
            default:
                result = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "操作不存在");
                break;
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return result;
    }

    /**
     * 文件存储卷内删除文件或文件夹
     */
    @ResponseBody
    @RequestMapping(value = { "/file/{storageFileId}/file" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "文件存储卷内删除文件或文件夹", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageFileId", value = "文件存储卷ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "fileName", value = "文件名称", required = true, dataType = "String") })
    public ApiResult cephFileDelete(@PathVariable("storageFileId") String storageFileId,
            @RequestParam(value = "fileName", required = true) @ApiParam(value = "文件名称", required = true) String fileName) {

        try {
            cephFileService.removeFile(storageFileId, fileName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "文件存储卷内删除文件或文件夹成功");
    }

    /**
     * 格式化存储卷
     */
    @ResponseBody
    @RequestMapping(value = { "/file/{storageFileId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "格式化存储卷", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageFileId", value = "文件存储卷ID", required = true, dataType = "String") })
    public ApiResult cephFileFormat(@PathVariable("storageFileId") String storageFileId,
            @Valid @RequestBody CephFileFormatDirDTO json) {

        try {
            cephFileService.clear(storageFileId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "格式化存储卷成功");
    }

    /**
     * 删除存储卷
     */
    @ResponseBody
    @RequestMapping(value = { "/file/{storageFileId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除存储卷", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageFileId", value = "文件存储卷ID", required = true, dataType = "String")
    public ApiResult cephFileDelete(@PathVariable("storageFileId") String storageFileId) {

        try {
            cephFileService.delete(storageFileId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除存储卷成功");
    }

    /**
     * 获取存储卷指定目录下文件信息列表
     */
    @ResponseBody
    @RequestMapping(value = { "/file/{storageFileId}/files" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取存储卷指定目录下文件信息列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageFileId", value = "文件存储卷ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "path", value = "路径", required = false, dataType = "String") })
    public ApiResult getCephFile(@PathVariable("storageFileId") String storageFileId,
            @RequestParam(value = "path", required = false) String path) {
        List<FileInfo> fileList = null;
        try {
            fileList = cephFileService.listFiles(storageFileId, path);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, fileList, "获取存储卷指定目录下文件信息成功");
    }

    /**
     * 新建块储卷
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd" }, method = RequestMethod.POST)
    @ApiOperation(value = "新建块存储", notes = "")
    public ApiResult createRdb(@Valid @RequestBody CephRbdDTO json) {

        String rbdName = json.getRbdName();
        String description = json.getDescription();
        String tenantName = json.getTenantName();
        Double size = json.getSize();
        String projectId = json.getProjectId();
        String createdBy = json.getCreatedBy();

        try {
            cephRbdService.add(tenantName, createdBy, projectId, rbdName, size, description);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "新建块存储成功");
    }

    /**
     * 块储卷列表(模糊查询)
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd" }, method = RequestMethod.GET)
    @ApiOperation(value = "块存储列表", notes = "")
    public ApiResult findRdbList(
            @RequestParam(value = "tenantName", required = true) @ApiParam(value = "租户名称", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) @ApiParam(value = "项目ID", required = false) String projectId,
            @RequestParam(value = "storageRbdName", required = false) @ApiParam(value = "文件存储名称", required = false) String storageRbdName,
            @RequestParam(value = "size", required = false, defaultValue = "2000") @ApiParam(value = "页大小", required = false) int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") @ApiParam(value = "页", required = false) int page) {

        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        Page<CephRbd> rbdList = null;
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createTime");
        try {
            rbdList = cephRbdService.list(tenantName, storageRbdName, projectId, pageable);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, rbdList, "获取块存储列表成功");

    }

    /**
     * 块储卷详情
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "块存储详情", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult findRdbInfo(@PathVariable("storageRbdId") String storageRbdId) {

        CephRbd cephRbd = null;
        try {
            cephRbd = cephRbdService.get(storageRbdId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, cephRbd, "获取块存储详情成功");
    }

    /**
     * 块存储扩容
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "块存储扩容", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult expandRdb(@PathVariable("storageRbdId") String storageRbdId,
            @Valid @RequestBody CephRbdExpandDTO json) {

        try {
            cephRbdService.resize(storageRbdId, json.getSize());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "块存储扩容成功");
    }

    /**
     * 删除块存储
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除块存储", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult deleteRdb(@PathVariable("storageRbdId") String storageRbdId) {

        try {
            cephRbdService.delete(storageRbdId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除块存储成功");
    }

    /**
     * 获取快照列表
     *
     * @Description
     * @param storageRbdId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot/list" }, method = RequestMethod.GET)
    @ApiOperation(value = "查看快照列表", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult snapList(@PathVariable("storageRbdId") String storageRbdId) {
        List<CephSnap> snapList = null;
        snapList = cephRbdService.snapList(storageRbdId);

        return new ApiResult(ReturnCode.CODE_SUCCESS, snapList, "快照列表获取成功");
    }

    /**
     * 获取快照策略
     *
     * @Description
     * @param storageRbdId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot/plan" }, method = RequestMethod.GET)
    @ApiOperation(value = "查看快照策略", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult getSnapStrategy(@PathVariable("storageRbdId") String storageRbdId) {
        SnapStrategy snapStrategy = cephRbdService.getSnapStrategy(storageRbdId);
        return new ApiResult(ReturnCode.CODE_SUCCESS, snapStrategy, "查看快照策略成功");
    }

    /**
     * 新增快照策略
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot/plan" }, method = RequestMethod.POST)
    @ApiOperation(value = "新增快照策略", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult createRdbSnapshotPlan(@PathVariable("storageRbdId") String storageRbdId,
            @Valid @RequestBody CephRbdSnapshotDTO json) {

        try {
            cephRbdService.addSnapStrategy(storageRbdId, json.getWeek(), json.getTime(), json.getEndDate(),
                    json.getStatus());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "新增快照策略成功");
    }

    /**
     * 修改快照策略
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot/plan" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改快照策略", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult updateRdbSnapshotPlan(@PathVariable("storageRbdId") String storageRbdId,
            @Valid @RequestBody CephRbdSnapshotDTO json) {

        ApiResult apiResult = null;

        apiResult = checkRbdUpdateParam(json.getWeek(), json.getTime(), json.getEndDate(), json.getStatus());
        if (null != apiResult) {
            return apiResult;
        }

        try {
            cephRbdService.updateSnapStrategy(storageRbdId, json.getWeek(), json.getTime(), json.getEndDate(),
                    json.getStatus());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "修改快照策略成功");
    }

    private ApiResult checkRbdUpdateParam(String week, String time, Date endDate, int status) {
        // check week
        if (!StringUtils.isWeekStrings(week)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "星期不符合规范");
        }

        // check time
        if (!StringUtils.isTimeStrings(time)) {

            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "时间不符合规范");
        }

        // check endDate
        if (endDate.compareTo(new Date()) < 0) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "结束日期不符合规范");
        }

        // check status
        if (status != SnapStrategy.STATUS_RUNNING && status != SnapStrategy.STATUS_STOP) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "状态不符合规范");
        }
        return null;
    }

    /**
     * 删除快照策略
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot/plan" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除快照策略", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult deleteRdbSnapshotPlan(@PathVariable("storageRbdId") String storageRbdId) {

        try {
            cephRbdService.deleteSnapStrategy(storageRbdId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除快照策略成功");
    }

    /**
     * 块存储手动拍快照
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot" }, method = RequestMethod.POST)
    @ApiOperation(value = "块存储手动拍快照", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult rdbSnapshot(@PathVariable("storageRbdId") String storageRbdId,
            @Valid @RequestBody CephRbdSnapDTO json) {

        try {
            cephRbdService.createSnap(storageRbdId, json.getSnapName(), json.getSnapDescription());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "块存储手动拍快照成功");
    }

    /**
     * 块存储快照删除
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbd/{storageRbdId}/snapshot/{snapshotId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "块存储快照删除", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "snapshotId", value = "快照ID", required = true, dataType = "String") })
    public ApiResult deleteRdbSnapshot(@PathVariable("storageRbdId") String storageRbdId,
            @PathVariable("snapshotId") String snapshotId) {

        try {
            cephRbdService.deleteSnap(storageRbdId, snapshotId);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "块存储快照删除成功");
    }

    /**
     * 块存储快照回滚
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/rbdrollback/{storageRbdId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "块存储快照回滚", notes = "")
    @ApiImplicitParam(paramType = "path", name = "storageRbdId", value = "块存储ID", required = true, dataType = "String")
    public ApiResult rollbackRdbSnapshot(@PathVariable("storageRbdId") String storageRbdId,
            @Valid @RequestBody CephRbdSnapRollbackDTO json) {

        try {
            cephRbdService.snapRollBack(storageRbdId, json.getSnapshotId());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "块存储快照回滚成功");
    }

    /**
     * 获取对象存储终端地址
     *
     * @Description
     * @return
     */
    @ResponseBody
    @RequestMapping(value = { "/bucketEndpoint" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取对象存储终端地址", notes = "")
    public ApiResult getEndpoint() {
        return new ApiResult(ReturnCode.CODE_SUCCESS, XcloudProperties.getConfigMap().get(Global.CEPH_RGW_ENDPOINT),
                "获取成功");
    }

    /**
     * 新建桶 桶名称形式：{projectId}${BUCKET_NAME_SPLIT}{bucketName}
     */
    @ResponseBody
    @RequestMapping(value = { "/bucket" }, method = RequestMethod.POST)
    @ApiOperation(value = "新建桶", notes = "")
    public ApiResult createBucket(@Valid @RequestBody CephObjDTO json) {

        if (!ACCOUNT_PATTERN.matcher(json.getBucketName()).matches()) {
            return new ApiResult(ReturnCode.CODE_CEPH_CREATE, "桶名称不合法，应由5到15位小写字母、数字和下划线组成，以字母开头");
        }

        try {
            String bucketName = json.getProjectId() + BUCKET_NAME_SPLIT + json.getBucketName();
            cephObjectService.createBucket(json.getTenantName(), bucketName, json.getAccessControlList());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "新建桶成功");
    }

    /**
     * 桶列表
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/bucket" }, method = RequestMethod.GET)
    @ApiOperation(value = "桶列表", notes = "")
    public ApiResult findBucketList(
            @RequestParam(value = "tenantName", required = true) @ApiParam(value = "租户名称", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) @ApiParam(value = "项目ID", required = false) String projectId,
            @RequestParam(value = "bucketName", required = false) @ApiParam(value = "桶名称", required = false) String bucketName,
            @RequestParam(value = "size", required = false, defaultValue = "2000") @ApiParam(value = "页大小", required = false) int size,
            @RequestParam(value = "page", required = false, defaultValue = "0") @ApiParam(value = "页", required = false) int page) {

        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        List<Bucket> buckets = null;
        List<CephBucket> retBuckets = null;
        Page<CephBucket> bPages = null;

        try {
            buckets = cephObjectService.listBuckets(tenantName);
            retBuckets = new ArrayList<CephBucket>();

            for (Bucket bucket : buckets) {
                String bName = bucket.getName();
                int index = bName.indexOf(BUCKET_NAME_SPLIT);
                if (index < 0) {
                    continue;
                }
                String pre = bName.substring(0, index);
                String aft = bName.substring(index + 6, bName.length());
                boolean newFlag = (StringUtils.isEmpty(projectId) || (!StringUtils.isEmpty(projectId) && pre.equals(projectId)))
                && aft.contains(bucketName == null ? "" : bucketName);
                if (newFlag) {
                    CephBucket newBucket = new CephBucket();
                    newBucket.setName(aft);
                    newBucket.setOwner(bucket.getOwner());
                    newBucket.setProjectId(pre);
                    newBucket.setCreateTime(bucket.getCreationDate());
                    newBucket.setCreationDate(bucket.getCreationDate());
                    retBuckets.add(newBucket);
                }
            }
            bPages = new PageImpl<CephBucket>(retBuckets, PageUtil.getPageable(page, size), retBuckets.size());
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, bPages, "桶列表成功");
    }

    /**
     * 删除桶
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/bucket/{storageBucketName}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除桶", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageBucketName", value = "对象存储名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = true, dataType = "String") })
    public ApiResult deleteBucket(@PathVariable("storageBucketName") String storageBucketName,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = true) String projectId) {

        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        try {
            String bucketName = projectId + BUCKET_NAME_SPLIT + storageBucketName;
            cephObjectService.deleteBucket(tenantName, bucketName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除桶成功");
    }

    /**
     * 查看桶内对象
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/obj/{storageBucketName}" }, method = RequestMethod.GET)
    @ApiOperation(value = "查看桶内对象", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageBucketName", value = "对象存储名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = true, dataType = "String") })
    public ApiResult findObjs(@PathVariable("storageBucketName") String storageBucketName,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = true) String projectId) {

        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        List<S3ObjectSummary> s3ObjectSummaries = null;
        List<ObjectInBucket> objectInBuckets = new ArrayList<ObjectInBucket>();

        try {
            String bucketName = projectId + BUCKET_NAME_SPLIT + storageBucketName;
            s3ObjectSummaries = cephObjectService.listObjects(tenantName, bucketName);

            for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
                objectInBuckets.add(new ObjectInBucket(s3ObjectSummary));
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, objectInBuckets, "查看桶内对象成功");
    }

    /**
     * 删除桶内文件
     *
     */
    @ResponseBody
    @RequestMapping(value = { "/obj/{storageBucketName}/file" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除桶内文件", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageBucketName", value = "对象存储名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "fileName", value = "文件名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = true, dataType = "String") })
    public ApiResult deleteObj(@PathVariable("storageBucketName") String storageBucketName,
            @RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "fileName", required = true) String fileName,
            @RequestParam(value = "projectId", required = true) String projectId) {

        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        try {
            String bucketName = projectId + BUCKET_NAME_SPLIT + storageBucketName;
            cephObjectService.deleteObject(tenantName, bucketName, fileName);
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "删除桶内文件成功");
    }

    /**
     * 桶内上传文件、桶内下载文件
     *
     */
    @ResponseBody
    @RequestMapping(value = {
            "/obj/{storageBucketName}/file" }, headers = "content-type=multipart/form-data", method = RequestMethod.POST)
    @ApiOperation(value = "桶内上传文件、桶内下载文件", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageBucketName", value = "对象存储名称", required = true, dataType = "String") })
    public ApiResult upOrDownloadObjFile(@PathVariable("storageBucketName") String storageBucketName,
            @RequestParam(value = "tenantName", required = true) @ApiParam(value = "租户名称", required = true) String tenantName,
            @RequestParam(value = "projectId", required = true) @ApiParam(value = "项目ID", required = true) String projectId,
            @RequestParam(value = "fileName", required = false) @ApiParam(value = "文件名称(下载时必填)", required = false) String fileName,
            @RequestParam(value = "operation", required = true) @ApiParam(value = "操作(上传填写：upload,下载填写：download)", required = true) String operation,
            @ApiParam(value = "文件(上传时必填)", required = false) MultipartFile file,
            @RequestParam(value = "storageClass", required = false) @ApiParam(value = "存储类型", required = false) String storageClass,
            @RequestParam(value = "acl", required = false) @ApiParam(value = "访问权限", required = false) String acl,
            HttpServletRequest request, HttpServletResponse response) {

        ApiResult result = null;

        result = checkTenantName(tenantName);
        if (null != result) {
            return result;
        }

        try {
            String bucketName = projectId + BUCKET_NAME_SPLIT + storageBucketName;
            switch (operation) {
            case "upload":
                cephObjectService.upLoad(file, tenantName, acl, storageClass, bucketName);
                break;
            case "download":
                cephObjectService.downLoad(tenantName, bucketName, fileName, response);
                break;
            default:
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "操作不存在");
            }
        } catch (ErrorMessageException e) {
            return new ApiResult(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库操作失败");
        }

        return new ApiResult(ReturnCode.CODE_SUCCESS, "桶内文件操作成功");
    }

    private ApiResult checkTenantName(String tenantName) {

        if (StringUtils.isEmpty(tenantName) || !tenantName.matches(Global.CHECK_TENANT_NAME)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "租户名称规则不符合规范");
        }

        Tenant tenant = null;
        try {
            tenant = tenantService.findTenantByTenantName(tenantName);
        } catch (Exception e) {
            return new ApiResult(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询tenantName: " + tenantName + " 失败");
        }
        if (null == tenant) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户tenantName: " + tenantName + " 不存在");
        }
        return null;
    }

}
