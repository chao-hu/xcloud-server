package com.xxx.xcloud.module.ci.model;

/**
 * 语言版本路径信息
 *
 * @author Administrator
 */
public class Lang {
    private String version;

    private String path;

    /**
     * 1:java    2:go   3:python
     */
    private String type;

    public Lang() {

    }

    public Lang(String version, String path, String type) {
        this.version = version;
        this.path = path;
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
