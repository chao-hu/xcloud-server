package com.xxx.xcloud.module.devops.credentials.service.impl;

import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.devops.credentials.pojo.BaseCredential;
import com.xxx.xcloud.module.devops.credentials.pojo.UsernamePasswordCredential;
import com.xxx.xcloud.module.devops.credentials.service.CredentialService;
import com.xxx.xcloud.module.devops.util.DevopsClient;
import com.xxx.xcloud.module.harbor.entity.HarborUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * com.xxx.xcloud.module.devops.credentials.service.impl.CredentialServiceImpl
 *
 * @author xujiangpeng
 * @date 2019/3/20
 */
@Service
public class CredentialServiceImpl implements CredentialService {

    private static Logger LOG = LoggerFactory.getLogger(CredentialServiceImpl.class);

    @Override
    public void createCredential(BaseCredential crd, String domainName) throws IOException {
        DevopsClient devopsClient = DevopsClient.getClient();
        devopsClient.createCredential(crd, domainName);
    }

    @Override
    public void deleteCredential(String credentialsId, String domainName) throws IOException {
        DevopsClient devopsClient = DevopsClient.getClient();
        devopsClient.deleteCredential(credentialsId, domainName);
    }

    @Override
    public void createPushCredentialId(HarborUser harborUser) throws IOException {
        UsernamePasswordCredential crd = new UsernamePasswordCredential();
        crd.setScope(CiConstant.CREDENTIALS_SCOPE);
        crd.setId(generatePushCredentialId(harborUser));
        crd.setUsername(harborUser.getUsername());
        crd.setPassword(harborUser.getPassword());

        try {
            createCredential(crd, null);
        } catch (IOException e) {
            LOG.error("Jenkins createPushCredentialId exception ", e);
            throw e;
        }

    }

    @Override
    public void deletePushCredentialId(HarborUser harborUser) throws IOException {
        String pushCredentialId = generatePushCredentialId(harborUser);

        try {
            deleteCredential(pushCredentialId, null);
        } catch (IOException e) {
            LOG.error("Jenkins deletePushCredentialId exception ", e);
            throw e;
        }
    }

    /**
     * 根据HarborUser获取认证ID
     * 
     * @param harborUser
     * @return String
     * @date: 2019年4月17日 下午2:21:54
     */
    @Override
    public String generatePushCredentialId(HarborUser harborUser) {
        return harborUser.getHarborId() + harborUser.getUsername();
    }
}
