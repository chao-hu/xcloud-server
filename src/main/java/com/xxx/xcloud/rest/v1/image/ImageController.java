package com.xxx.xcloud.rest.v1.image;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.entity.Ci;
import com.xxx.xcloud.module.ci.service.ICiService;
import com.xxx.xcloud.module.image.consts.ImageConstant;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import com.xxx.xcloud.module.image.model.ImageDetail;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.tenant.entity.Tenant;
import com.xxx.xcloud.module.tenant.service.impl.TenantServiceImplV1;
import com.xxx.xcloud.rest.v1.image.model.UpdateImageDescriptionDTO;
import com.xxx.xcloud.rest.v1.image.model.UpdateImageTypeDTO;
import com.xxx.xcloud.rest.v1.image.model.UploadImageDTO;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.utils.StringUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author xjp
 * @ClassName: ImageController
 * @Description: 镜像管理
 * @date 2019年10月24日
 */
@Controller
@RequestMapping("/v1/image")
public class ImageController {

    private static final Logger LOG = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ImageService imageServiceImpl;

    @Autowired
    private ICiService ciServiceImpl;

    @Autowired
    private TenantServiceImplV1 tenantService;

    // TODO: 2019/11/12  引用应用服务
    //    @Autowired
    //    private Application application;

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取镜像列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "imageName", value = "镜像名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getImageGroup(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "imageName", required = false) String imageName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult;

        // 参数解析
        // 不需要筛选
        if (StringUtils.isEmpty(imageName)) {
            imageName = null;
        }

        apiResult = checkTenantName(tenantName);
        if (null != apiResult) {
            return apiResult;
        }

        // 调用service方法
        try {
            Page<Image> groupImages = imageServiceImpl.getImages(tenantName, imageName, projectId,
                    PageUtil.getPageable(page, size, ImageConstant.SORT_DEFAULT));

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, groupImages, "获取镜像列表成功！");

        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{imageId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据镜像ID, 获取镜像信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "imageId", value = "镜像名称", required = true, dataType = "String") })
    public ApiResult getImageByImageId(@PathVariable("imageId") String imageId) {

        ApiResult apiResult = null;

        // 租户名称校验
        if (StringUtils.isEmpty(imageId)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像ID不能为空！");
        }
        if (null != apiResult) {
            return apiResult;
        }

        // 调用service方法
        try {
            Image image = imageServiceImpl.getImageById(imageId);
            // 公有镜像，设置公有版本数量
            if (image != null && Objects.equals(image.getImageType(), ImageConstant.IMAGE_TYPE_PUBLIC)) {
                image.setVersionNumPublic(imageServiceImpl.getPublicImageVersionNumByImageId(imageId).intValue());
            }
            LOG.info("----------------------" + JSON.toJSONString(image));
            if (null == image) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像不存在！");
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, image, "获取镜像详情成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/imageVersion/{imageId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据镜像ID, 获取镜像版本信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "imageId", value = "镜像ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getImageVersionsByImageId(@PathVariable("imageId") String imageId,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult = null;

        // 租户名称校验
        if (StringUtils.isEmpty(imageId)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像ID不能为空！");
        }
        if (null != apiResult) {
            return apiResult;
        }

        // 调用service方法
        try {
            Image image = imageServiceImpl.getImageById(imageId);
            if (image == null) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像信息不存在！");
            }
            Page<ImageVersion> imageVersions = imageServiceImpl
                    .getImageVersionsByImageId(imageId, PageUtil.getPageable(page, size, ImageConstant.SORT_DEFAULT));
            if (null == imageVersions || imageVersions.isEmpty()) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像不存在！");
            }
            List<ImageVersion> imageVersionList = imageVersions.getContent();
            for (ImageVersion imageVersion : imageVersionList) {
                imageVersion.setRegistryImageName(imageServiceImpl.getRegistryImageName(image, imageVersion));
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, imageVersions, "获取镜像版本信息成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/public" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取公共镜像列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "imageName", value = "镜像名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getPublicImages(@RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "imageName", required = false) String imageName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult;

        // 参数解析

        // 不需要筛选
        if (StringUtils.isEmpty(imageName)) {
            imageName = null;
        }

        // 调用service方法
        try {
            Page<Image> images = imageServiceImpl.getPublicImages(imageName, projectId,
                    PageUtil.getPageable(page, size, ImageConstant.SORT_DEFAULT));
            if (!images.isEmpty()) {
                List<Image> imageList = images.getContent();
                for (Image image : imageList) {
                    image.setVersionNumPublic(
                            imageServiceImpl.getPublicImageVersionNumByImageId(image.getId()).intValue());
                }
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, images, "获取镜像列表成功！");

        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/ownAndPublic" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取自己的和公共镜像列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "imageName", value = "镜像名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getOwnAndPublicImages(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "imageName", required = false) String imageName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult;

        // 参数解析

        // 不需要筛选
        if (StringUtils.isEmpty(imageName)) {
            imageName = null;
        }

        // 调用service方法
        try {
            Page<Image> images = imageServiceImpl.getOwnAndPublicImages(tenantName, imageName, projectId,
                    PageUtil.getPageable(page, size, ImageConstant.SORT_DEFAULT));

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, images, "获取镜像列表成功！");

        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/public/imageVersion/{imageId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据镜像ID, 获取公共镜像版本信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "imageId", value = "镜像ID", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "projectId", value = "项目ID", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页数", required = false, dataType = "int", defaultValue = "0"),
            @ApiImplicitParam(paramType = "query", name = "size", value = "每页数量", required = false, dataType = "int", defaultValue = "2000") })
    public ApiResult getPublicImageVersionsByImageId(@PathVariable("imageId") String imageId,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "2000") int size) {

        ApiResult apiResult = null;

        // 租户名称校验
        if (StringUtils.isEmpty(imageId)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像ID不能为空！");
        }
        if (null != apiResult) {
            return apiResult;
        }

        // 调用service方法
        try {
            Image image = imageServiceImpl.getImageById(imageId);
            if (image == null) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像信息不存在！");
            }
            Page<ImageVersion> imageVersions = imageServiceImpl.getPublicImageVersionsByImageId(imageId,
                    PageUtil.getPageable(page, size, ImageConstant.SORT_DEFAULT));
            LOG.info("----------------------" + JSON.toJSONString(imageVersions));
            if (null == imageVersions || imageVersions.isEmpty()) {
                return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像不存在！");
            }
            List<ImageVersion> imageVersionList = imageVersions.getContent();
            for (ImageVersion imageVersion : imageVersionList) {
                imageVersion.setRegistryImageName(imageServiceImpl.getRegistryImageName(image, imageVersion));
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, imageVersions, "获取镜像版本信息成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/public/imageVersionNum/{imageId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据镜像ID, 获取公共镜像版本数量信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "imageId", value = "镜像ID", required = true, dataType = "String") })
    public ApiResult getPublicImageVersionNumByImageId(@PathVariable("imageId") String imageId) {

        ApiResult apiResult = null;

        // 租户名称校验
        if (StringUtils.isEmpty(imageId)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像ID不能为空！");
        }
        if (null != apiResult) {
            return apiResult;
        }

        // 调用service方法
        try {
            Long num = imageServiceImpl.getPublicImageVersionNumByImageId(imageId);
            LOG.info("----------------------" + JSON.toJSONString(num));
            if (null == num) {
                num = 0L;
            }
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, num, "获取镜像版本数量信息成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    private ApiResult checkGetImage(String imageName, String tenantName) {
        ApiResult apiResult;
        apiResult = checkTenantName(tenantName);
        if (null != apiResult) {
            return apiResult;
        }

        if (StringUtils.isEmpty(imageName)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名称不能为空！");
        }
        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/imageName" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取镜像，判断是否已经存在", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "imageName", value = "镜像名称", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "imageVersion", value = "镜像版本", required = true, dataType = "String"), })
    public ApiResult getImageByName(@RequestParam(value = "tenantName", required = true) String tenantName,
            @RequestParam(value = "imageName", required = true) String imageName,
            @RequestParam(value = "imageVersion", required = true) String imageVersion) {

        ApiResult apiResult;

        // 解析参数
        apiResult = checkGetImageByName(imageName, tenantName, imageVersion);
        if (null != apiResult) {
            return apiResult;
        }
        // 调用service方法
        boolean isExist = false;
        String message = "不存在当前镜像！";

        try {

            boolean imageExist = imageServiceImpl.isImageExist(tenantName, imageName, imageVersion);

            if (!imageExist) {
                Ci ci = ciServiceImpl.getCiByImageNameAndVersion(tenantName, imageName, imageVersion);
                if (null != ci) {
                    isExist = true;
                    message = "构建列表存在重复镜像！";
                }
            } else {
                isExist = true;
                message = "镜像列表存在重复！";
            }

            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, isExist, message);

        } catch (ErrorMessageException e) {
            LOG.error("获取镜像失败", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    private ApiResult checkGetImageByName(String imageName, String tenantName, String imageVersion) {
        ApiResult apiResult;

        apiResult = checkGetImage(imageName, tenantName);
        if (null != apiResult) {
            return apiResult;
        }

        if (StringUtils.isEmpty(imageVersion)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像版本不能为空！");
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/{imageVersionId}" }, method = RequestMethod.DELETE)
    @ApiOperation(value = "删除镜像", notes = "")
    public ApiResult deleteImage(@PathVariable String imageVersionId) {

        ApiResult apiResult;

        // 调用service方法
        try {
            boolean result = imageServiceImpl.deleteImage(imageVersionId);
            if (result) {
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "镜像删除成功！");
            } else {
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_DELETE_FAILED, "镜像删除失败！");
            }
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "" }, method = RequestMethod.POST)
    @ApiOperation(value = "上传镜像", notes = "")
    public ApiResult uploadImage(@RequestBody UploadImageDTO image) {

        ApiResult apiResult;

        // 参数解析
        String tenantName = image.getTenantName();

        // 1公用2私有
        byte imageType = image.getImageType();
        String imageName = image.getImageName();
        String description = image.getDescription();
        String imageVersion = image.getImageVersion();
        String imageFilePath = image.getImageFilePath();
        String createdBy = image.getCreatedBy();
        String projectId = image.getProjectId();

        // 校验
        apiResult = checkUploadParam(tenantName, imageName, imageType, imageVersion, imageFilePath);
        if (null != apiResult) {
            return apiResult;
        }
        // 调用service方法
        try {
            Image imageAdd = imageServiceImpl
                    .uploadImage(tenantName, imageType, imageName, description, imageVersion, imageFilePath, createdBy,
                            projectId);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, imageAdd, "上传镜像成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/modify/type/{imageVersionId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改镜像类型", notes = "")
    @ApiImplicitParam(paramType = "path", name = "imageVersionId", value = "镜像记录ID", required = true, dataType = "String")
    public ApiResult modifyImage(@PathVariable("imageVersionId") String imageVersionId,
            @RequestBody UpdateImageTypeDTO json) {
        ApiResult apiResult;

        Byte imageType = json.getImageType();
        String tenantName = json.getTenantName();

        if (!(imageType.equals(ImageConstant.IMAGE_TYPE_PUBLIC)) && !(imageType
                .equals(ImageConstant.IMAGE_TYPE_PRIVATE))) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "镜像类型必填, 且仅支持1公有2私有!");
        }
        if (StringUtils.isEmpty(imageVersionId)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像版本ID不能为空!");
        }
        apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }

        try {
            imageServiceImpl.modifyImageType(imageVersionId, tenantName, imageType);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "修改镜像成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/modify/env/{imageVersionId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改镜像类型环境变量", notes = "")
    @ApiImplicitParam(paramType = "path", name = "imageVersionId", value = "镜像记录ID", required = true, dataType = "String")
    public ApiResult modifyImageEnv(@PathVariable("imageVersionId") String imageVersionId,
            @RequestBody UpdateImageTypeDTO json) {
        ApiResult apiResult;

        String envVariables = json.getEnvVariables();
        String tenantName = json.getTenantName();

        if (StringUtils.isEmpty(imageVersionId)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像版本ID不能为空!");
        }
        apiResult = checkTenantName(tenantName);
        if (apiResult != null) {
            return apiResult;
        }

        try {
            imageServiceImpl.modifyImageEnv(imageVersionId, tenantName, envVariables);
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "修改镜像成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/modify/description/{imageId}" }, method = RequestMethod.PUT)
    @ApiOperation(value = "修改镜像描述", notes = "")
    @ApiImplicitParam(paramType = "path", name = "imageId", value = "镜像记录ID", required = true, dataType = "String")
    public ApiResult modifyImage(@PathVariable("imageId") String imageId, @RequestBody UpdateImageDescriptionDTO json) {
        ApiResult apiResult;

        String description = json.getDescription();

        if (StringUtils.isEmpty(description)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像描述不能为空!");
        }
        try {
            imageServiceImpl.modifyImageDescription(imageId, description);
            ;
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, "修改镜像成功！");
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/use/{imageVersionId}" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据镜像版本ID判断镜像是否被使用", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "imageVersionId", value = "镜像版本记录ID", required = true, dataType = "String") })
    public ApiResult isImageUsing(@PathVariable("imageVersionId") String imageVersionId) {
        ApiResult apiResult = null;

        if (imageVersionId == null) {
            imageVersionId = "";
        }
        try {
            // TODO: 2019/11/12
            //            List<Service> services = application.getServicesByImageVersionId(imageVersionId);
            //            if (services.size() > 0) {
            //                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, true, "镜像已经被使用!");
            //            } else {
            //                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, false, "镜像没有被使用!");
            //            }
        } catch (ErrorMessageException e) {
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        return apiResult;
    }

    private ApiResult checkUploadParam(String tenantName, String imageName, byte imageType, String imageVersion,
            String imageFilePath) {

        ApiResult apiResult;

        apiResult = checkGetImage(imageName, tenantName);
        if (null != apiResult) {
            return apiResult;
        }

        int type = imageType;

        if (StringUtils.isEmpty(imageVersion)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像版本不能为空！");
        }

        if (StringUtils.isEmpty(imageFilePath)) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像包路径不能为空！");
        }

        if (type != ImageConstant.IMAGE_TYPE_PRIVATE && type != ImageConstant.IMAGE_TYPE_PUBLIC) {
            apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "镜像版本格式不符合规定！");
        }

        return apiResult;
    }

    private ApiResult checkTenantName(String tenantName) {

        if (StringUtils.isEmpty(tenantName) || !tenantName.matches(Global.CHECK_TENANT_NAME)) {
            return new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "租户名称规则不符合规范");
        }

        Tenant tenant;
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

    @ResponseBody
    @RequestMapping(value = { "/info" }, method = RequestMethod.GET)
    @ApiOperation(value = "根据镜像版本ID获取镜像详情或根据租户名和镜像名获取镜像信息(都填写，根据镜像版本ID查询)", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "imageVersionId", value = "镜像版本Id", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "tenantName", value = "租户名称", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "imageName", value = "镜像名称", required = false, dataType = "String") })
    public ApiResult getImageByQueryInfo(
            @RequestParam(value = "imageVersionId", required = false) String imageVersionId,
            @RequestParam(value = "tenantName", required = false) String tenantName,
            @RequestParam(value = "imageName", required = false) String imageName) {

        ApiResult apiResult;

        ImageDetail imageDetail;

        try {
            // 根据镜像版本ID查询详情
            if (!StringUtils.isEmpty(imageVersionId)) {

                imageDetail = imageServiceImpl.getDetailByImageVersionId(imageVersionId);
                imageDetail.getImageVersion().setRegistryImageName(
                        imageServiceImpl.getRegistryImageName(imageDetail.getImage(), imageDetail.getImageVersion()));
                LOG.info("----------------------" + JSON.toJSONString(imageDetail));
                apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, imageDetail, "获取镜像详情成功！");

            }
            // 根据租户和镜像名查询
            else if (!StringUtils.isEmpty(tenantName) && !StringUtils.isEmpty(imageName)) {
                imageDetail = new ImageDetail();
                List<Image> images = imageServiceImpl.getImagesByTenantNameAndImageName(tenantName, imageName);
                if (!images.isEmpty()) {
                    imageDetail.setImage(images.get(0));
                    LOG.info("----------------------" + JSON.toJSONString(imageDetail));
                    apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, imageDetail, "获取镜像详情成功！");
                } else {
                    apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像不存在！");
                }
            } else {
                apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NULL, "镜像版本ID为空且租户或镜像名也为空！");
            }
        } catch (ErrorMessageException e) {
            LOG.error("", e);
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }

        // 调用service方法

        return apiResult;
    }

    @ResponseBody
    @RequestMapping(value = { "/imagequalitymetric" }, method = RequestMethod.GET)
    @ApiOperation(value = "获取镜像质量统计结果", notes = "")
    public ApiResult getImageByQueryInfo() {
        ApiResult apiResult = null;
        try {
            apiResult = new ApiResult(ReturnCode.CODE_SUCCESS, imageServiceImpl.getImageQualityStatistics(),
                    "获取镜像详情成功！");
        } catch (ErrorMessageException e) {
            apiResult = new ApiResult(e.getCode(), e.getMessage());
        }
        return apiResult;
    }
}
