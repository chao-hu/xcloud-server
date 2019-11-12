package com.xxx.xcloud.module.ci.model;

import com.xxx.xcloud.module.ci.entity.*;

import java.util.List;

/**
 * @author mengaijun
 * @Description: 构建详情
 * @date: 2018年12月17日 下午2:16:00
 */
public class CiDetail {
	private Ci ci;
	private CiFile ciFile;
	private CodeInfo codeInfo;
    private CiCodeCredentials ciCodeCredentials;
	private List<CiRecord> ciRecords;
	/**
	 * 构建总持续时间
	 */
	private int constructionDurationTotal = 0;
	/**
	 * 构建成功次数
	 */
	private int constructionOkTotal = 0;
	/**
	 * 构建失败次数
	 */
	private int constructionFailTotal = 0;
	public Ci getCi() {
		return ci;
	}
	public void setCi(Ci ci) {
		this.ci = ci;
	}
	public CiFile getCiFile() {
		return ciFile;
	}
	public void setCiFile(CiFile ciFile) {
		this.ciFile = ciFile;
	}

	public CodeInfo getCodeInfo() {
		return codeInfo;
	}
	public void setCodeInfo(CodeInfo codeInfo) {
		this.codeInfo = codeInfo;
	}
	public List<CiRecord> getCiRecords() {
		return ciRecords;
	}
	public void setCiRecords(List<CiRecord> ciRecords) {
		this.ciRecords = ciRecords;
	}
	public int getConstructionDurationTotal() {
		return constructionDurationTotal;
	}
	public void setConstructionDurationTotal(int constructionDurationTotal) {
		this.constructionDurationTotal = constructionDurationTotal;
	}
	public int getConstructionOkTotal() {
		return constructionOkTotal;
	}
	public void setConstructionOkTotal(int constructionOkTotal) {
		this.constructionOkTotal = constructionOkTotal;
	}
	public int getConstructionFailTotal() {
		return constructionFailTotal;
	}
	public void setConstructionFailTotal(int constructionFailTotal) {
		this.constructionFailTotal = constructionFailTotal;
	}

    public CiCodeCredentials getCiCodeCredentials() {
        return ciCodeCredentials;
    }

    public void setCiCodeCredentials(CiCodeCredentials ciCodeCredentials) {
        this.ciCodeCredentials = ciCodeCredentials;
    }

}
