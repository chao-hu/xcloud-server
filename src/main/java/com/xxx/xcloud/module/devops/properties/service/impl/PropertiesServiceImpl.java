package com.xxx.xcloud.module.devops.properties.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.common.CredentialType;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.common.ScmType;
import com.xxx.xcloud.module.devops.model.GitLabModel;
import com.xxx.xcloud.module.devops.model.ScmModel;
import com.xxx.xcloud.module.devops.properties.pojo.*;
import com.xxx.xcloud.module.devops.properties.service.PropertiesService;
import com.xxx.xcloud.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PropertiesServiceImpl implements PropertiesService {

    @Override
    public Properties getProperties(ScmModel scmModel) throws DevopsException {
        Properties properties = new Properties();
        ParametersDefinitionProperty parametersDefinitionProperty = new ParametersDefinitionProperty();
        ParameterDefinitions parameterDefinitions = new ParameterDefinitions();
        List<TextParameterDefinition> textParameterDefinitions = new ArrayList<TextParameterDefinition>();

        String[] params = null;
        if (StringUtils.isEmpty(XcloudProperties.getConfigMap().get(Global.DEVOPS_PARAM))) {
            params = new String[] { "image" };
        } else {
            params = XcloudProperties.getConfigMap().get(Global.DEVOPS_PARAM).split(",");
        }
        for (String param : params) {
            TextParameterDefinition textParameterDefinition = new TextParameterDefinition();
            textParameterDefinition.setName(param);
            textParameterDefinitions.add(textParameterDefinition);
        }

        parameterDefinitions.setTextParameterDefinitions(textParameterDefinitions);
        parametersDefinitionProperty.setParameterDefinitions(parameterDefinitions);
        properties.setParametersDefinitionProperty(parametersDefinitionProperty);

        if (scmModel == null) {
            return properties;
        }

        String scmType = scmModel.getScmType();
        String credentialType = scmModel.getCredentialType();

        if (StringUtils.isEmpty(scmType) || StringUtils.isEmpty(credentialType)) {
            return properties;
        }

        if (scmModel.getScmType().equals(ScmType.SCM_GIT_LAB)
                && scmModel.getCredentialType().equals(CredentialType.TOKEN)) {
            // check
            GitLabModel gitLabModel = scmModel.getGitLabModel();
            String connectionName = gitLabModel.getConnectionName();
            if (gitLabModel == null) {
                throw new DevopsException(500, "Gitlab配置信息为空");
            }

            if (StringUtils.isEmail(connectionName)) {
                throw new DevopsException(500, "connectionName(连接名)为空");
            }

            GitLabConnectionProperty gitLabConnectionProperty = new GitLabConnectionProperty();
            gitLabConnectionProperty.setGitLabConnection(connectionName);
            gitLabConnectionProperty.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_GITLAB));

            properties.setGitLabConnectionProperty(gitLabConnectionProperty);
        }

        return properties;
    }

}
