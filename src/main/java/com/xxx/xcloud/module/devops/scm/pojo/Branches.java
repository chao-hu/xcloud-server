package com.xxx.xcloud.module.devops.scm.pojo;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author xingej
 * git对应的分支
 */
public class Branches {

	private BranchSpec branchSpec;

	@XmlElement(name = "hudson.plugins.git.BranchSpec")
	public BranchSpec getBranchSpec() {
		return branchSpec;
	}

	public void setBranchSpec(BranchSpec branchSpec) {
		this.branchSpec = branchSpec;
	}
}
