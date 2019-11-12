package com.xxx.xcloud.module.sonar.entity;

import com.xxx.xcloud.module.ci.entity.CiCodeCredentials;
import com.xxx.xcloud.module.ci.entity.CodeInfo;

/**
 * 代码检查信息详情
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年5月14日 下午4:57:32
 */
public class CodeCheckInfo {
    /**
     * 任务信息
     */
    private CodeCheckTask codeCheckTask;

    /**
     * 代码信息
     */
    private CodeInfo codeInfo;

    /**
     * 代码认证信息
     */
    private CiCodeCredentials ciCodeCredentials;

    private CodeCheckResult codeCheckResult;

    public CodeCheckTask getCodeCheckTask() {
        return codeCheckTask;
    }

    public void setCodeCheckTask(CodeCheckTask codeCheckTask) {
        this.codeCheckTask = codeCheckTask;
    }

    public CodeInfo getCodeInfo() {
        return codeInfo;
    }

    public void setCodeInfo(CodeInfo codeInfo) {
        this.codeInfo = codeInfo;
    }

    public CiCodeCredentials getCiCodeCredentials() {
        return ciCodeCredentials;
    }

    public void setCiCodeCredentials(CiCodeCredentials ciCodeCredentials) {
        this.ciCodeCredentials = ciCodeCredentials;
    }

    public CodeCheckResult getCodeCheckResult() {
        return codeCheckResult;
    }

    public void setCodeCheckResult(CodeCheckResult codeCheckResult) {
        this.codeCheckResult = codeCheckResult;
    }

}
