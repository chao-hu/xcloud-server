package com.xxx.xcloud.client.github.model;

import com.google.gson.annotations.SerializedName;

/**
 * 仓库
 *
 * @author mengaijun
 * @date: 2019年1月2日 下午5:59:39
 */
public class GithubRepos {
    private String name;

    @SerializedName("clone_url")
    private String cloneUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

}
