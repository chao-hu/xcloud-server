package com.xxx.xcloud.module.image.consts;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * @author xjp
 * @Description:
 * @date: 2019年11月10日
 */
public class ImageConstant {
    /**
     * 镜像类型 ： 1公有镜像2私有镜像
     */
    public static final Byte IMAGE_TYPE_PUBLIC = 1;
    public static final Byte IMAGE_TYPE_PRIVATE = 2;

    /**
     * 镜像构建类型: 0:镜像上传 1:代码构建 2:dockerfile构建
     */
    public static final byte IMAGE_CI_TYPE_UPLOAD = 0;
    public static final byte IMAGE_CI_TYPE_CODECI = 1;
    public static final byte IMAGE_CI_TYPE_DOCKERFILE = 2;

    /**
     * 镜像是否删除标记
     */
    public static final Byte IMAGE_DELETE_FLAG = 1;
    public static final Byte IMAGE_NOT_DELETE_FLAG = 0;

    /**
     * docker export命令导出tar包中的信息文件
     */
    public static final String MANIFEST_FILE = "manifest.json";

    /**
     * 1000 * 1000 镜像大小换算成 M, 的被除数
     */
    public static final Double IMAGE_SIZE_CONVERSION_VAL = 1000000.0;

    /**
     * 镜像名称验证正则表达式
     */
    public static final String IMAGE_NAME_REGEX_STR = "^[a-z][a-z0-9-_]*$";

    public static final String FILE_END_TAR = "tar";

    public static final String HARBOR_SCAN_IMAGE_STATUS_FINISHED = "finished";

    public static final String IMAGE_OPERATOR_ADD = "add";

    public static final String IMAGE_OPERATOR_DEL = "del";

    public static final String DESC_FIELD_CREATE_TIME = "createTime";

    public static final Sort SORT_DEFAULT = Sort.by(Direction.DESC, DESC_FIELD_CREATE_TIME);

}
