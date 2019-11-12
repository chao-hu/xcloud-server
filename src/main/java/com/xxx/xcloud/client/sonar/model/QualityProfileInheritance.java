/**
 * Project Name:EPM_PAAS_CLOUD
 * File Name:QualityProfiles.java
 * Package Name:com.bonc.epm.paas.sonar.model
 * Date:2018年2月5日下午4:59:58
 * Copyright (c) 2018, longkaixiang@bonc.com.cn All Rights Reserved.
 *
 */

package com.xxx.xcloud.client.sonar.model;

import java.util.List;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月10日 上午10:12:41
 */
public class QualityProfileInheritance {
	private SonarQualityProfile profile;
	private List<String> ancestors;
	private List<String> children;
    public SonarQualityProfile getProfile() {
        return profile;
    }
    public void setProfile(SonarQualityProfile profile) {
        this.profile = profile;
    }
    public List<String> getAncestors() {
        return ancestors;
    }
    public void setAncestors(List<String> ancestors) {
        this.ancestors = ancestors;
    }
    public List<String> getChildren() {
        return children;
    }
    public void setChildren(List<String> children) {
        this.children = children;
    }
    
	@Override
	public String toString() {
		return "QualityProfileInheritance [profile=" + profile + ", ancestors=" + ancestors + ", children=" + children
				+ "]";
	}

}
