package com.xxx.xcloud.module.ci.service;

import com.xxx.xcloud.client.github.model.GitHubBranch;
import com.xxx.xcloud.client.github.model.GithubRepos;
import com.xxx.xcloud.client.gitlab.model.GitlabBranch;
import com.xxx.xcloud.client.gitlab.model.GitlabRepos;
import com.xxx.xcloud.client.gitlab.model.GitlabTag;
import com.xxx.xcloud.module.ci.entity.CiCodeCredentials;
import com.xxx.xcloud.module.ci.entity.CodeInfo;

import java.util.List;

/**
 * @author mengaijun
 * @Description: 认证接口
 * @date: 2018年12月20日 下午5:11:34
 */
public interface CiCodeCredentialsService {
    /**
     * 根据ID查询
     *
     * @param id ID
     * @return CiCodeCredentials
     * @date: 2018年12月20日 下午5:15:13
     */
    CiCodeCredentials getById(String id);

    /**
     * 根据ID删除
     *
     * @param id
     * @return boolean
     * @date: 2018年12月21日 下午8:00:29
     */
    boolean deleteCredentials(String id);

    /**
     * 添加gitlab方式的认证
     *
     * @param tenantName       租户
     * @param userName         用户名
     * @param password         密码
     * @param registoryAddress 仓库地址
     * @param projectId
     * @param createdBy
     * @return CiCodeCredentials 认证信息
     * @date: 2018年12月21日 上午10:24:05
     */
    CiCodeCredentials addGitlabCredentials(String tenantName, String userName, String password, String registoryAddress,
            String projectId, String createdBy);

    /**
     * 添加svn认证信息
     *
     * @param tenantName          租户名
     * @param userName            用户
     * @param password            密码
     * @param registoryAddress    svn地址
     * @param publicOrPrivateFlag 0:公有   1:私有
     * @param projectId
     * @param createdBy
     * @return CiCodeCredentials 添加的认证对象
     * @date: 2018年12月27日 下午6:48:40
     */
    CiCodeCredentials addSvnCredentials(String tenantName, String userName, String password, String registoryAddress,
            byte publicOrPrivateFlag, String projectId, String createdBy);

    /**
     * 添加github认证信息
     *
     * @param tenantName  租户名
     * @param userName    用户
     * @param accessToken token
     * @param projectId
     * @param createdBy
     * @return CiCodeCredentials
     * @date: 2019年1月2日 上午11:24:34
     */
    CiCodeCredentials addGithubCredentials(String tenantName, String userName, String accessToken, String projectId,
            String createdBy);

    /**
     * 获取租户下信息
     *
     * @param tenantName
     * @param projectId
     * @param codeControlType 查询认证类型
     * @return List<CiCodeCredentials>
     * @date: 2018年12月21日 下午7:49:36
     */
    List<CiCodeCredentials> getCredentials(String tenantName, String projectId, byte codeControlType);

    /**
     * 获取仓库所有分支
     *
     * @param credentialsId 认证ID
     * @param reposName     仓库名
     * @return List<GitHubBranch>
     * @date: 2019年1月2日 下午6:14:37
     */
    List<GitHubBranch> getGitHubBranches(String credentialsId, String reposName);

    /**
     * 获取用户所有仓库
     *
     * @param credentialsId 认证ID
     * @return List<GithubRepos>
     * @date: 2019年1月2日 下午6:14:11
     */
    List<GithubRepos> getGitHubRepos(String credentialsId);

    /**
     * 获取gitlab用户token
     *
     * @param id 记录ID
     * @return String token
     * @date: 2018年12月21日 上午10:32:41
     */
    String getGitlabUserToken(String id);

    /**
     * 获取认证下的项目
     *
     * @param id
     * @return List<GitlabRepos>
     * @date: 2018年12月21日 下午3:52:42
     */
    List<GitlabRepos> getGitlabRepos(String id);

    /**
     * 获取项目的所有分支
     *
     * @param credentialsId 认证ID
     * @param reposId       项目ID
     * @return List<GitlabProjectBranch>
     * @date: 2018年12月21日 下午5:53:20
     */
    List<GitlabBranch> getGitlabBranchs(String credentialsId, int reposId);

    /**
     * 获取标签
     *
     * @param credentialsId
     * @param reposId
     * @return List<GitlabTag>
     * @date: 2019年7月24日 下午3:30:32
     */
    List<GitlabTag> getGitlabTags(String credentialsId, int reposId);

    /**
     * 保存代码信息
     *
     * @param codeInfo
     * @return CodeInfo
     * @date: 2018年12月26日 下午5:41:09
     */
    CodeInfo saveCodeInfo(CodeInfo codeInfo);

    /**
     * 根据ID查询代码信息
     *
     * @param id
     * @return CodeInfo
     * @date: 2018年12月26日 下午5:45:56
     */
    CodeInfo getCodeInfoById(String id);

    /**
     * 根据ID删除代码信息
     *
     * @param id
     * @date: 2018年12月26日 下午6:28:25
     */
    void deleteCodeInfo(String id);
}
