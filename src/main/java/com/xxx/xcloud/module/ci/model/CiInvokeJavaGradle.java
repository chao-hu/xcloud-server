package com.xxx.xcloud.module.ci.model;

import lombok.Data;

/**
 * gradle信息
 *
 * @author mengaijun
 * @date: 2019年8月6日 上午11:06:50
 */
@Data
public class CiInvokeJavaGradle {
    /**
     * 命令
     */
    private String tasks;

    /**
     * jenkins上的gradle版本
     */
    private String gradleName;

    /**
     * false：选用invoke gradle（使用jenkins配置的gradle）
     * true：Use Gradle Wrapper，使用项目自带的gradle
     */
    private Boolean useWrapper;

    /**
     * 是否使用gradlew工具
     */
    private Boolean makeExecutable;

    /**
     * gradle在项目的地址
     */
    private String wrapperLocation;

    private String switchs;

    private String rootBuildScriptDir;

    private String buildFile;

    private Boolean useWorkspaceAsHome;

    private String systemProperties;

    private Boolean passAllAsSystemProperties;

    private String projectProperties;

    private Boolean passAllAsProjectProperties;

}
