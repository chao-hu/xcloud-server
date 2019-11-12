package com.xxx.xcloud.module.ci.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.github.GithubApi;
import com.xxx.xcloud.client.github.GithubClientFactory;
import com.xxx.xcloud.client.github.model.GitHubBranch;
import com.xxx.xcloud.client.github.model.GithubRepos;
import com.xxx.xcloud.client.github.model.GithubUser;
import com.xxx.xcloud.client.gitlab.GitlabApi;
import com.xxx.xcloud.client.gitlab.GitlabClientFactory;
import com.xxx.xcloud.client.gitlab.GitlabClientFactory.ApiVersion;
import com.xxx.xcloud.client.gitlab.exception.GitlabException;
import com.xxx.xcloud.client.gitlab.model.GitlabBranch;
import com.xxx.xcloud.client.gitlab.model.GitlabRepos;
import com.xxx.xcloud.client.gitlab.model.GitlabTag;
import com.xxx.xcloud.client.gitlab.model.GitlabToken;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;
import com.xxx.xcloud.module.ci.consts.CiConstant;
import com.xxx.xcloud.module.ci.entity.CiCodeCredentials;
import com.xxx.xcloud.module.ci.entity.CodeInfo;
import com.xxx.xcloud.module.ci.repository.CiCodeCredentialsRepository;
import com.xxx.xcloud.module.ci.repository.CodeInfoRepository;
import com.xxx.xcloud.module.ci.service.CiCodeCredentialsService;
import com.xxx.xcloud.module.devops.credentials.pojo.BaseCredential;
import com.xxx.xcloud.module.devops.credentials.pojo.UsernamePasswordCredential;
import com.xxx.xcloud.module.devops.credentials.service.impl.CredentialServiceImpl;
import com.xxx.xcloud.utils.PageUtil;
import com.xxx.xcloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 代码认证相关, service方法
 *
 * @author mengaijun
 * @date: 2018年12月20日 下午5:12:04
 */
@Service
public class CiCodeCredentialsServiceImpl implements CiCodeCredentialsService {

    @Autowired
    private CiCodeCredentialsRepository ciCodeCredentialsRepository;

    @Autowired
    private CodeInfoRepository codeInfoRepository;

    @Autowired
    private CredentialServiceImpl credentialServiceImpl;

    private static final Logger LOG = LoggerFactory.getLogger(CiCodeCredentialsServiceImpl.class);

    /**
     * svn地址公有还是私有
     */
    public static final byte SVN_PUBLIC = 0;
    public static final byte SVN_PRIVATE = 1;

    @Override
    public CiCodeCredentials getById(String id) {
        try {
            return ciCodeCredentialsRepository.getById(id);
        } catch (Exception e) {
            LOG.error("数据库根据ID查询CiCodeCredentials失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "数据库查询失败!");
        }
    }

    /**
     * 认证是否被使用
     *
     * @param ciCodeCredentialsId
     * @return boolean
     * @date: 2019年7月3日 上午10:48:54
     */
    private boolean isCredentialsUsed(String ciCodeCredentialsId) {
        Pageable pageable = PageUtil.getPageable(0, 1);
        try {
            Page<CodeInfo> codeInfos = codeInfoRepository.findByCiCodeCredentialsId(ciCodeCredentialsId, pageable);
            if (codeInfos != null && !codeInfos.isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("根据认证信息查新代码信息错误！", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "查询认证是否被使用错误！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteCredentials(String id) {

        CiCodeCredentials credentials = getById(id);
        if (credentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "凭据信息不存在!");
        }
        // if (isCredentialsUsed(id)) {
        // throw new
        // ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_NOT_UPDATE,
        // "凭据信息被检查任务使用，不允许删除!");
        // }
        deleteCiCodeCredentials(id);

        try {
            credentialServiceImpl.deleteCredential(credentials.getUniqueKey(), null);
        } catch (IOException e) {
            LOG.error("Jenkins delete credential by id  exception ", e);
            // throw new
            // ErrorMessageException(ReturnCode.CODE_JENKINS_CREDENTIAL_DELETE_FAILED,
            // "删除认证信息失败!");
        } catch (Exception e) {
            LOG.error("Jenkins delete credential by id  exception ", e);
        }

        return true;
    }

    /**
     * 删除数据库认证信息
     *
     * @param id 记录ID
     * @date: 2018年12月27日 下午7:11:54
     */
    private void deleteCiCodeCredentials(String id) {
        try {
            ciCodeCredentialsRepository.deleteById(id);
        } catch (Exception e) {
            LOG.error("数据库根据ID删除CiCodeCredentials记录失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_DELETE_FAILED, "删除认证信息失败!");
        }
    }

    @Override
    public List<CiCodeCredentials> getCredentials(String tenantName, String projectId, byte codeControlType) {
        try {
            if (StringUtils.isEmpty(projectId)) {
                return ciCodeCredentialsRepository.getCodeCredentials(tenantName, codeControlType);
            }
            return ciCodeCredentialsRepository.getCodeCredentials(tenantName, projectId, codeControlType);
        } catch (Exception e) {
            LOG.error("数据库查询租户下CiCodeCredentials失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_LIST_FAILED, "数据库查询错误!");
        }
    }

    /**
     * 生成获取gitlab项目地址
     *
     * @param gitlabUrl gitlab项目名
     * @return String
     * @date: 2019年1月4日 下午6:43:51
     */
    private String generteGitlabUrl(String gitlabUrl) {
        // 如果url结尾包含"/"，清除结尾的"/"字符
        String endStr = "/";
        gitlabUrl = gitlabUrl.trim();
        if (gitlabUrl.endsWith(endStr)) {
            gitlabUrl = gitlabUrl.substring(0, gitlabUrl.length() - 1);
        }
        // 如果url不以http或https开头，默认为http方式
        String httpStr = "http://";
        String httpsStr = "https://";
        if (gitlabUrl.contains(httpStr) || gitlabUrl.contains(httpsStr)) {
            return gitlabUrl;
        }
        return httpStr + gitlabUrl;
    }

    @Override
    public CiCodeCredentials addGitlabCredentials(String tenantName, String userName, String password,
            String registryAddress, String projectId, String createdBy) {

        CiCodeCredentials credentials = new CiCodeCredentials();
        credentials.setTenantName(tenantName);
        credentials.setUserName(userName);
        credentials.setPassword(password);
        credentials.setRegistoryAddress(generteGitlabUrl(registryAddress));
        credentials.setCodeControlType(CiConstant.CODE_TYPE_GITLAB);
        credentials.setCreateTime(new Date());
        credentials.setType(CiConstant.AUTH_TYPE_HTTP);
        credentials.setProjectId(projectId);
        credentials.setCreatedBy(createdBy);

        // 用户名+仓库地址 确定一条认证记录
        if (getCredentialsByUsernameRegistoryAddrControlType(tenantName, userName, registryAddress,
                CiConstant.CODE_TYPE_GITLAB) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "此用户和仓库地址已经存在!");
        }

        // 如果 用户名+密码+URL 不能生成token, 说明用户名或密码错误
        GitlabApi gitlabApi = GitlabClientFactory.getGitlabInstance(credentials.getRegistoryAddress());
        String gitlabToken = getGitlabUserToken(userName, password, gitlabApi);
        if (gitlabToken == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CODE_AUTH_FAILED, "用户名密码不正确!");
        }

        // 存储token
        credentials.setGitlabToken(gitlabToken);

        // 保存凭据信息到数据库和Jenkins
        credentials = addCredentialsToDatabaseAndJenkins(credentials);

        return credentials;
    }

    /**
     * 保存CiCodeCredentials
     *
     * @param credentials 对象
     * @return {@link CiCodeCredentials}
     * @date: 2018年12月27日 下午6:18:59
     */
    private CiCodeCredentials saveCiCodeCredentials(CiCodeCredentials credentials) {
        try {
            return ciCodeCredentialsRepository.save(credentials);
        } catch (Exception e) {
            LOG.error("保存数据库失败", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存数据库失败!");
        }
    }

    @Override
    public CiCodeCredentials addSvnCredentials(String tenantName, String userName, String password,
            String registryAddress, byte publicOrPrivateFlag, String projectId, String createdBy) {

        // 公有镜像, 不需要用户名密码就能下载, 但验证需要用户名和密码否则接口不能调用, 设置默认的值(错误的用户名密码, 但能下载代码)
        if (publicOrPrivateFlag == SVN_PUBLIC) {
            userName = "svn_public_default_username";
            password = "svn_public_default_password";
        }

        CiCodeCredentials credentials = new CiCodeCredentials();
        credentials.setTenantName(tenantName);
        credentials.setUserName(userName);
        credentials.setPassword(password);
        credentials.setRegistoryAddress(registryAddress);
        credentials.setIsPublic(publicOrPrivateFlag);
        credentials.setCodeControlType(CiConstant.CODE_TYPE_SVN);
        credentials.setCreateTime(new Date());
        credentials.setType(CiConstant.AUTH_TYPE_HTTP);
        credentials.setProjectId(projectId);
        credentials.setCreatedBy(createdBy);

        // 判断认证是否在数据库中已经存在
        if (getCredentialsByUsernameRegistoryAddrControlType(tenantName, userName, registryAddress,
                CiConstant.CODE_TYPE_SVN, publicOrPrivateFlag) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "此用户和仓库地址已经存在!");
        }

        //获取连接测试结果
        int checkCodeRet = checkSvnConnection(registryAddress, userName, password);
        switch (checkCodeRet) {
        case -1:
            throw new ErrorMessageException(ReturnCode.CODE_CODE_AUTH_FAILED, "用户名密码与URL不匹配！");
        case 0:
            LOG.info("svnConnection success !");
            break;
        case 1:
            throw new ErrorMessageException(ReturnCode.CODE_GITLAB_GET_PROJECTS_FAILED, "请检查URL的合理性，不建议指向单个文件！");
        default:
            throw new ErrorMessageException(ReturnCode.CODE_GITLAB_GET_PROJECTS_FAILED, "请检查URL的合理性，无法拉取项目信息！");
        }

        // 保存凭据信息到数据库和Jenkins
        credentials = addCredentialsToDatabaseAndJenkins(credentials);

        return credentials;
    }

    /**
     * The user's authentication failed or connection failed.(-1)
     * <p>
     * If connected,the url maybe refer directory(0)、file(1)、none(2)、unknown(3)
     *
     * @param url  svn url.
     * @param name user name.
     * @param pwd  user pwd.
     * @return check result code.
     */
    private int checkSvnConnection(String url, String name, String pwd) {
        int checkCodeRet = -1;

        SVNRepository repository;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        } catch (SVNException e) {
            LOG.error("svn url encode exception!", e);
            return checkCodeRet;
        }

        ISVNAuthenticationManager auth = SVNWCUtil.createDefaultAuthenticationManager(name, pwd.toCharArray());
        repository.setAuthenticationManager(auth);

        try {
            //find target by item's path + revision number.
            SVNNodeKind nodeKind = repository.checkPath(StringUtils.EMPTY, -1);
            checkCodeRet = nodeKind.getID();
        } catch (SVNException e) {
            LOG.error("svn url check exception!", e);
            return checkCodeRet;
        }

        return checkCodeRet;
    }

    @Override
    public CiCodeCredentials addGithubCredentials(String tenantName, String userName, String accessToken,
            String projectId, String createdBy) {
        CiCodeCredentials credentials = new CiCodeCredentials();
        credentials.setTenantName(tenantName);
        credentials.setUserName(userName);
        credentials.setPassword(accessToken);
        credentials.setCodeControlType(CiConstant.CODE_TYPE_GITHUB);
        credentials.setCreateTime(new Date());
        credentials.setType(CiConstant.AUTH_TYPE_HTTP);
        credentials.setProjectId(projectId);
        credentials.setCreatedBy(createdBy);

        // 判断用户名是否已经存在
        if (getCodeCredentialsByUserNameAndType(tenantName, userName, CiConstant.CODE_TYPE_GITHUB) != null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_EXIST, "用户名信息已经存在!");
        }

        // 验证是否能通过
        GithubApi githubApi = GithubClientFactory.getGithubInstance();
        GithubUser user = getGithubUserByToken(accessToken, githubApi);
        if (user == null || !user.getLogin().equals(userName)) {
            throw new ErrorMessageException(ReturnCode.CODE_CODE_AUTH_FAILED, "token信息与用户名不匹配!");
        }

        // 保存凭据信息到数据库和Jenkins
        credentials = addCredentialsToDatabaseAndJenkins(credentials);

        return credentials;
    }

    /**
     * 添加凭据信息到数据库 + Jenkins后台
     *
     * @param credentials
     * @return {@link CiCodeCredentials}
     * @date: 2019年1月4日 上午11:39:20
     */
    private CiCodeCredentials addCredentialsToDatabaseAndJenkins(CiCodeCredentials credentials) {

        String uuid = UUID.randomUUID().toString();
        credentials.setUniqueKey(uuid);

        //1、保存凭据到Jenkins
        try {
            saveCredentialsToJenkins(credentials);
        } catch (Exception e) {
            LOG.error("saveCredentialsToJenkins exception", e);
            throw new ErrorMessageException(ReturnCode.CODE_XCLOUD_REGISTER_CREDENTIALS_FAILED, "Jenkins服务端保存关联信息失败!");
        }

        //2、保存凭据到数据库
        try {
            credentials = saveCiCodeCredentials(credentials);
        } catch (Exception e) {

            try {
                credentialServiceImpl.deleteCredential(credentials.getUniqueKey(), null);
            } catch (IOException e1) {
                LOG.error("Jenkins delete credential by id  exception ", e1);
            }

            LOG.error("addCredentialsToDatabaseAndJenkins exception!", e);
            throw e;
        }

        return credentials;
    }

    private void saveCredentialsToJenkins(CiCodeCredentials credentials) throws Exception {
        BaseCredential credential;

        byte codeControlType = credentials.getCodeControlType();
        switch (codeControlType) {
        case CiConstant.CODE_TYPE_GITLAB:
            credential = initGitlabCrd(credentials);
            break;

        case CiConstant.CODE_TYPE_GITHUB:
            credential = initGithubCrd(credentials);
            break;

        case CiConstant.CODE_TYPE_SVN:
            credential = initSvnCrd(credentials);
            break;

        default:
            throw new Exception("Credential type is illegal, codeControlType = " + codeControlType);
        }

        try {
            credentialServiceImpl.createCredential(credential, null);
        } catch (IOException e) {
            LOG.error("saveCredentialsToJenkins exception.", e);
            throw e;
        }

    }

    private BaseCredential initSvnCrd(CiCodeCredentials credentials) {
        return initGitlabCrd(credentials);
    }

    private BaseCredential initGithubCrd(CiCodeCredentials credentials) {
        //Gitlab access token is equals to password with here, so call gitlab init.
        return initGitlabCrd(credentials);
    }

    private BaseCredential initGitlabCrd(CiCodeCredentials credentials) {
        String id = credentials.getUniqueKey();
        String userName = credentials.getUserName();
        String password = credentials.getPassword();

        UsernamePasswordCredential upCrd = new UsernamePasswordCredential();
        upCrd.setScope(CiConstant.CREDENTIALS_SCOPE);
        upCrd.setId(id);
        upCrd.setUsername(userName);
        upCrd.setPassword(password);
        return upCrd;
    }

    /**
     * 根据token获取用户信息
     *
     * @param token
     * @param githubApi
     * @return GithubUser
     * @date: 2019年1月2日 下午5:42:33
     */
    private GithubUser getGithubUserByToken(String token, GithubApi githubApi) {
        try {
            return githubApi.getUserInfoByToken(token);
        } catch (Exception e) {
            LOG.error("根据token获取github用户信息失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_CODE_AUTH_FAILED, "根据token获取github用户信息失败!");
        }
    }

    /**
     * 根据用户名和类型信息查询
     *
     * @param tenantName
     * @param userName
     * @param codeControlType
     * @return CiCodeCredentials
     * @date: 2019年1月4日 上午11:32:01
     */
    private CiCodeCredentials getCodeCredentialsByUserNameAndType(String tenantName, String userName,
            byte codeControlType) {
        try {
            return ciCodeCredentialsRepository
                    .getCodeCredentialsByUserNameAndType(tenantName, userName, codeControlType);
        } catch (Exception e) {
            LOG.error("查询用户信息认证信息失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询用户认证信息失败!");
        }
    }

    /**
     * 根据租户,用 户名,仓库地址,代码类型获取认证对象
     *
     * @param tenantName
     * @param userName        用户名
     * @param registryAddress 仓库地址
     * @param codeControlType 认证类型
     * @return CiCodeCredentials
     * @date: 2018年12月21日 上午11:13:21
     */
    private CiCodeCredentials getCredentialsByUsernameRegistoryAddrControlType(String tenantName, String userName,
            String registryAddress, byte codeControlType) {
        try {
            return ciCodeCredentialsRepository
                    .getByUserNameAndRegistoryAdress(tenantName, userName, registryAddress, codeControlType);
        } catch (Exception e) {
            LOG.error("根据用户名和仓库地址查询gitlab认证信息错误!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_CONNECT_FAILED, "数据库连接异常!");
        }
    }

    /**
     * 根据租户,用 户名,仓库地址,代码类型获取认证对象
     *
     * @param tenantName
     * @param userName        用户名
     * @param registryAddress 仓库地址
     * @param codeControlType 认证类型
     * @param isPublic        svn是否公有
     * @return CiCodeCredentials
     * @date: 2018年12月21日 上午11:13:21
     */
    private CiCodeCredentials getCredentialsByUsernameRegistoryAddrControlType(String tenantName, String userName,
            String registryAddress, byte codeControlType, byte isPublic) {
        try {
            if (SVN_PUBLIC == isPublic) {
                return ciCodeCredentialsRepository
                        .getByUserNameAndRegistoryAdress(tenantName, registryAddress, codeControlType, isPublic);
            } else {
                return ciCodeCredentialsRepository
                        .getByUserNameAndRegistoryAdress(tenantName, userName, registryAddress, codeControlType,
                                isPublic);
            }
        } catch (Exception e) {
            LOG.error("根据用户名和仓库地址查询gitlab认证信息错误!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_CONNECT_FAILED, "数据库连接异常!");
        }
    }

    @Override
    public List<GitlabRepos> getGitlabRepos(String credentialsId) {
        return getGitlabInfo(credentialsId, null, GitlabRepos.class);
    }

    /**
     * 根据条件获取gitlab信息（返回clazz类型对应的的信息）
     *
     * @param credentialsId
     * @param reposId
     * @param clazz
     * @return List<T>
     * @date: 2019年7月24日 下午5:54:23
     */
    public <T> List<T> getGitlabInfo(String credentialsId, Integer reposId, Class<T> clazz) {
        CiCodeCredentials ciCodeCredentials = getById(credentialsId);
        if (ciCodeCredentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "认证信息不存在!");
        }

        // 获取认证token(先使用之前记录的token)
        String token = getGitlabTokenByCredentials(ciCodeCredentials);
        boolean isNewToken = ciCodeCredentials.getGitlabToken() == null ? true : false;
        if (token == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CODE_AUTH_FAILED, "获取认证token失败!");
        }

        // 获取gitlab相关信息
        List<T> infos = null;
        try {
            infos = getGitlabInfo(ciCodeCredentials, token, reposId, clazz);
        } catch (Exception e) {

        }
        if (infos == null) {
            GitlabApi gitlabApi = GitlabClientFactory
                    .getGitlabInstance(generteGitlabUrl(ciCodeCredentials.getRegistoryAddress()));
            // 如果获取gitlab信息失败, 可能是使用的之前的token, 且token失效了, 重新获取token, 再尝试一次
            token = getGitlabUserToken(ciCodeCredentials.getUserName(), ciCodeCredentials.getPassword(), gitlabApi);
            isNewToken = true;
            if (token == null) {
                throw new ErrorMessageException(ReturnCode.CODE_CODE_AUTH_FAILED, "获取认证token失败!");
            }
            infos = getGitlabInfo(ciCodeCredentials, token, reposId, clazz);
        }
        // 如果仍没有获取项目信息, 抛出异常
        if (infos == null) {
            throw new ErrorMessageException(ReturnCode.CODE_GITLAB_GET_PROJECTS_FAILED, "获取gitlab信息失败!");
        }

        // 如果生成了新的token, 更新一下, 更新失败, 不做处理
        if (isNewToken) {
            saveGitlabToken(token, ciCodeCredentials);
        }

        return infos;
    }

    /**
     * 根据条件获取gitlab信息（返回clazz类型对应的的信息）
     *
     * @param ciCodeCredentials
     * @param token
     * @param reposId
     * @param clazz
     * @return List<T>
     * @date: 2019年7月24日 下午5:52:32
     */
    private <T> List<T> getGitlabInfo(CiCodeCredentials ciCodeCredentials, String token, Integer reposId,
            Class<T> clazz) {
        if (clazz == GitlabRepos.class) {
            return (List<T>) getGitlabReposByToken(ciCodeCredentials, token);
        }

        if (clazz == GitlabBranch.class) {
            return (List<T>) getGitlabBranchsByTokenAndRepos(ciCodeCredentials, reposId, token);
        }

        return (List<T>) getGitlabTagsByTokenAndRepos(ciCodeCredentials, reposId, token);
    }

    /**
     * 根据token查询项目
     *
     * @param ciCodeCredentials
     * @param token
     * @return List<GitlabRepos>
     * @date: 2019年3月6日 下午2:09:54
     */
    private List<GitlabRepos> getGitlabReposByToken(CiCodeCredentials ciCodeCredentials, String token) {
//        setGitlabApiVersoinInfo(ciCodeCredentials, token);
//        List<GitlabRepos> projects = null;
//        try {
//            int curPage = 1;
//            JSONArray jsonArr = getGitlabApi(ciCodeCredentials).getProkectsByToken(token, Integer.MAX_VALUE, curPage);
//            projects = jsonArr.toJavaList(GitlabRepos.class);
//        } catch (GitlabException e) {
//            LOG.error("调用接口异常" + e.getCode() + " " + e.getMessage(), e);
//        } catch (Exception e) {
//            LOG.error("获取用户下的项目失败!", e);
//        }
//        return projects;
        return  null;
    }

    /**
     * 设置api版本
     *
     * @param ciCodeCredentials
     * @param token             void
     * @date: 2019年8月8日 下午5:02:05
     */
    private void setGitlabApiVersoinInfo(CiCodeCredentials ciCodeCredentials, String token) {
        String uri = ciCodeCredentials.getRegistoryAddress();
        // 如果之前已经设置好版本
        if (GitlabClientFactory.getApiVersionOfUri(uri) != null) {
            return;
        }

        // 设置版本
        if (!setGitlabApiVersoinInfo(uri, token)) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "gitlab api 版本不支持！");
        }
    }

    /**
     * 设置uri支持的版本（遍历所有版本；根据接口是否能调通，判断是否支持）
     *
     * @param uri
     * @param token
     * @return boolean
     * @date: 2019年8月8日 下午4:57:35
     */
    private boolean setGitlabApiVersoinInfo(String uri, String token) {
        for (ApiVersion apiVersion : ApiVersion.values()) {
            String uriWithApiVer = GitlabClientFactory.getUrlWithVersion(uri, apiVersion);
            GitlabApi gitlabApi = GitlabClientFactory.getGitlabInstance(uriWithApiVer);
            int curPage = 1;
            try {
                // 如果可以调用成功，就使用；调用失败，表示不支持
                gitlabApi.getProjectsByToken(token, 1, curPage);
                GitlabClientFactory.setUrlVersion(uri, apiVersion);
                return true;
            } catch (Exception e) {
                LOG.error("gitlab版本不支持：" + apiVersion, e);
                GitlabClientFactory.removeGitlabInstance(uriWithApiVer);
            }
        }

        return false;
    }

    @Override
    public List<GithubRepos> getGitHubRepos(String credentialsId) {
        CiCodeCredentials ciCodeCredentials = getById(credentialsId);
        if (ciCodeCredentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "认证信息不存在!");
        }
        List<GithubRepos> reposes = null;
        try {
            GithubApi githubApi = GithubClientFactory.getGithubInstance();
            JSONArray jsonArr = githubApi.getReposes(ciCodeCredentials.getUserName(), ciCodeCredentials.getPassword());
            reposes = jsonArr.toJavaList(GithubRepos.class);
        } catch (Exception e) {
            LOG.error("获取用户下的项目失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_GITLAB_GET_PROJECTS_FAILED, "获取用户项目列表失败!");
        }
        return reposes;
    }

    @Override
    public List<GitHubBranch> getGitHubBranches(String credentialsId, String reposName) {
        CiCodeCredentials ciCodeCredentials = getById(credentialsId);
        if (ciCodeCredentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "认证信息不存在!");
        }
        List<GitHubBranch> branches = null;
        try {
            GithubApi githubApi = GithubClientFactory.getGithubInstance();
            branches = githubApi
                    .getBranches(ciCodeCredentials.getUserName(), reposName, ciCodeCredentials.getPassword());
        } catch (Exception e) {
            LOG.error("获取用户下的项目失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_GITLAB_GET_PROJECTS_FAILED, "获取用户项目列表失败!");
        }
        return branches;
    }

    @Override
    public String getGitlabUserToken(String credentialsId) {
        CiCodeCredentials ciCodeCredentials = getById(credentialsId);
        if (ciCodeCredentials == null) {
            throw new ErrorMessageException(ReturnCode.CODE_CHECK_PARAM_IS_NOT_EXIST, "认证信息不存在!");
        }
        GitlabApi gitlabApi = GitlabClientFactory
                .getGitlabInstance(generteGitlabUrl(ciCodeCredentials.getRegistoryAddress()));
        return getGitlabUserToken(ciCodeCredentials.getUserName(), ciCodeCredentials.getPassword(), gitlabApi);
    }

    /**
     * 根据用户名密码生成token
     *
     * @param userName  用户名
     * @param password  密码
     * @param gitlabApi
     * @return String
     * @date: 2018年12月27日 下午6:36:41
     */
    private String getGitlabUserToken(String userName, String password, GitlabApi gitlabApi) {
        String result = null;
        // 调用接口
        try {
            JSONObject jsonObj = gitlabApi.getToken(CiConstant.GITLAB_GRANTTYPE_PASSWORD, userName, password);
            GitlabToken token = jsonObj.toJavaObject(GitlabToken.class);
            result = token.getAccessToken();
        } catch (GitlabException e) {
            LOG.error("调用获取token接口异常:" + e.getCode() + " " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("获取用户token异常", e);
        }

        return result;
    }

    @Override
    public List<GitlabBranch> getGitlabBranchs(String credentialsId, int reposId) {
        return getGitlabInfo(credentialsId, reposId, GitlabBranch.class);
    }

    /**
     * 保存token到数据库
     *
     * @param token
     * @param ciCodeCredentials
     * @date: 2019年7月24日 下午3:58:54
     */
    private void saveGitlabToken(String token, CiCodeCredentials ciCodeCredentials) {
        ciCodeCredentials.setGitlabToken(token);
        try {
            saveCiCodeCredentials(ciCodeCredentials);
        } catch (Exception e) {
        }
    }

    /**
     * 根据认证对象信息获取token
     *
     * @param credentials
     * @return String
     * @date: 2019年7月24日 下午3:44:04
     */
    private String getGitlabTokenByCredentials(CiCodeCredentials credentials) {
        String token = credentials.getGitlabToken();
        if (token == null) {
            token = getGitlabUserToken(credentials.getUserName(), credentials.getPassword(),
                    GitlabClientFactory.getGitlabInstance(credentials.getRegistoryAddress()));
        }
        return token;
    }

    @Override
    public List<GitlabTag> getGitlabTags(String credentialsId, int reposId) {
        return getGitlabInfo(credentialsId, reposId, GitlabTag.class);
    }

    /**
     * 根据token和仓库信息获取分支
     *
     * @param
     * @param reposId   仓库ID
     * @param token     token
     * @return List<GitlabBranch>
     * @date: 2019年3月6日 下午2:33:35
     */
    private List<GitlabBranch> getGitlabBranchsByTokenAndRepos(CiCodeCredentials ciCodeCredentials, int reposId,
            String token) {
        setGitlabApiVersoinInfo(ciCodeCredentials, token);
        // 获取project信息
        List<GitlabBranch> branchs = null;
        // 调用接口
        try {
            int curPage = 1;

            JSONArray jsonArr = getGitlabApi(ciCodeCredentials)
                    .getBranchsByProjectIdAndToken(reposId, token, Integer.MAX_VALUE, curPage);
            branchs = jsonArr.toJavaList(GitlabBranch.class);
        } catch (GitlabException e) {
            LOG.error("调用获取branch接口异常:" + e.getCode() + " " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("获取branch异常", e);
        }
        return branchs;
    }

    /**
     * 获取api对象
     *
     * @param ciCodeCredentials
     * @return GitlabApi
     * @date: 2019年8月8日 下午5:06:53
     */
    private GitlabApi getGitlabApi(CiCodeCredentials ciCodeCredentials) {
        String uriWithVersion = GitlabClientFactory.getUrlWithVersion(ciCodeCredentials.getRegistoryAddress());
        return GitlabClientFactory.getGitlabInstance(uriWithVersion);
    }

    /**
     * 根据token和仓库信息获取tag
     *
     * @param
     * @param reposId   仓库ID
     * @param token     token
     * @return List<GitlabTag>
     * @date: 2019年3月6日 下午2:33:35
     */
    private List<GitlabTag> getGitlabTagsByTokenAndRepos(CiCodeCredentials ciCodeCredentials, int reposId,
            String token) {
        setGitlabApiVersoinInfo(ciCodeCredentials, token);
        // 获取project信息
        List<GitlabTag> tags = null;
        // 调用接口
        try {
            int curPage = 1;
            JSONArray jsonArr = getGitlabApi(ciCodeCredentials)
                    .getTagsByProjectIdAndToken(reposId, token, Integer.MAX_VALUE, curPage);
            tags = jsonArr.toJavaList(GitlabTag.class);
        } catch (GitlabException e) {
            LOG.error("调用获取tag接口异常:" + e.getCode() + " " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("获取tag异常", e);
        }
        return tags;
    }

    @Override
    public CodeInfo saveCodeInfo(CodeInfo codeInfo) {
        try {
            return codeInfoRepository.save(codeInfo);
        } catch (Exception e) {
            LOG.error("保存CodeInfo信息失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_SAVE_INFO_FAILED, "保存代码信息失败!");
        }
    }

    @Override
    public CodeInfo getCodeInfoById(String id) {
        try {
            return codeInfoRepository.getById(id);
        } catch (Exception e) {
            LOG.error("根据ID查询CodeInfo信息失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "查询代码信息失败!");
        }
    }

    @Override
    public void deleteCodeInfo(String id) {
        try {
            codeInfoRepository.deleteById(id);
        } catch (Exception e) {
            LOG.error("根据ID删除CodeInfo信息失败!", e);
            throw new ErrorMessageException(ReturnCode.CODE_SQL_FIND_ONE_FAILED, "删除代码信息失败!");
        }
    }
}
