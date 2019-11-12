package com.xxx.xcloud.module.image.service;

import com.bonc.bdos.harbor.client.model.DetailedTag;
import com.bonc.bdos.harbor.client.model.VulnerabilityItem;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.xxx.xcloud.module.harbor.entity.HarborUser;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.model.ImageQualityStatistics;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import com.xxx.xcloud.module.image.model.GroupImage;
import com.xxx.xcloud.module.image.model.ImageDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

/**
 * @author mengaijun
 */
public interface ImageService {
    /**
     * 修改镜像公有私有信息
     *
     * @param imageVersionid 镜像版本ID
     * @param tenantName
     * @param imageType      镜像类型 1公有2私有
     * @return boolean
     * @date: 2019年3月4日 下午2:45:31
     */
    boolean modifyImageType(String imageVersionid, String tenantName, byte imageType);

    /**
     * 修改镜像环境变量
     *
     * @param imageVersionid 镜像版本ID
     * @param tenantName
     * @param envVariables   镜像环境变量
     * @return boolean
     * @date: 2019年3月4日 下午2:45:31
     */
    boolean modifyImageEnv(String imageVersionid, String tenantName, String envVariables);

    /**
     * 修改镜像下所有版本的描述
     *
     * @param imageId
     * @param description
     * @date: 2019年3月8日 下午2:29:12
     */
    void modifyImageDescription(String imageId, String description);

    /**
     * 获取镜像列表
     *
     * @param tenantName
     * @param imageName  为空, 查询所有; 不为空, 模糊查询
     * @param projectId
     * @param pageable   分页信息
     * @return Page<Image>
     * @date: 2018年12月3日 下午3:59:43
     */
    Page<Image> getImages(String tenantName, String imageName, String projectId, Pageable pageable);

    /**
     * 获取公共镜像列表
     *
     * @param imageName 为空, 查询所有; 不为空, 模糊查询
     * @param projectId
     * @param pageable  分页信息
     * @return Page<Image>
     * @date: 2018年12月3日 下午3:59:43
     */
    Page<Image> getPublicImages(String imageName, String projectId, Pageable pageable);

    /**
     * 根据镜像版本ID获取镜像详情
     *
     * @param imageVersionid
     * @return {@link ImageDetail}
     * @date: 2018年12月10日 下午4:58:20
     */
    ImageDetail getDetailByImageVersionId(String imageVersionid);

    /**
     * 根据名称获取所有镜像列表
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @return List<Image>
     * @date: 2018年12月3日 下午4:03:04
     */
    List<Image> getImagesByName(String tenantName, String imageName, String projectId);

    /**
     * 根据名称获取所有镜像列表
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @param pageable
     * @return Page<Image>
     * @date: 2018年12月3日 下午4:03:04
     */
    // Page<Image> getImagesByName(String tenantName, String imageName, String
    // projectId, Pageable pageable);

    /**
     * 删除镜像
     *
     * @param imageVersionId 镜像版本ID
     * @return boolean
     * @date: 2018年12月3日 下午4:05:30
     */
    boolean deleteImage(String imageVersionId);

    /**
     * 上传镜像
     *
     * @param tenantName    租户名
     * @param imageType     镜像类型 1公用2私有
     * @param imageName     镜像名称
     * @param description   描述
     * @param imageVersion  版本
     * @param imageFilePath 上传文件路径
     * @param createdBy
     * @param projectId
     * @return Image
     * @date: 2018年12月3日 下午4:09:10
     */
    Image uploadImage(String tenantName, Byte imageType, String imageName, String description, String imageVersion,
            String imageFilePath, String createdBy, String projectId);

    /**
     * 判断镜像是否存在, 已经存在的话
     *
     * @param tenantName
     * @param imageName
     * @param imageVersion
     * @return boolean true:存在 false:不存在
     * @date: 2018年12月12日 下午7:28:19
     */
    boolean isImageExist(String tenantName, String imageName, String imageVersion);

    /**
     * 判断镜像名称是否合法
     *
     * @param imageName 镜像名称
     * @return boolean 是否合法 true:合法 false:不合法
     * @date: 2018年12月12日 下午7:25:40
     */
    boolean isImageNameLegal(String imageName);

    /**
     * 获取harbor镜像
     *
     * @param image        镜像
     * @param imageVersion 镜像版本
     * @return DetailedTag harbor镜像详情
     * @date: 2018年12月24日 上午9:28:27
     */
    DetailedTag getHarborImageDetail(Image image, ImageVersion imageVersion);

    /**
     * 判断Harbor镜像是否存在
     *
     * @param imageVersionId 镜像版本信息ID
     * @return boolean
     * @date: 2018年12月24日 上午11:15:32
     */
    boolean isHarborImageExist(String imageVersionId);

    /**
     * <p>
     * Description: 获取当前镜像的完整路径
     * </p>
     *
     * @param imageVersionId 镜像版本id
     * @return
     */
    String getRegistryImageName(String imageVersionId);

    /**
     * Description: 获取当前镜像的完整路径
     *
     * @param image
     * @param imageVersion
     * @return String
     * @date: 2019年3月25日 下午3:51:25
     */
    String getRegistryImageName(Image image, ImageVersion imageVersion);

    /**
     * 拼接镜像完成路径
     *
     * @param tenantName
     * @param imageName
     * @param imageVersion
     * @param imageType
     * @return String
     * @date: 2019年4月16日 上午9:49:43
     */
    String getRegistryImageName(String tenantName, String imageName, String imageVersion, byte imageType);

    /**
     * <p>
     * Description: 获取当前镜像暴露的端口号
     * </p>
     *
     * @param imageVersionId 镜像id
     * @return String 以半英逗号分隔的端口号
     */
    String getImageExposedPorts(String imageVersionId);

    /**
     * 从inspect镜像信息中获取开放端口
     *
     * @param imageResponse inspect命令得到的镜像信息
     * @return String
     * @date: 2019年1月16日 下午7:08:33
     */
    String getImageExposePortsFromInspectRep(InspectImageResponse imageResponse);

    /**
     * 根据镜像信息获取详情(先拉取镜像, 再inspect查询, 在删除镜像)
     *
     * @param image        镜像信息
     * @param imageVersion 镜像版本信息
     * @return InspectImageResponse
     * @date: 2019年1月16日 下午7:15:46
     */
    InspectImageResponse getInspectImageResponse(Image image, ImageVersion imageVersion);

    /**
     * 根据inspect命令查询镜像得到的信息 设置镜像信息(镜像大小和开放端口)
     *
     * @param image                镜像信息
     * @param inspectImageResponse inspect镜像信息
     * @date: 2019年1月17日 上午10:01:07
     */
    void setImageInspectInfo(Image image, InspectImageResponse inspectImageResponse);

    /**
     * 根据租户名查询harbor用户信息
     *
     * @param tenantName
     * @return HarborUser
     * @date: 2019年1月24日 下午6:04:24
     */
    HarborUser getHarborUserByByTenantName(String tenantName);

    /**
     * 安全扫描镜像
     *
     * @param id
     * @date: 2019年3月11日 上午11:12:35
     */
    void scanImage(String id);

    /**
     * 获取镜像安全扫描结果
     *
     * @param imageId 镜像ID
     * @return List<VulnerabilityItem>
     * @date: 2019年3月11日 下午4:45:23
     */
    List<VulnerabilityItem> getHarborImageScanResult(String imageId);

    /**
     * 将镜像信息根据镜像名称分组镜像, 将一页数据返回
     *
     * @param images   镜像信息
     * @param pageable
     * @return List<GroupImage>
     * @date: 2018年12月13日 上午11:28:17
     */
    Page<GroupImage> conversionImageGroupPage(List<Image> images, Pageable pageable);

    /**
     * 根据租户名和镜像名称查询镜像
     *
     * @param tenantName
     * @param imageName  镜像名
     * @return List<Image> 镜像列表
     * @date: 2019年3月22日 下午5:11:28
     */
    List<Image> getImagesByTenantNameAndImageName(String tenantName, String imageName);

    /**
     * 根据镜像ID和镜像版本查询版本信息
     *
     * @param imageId
     * @param imageVersion
     * @return List<ImageVersion>
     * @date: 2019年3月22日 下午5:23:56
     */
    List<ImageVersion> findImageVersionsByImageIdAndImageVersion(String imageId, String imageVersion);

    /**
     * 根据镜像名称和版本查询镜像信息
     *
     * @param tenantName
     * @param imageName
     * @param imageVersion
     * @return ImageDetail
     * @date: 2019年3月22日 下午5:33:25
     */
    ImageDetail getDetailByTenantNameImageNameAndImageVersoin(String tenantName, String imageName, String imageVersion);

    /**
     * 保存Image对象
     *
     * @param image
     * @return Image
     * @date: 2019年3月22日 下午6:24:29
     */
    Image saveImage(Image image);

    /**
     * 保存ImageVersion对象
     *
     * @param imageVersion
     * @return ImageVersion
     * @date: 2019年3月22日 下午6:25:00
     */
    ImageVersion saveImageVersion(ImageVersion imageVersion);

    /**
     * 同步更新镜像信息
     *
     * @param image
     * @param imageVersion
     * @param operator
     * @return Image
     * @date: 2019年3月25日 下午7:06:32
     */
    Image updateImageVersionCountSync(Image image, ImageVersion imageVersion, String operator);

    /**
     * 根据镜像ID获取镜像信息
     *
     * @param imageId
     * @return Image
     * @date: 2019年3月26日 下午4:53:56
     */
    Image getImageById(String imageId);

    /**
     * 根据镜像ID获取对应版本
     *
     * @param imageId
     * @param pageable
     * @return Page<ImageVersion>
     * @date: 2019年3月26日 下午5:03:06
     */
    Page<ImageVersion> getImageVersionsByImageId(String imageId, Pageable pageable);

    /**
     * 根据镜像ID获取对应公共版本
     *
     * @param imageId
     * @param pageable
     * @return Page<ImageVersion>
     * @date: 2019年3月26日 下午5:03:06
     */
    Page<ImageVersion> getPublicImageVersionsByImageId(String imageId, Pageable pageable);

    /**
     * 获取镜像的版本的数量
     *
     * @param imageId
     * @return int
     * @date: 2019年3月26日 下午5:36:14
     */
    Long getPublicImageVersionNumByImageId(String imageId);

    /**
     * 获取租户的镜像或公有镜像
     *
     * @param tenantName
     * @param imageName
     * @param projectId
     * @param pageable
     * @return Page<Image>
     * @date: 2019年5月7日 上午9:57:42
     */
    Page<Image> getOwnAndPublicImages(String tenantName, String imageName, String projectId, Pageable pageable);

    /**
     * 在一个事务中新增镜像和版本（PROPAGATION_REQUIRES_NEW方式）
     *
     * @param image
     * @param imageVersion
     * @return Image
     * @date: 2019年7月2日 下午3:16:50
     */
    Image addImageAndVersionTrans(Image image, ImageVersion imageVersion);

    /**
     * 在一个事务里删除镜像的版本信息（PROPAGATION_REQUIRES_NEW方式）
     *
     * @param image
     * @param imageVersion
     * @return Image
     * @date: 2019年7月2日 下午3:19:52
     */
    Image deleteImageAndVersionTrans(Image image, ImageVersion imageVersion);

    /**
     * 镜像质量统计信息
     *
     * @return Collection<ImageQualityStatistics>
     * @date: 2019年8月20日 下午3:06:57
     */
    Collection<ImageQualityStatistics> getImageQualityStatistics();
}
