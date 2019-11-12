package com.xxx.xcloud.module.devops.scm.service.impl;

import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.GitHubModel;
import com.xxx.xcloud.module.devops.model.ScmModel;
import org.springframework.stereotype.Component;

/**
 * @author daien
 * @date 2019年3月15日
 */
@Component("gitHubFactory")
public class GitHubFactory extends AbstractScmFactory {

    @Override
    protected void verify(ScmModel scmModel) throws DevopsException {
        GitHubModel gitHubModel = scmModel.getGitHubModel();
        if (gitHubModel == null) {
            throw new DevopsException(500, "GitHub配置信息为空");
        }
    }

    @Override
    protected String getUrl(ScmModel scmModel) {
        return scmModel.getGitHubModel().getUrl();
    }

    @Override
    protected String getCredentialId(ScmModel scmModel) {
        return scmModel.getGitHubModel().getCredentialId();
    }

    @Override
    protected String getBranch(ScmModel scmModel) {
        return scmModel.getGitHubModel().getBranch();
    }

    @Override
    protected String getName(ScmModel scmModel) {
        return scmModel.getGitHubModel().getName();
    }

    @Override
    protected String getRefspec(ScmModel scmModel) {
        return scmModel.getGitHubModel().getRefspec();
    }

}
