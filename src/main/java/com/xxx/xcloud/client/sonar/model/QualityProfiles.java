package com.xxx.xcloud.client.sonar.model;

import java.util.List;

/**
 * QualityProfile集合
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月27日 下午7:50:46
 */
public class QualityProfiles {
	private List<SonarQualityProfile> profiles;

	public List<SonarQualityProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<SonarQualityProfile> profiles) {
		this.profiles = profiles;
	}

    @Override
    public String toString() {
        return profiles.toString();
    }
}
