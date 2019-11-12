package com.xxx.xcloud.module.image.model;

import com.xxx.xcloud.module.image.entity.Image;
import com.xxx.xcloud.module.image.entity.ImageVersion;
import lombok.Data;
import org.springframework.data.domain.Page;

/**
 * 镜像详情(包含镜像信息和版本信息)
 *
 * @author xjp
 * @Description:
 * @date: 2019年11月10日
 */
@Data
public class ImageDetail {
    /**
     * 镜像信息
     */
    public Image image;

    /**
     * 版本信息
     */
    public ImageVersion imageVersion;

    /**
     * 所有版本信息
     */
    public Page<ImageVersion> imageVersions;

}
