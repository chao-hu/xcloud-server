package com.xxx.xcloud.module.image.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bonc.bdos.harbor.client.ApiCallback;
import com.bonc.bdos.harbor.client.ApiException;
import com.bonc.bdos.harbor.client.api.ProductsApi;
import com.bonc.bdos.harbor.client.model.DetailedTag;
import com.bonc.bdos.harbor.client.model.VulnerabilityItem;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Table;
import com.xxx.xcloud.client.docker.DockerClientFactory;
import com.xxx.xcloud.client.harbor.HarborClientFactory;
import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.service.ICiService;
import com.xxx.xcloud.module.cronjob.entity.Cronjob;
import com.xxx.xcloud.module.cronjob.repository.CronjobRepository;
import com.xxx.xcloud.module.docker.DockerService;
import com.xxx.xcloud.module.harbor.entity.HarborUser;
import com.xxx.xcloud.module.harbor.repositpry.HarborUserRepository;
import com.xxx.xcloud.module.image.consts.ImageConstant;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.model.ImageQualityStatistics;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import com.xxx.xcloud.module.image.model.GroupImage;
import com.xxx.xcloud.module.image.model.ImageDetail;
import com.xxx.xcloud.module.image.repository.ImageRepository;
import com.xxx.xcloud.module.image.repository.ImageVersionRepository;
import com.xxx.xcloud.module.image.service.ImageService;
import com.xxx.xcloud.module.sonar.service.SonarService;
import com.xxx.xcloud.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

/**
 * @author xjp
 * @Description: 镜像service方法
 * @date: 2019年11月11日
 */
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private HarborUserRepository harborUserRepository;

    @Autowired
    private DockerService dockerService;

    @Autowired
    private ICiService ciService;

    @Autowired
    private CronjobRepository cronjobRepository;

    @Autowired
    private ImageVersionRepository imageVersionRepository;

    @Autowired
    SonarService sonarService;

    private static final Logger LOG = LoggerFactory.getLogger(ImageServiceImpl.class);

    /**
     * 镜像名称验证
     */
    private static final Pattern PATTERN = Pattern.compile(ImageConstant.IMAGE_NAME_REGEX_STR);

    private static Interner<String> interner = Interners.newWeakInterner();
    // TODO: 2019/11/12  
    //    @Autowired
    //    private Application application;

    @Override
    public Page<Image> getImages(String tenantName, String imageName, String projectId, Pageable pageable) {
        // 获取镜像列表
        Page<Image> imagePage = null;

        if (StringUtils.isEmpty(imageName)) {
            imagePage = findImagesByTenantNamePage(tenantName, pageable);
        } else {
            imagePage = findImagesByTenantNameAndImageNameLikePage(tenantName, imageName, pageable);
        }

        return imagePage;
    }

    @Override
    public Page<Image> getPublicImages(String imageName, String projectId, Pageable pageable) {
        // 获取镜像列表
        Page<Image> imagePage = null;

        if (StringUtils.isEmpty(imageName)) {
            imagePage = findPublicImagesPage(pageable);
        } else {
            imagePage = findPublicImagesByImageNameLikePage(imageName, pageable);
        }

        return imagePage;
    }

    @Override
    public Page<Image> getOwnAndPublicImages(String tenantName, String imageName, String projectId, Pageable pageable) {
        // 获取镜像列表
        Page<Image> imagePage = null;

        if (StringUtils.isEmpty(imageName)) {
            imagePage = findByTenantNameOrImageType(tenantName, ImageConstant.IMAGE_TYPE_PUBLIC, pageable);
        } else {
            imagePage = findByTenantNameOrImageTypeAndImageNameLike(tenantName, ImageConstant.IMAGE_TYPE_PUBLIC,
                    imageName, pageable);
        }

        return imagePage;
    }

    /**
     * 根据镜像ID,镜像类型, 查询版本数量
     *
     * @param imageId
     * @param imageType
     * @return Long
     * @date: 2019年3月25日 下午5:47:54
     */
    private Long countImageVersionsByImageIdAndImageType(String imageId, byte imageType) {
        try {
            return imageVersionRepository.countByImageIdAndImageType(imageId, imageType);
        } catch (Exception e) {
            LOG.error("根据镜像ID和镜像类型判断镜像版本数量!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "根据镜像ID,镜像类型, 查询版本数量错误!");
        }
    }

    /**
     * 根据镜像ID, 查询版本数量
     *
     * @param imageId
     * @param
     * @return Long
     * @date: 2019年3月25日 下午5:47:54
     */
    private Long countImageVersionsByImageId(String imageId) {
        try {
            return imageVersionRepository.countByImageId(imageId);
        } catch (Exception e) {
            LOG.error("根据镜像ID和镜像类型判断镜像版本数量!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "根据镜像ID,镜像类型, 查询版本数量错误!");
        }
    }

    @Override
    public Image updateImageVersionCountSync(Image image, ImageVersion imageVersion, String operator) {
        String lockStr = getImageOperatorSyncLockStr(image);
        // 对同一租户，同一镜像名进行的操作，需要同步
        synchronized (lockStr) {
            if (operator.equals(ImageConstant.IMAGE_OPERATOR_DEL)) {
                return SpringContextHolder.getBean(ImageService.class).deleteImageAndVersionTrans(image, imageVersion);
            }

            if (operator.equals(ImageConstant.IMAGE_OPERATOR_ADD)) {
                return SpringContextHolder.getBean(ImageService.class).addImageAndVersionTrans(image, imageVersion);
            }
        }
        return image;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Image deleteImageAndVersionTrans(Image image, ImageVersion imageVersion) {
        // 删除操作
        deleteImageVersionById(imageVersion.getId());

        // 删除操作后，公有镜像版本数量
        int publicVerisonNum = 0;
        // 如果删除公有镜像，判断删除后是否仍有共有镜像
        if (imageVersion.getImageType().equals(ImageConstant.IMAGE_TYPE_PUBLIC)) {
            publicVerisonNum = countImageVersionsByImageIdAndImageType(image.getId(), ImageConstant.IMAGE_TYPE_PUBLIC)
                    .intValue();
        }

        // 删除操作后，镜像版本数量
        Long count = countImageVersionsByImageId(image.getId());
        int versionNum = count == null ? 0 : count.intValue();

        // 删除操作后没有镜像版本了，删除镜像
        if (versionNum <= 0) {
            deleteImageById(image.getId());
            return image;
        }

        // 删除操作后仍有镜像，设置镜像当前最新版本
        Pageable pageable = PageUtil.getPageable(0, 1, ImageConstant.SORT_DEFAULT);
        Page<ImageVersion> imageVersionPage = getImageVersionsByImageId(image.getId(), pageable);
        if (!imageVersionPage.getContent().isEmpty()) {
            ImageVersion imageVersionNewest = imageVersionPage.getContent().get(0);
            image.setImageSize(imageVersionNewest.getImageSize());
            image.setCreateTime(imageVersionNewest.getCreateTime());
            image.setCreatedBy(imageVersionNewest.getCreatedBy());
            image.setPorts(imageVersionNewest.getPorts());
        }
        // 设置版本数
        image.setVersionNum(versionNum);

        // 如果删除的是公有镜像版本，设置镜像类型
        if (imageVersion.getImageType().equals(ImageConstant.IMAGE_TYPE_PUBLIC)) {
            // 判断删除版本后, 镜像是否仍有公有版本
            if (publicVerisonNum > 0) {
                image.setImageType(ImageConstant.IMAGE_TYPE_PUBLIC);
            } else {
                image.setImageType(ImageConstant.IMAGE_TYPE_PRIVATE);
            }
        }
        return saveImage(image);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Image addImageAndVersionTrans(Image image, ImageVersion imageVersion) {
        try {
            // 操作的镜像对象
            Image imageOperator = getImageOnOperator(image);
            ImageVersion imageVersionOperator = getImageVersionOnOperator(imageOperator, imageVersion);

            // 如果新添加Image对象，未添加前，版本数为0；如果已经存在镜像，计算现在版本数；如果版本为新增并加1，版本为更新，不变
            int versionNum = 0;
            if (imageOperator.getId() != null) {
                Long count = countImageVersionsByImageId(imageOperator.getId());
                versionNum = count == null ? 0 : count.intValue();
            }
            // 如果ImageVersion是新增操作，版本数需要加一
            if (imageVersionOperator.getId() == null) {
                versionNum++;
            }
            if (imageVersionOperator.getImageType().equals(ImageConstant.IMAGE_TYPE_PUBLIC)) {
                image.setImageType(ImageConstant.IMAGE_TYPE_PUBLIC);
            }
            imageOperator.setVersionNum(versionNum);

            // 保存
            imageOperator = saveImage(imageOperator);
            imageVersionOperator.setImageId(imageOperator.getId());
            saveImageVersion(imageVersionOperator);
            return imageOperator;
        } catch (Exception e) {
            LOG.error("保存镜像版版错误.", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存镜像信息错误！");
        }
    }

    /**
     * 获取镜像操作sync字符串锁 以租户名_镜像名_str作为同步锁对象；使用intern方法，使相同内容字符串指向同一地址
     *
     * @param image
     * @return String
     * @date: 2019年6月24日 下午6:34:15
     */
    private String getImageOperatorSyncLockStr(Image image) {
        return interner.intern(image.getTenantName() + "_" + image.getImageName() + "_image_operator_lock");
    }

    /**
     * 根据镜像信息，判断镜像操作的具体是哪个镜像信息
     *
     * @param image
     * @return Image
     * @date: 2019年6月21日 下午2:56:57
     */
    private Image getImageOnOperator(Image image) {
        Image imageOperator = null;
        // 添加镜像的Image信息是否已经存在
        if (image.getId() != null) {
            imageOperator = getImageById(image.getId());
        }

        // 之前ID的镜像信息被删除，相同镜像信息又被创建
        if (imageOperator == null) {
            imageOperator = getImageByName(image.getTenantName(), image.getImageName(), null);
        }

        if (imageOperator == null) {
            // 镜像不存在，需要新建
            imageOperator = image;
        } else {
            // 镜像已经存在，设置最新版本信息
            imageOperator.setCiType(image.getCiType());
            imageOperator.setCreatedBy(image.getCreatedBy());
            imageOperator.setCreateTime(image.getCreateTime());
            imageOperator.setImageSize(image.getImageSize());
            imageOperator.setImageVersion(image.getImageVersion());
            imageOperator.setProjectId(image.getProjectId());
            imageOperator.setPorts(image.getPorts());
        }

        return imageOperator;
    }

    /**
     * 获取镜像操作的镜像版本对象
     *
     * @param image
     * @param imageVersion
     * @return ImageVersion
     * @date: 2019年6月24日 下午7:43:29
     */
    private ImageVersion getImageVersionOnOperator(Image image, ImageVersion imageVersion) {
        // 如果镜像版本已经存在，就不进行更新操作了
        // 如果同一个镜像版本同时添加两次，一次操作成功了，另一次操作同步进行到这，发现版本已经存在，更新存在的版本，而不是新建
        // 问题：如果一个服务构建在进行中，此时重新启动一个服务，会检测到执行中的构建，再次启动构建；两个服务构建任务一起执行，最终都执行完成，都会进行添加镜像的操作；就会导致添加多个相同版本
        if (image.getId() != null) {
            List<ImageVersion> imageVersions = findImageVersionsByImageIdAndImageVersion(image.getId(),
                    imageVersion.getImageVersion());
            if (imageVersions != null && !imageVersions.isEmpty()) {
                // 设置新镜像信息
                ImageVersion imageVersionOperator = imageVersions.get(0);
                imageVersionOperator.setCreateTime(imageVersion.getCreateTime());
                imageVersionOperator.setImageSize(imageVersion.getImageSize());
                imageVersionOperator.setPorts(imageVersion.getPorts());
                imageVersionOperator.setEnvVariables(imageVersion.getEnvVariables());
                imageVersionOperator.setCodeBaseName(imageVersion.getCodeBaseName());
                return imageVersionOperator;
            }
        }

        return imageVersion;
    }

    /**
     * 根据ID删除镜像信息
     *
     * @param id
     * @date: 2019年3月25日 下午7:33:57
     */
    private void deleteImageById(String id) {
        try {
            imageRepository.deleteById(id);
        } catch (Exception e) {
            LOG.error("根据ID删除镜像记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "根据ID删除镜像信息失败!");
        }
    }

    /**
     * 根据ID删除镜像版本信息
     *
     * @param id
     * @date: 2019年3月25日 下午7:33:57
     */
    private void deleteImageVersionById(String id) {
        try {
            imageVersionRepository.deleteById(id);
        } catch (Exception e) {
            LOG.error("根据ID删除镜像版本记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "根据ID删除镜像版本信息失败!");
        }
    }

    /**
     * 根据镜像名称和镜像类型查询镜像信息
     *
     * @param imageName
     * @param imageType
     * @return List<Image>
     * @date: 2019年3月26日 下午7:10:33
     */
    private List<Image> getImagesByImageNameAndImageType(String imageName, byte imageType) {
        try {
            return imageRepository.findByImageNameAndImageType(imageName, imageType);
        } catch (Exception e) {
            LOG.error("根据镜像名称和镜像类型查询镜像失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "根据镜像名称和镜像类型查询镜像失败!");
        }
    }

    /**
     * 镜像是否被其他租户公有了
     *
     * @param imageName
     * @param tenantName
     * @return boolean true:被其他租户公有了 false:没有被其他租户公有
     * @date: 2019年3月26日 下午7:14:53
     */
    private boolean isImagePublicByOther(String imageName, String tenantName) {
        List<Image> images = getImagesByImageNameAndImageType(imageName, ImageConstant.IMAGE_TYPE_PUBLIC);
        if (!images.isEmpty()) {
            Image image = images.get(0);
            if (!image.getTenantName().equals(tenantName)) {
                return true;
            }
        }
        return false;
    }

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private TransactionDefinition transactionDefinition;

    @Override
    public boolean modifyImageType(String imageVersionid, String tenantName, byte imageType) {

        ImageVersion imageVersion = getImageVersionById(imageVersionid);
        if (imageVersion == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像版本信息不存在!");
        }
        // 修改ID的镜像
        Image image = getImageById(imageVersion.getImageId());
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像不存在!");
        }
        if (!image.getTenantName().equals(tenantName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "无权修改其他租户的镜像!");
        }
        if (imageType == imageVersion.getImageType()) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "镜像公有私有信息未改变!");
        }
        // TODO: 2019/11/12  判断镜像是否被使用
        //        List<xxx.xcloud.module.application.entity.Service> services = application
        //                .getServicesByImageVersionId(imageVersionid);
        //        if (!services.isEmpty()) {
        //            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE, "镜像已经被服务使用, 不允许修改公有私有属性!");
        //        }
        // 如果是修改为公有的, 判断是否已有同名镜像被其他租户公有
        if (imageType == ImageConstant.IMAGE_TYPE_PUBLIC && isImagePublicByOther(image.getImageName(), tenantName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "已有其他租户共享了同名镜像, 无法共享!");
        }

        // 原来的类型
        byte imageTypeOriginal = imageVersion.getImageType();
        // 修改后的类型
        imageVersion.setImageType(imageType);
        // 修改镜像在仓库中的位置
        modifyImageType(image, imageVersion, imageTypeOriginal);

        // 修改数据库
        updateImageAndVersionTypeTrans(image, imageVersion, imageType);

        // 删除harbor原来的镜像
        // 镜像原来是否在公有仓库
        String publicRepo = "";
        if (imageTypeOriginal == ImageConstant.IMAGE_TYPE_PUBLIC) {
            publicRepo = XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/";
        }
        try {
            HarborClientFactory.getProductsApi()
                    .repositoriesRepoNameTagsTagDelete(publicRepo + image.getTenantName() + "/" + image.getImageName(),
                            imageVersion.getImageVersion());
        } catch (ApiException e) {
            LOG.error("修改镜像到新仓库后, 删除仓库原来的镜像失败!", e);
        }

        return true;
    }

    @Override
    public boolean modifyImageEnv(String imageVersionid, String tenantName, String envVariables) {
        ImageVersion imageVersion = getImageVersionById(imageVersionid);
        if (imageVersion == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像版本信息不存在!");
        }
        imageVersion.setEnvVariables(envVariables);
        saveImageVersion(imageVersion);
        return true;
    }

    /**
     * 事务内更新镜像表和镜像版本表信息的公有私有类型
     *
     * @param image
     * @param imageVersion
     * @param imageType    镜像版本更新后的类型
     * @date: 2019年7月4日 上午10:02:18
     */
    private void updateImageAndVersionTypeTrans(Image image, ImageVersion imageVersion, byte imageType) {
        String lockStr = getImageOperatorSyncLockStr(image);
        synchronized (lockStr) {
            // 修改镜像表信息, 判断镜像是公有私有的
            byte imageTypeForImageTable = ImageConstant.IMAGE_TYPE_PRIVATE;
            // 私有改公有
            if (imageType == ImageConstant.IMAGE_TYPE_PUBLIC) {
                imageTypeForImageTable = ImageConstant.IMAGE_TYPE_PUBLIC;
            } else { // 公有改为私有
                if (countImageVersionsByImageIdAndImageType(image.getId(), ImageConstant.IMAGE_TYPE_PUBLIC) - 1 > 0) {
                    imageTypeForImageTable = ImageConstant.IMAGE_TYPE_PUBLIC;
                }
            }
            TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);
            try {
                imageVersionRepository.updateImageVersionType(imageType, imageVersion.getId());
                imageRepository.updateImageType(imageTypeForImageTable, image.getId());
                platformTransactionManager.commit(transactionStatus);
            } catch (Exception e) {
                platformTransactionManager.rollback(transactionStatus);
                LOG.error("更新镜像类型失败!", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新镜像类型失败!");
            }
        }
    }

    /**
     * 修改镜像公有私有
     *
     * @param image             镜像信息
     * @param imageVersion      镜像版本信息
     * @param imageTypeOriginal 修改前的镜像类型
     * @date: 2019年3月4日 下午2:47:07
     */
    private void modifyImageType(Image image, ImageVersion imageVersion, byte imageTypeOriginal) {
        HarborUser harborUser = getHarborUserByByTenantName(image.getTenantName());
        if (harborUser == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户harbor信息不存在!");
        }
        DockerClient dockerClient = DockerClientFactory
                .getDockerClientInstance(harborUser.getUsername(), harborUser.getPassword(), harborUser.getEmail());

        // 镜像新的类型
        byte imageType = imageVersion.getImageType();
        InspectImageResponse res = null;
        try {
            // 拉取镜像, 根据原来的信息拉取
            imageVersion.setImageType(imageTypeOriginal);
            res = dockerService.pullImage(image, imageVersion, dockerClient);
            LOG.info("***************pull镜像完成***********");
            // 修改新名称, 根据新的信息修改, 并上传
            imageVersion.setImageType(imageType);
            dockerService.tagImage(res.getId(), image, imageVersion, dockerClient);
            LOG.info("***************tag镜像完成***********");
            dockerService.pushImage(image, imageVersion, dockerClient);
            LOG.info("***************push镜像完成***********");
        } finally {
            // 删除pull下来的镜像
            if (res != null) {
                try {
                    dockerService.removeImage(res.getId(), dockerClient);
                } catch (ErrorMessageException e) {
                    LOG.error("修改镜像公有私有权限时, 从仓库拉取下来的镜像删除失败!");
                }
            }
            if (dockerClient != null) {
                try {
                    dockerClient.close();
                } catch (Exception e) {
                    LOG.error("关闭dockerClient失败", e);
                }
            }
        }
    }

    @Override
    public void modifyImageDescription(String imageId, String description) {
        Image image = getImageById(imageId);
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户下不存在此镜像!");
        }

        String lockStr = getImageOperatorSyncLockStr(image);
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);
        synchronized (lockStr) {
            try {
                // 镜像更新，只更新描述信息
                imageRepository.updateImageDescreption(description, imageId);
                platformTransactionManager.commit(transactionStatus);
            } catch (Exception e) {
                platformTransactionManager.rollback(transactionStatus);
                LOG.error("更新镜像描述信息失败!", e);
                throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "更新镜像描述信息失败!");
            }
        }
    }

    /**
     * 根据租户和projectId查询镜像
     *
     * @param tenantName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月16日 下午3:51:48
     */
    private List<Image> getPrivateImagesByProjectId(String tenantName, String projectId) {
        try {
            return imageRepository.getAllPrivate(tenantName, projectId);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 根据租户和projectId查询镜像
     *
     * @param tenantName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月16日 下午3:51:48
     */
    private List<Image> getImagesByProjectId(String tenantName, String projectId) {
        try {
            return imageRepository.getAll(tenantName, projectId);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 获取租户下所有镜像
     *
     * @param tenantName
     * @return List<Image>
     * @date: 2019年1月18日 上午10:24:46
     */
    private List<Image> getPrivateImages(String tenantName) {
        try {
            return imageRepository.getAllPrivate(tenantName);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 获取租户下所有镜像
     *
     * @param tenantName
     * @return List<Image>
     * @date: 2019年1月18日 上午10:24:46
     */
    private List<Image> getImages(String tenantName) {
        try {
            return imageRepository.getAll(tenantName);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 查询租户下的镜像
     *
     * @param tenantName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年3月26日 上午9:44:36
     */
    private Page<Image> findImagesByTenantNamePage(String tenantName, Pageable pageable) {
        try {
            return imageRepository.findByTenantName(tenantName, pageable);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 查询租户下的镜像
     *
     * @param
     * @param pageable
     * @return Page<Image>
     * @date: 2019年3月26日 上午9:44:36
     */
    private Page<Image> findPublicImagesPage(Pageable pageable) {
        try {
            return imageRepository.findByImageType(ImageConstant.IMAGE_TYPE_PUBLIC, pageable);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 查询租户下的镜像
     *
     * @param tenantName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年3月26日 上午9:44:36
     */
    private Page<Image> findByTenantNameOrImageType(String tenantName, byte imageType, Pageable pageable) {
        try {
            return imageRepository.findByTenantNameOrImageType(tenantName, imageType, pageable);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 查询租户下的镜像
     *
     * @param tenantName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年3月26日 上午9:44:36
     */
    private Page<Image> findByTenantNameOrImageTypeAndImageNameLike(String tenantName, byte imageType, String imageName,
            Pageable pageable) {
        try {
            return imageRepository
                    .getByTenantNameOrImageTypeAndImageNameLike(tenantName, imageType, "%" + imageName + "%", pageable);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 查询租户下的镜像, 根据名称过滤
     *
     * @param tenantName
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年3月26日 上午9:50:47
     */
    private Page<Image> findImagesByTenantNameAndImageNameLikePage(String tenantName, String imageName,
            Pageable pageable) {
        try {
            return imageRepository.findByTenantNameAndImageNameLike(tenantName, "%" + imageName + "%", pageable);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 查询租户下的镜像, 根据名称过滤
     *
     * @param
     * @param imageName
     * @param pageable
     * @return Page<Image>
     * @date: 2019年3月26日 上午9:50:47
     */
    private Page<Image> findPublicImagesByImageNameLikePage(String imageName, Pageable pageable) {
        try {
            return imageRepository
                    .findByImageTypeAndImageNameLike(ImageConstant.IMAGE_TYPE_PUBLIC, "%" + imageName + "%", pageable);
        } catch (Exception e) {
            LOG.error("根据projectId查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 根据租户, 镜像名和projectId查询镜像
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月16日 下午3:54:10
     */
    private List<Image> getPrivateImagesByProjectIdAndNameLike(String tenantName, String imageName, String projectId) {
        try {
            return imageRepository.getByNameLikePrivate(tenantName, "%" + imageName.trim() + "%", projectId);
        } catch (Exception e) {
            LOG.error("根据projectId和imageName查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 根据租户, 镜像名和projectId查询镜像
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2019年1月16日 下午3:54:10
     */
    private List<Image> getImagesByProjectIdAndNameLike(String tenantName, String imageName, String projectId) {
        try {
            return imageRepository.getByNameLike(tenantName, "%" + imageName.trim() + "%", projectId);
        } catch (Exception e) {
            LOG.error("根据projectId和imageName查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 根据租户, 镜像名查询镜像
     *
     * @param tenantName
     * @param imageName
     * @return List<Image>
     * @date: 2019年1月18日 上午10:28:47
     */
    private List<Image> getPrivateImagesByNameLike(String tenantName, String imageName) {
        try {
            return imageRepository.getByNameLikePrivate(tenantName, "%" + imageName.trim() + "%");
        } catch (Exception e) {
            LOG.error("根据projectId和imageName查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    /**
     * 根据租户, 镜像名查询镜像
     *
     * @param tenantName
     * @param imageName
     * @return List<Image>
     * @date: 2019年1月18日 上午10:28:47
     */
    private List<Image> getImagesByNameLike(String tenantName, String imageName) {
        try {
            return imageRepository.getByNameLike(tenantName, "%" + imageName.trim() + "%");
        } catch (Exception e) {
            LOG.error("根据projectId和imageName查询镜像失败: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询列表失败!");
        }
    }

    @Override
    public ImageDetail getDetailByImageVersionId(String imageVersionid) {
        ImageVersion imageVersion = getImageVersionById(imageVersionid);
        if (imageVersion == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像版本不存在!");
        }
        Image image = getImageById(imageVersion.getImageId());
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像信息不存在!");
        }
        ImageDetail imageDetail = new ImageDetail();
        imageDetail.setImage(image);
        imageDetail.setImageVersion(imageVersion);
        return imageDetail;
    }

    @Override
    public Image getImageById(String id) {
        try {
            return imageRepository.getById(id);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_CONNECT_FAILED, "数据库连接异常");
        }
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return Image
     * @date: 2019年3月27日 上午10:08:41
     */
    private Image getImageByIdForUpdate(String id) {
        try {
            return imageRepository.getByIdForUpdate(id);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_CONNECT_FAILED, "数据库连接异常");
        }
    }

    @Override
    public Page<GroupImage> conversionImageGroupPage(List<Image> images, Pageable pageable) {
        // 按镜像名称分组
        Map<String, GroupImage> imageGroupMap = new LinkedHashMap<>();
        for (Image image : images) {
            if (imageGroupMap.get(image.getImageName()) == null) {
                imageGroupMap.put(image.getImageName(), new GroupImage(image.getImageName(), image));
            } else {
                imageGroupMap.get(image.getImageName()).getImages().add(image);
            }
        }
        List<GroupImage> groupImages = new ArrayList<GroupImage>(imageGroupMap.values());
        List<GroupImage> pageImages = new ArrayList<GroupImage>();

        // 分页(pageNumber为0时, 为统计第一页的数据)
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;
        int total = groupImages.size();
        end = end <= total - 1 ? end : total - 1;
        while (start <= end) {
            pageImages.add(groupImages.get(start++));
        }

        // 生成分页对象
        Page<GroupImage> page = new PageImpl<>(pageImages, pageable, total);
        return page;
    }

    /**
     * 根据镜像名称获取镜像
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return Image
     * @date: 2019年6月20日 下午7:26:54
     */
    private Image getImageByName(String tenantName, String imageName, String projectId) {
        List<Image> images = getImagesByName(tenantName, imageName, projectId);
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0);
    }

    @Override
    public List<Image> getImagesByName(String tenantName, String imageName, String projectId) {
        try {
            if (StringUtils.isEmpty(projectId)) {
                return imageRepository.getByName(tenantName, imageName);
            }
            return imageRepository.getByName(tenantName, imageName, projectId);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_CONNECT_FAILED, "数据库连接异常");
        }
    }

    @Override
    public boolean deleteImage(String imageVersionId) {
        // 查询镜像信息
        ImageVersion imageVersion = getImageVersionById(imageVersionId);
        if (imageVersion == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "镜像版本信息不存在!");
        }
        Image image = getImageById(imageVersion.getImageId());
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "镜像信息不存在!");
        }

        // 查询镜像是否在使用
        // List<xxx.xcloud.module.application.entity.Service> serviceList =
        // serviceRepository
        // .findByImageId(imageVersionId);
        // TODO: 2019/11/12
        //        List<xxx.xcloud.module.application.entity.Service> serviceList = application
        //                .getServicesByImageVersionId(imageVersionId);
        //        if (null != serviceList && !serviceList.isEmpty()) {
        //            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "有服务正在使用该镜像，无法删除");
        //        }

        List<Cronjob> taskCronjobs = cronjobRepository.findByImageVerisonId(imageVersionId);
        LOG.info("--------id---------" + imageVersionId);
        LOG.info("-------taskCronjobs--------" + JSONObject.toJSONString(taskCronjobs));
        if (null != taskCronjobs && !taskCronjobs.isEmpty()) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "有定时任务正在使用该镜像，无法删除");
        }

        // 删除数据库信息
        try {
            updateImageVersionCountSync(image, imageVersion, ImageConstant.IMAGE_OPERATOR_DEL);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除数据库数据失败!");
        }

        ProductsApi productsApi = null;
        try {
            productsApi = HarborClientFactory.getProductsApi();
        } catch (Exception e) {
            LOG.error("删除harbor操作ProductsApi错误：" + e.getMessage());
        }
        // 删除harbor镜像
        // 如果存在镜像
        if (productsApi != null) {
            // 删除公有的和私有的都做一下删除，避免因修改镜像类型出错而出现多余的镜像
            try {
                imageVersion.setImageType(ImageConstant.IMAGE_TYPE_PUBLIC);
                if (getHarborImageDetail(image, imageVersion, productsApi) != null) {
                    // 删除harbor中的镜像
                    deleteHarborImage(image, imageVersion, productsApi);
                }
            } catch (Exception e) {
                LOG.error("删除harbor镜像操作错误：", e);
            }
            try {
                imageVersion.setImageType(ImageConstant.IMAGE_TYPE_PRIVATE);
                if (getHarborImageDetail(image, imageVersion, productsApi) != null) {
                    // 删除harbor中的镜像
                    deleteHarborImage(image, imageVersion, productsApi);
                }
            } catch (Exception e) {
                LOG.error("删除harbor镜像操作错误：", e);
            }
        }

        return true;
    }

    /**
     * 根据ID获取版本信息
     *
     * @param id
     * @return ImageVersion
     * @date: 2019年3月25日 上午11:27:14
     */
    private ImageVersion getImageVersionById(String id) {
        try {
            Optional<ImageVersion> optional = imageVersionRepository.findById(id);
            return optional.isPresent() ? optional.get() : null;
        } catch (Exception e) {
            LOG.error("根据ID查询镜像版本错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "根据ID查询镜像版本错误" + e.getMessage());
        }
    }

    /**
     * 根据租户名查询harbor用户信息
     *
     * @param tenantName
     * @return HarborUser
     * @date: 2019年1月24日 下午6:04:24
     */
    @Override
    public HarborUser getHarborUserByByTenantName(String tenantName) {
        try {
            return harborUserRepository.findByTenantName(tenantName);
        } catch (Exception e) {
            LOG.error("查询harbor用户信息失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询harbor用户失败!");
        }
    }

    @Override
    public boolean isHarborImageExist(String imageVersionId) {
        boolean isExist = false;
        ImageVersion imageVersion = getImageVersionById(imageVersionId);
        if (imageVersion != null) {
            Image image = getImageById(imageVersion.getImageId());
            if (image != null) {
                if (getHarborImageDetail(image, imageVersion) != null) {
                    isExist = true;
                }
            }
        }
        return isExist;
    }

    @Override
    public DetailedTag getHarborImageDetail(Image image, ImageVersion imageVersion) {
        ProductsApi productsApi = HarborClientFactory.getProductsApi();
        return getHarborImageDetail(image, imageVersion, productsApi);
    }

    /**
     * 删除harbor中的镜像
     *
     * @param image        镜像信息
     * @param imageVersion 镜像版本信息
     * @param
     * @date: 2018年12月18日 下午5:33:31
     */
    private void deleteHarborImage(Image image, ImageVersion imageVersion, ProductsApi productsApi) {
        try {
            productsApi.repositoriesRepoNameTagsTagDelete(
                    generateImageNameTotalWithOutRegistryUrl(imageVersion.getImageType(), image.getTenantName(),
                            image.getImageName()), imageVersion.getImageVersion());
        } catch (Exception e) {
            LOG.error("删除harbor中的镜像错误: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_HARBOR_DELETE_IMAGE_FAILED, "删除harbor仓库的镜像失败");
        }
    }

    /**
     * 获取harbor镜像
     *
     * @param image        镜像
     * @param imageVersion 镜像版本信息
     * @return DetailedTag harbor镜像详情
     * @date: 2018年12月24日 上午9:28:27
     */
    public DetailedTag getHarborImageDetail(Image image, ImageVersion imageVersion, ProductsApi productsApi) {
        try {
            return productsApi.repositoriesRepoNameTagsTagGet(
                    generateImageNameTotalWithOutRegistryUrl(imageVersion.getImageType(), image.getTenantName(),
                            image.getImageName()), imageVersion.getImageVersion());
        } catch (ApiException e) {
            LOG.error("获取harbor镜像详情错误: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_HARBOR_FIND_IMAGE_FAILED, "harbor查询镜像失败!");
        } catch (Exception e) {
            LOG.error("获取harbor镜像详情错误: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_HARBOR_FIND_IMAGE_FAILED, "harbor查询镜像失败!");
        }
    }

    @Override
    public Image uploadImage(String tenantName, Byte imageType, String imageName, String description,
            String imageVersion, String imageFilePath, String createdBy, String projectId) {
        // 判断上传文件是否是tar包
        if (!imageFilePath.endsWith(ImageConstant.FILE_END_TAR)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "镜像上传仅支持tar包上传!");
        }
        // 判断镜像名称是否合法
        if (!isImageNameLegal(imageName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT,
                    "镜像名称只能是小写字母, 数字, 横线, 下划线, 且必须以字母开头.");
        }
        // 判断镜像名和版本是否已经存在(image表和ci表都不存在)
        if (isImageExist(tenantName, imageName, imageVersion)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名和版本已经存在!");
        }
        if (ciService.getCiByImageNameAndVersion(tenantName, imageName, imageVersion) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "镜像名和版本已经存在!");
        }
        // 下载tar包到本地
        String localPath = loadImageFileFromFtp(imageFilePath, tenantName, imageName, imageVersion);
        if (localPath == null) {
            throw new ErrorMessageException(ReturnCode.CODE_IMAGE_TAR_OPERATION_FAILED, "获取上传镜像包失败!");
        }
        LOG.info("****************从ftp下载tar包到本地完成***********");
        File imageTarFile = new File(localPath);
        Date now = new Date();

        // 生成镜像信息，版本信息
        Image image = new Image(tenantName, now, imageType, imageName, description, imageVersion, 0.0d,
                ImageConstant.IMAGE_CI_TYPE_UPLOAD, createdBy, projectId);
        ImageVersion imageVersionObj = new ImageVersion();
        imageVersionObj.setImageVersion(imageVersion);
        imageVersionObj.setImageType(imageType);

        // 生成镜像, 删除tar文件
        try {
            loadPushInspectDelImage(image, imageVersionObj, imageTarFile, localPath);
        } finally {
            // 删除本地tar文件(删除到imageVersion一级)
            FileUtils.delFolder(imageTarFile.getParent());
        }

        // 保存数据库, 如果保存失败, 删除仓库的镜像
        try {
            // 镜像对象最新信息设置到版本对象里
            imageVersionObj = new ImageVersion(image.getId(), now, imageType, imageVersion, image.getImageSize(),
                    ImageConstant.IMAGE_CI_TYPE_UPLOAD, image.getPorts(), createdBy, image.getProjectId(), null);
            return updateImageVersionCountSync(image, imageVersionObj, ImageConstant.IMAGE_OPERATOR_ADD);
        } catch (Exception e) {
            LOG.error("镜像上传, 数据库保存Image和ImageVersion失败: " + e.getMessage(), e);
            try {
                HarborClientFactory.getProductsApi().repositoriesRepoNameTagsTagDelete(
                        generateImageNameTotalWithOutRegistryUrl(imageType, tenantName, imageName),
                        imageVersionObj.getImageVersion());
            } catch (Exception delHarborImageException) {
                LOG.error("镜像保存数据库失败后, 删除harbor仓库镜像也失败: " + image.getTenantName() + "/" + image.getImageName() + ":"
                        + image.getImageVersion(), e);
            }
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "镜像信息保存数据库失败!");
        }
    }

    /**
     * 从ftp下载镜像tar包到本地, 下载完成后删除镜像tar文件, 返回本地tar包路径
     *
     * @param imageFilePath
     * @param tenantName
     * @param imageName
     * @param imageVersion
     * @return String
     * @date: 2019年1月16日 下午3:21:21
     */
    private String loadImageFileFromFtp(String imageFilePath, String tenantName, String imageName,
            String imageVersion) {
        // 下载到本地目录
        String localPath =
                XcloudProperties.getConfigMap().get(Global.CI_IMAGE_TEMP_PATH) + tenantName + File.separator + imageName
                        + File.separator + imageVersion;
        // ftp路径信息
        // FtpFilePath ftpFilePath = JSONObject.parseObject(imageFilePath,
        // FtpFilePath.class);
        // ftp tar包完整路径
        // String filePath = ftpFilePath.getFilePath();
        // ftp tar包所在目录
        String filePathPar = imageFilePath.substring(0, imageFilePath.lastIndexOf(FtpUtils.FTP_SEPARATOR));
        // tar包名称
        String fileName = imageFilePath.substring(imageFilePath.lastIndexOf(FtpUtils.FTP_SEPARATOR) + 1);

        try {
            if (FtpUtils.downloadFtpFiles(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                    XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                    XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                    StringUtils.parseInt(XcloudProperties.getConfigMap().get(Global.FTP_PORT)), filePathPar, localPath,
                    new String[] { fileName })) {
                return localPath + File.separator + fileName;
            }
            return null;
        } finally {// 删除ftp上的tar文件
            try {
                FtpUtils.removeDirAndSubFile(XcloudProperties.getConfigMap().get(Global.FTP_HOST),
                        XcloudProperties.getConfigMap().get(Global.FTP_USERNAME),
                        XcloudProperties.getConfigMap().get(Global.FTP_PASSWORD),
                        StringUtils.parseInt(XcloudProperties.getConfigMap().get(Global.FTP_PORT)), filePathPar,
                        new String[] { fileName });
            } catch (Exception e) {
                LOG.error("删除在ftp上传的tar文件失败!", e);
            }
        }

    }

    @Override
    public boolean isImageNameLegal(String imageName) {
        if (!PATTERN.matcher(imageName).matches()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isImageExist(String tenantName, String imageName, String imageVersion) {

        ImageDetail imageDetail = getDetailByTenantNameImageNameAndImageVersoin(tenantName, imageName, imageVersion);

        if (imageDetail.getImage() != null && imageDetail.getImageVersion() != null) {
            // 镜像信息已经存在
            return true;
        }
        // 镜像信息不存在
        return false;
    }

    @Override
    public String getRegistryImageName(String imageVersionId) {
        // 查询镜像信息
        ImageVersion imageVersion = getImageVersionById(imageVersionId);
        if (imageVersion == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "镜像版本信息不存在!");
        }
        Image image = getImageById(imageVersion.getImageId());
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "镜像信息不存在!");
        }

        return getRegistryImageName(image, imageVersion);
    }

    @Override
    public String getRegistryImageName(Image image, ImageVersion imageVersion) {
        String registryImageName =
                dockerService.generateImageName(image, imageVersion.getImageType()) + ":" + imageVersion
                        .getImageVersion();
        return registryImageName;
    }

    @Override
    public String getRegistryImageName(String tenantName, String imageName, String imageVersion, byte imageType) {
        String registryImageName =
                dockerService.generateImageName(tenantName, imageName, imageType) + ":" + imageVersion;
        return registryImageName;
    }

    @Override
    public InspectImageResponse getInspectImageResponse(Image image, ImageVersion imageVersion) {
        HarborUser harborUser = getHarborUserByByTenantName(image.getTenantName());
        if (harborUser == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户harbor信息不存在!");
        }
        DockerClient dockerClient = DockerClientFactory
                .getDockerClientInstance(harborUser.getUsername(), harborUser.getPassword(), harborUser.getEmail());

        // 拉取镜像到本地
        InspectImageResponse imageResponse = null;
        try {
            PullImageResultCallback callback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    super.onNext(item);
                }
            };
            dockerClient.pullImageCmd(dockerService.generateImageName(image, imageVersion.getImageType()))
                    .withTag(imageVersion.getImageVersion()).exec(callback).awaitCompletion();
            imageResponse = dockerClient.inspectImageCmd(getRegistryImageName(image, imageVersion)).exec();
        } catch (Exception e) {
            LOG.error("获取当前镜像详情失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_INSPECT_IMAGE_FAILED, "获取当前镜像详情失败");
        }
        // 删除镜像
        try {
            dockerService.removeImage(imageResponse.getId(), dockerClient);
        } catch (Exception e) {
            LOG.error("删除本地镜像失败", e);
        }
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                LOG.error("关闭dockerClient失败!", e);
            }
        }
        return imageResponse;
    }

    @Override
    public String getImageExposePortsFromInspectRep(InspectImageResponse imageResponse) {
        StringBuffer buffer = new StringBuffer();
        if (imageResponse != null) {
            int countOfExposedPort = 0;
            if (imageResponse.getContainerConfig().getExposedPorts() != null) {
                countOfExposedPort = imageResponse.getContainerConfig().getExposedPorts().length;
            }

            if (countOfExposedPort > 0) {
                ExposedPort[] exposedPorts = imageResponse.getContainerConfig().getExposedPorts();
                for (int i = 0; i < countOfExposedPort; i++) {
                    buffer.append(exposedPorts[i].getPort() + ",");
                }
            } else {
                return "";
            }
        }
        return buffer.substring(0, buffer.length() - 1);
    }

    @Override
    public String getImageExposedPorts(String imageVersionId) {
        // 查询镜像信息
        ImageVersion imageVersion = getImageVersionById(imageVersionId);
        if (imageVersion == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "镜像版本信息不存在!");
        }
        Image image = getImageById(imageVersion.getImageId());
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "镜像信息不存在!");
        }

        return imageVersion.getPorts();
        // InspectImageResponse imageResponse = getInspectImageResponse(image,
        // imageVersion);
        // return getImageExposePortsFromInspectRep(imageResponse);
    }

    /**
     * 根据tar包生成镜像, 上传到仓库, 查询镜像大小, 删除本地镜像
     *
     * @param image         镜像对象
     * @param imageVersion  镜像版本信息
     * @param imageTarFile  tar文件File对象
     * @param imageFilePath tar文件路径 void
     * @date: 2018年12月12日 上午11:42:48
     */
    private void loadPushInspectDelImage(Image image, ImageVersion imageVersion, File imageTarFile,
            String imageFilePath) {
        HarborUser harborUser = getHarborUserByByTenantName(image.getTenantName());
        if (harborUser == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "租户harbor信息不存在!");
        }
        DockerClient dockerClient = DockerClientFactory
                .getDockerClientInstance(harborUser.getUsername(), harborUser.getPassword(), harborUser.getEmail());
        LOG.info("********生成dockerClient: " + dockerClient + "****************");
        String imageId = null;
        boolean isPushSuccess = false;
        try {
            // 加载镜像到本地，获取镜像ID
            imageId = loadImage(image, imageVersion, imageTarFile, imageFilePath, dockerClient);
            LOG.info("****************根据镜像tar文件加载镜像到本地完成******************");
            // 上传到仓库, 如果失败, 删除本地镜像
            dockerService.pushImage(image, imageVersion, dockerClient);
            LOG.info("******************推送镜像到仓库完成******************");
            isPushSuccess = true;
        } finally {
            // 上传到镜像仓库成功后, 就当成上传成功
            if (isPushSuccess) {
                // 查询镜像详情, 设置镜像大小和开放端口号
                InspectImageResponse inspectImageRes = null;
                try {
                    inspectImageRes = dockerService.inspectImage(imageId, dockerClient);
                } catch (ErrorMessageException e) {
                    LOG.info("镜像上传到仓库成功后, 查询镜像详情失败: " + image.getImageName() + ":" + imageVersion.getImageVersion());
                }
                if (inspectImageRes != null) {
                    setImageInspectInfo(image, inspectImageRes);
                }
            }
            // 删除本地镜像(无论镜像上传到仓库是否成功, 都需要删除)
            if (imageId != null) {
                try {
                    dockerService.removeImage(imageId, dockerClient);
                } catch (ErrorMessageException e) {
                    LOG.info("删除本地镜像失败: " + image.getImageName() + ":" + imageVersion.getImageVersion());
                }
            }

            if (dockerClient != null) {
                try {
                    dockerClient.close();
                } catch (IOException e) {
                    LOG.error("关闭dockerClient失败!", e);
                }
            }
        }
    }

    @Override
    public void setImageInspectInfo(Image image, InspectImageResponse inspectImageResponse) {
        if (inspectImageResponse != null) {
            image.setImageSize(inspectImageResponse.getSize() / ImageConstant.IMAGE_SIZE_CONVERSION_VAL);
            image.setPorts(getImageExposePortsFromInspectRep(inspectImageResponse));
        }
    }

    /**
     * 加载tar包, 在本地生成镜像
     *
     * @param image         镜像对象
     * @param imageVersion  镜像版本信息
     * @param imageTarFile  镜像tar文件
     * @param imageFilePath 镜像文件路径
     * @param dockerClient
     * @return String 生成镜像ID
     * @date: 2018年12月13日 上午9:55:37
     */
    private String loadImage(Image image, ImageVersion imageVersion, File imageTarFile, String imageFilePath,
            DockerClient dockerClient) {
        InputStream uploadStream = null;
        try {
            uploadStream = Files.newInputStream(Paths.get(imageFilePath));
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_IMAGE_TAR_OPERATION_FAILED, "镜像上传tar包转换为文件流失败!");
        }
        String imageId = null;

        // 查询tar包中是否有manifest.json文件
        boolean flag = false;
        try {
            flag = FileUtil.visitTAR(imageTarFile, ImageConstant.MANIFEST_FILE);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_IMAGE_TAR_OPERATION_FAILED, "访问tar包内容失败!");
        }

        // 如果有manifest.json, 就是docker export命令导出的tar包
        if (flag) {
            // 解压出manifest.json文件, 从中提取出镜像ID信息
            flag = FileUtil.extTarFileList(imageTarFile, imageTarFile.getParent(), ImageConstant.MANIFEST_FILE);
            if (flag) {
                imageId = FileUtil.getImageId(imageTarFile.getParent(), ImageConstant.MANIFEST_FILE);
                if (imageId == null) {
                    throw new ErrorMessageException(ReturnCode.CODE_IMAGE_TAR_OPERATION_FAILED,
                            "tar包中的manifest.json文件未解析出镜像ID!");
                }
                // 载入镜像, 修改镜像名
                dockerService.loadImage(uploadStream, dockerClient);
                dockerService.tagImage(imageId, image, imageVersion, dockerClient);
            } else {
                throw new ErrorMessageException(ReturnCode.CODE_IMAGE_TAR_OPERATION_FAILED,
                        "解压tar包中的manifest.json文件失败!");
            }
        } else {// 没有manifest.json文件, docker save命令导出的tar包
            // 生成镜像
            imageId = dockerService.createImage(uploadStream, image, imageVersion, dockerClient);
        }
        return imageId;
    }

    /**
     * 生成镜像名称(不带仓库地址)
     *
     * @param imageType
     * @param tenantName
     * @param imageName
     * @return String 镜像名称(公有镜像: 公共镜像仓库名/租户/镜像名 私有镜像: 租户/镜像名)
     * @date: 2019年3月5日 下午6:06:56
     */
    private String generateImageNameTotalWithOutRegistryUrl(byte imageType, String tenantName, String imageName) {
        if (imageType == ImageConstant.IMAGE_TYPE_PUBLIC) {
            return XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/" + tenantName + "/"
                    + imageName;
        }
        return tenantName + "/" + imageName;
    }

    @Override
    public void scanImage(String id) {
        Image image = imageRepository.getById(id);
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像不存在!");
        }

        CountDownLatch latch = new CountDownLatch(1);
        final ApiResult apiResult = new ApiResult();
        ApiCallback<Void> callback = new ApiCallback<Void>() {
            @Override
            public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                LOG.error("镜像扫描启动失败! " + responseHeaders);
                apiResult.setCode(ReturnCode.CODE_HARBOR_SCAN_IMAGE_FAILED);
                latch.countDown();
            }

            @Override
            public void onSuccess(Void result, int statusCode, Map<String, List<String>> responseHeaders) {
                LOG.error("镜像扫描启动成功! ");
                apiResult.setCode(ReturnCode.CODE_SUCCESS);
                latch.countDown();
            }

            @Override
            public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

            }

            @Override
            public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

            }
        };

        // String repoName = generateImageNameWithOutRegistryUrl(image);
        String repoName = "";
        String tag = image.getImageVersion();

        try {
            HarborClientFactory.getProductsApi().repositoriesRepoNameTagsTagScanPostAsync(repoName, tag, callback);
            latch.await();
            if (apiResult.getCode() != ReturnCode.CODE_SUCCESS) {
                throw new ErrorMessageException(ReturnCode.CODE_HARBOR_SCAN_IMAGE_FAILED, "启动镜像扫描失败!");
            }
        } catch (ApiException e1) {
            LOG.error("启动扫描失败!", e1);
            throw new ErrorMessageException(ReturnCode.CODE_HARBOR_SCAN_IMAGE_FAILED, "启动镜像扫描失败!");
        } catch (InterruptedException e1) {
            LOG.error("等待扫描请求发送被中断!", e1);
            throw new ErrorMessageException(ReturnCode.CODE_HARBOR_SCAN_IMAGE_FAILED, "启动镜像扫描失败!");
        }
    }

    @Override
    public List<VulnerabilityItem> getHarborImageScanResult(String imageId) {
        Image image = imageRepository.getById(imageId);
        if (image == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "镜像不存在!");
        }

        ProductsApi productsApi = HarborClientFactory.getProductsApi();

        // String repoName = generateImageNameWithOutRegistryUrl(image);
        String repoName = "";
        String tag = image.getImageVersion();
        try {
            DetailedTag detail = productsApi.repositoriesRepoNameTagsTagGet(repoName, tag);
            if (!ImageConstant.HARBOR_SCAN_IMAGE_STATUS_FINISHED.equals(detail.getScanOverview().getScanStatus())) {
                throw new ErrorMessageException(ReturnCode.CODE_HARBOR_GET_IMAGE_SCAN_RESULT_FAILED,
                        "镜像扫描还未完成, 请等待扫描结束!");
            }
            return productsApi.repositoriesRepoNameTagsTagVulnerabilityDetailsGet(repoName, tag);
        } catch (ApiException e) {
            LOG.error("获取镜像安全扫描信息失败!");
            throw new ErrorMessageException(ReturnCode.CODE_HARBOR_GET_IMAGE_SCAN_RESULT_FAILED, "获取镜像扫描结果失败!");
        }
    }

    @Override
    public List<Image> getImagesByTenantNameAndImageName(String tenantName, String imageName) {
        try {
            return imageRepository.findByTenantNameAndImageName(tenantName, imageName);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库根据镜像名查询镜像失败!");
        }
    }

    @Override
    public List<ImageVersion> findImageVersionsByImageIdAndImageVersion(String imageId, String imageVersion) {
        try {
            return imageVersionRepository.findByImageIdAndImageVersion(imageId, imageVersion);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库根据镜像ID和镜像版本查询镜像失败!");
        }
    }

    @Override
    public Page<ImageVersion> getImageVersionsByImageId(String imageId, Pageable pageable) {
        try {
            return imageVersionRepository.findByImageId(imageId, pageable);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库根据镜像ID查询镜像版本信息失败!");
        }
    }

    @Override
    public Page<ImageVersion> getPublicImageVersionsByImageId(String imageId, Pageable pageable) {
        try {
            return imageVersionRepository.findByImageIdAndImageType(imageId, ImageConstant.IMAGE_TYPE_PUBLIC, pageable);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库根据镜像ID查询镜像版本信息失败!");
        }
    }

    @Override
    public Long getPublicImageVersionNumByImageId(String imageId) {
        try {
            return imageVersionRepository.countByImageIdAndImageType(imageId, ImageConstant.IMAGE_TYPE_PUBLIC);
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库根据镜像ID查询镜像版本信息失败!");
        }
    }

    @Override
    public ImageDetail getDetailByTenantNameImageNameAndImageVersoin(String tenantName, String imageName,
            String imageVersion) {
        ImageDetail imageDetail = new ImageDetail();
        List<Image> images = getImagesByTenantNameAndImageName(tenantName, imageName);
        Image image = images.isEmpty() ? null : images.get(0);
        imageDetail.setImage(image);
        if (image != null) {
            List<ImageVersion> imageVersions = findImageVersionsByImageIdAndImageVersion(image.getId(), imageVersion);
            imageDetail.setImageVersion(imageVersions.isEmpty() ? null : imageVersions.get(0));
        }

        return imageDetail;
    }

    @Override
    public ImageVersion saveImageVersion(ImageVersion imageVersion) {
        try {
            return imageVersionRepository.save(imageVersion);
        } catch (Exception e) {
            LOG.error("保存镜像信息失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存镜像版本信息失败!");
        }
    }

    @Override
    public Image saveImage(Image image) {
        try {
            return imageRepository.save(image);
        } catch (Exception e) {
            LOG.error("保存镜像信息失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存镜像信息失败!");
        }
    }

    @Override
    public Collection<ImageQualityStatistics> getImageQualityStatistics() {
        // 1，查使用中的镜像
        List<Map<String, Object>> imagesUsed = null;
        try {
            imagesUsed = imageRepository.getImagesUsed();
        } catch (Exception e) {
            LOG.error("查询被服务使用的镜像错误", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询使用中的镜像错误！");
        }
        // 如果没有被使用的镜像（没有服务）
        if (imagesUsed.isEmpty()) {
            return new ArrayList<>(0);
        }

        // 2，根据使用中的镜像设置信息
        // 2.1 被使用的镜像使用了哪些代码库
        Map<String, List<ImageQualityStatistics>> codeBaseNameMap = getCodeBaseNameUsedMap(imagesUsed);
        // 2.2租户，镜像名，具体信息（一个名称的镜像可能被多个服务使用）
        Table<String, String, ImageQualityStatistics> table = getImageTable(imagesUsed);
        // 2.3使用的镜像名，租户名
        Set<String> tenantNameSet = table.rowKeySet();
        Set<String> imageNameSet = table.columnKeySet();

        // 构建次数统计信息（今天）
        List<Map<String, Object>> ciStatisticsToday = ciService.getCiStatisticsToday(tenantNameSet, imageNameSet);
        // 最新代码检查信息查询
        List<Map<String, Object>> codeCheckRes = sonarService.getCodeCheckLatestResult(codeBaseNameMap.keySet());
        // 构建次数统计信息（所有）
        List<Map<String, Object>> ciStatistics = ciService.getCiStatistics(tenantNameSet, imageNameSet);
        // 设置构建统计结果
        ciStatisticsToday.forEach((ciStatistic) -> {
            String imageName = String.valueOf(ciStatistic.get("imageName"));
            String tenantName = String.valueOf(ciStatistic.get("tenantName"));
            ImageQualityStatistics statistic = table.get(tenantName, imageName);

            // 查询的信息为没用的
            if (statistic == null) {
                return;
            }
            statistic.setConstructionTotal(Integer.valueOf(String.valueOf(ciStatistic.get("constructionTotal"))));
            statistic.setConstructionOkTotal(Integer.valueOf(String.valueOf(ciStatistic.get("constructionOkTotal"))));
            statistic.setConstructionFailTotal(
                    Integer.valueOf(String.valueOf(ciStatistic.get("constructionFailTotal"))));
            statistic.setConstructionOkRate(String.valueOf(ciStatistic.get("constructionOkRate")));
        });

        // 设置构建统计结果
        ciStatistics.forEach((ciStatistic) -> {
            String imageName = String.valueOf(ciStatistic.get("imageName"));
            String tenantName = String.valueOf(ciStatistic.get("tenantName"));
            ImageQualityStatistics statistic = table.get(tenantName, imageName);

            // 查询的信息为没用的
            if (statistic == null) {
                return;
            }
            statistic.setConstructionTotalAll(Integer.valueOf(String.valueOf(ciStatistic.get("constructionTotal"))));
            statistic
                    .setConstructionOkTotalAll(Integer.valueOf(String.valueOf(ciStatistic.get("constructionOkTotal"))));
            statistic.setConstructionFailTotalAll(
                    Integer.valueOf(String.valueOf(ciStatistic.get("constructionFailTotal"))));
            statistic.setConstructionOkRateAll(String.valueOf(ciStatistic.get("constructionOkRate")));
        });

        // 设置代码检查统计结果
        codeCheckRes.forEach((codeCheck) -> {
            String codeBaseName = String.valueOf(codeCheck.get("codeBaseName"));
            List<ImageQualityStatistics> statisList = codeBaseNameMap.get(codeBaseName);
            if (statisList == null) {
                return;
            }
            statisList.forEach((statis) -> {
                List<Map<String, Object>> codeCheckInfos = table.get(statis.getTenantName(), statis.getImageName())
                        .getCodeCheckInfos();
                if (codeCheckInfos == null) {
                    codeCheckInfos = new ArrayList<>();
                    table.get(statis.getTenantName(), statis.getImageName()).setCodeCheckInfos(codeCheckInfos);
                }
                codeCheckInfos.add(codeCheck);
            });
        });

        return table.values();
    }

    /**
     * 获取被使用的镜像使用了哪些代码库
     *
     * @param imagesUsed
     * @return Map<String, List < ImageQualityStatistics>>
     * @date: 2019年8月22日 上午9:44:54
     */
    private Map<String, List<ImageQualityStatistics>> getCodeBaseNameUsedMap(List<Map<String, Object>> imagesUsed) {
        // 代码信息；租户信息；租户下的镜像
        Map<String, List<ImageQualityStatistics>> map = new HashMap<>();
        for (Map<String, Object> imageInfo : imagesUsed) {
            String codeBaseName = String.valueOf(imageInfo.get("codeBaseName"));
            String imageName = String.valueOf(imageInfo.get("imageName"));
            String tenantName = String.valueOf(imageInfo.get("tenantName"));

            if (isEmpty(codeBaseName)) {
                continue;
            }

            if (!map.containsKey(codeBaseName)) {
                List<ImageQualityStatistics> list = new ArrayList<>();
                list.add(new ImageQualityStatistics(tenantName, imageName));
                map.put(codeBaseName, list);
            } else {
                map.get(codeBaseName).add(new ImageQualityStatistics(tenantName, imageName));
            }

        }
        return map;
    }

    /**
     * @param str
     * @return boolean
     * @date: 2019年8月20日 下午4:39:41
     */
    private boolean isEmpty(String str) {
        return StringUtils.isEmpty(str) || "null".equals(str);
    }

    /**
     * 获取租户->镜像名->具体信息 的table信息
     *
     * @param imagesUsed
     * @return Table<String, String, ImageQualityStatistics>
     * @date: 2019年8月22日 上午9:47:13
     */
    private Table<String, String, ImageQualityStatistics> getImageTable(List<Map<String, Object>> imagesUsed) {
        Table<String, String, ImageQualityStatistics> table = HashBasedTable.create();

        for (Map<String, Object> imageInfo : imagesUsed) {
            String imageName = String.valueOf(imageInfo.get("imageName"));
            String tenantName = String.valueOf(imageInfo.get("tenantName"));

            if (!table.containsRow(tenantName) || !table.contains(tenantName, imageName)) {
                // 新租户或新的镜像名字
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(imageInfo);
                ImageQualityStatistics statistics = new ImageQualityStatistics(tenantName, imageName, list);
                table.put(tenantName, imageName, statistics);
            } else {
                // 租户名和镜像名都是存在的，说明是多个服务共用一个名称的镜像
                table.get(tenantName, imageName).getServices().add(imageInfo);
            }

        }
        return table;
    }
}
