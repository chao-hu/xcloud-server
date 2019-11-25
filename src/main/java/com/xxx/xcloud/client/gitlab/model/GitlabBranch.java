package com.xxx.xcloud.client.gitlab.model;

/**
 * 项目分支
 * 
 * @author mengaijun
 * @date: 2018年12月21日 下午5:30:34
 */
public class GitlabBranch {
    /**
     * 分支名称
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GitlabProjectBranch [name=" + name + "]";
    }
}
