package com.xxx.xcloud.module.devops.credentials.service;

import com.xxx.xcloud.module.devops.credentials.pojo.BaseCredential;
import com.xxx.xcloud.module.harbor.entity.HarborUser;

import java.io.IOException;

/**
 * Credential Service
 *
 * @author xujiangpeng
 * @date 2019/3/15
 */
public interface CredentialService {

    /**
     * create credential.
     *
     * @param crd        The credential you wang to create.
     * @param domainName The credential domain name.use global if null.
     * @throws IOException throw HttpResponseException if conflict.
     */
    void createCredential(BaseCredential crd, String domainName) throws IOException;

    /**
     * delete credential.
     *
     * @param credentialsId The credential ID .
     * @param domainName    The credential domain name.use global if null.
     * @throws IOException in case of an error.
     */
    void deleteCredential(String credentialsId, String domainName) throws IOException;

    /**
     * create pushCredentialId ,if you need push images to harbor.
     *
     * @param harborUser harbor info.
     * @throws IOException in case of an error.
     */
    void createPushCredentialId(HarborUser harborUser) throws IOException;

    /**
     * delete pushCredentialId if harbor user-deleted.
     *
     * @param harborUser harbor info.
     * @throws IOException in case of an error.
     */
    void deletePushCredentialId(HarborUser harborUser) throws IOException;

    /**
     * generate pushCredentialId
     * 
     * @param harborUser
     * @return String
     * @date: 2019年4月17日 下午2:22:51
     */
    String generatePushCredentialId(HarborUser harborUser);

}
