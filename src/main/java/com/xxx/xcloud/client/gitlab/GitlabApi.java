package com.xxx.xcloud.client.gitlab;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.gitlab.exception.GitlabException;

import feign.Param;
import feign.RequestLine;

/**
 *
 * @author mengaijun
 * @Description: gitlab 接口定义
 * @date: 2018年12月20日 上午11:31:29
 */
public interface GitlabApi {
    /**
     * 根据用户名密码获取token
     *
     * @param grantType "password", 表示是根据用户名密码获取token
     * @param username 用户名
     * @param password 密码
     * @return String
     * @throws GitlabException
     * @date: 2018年12月20日 下午8:51:32
     */
    @RequestLine("POST /oauth/token?grant_type={grant_type}&username={username}&password={password}")
    public JSONObject getToken(@Param("grant_type") String grantType, @Param("username") String username,
            @Param("password") String password) throws GitlabException;

    /**
     * 基于token， projectId 获取工程project的分支列表
     *
     * @Title: getBranchsByProjectIdAndToken
     * @Description: 基于token， projectId 获取工程project的分支列表，带分页功能
     * @param @param id
     * @param @param accessToken
     * @param @param perPage
     * @param @param page
     * @param @return
     * @param @throws GitlabException 参数
     * @return JSONArray 返回类型
     * @throws
     */
    @RequestLine("GET /projects/{id}/repository/branches?access_token={accessToken}&per_page={perPage}&page={page}")
    public JSONArray getBranchsByProjectIdAndToken(@Param("id") int id, @Param("accessToken") String accessToken,
            @Param("perPage") int perPage, @Param("page") int page) throws GitlabException;

    /**
     *
     * 基于token获取project列表
     *
     * @Title: getProjectsByToken
     * @Description: 基于token 获取当前用户的所有project列表，带分页功能
     * @param @param accessToken
     * @param @param perPage
     * @param @param page
     * @param @return
     * @param @throws GitlabException 参数
     * @return JSONArray 返回类型
     * @throws
     */
    @RequestLine("GET /projects?access_token={accessToken}&owned=true&per_page={perPage}&page={page}")
    public JSONArray getProjectsByToken(@Param("accessToken") String accessToken, @Param("perPage") int perPage,
            @Param("page") int page) throws GitlabException;

    /**
     * 获取项目所有tag
     *
     * @param id
     * @param accessToken
     * @param perPage
     * @param page
     * @return
     * @throws GitlabException JSONArray
     * @date: 2019年7月24日 上午10:50:29
     */
    @RequestLine("GET /projects/{id}/repository/tags?access_token={accessToken}&per_page={perPage}&page={page}")
    public JSONArray getTagsByProjectIdAndToken(@Param("id") int id, @Param("accessToken") String accessToken,
            @Param("perPage") int perPage, @Param("page") int page) throws GitlabException;

}
