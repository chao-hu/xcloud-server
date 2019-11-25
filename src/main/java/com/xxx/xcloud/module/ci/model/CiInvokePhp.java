package com.xxx.xcloud.module.ci.model;

import lombok.Data;

/**
 * 
 * @author mengaijun
 * @date: 2019年5月30日 上午11:13:39
 */
@Data
public class CiInvokePhp {
    /**
     * phing版本
     */
    private String phingVersion;

    /**
     * 目标命令
     */
    private String targets;

    /**
     * build文件
     */
    private String phingBuildFile;

}
