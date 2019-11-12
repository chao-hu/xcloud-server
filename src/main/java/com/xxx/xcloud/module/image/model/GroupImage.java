package com.xxx.xcloud.module.image.model;

import com.xxx.xcloud.module.image.entity.Image;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xjp
 * @Description: 镜像根据名称分组后, 一组同名镜像的信息
 * @date: 2019年11月13日
 */
@Data
public class GroupImage {
    /**
     * 镜像名称
     */
    private String imageName;
    /**
     * 所有镜像
     */
    private List<Image> images;

    public GroupImage(String imageName, Image image) {
        this.imageName = imageName;
        images = new ArrayList<Image>();
        images.add(image);
    }

}
