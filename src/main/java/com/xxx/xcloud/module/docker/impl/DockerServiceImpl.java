package com.xxx.xcloud.module.docker.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.entity.CiRecord;
import com.xxx.xcloud.module.ci.service.ICiService;
import com.xxx.xcloud.module.docker.DockerService;
import com.xxx.xcloud.module.image.consts.ImageConstant;
import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import com.xxx.xcloud.module.image.model.ImageDetail;
import com.xxx.xcloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * 
 * @author mengaijun
 * @Description: docker镜像操作
 * @date: 2018年12月7日 下午5:57:55
 */
@Service
public class DockerServiceImpl implements DockerService {

    private static final String STR_NULL_LOWERCASE = "null";

    @Autowired
    private ICiService ciService;

    private static final Logger LOG = LoggerFactory.getLogger(DockerServiceImpl.class);

    @Override
    public String generateImageName(Image image, byte imageType) {
        if (imageType == ImageConstant.IMAGE_TYPE_PUBLIC) {
            if (StringUtils.isEmpty(image.getTenantName())) {
                return XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/"
                        + XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/"
                        + image.getImageName();
            }
            return XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/"
                    + XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/" + image.getTenantName()
                    + "/" + image.getImageName();
        }
        return XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/" + image.getTenantName() + "/"
                + image.getImageName();
    }

    @Override
    public String generateImageName(String tenantName, String imageName, byte imageType) {
        if (imageType == ImageConstant.IMAGE_TYPE_PUBLIC) {
            if (StringUtils.isEmpty(tenantName)) {
                return XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/"
                        + XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/" + imageName;
            }
            return XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/"
                    + XcloudProperties.getConfigMap().get(Global.HARBOR_PUBLIC_PROJECT_NAME) + "/" + tenantName + "/"
                    + imageName;
        }
        return XcloudProperties.getConfigMap().get(Global.HARBOR_REGISTRY_ADDRESS) + "/" + tenantName + "/" + imageName;
    }

    @Override
    public void loadImage(InputStream inputStream, DockerClient dockerClient) {
        try {
            dockerClient.loadImageCmd(inputStream).exec();
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_LOAD_IMAGE_FAILED, "根据tar包导入镜像失败!");
        }
    }

    @Override
    public void tagImage(String imageId, Image image, ImageVersion imageVersion, DockerClient dockerClient) {
        try {
            dockerClient.tagImageCmd(imageId, generateImageName(image, imageVersion.getImageType()),
                    imageVersion.getImageVersion()).withForce().exec();
        } catch (Exception e) {
            LOG.error("tag镜像错误: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_TAG_IMAGE_FAILED, "修改镜像名称失败!");
        }

    }

    @Override
    public void pushImage(Image image, ImageVersion imageVersion, DockerClient dockerClient) {
        try {
            PushImageResultCallback callback = new PushImageResultCallback() {
                @Override
                public void onNext(PushResponseItem item) {
                    super.onNext(item);
                }
            };
            dockerClient.pushImageCmd(generateImageName(image, imageVersion.getImageType()))
                    .withTag(imageVersion.getImageVersion()).exec(callback).awaitSuccess();
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_PUSH_IMAGE_FAILED, "上传镜像到仓库失败!");
        }
    }

    @Override
    public String createImage(InputStream inputStream, Image image, ImageVersion imageVersion,
            DockerClient dockerClient) {
        try {
            return dockerClient.createImageCmd(
                    generateImageName(image, imageVersion.getImageType()) + ":" + imageVersion.getImageVersion(),
                    inputStream).exec().getId();
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_CREATE_IMAGE_FAILED, "根据tar包生成镜像错误");
        }
    }

    @Override
    public String buildImage(String dockerfilePath, final CiRecord ciRecord, DockerClient dockerClient) {
        File dockerfileFolder = new File(dockerfilePath);
        // 根据文件内容构建镜像
        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                if (item != null && item.getStream() != null) {
                    if (!item.getStream().contains(STR_NULL_LOWERCASE)) {
                        ciService.addLogPrint(ciRecord, item.getStream());
                    }
                }
                super.onNext(item);
            }
        };
        return dockerClient.buildImageCmd(dockerfileFolder).exec(callback).awaitImageId();
    }

    @Override
    public boolean pushImage(ImageDetail imageDetail, final CiRecord ciRecord, DockerClient dockerClient) {
        PushImageResultCallback callback = new PushImageResultCallback() {
            @Override
            public void onNext(PushResponseItem item) {
                if (item != null && item.getStream() != null) {
                    if (!item.getStream().contains(STR_NULL_LOWERCASE)) {
                        ciService.addLogPrint(ciRecord, item.getStream());
                    }
                }
                super.onNext(item);
            }
        };
        dockerClient
                .pushImageCmd(generateImageName(imageDetail.getImage(), imageDetail.getImageVersion().getImageType()))
                .withTag(imageDetail.getImageVersion().getImageVersion()).exec(callback).awaitSuccess();
        return true;
    }

    @Override
    public void removeImage(String imageId, DockerClient dockerClient) {
        try {
            dockerClient.removeImageCmd(imageId).withForce(true).exec();
        } catch (Exception e) {
            LOG.error("docker remove失败, 错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_DELETE_IMAGE_FAILED, "删除镜像失败");
        }
    }

    @Override
    public InspectImageResponse inspectImage(String imageId, DockerClient dockerClient) {
        try {
            return dockerClient.inspectImageCmd(imageId).exec();
        } catch (Exception e) {
            LOG.error("错误信息: " + e.getMessage(), e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_INSPECT_IMAGE_FAILED, "获取镜像详情失败!");
        }
    }

    @Override
    public InspectImageResponse pullImage(Image image, ImageVersion imageVersion, DockerClient dockerClient) {
        String imageNameTotal = generateImageName(image, imageVersion.getImageType());
        try {
            PullImageResultCallback callback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    super.onNext(item);
                }
            };

            LOG.info("开始拉取镜像" + imageNameTotal);
            dockerClient.pullImageCmd(imageNameTotal).withTag(imageVersion.getImageVersion()).exec(callback)
                    .awaitCompletion();
        } catch (Exception e) {
            LOG.error("拉取镜像失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_INSPECT_IMAGE_FAILED, "拉取镜像失败");
        }

        try {
            LOG.info("开始inspect镜像" + imageNameTotal);
            return dockerClient.inspectImageCmd(imageNameTotal + ":" + imageVersion.getImageVersion()).exec();
        } catch (Exception e) {
            LOG.error("查询镜像详情失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_DOCKER_INSPECT_IMAGE_FAILED, "查询镜像详情失败" + e.getMessage());
        }
    }
}
