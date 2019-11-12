package com.xxx.xcloud.module.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.xxx.xcloud.module.ci.entity.CiRecord;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import com.xxx.xcloud.module.image.model.ImageDetail;

import java.io.InputStream;

/**
 * @author mengaijun
 * @Description: 封装Docker接口
 * @date: 2018年12月7日 下午5:56:35
 */
public interface DockerService {

    /**
     * 根据镜像tar包文件流导入镜像(docker export命令导出的jar包)
     *
     * @param inputStream
     * @param dockerClient
     * @date: 2018年12月10日 下午3:11:12
     */
    void loadImage(InputStream inputStream, DockerClient dockerClient);

    /**
     * 修改id为imageId的镜像的名称
     *
     * @param imageId      镜像ID
     * @param image        镜像信息
     * @param imageVersion 镜像版本信息
     * @param dockerClient
     * @date: 2018年12月10日 下午3:15:48
     */
    void tagImage(String imageId, Image image, ImageVersion imageVersion, DockerClient dockerClient);

    /**
     * 上传镜像到仓库
     *
     * @param image
     * @param imageVersion
     * @param dockerClient
     * @date: 2018年12月10日 下午3:25:17
     */
    void pushImage(Image image, ImageVersion imageVersion, DockerClient dockerClient);

    /**
     * 根据镜像tar包文件流导入镜像(docker save命令导出的jar包)
     *
     * @param inputStream
     * @param image
     * @param imageVersion
     * @param dockerClient
     * @return String 镜像ID
     * @date: 2018年12月10日 下午3:36:46
     */
    String createImage(InputStream inputStream, Image image, ImageVersion imageVersion, DockerClient dockerClient);

    /**
     * 生成镜像名称(根据仓库地址, 租户, 镜像名拼接)
     *
     * @param image     镜像对象
     * @param imageType 镜像类型
     * @return String 镜像标签
     * @date: 2018年12月10日 上午11:48:16
     */
    String generateImageName(Image image, byte imageType);

    /**
     * 生成镜像名(带harbor地址)
     *
     * @param tenantName
     * @param imageName
     * @param imageType
     * @return String
     * @date: 2019年4月16日 上午9:41:28
     */
    String generateImageName(String tenantName, String imageName, byte imageType);

    /**
     * 根据dockerfile构建镜像
     *
     * @param dockerfilePath dockerfile文件路径
     * @param ciRecord       本次构建日志记录
     * @param dockerClient
     * @return String 镜像ID
     * @date: 2018年12月10日 上午11:35:54
     */
    String buildImage(String dockerfilePath, final CiRecord ciRecord, DockerClient dockerClient);

    /**
     * 上传镜像到仓库
     *
     * @param imageDetail  镜像
     * @param ciRecord     本次构建日志记录
     * @param dockerClient
     * @return boolean 是否成功
     * @date: 2018年12月10日 下午2:06:50
     */
    boolean pushImage(ImageDetail imageDetail, final CiRecord ciRecord, DockerClient dockerClient);

    /**
     * 移除镜像
     *
     * @param imageId
     * @param dockerClient
     * @date: 2018年12月10日 下午3:58:20
     */
    void removeImage(String imageId, DockerClient dockerClient);

    /**
     * 查看镜像详情
     *
     * @param imageId
     * @param dockerClient
     * @return InspectImageResponse
     * @date: 2018年12月11日 上午10:09:48
     */
    InspectImageResponse inspectImage(String imageId, DockerClient dockerClient);

    /**
     * 拉取镜像
     *
     * @param image
     * @param imageVersion
     * @param dockerClient
     * @return {@link InspectImageResponse} 返回镜像详情
     * @date: 2019年3月4日 下午2:52:39
     */
    InspectImageResponse pullImage(Image image, ImageVersion imageVersion, DockerClient dockerClient);
}
