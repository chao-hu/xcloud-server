package com.xxx.xcloud.client.gitlab;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxx.xcloud.client.gitlab.exception.GitlabException;

import feign.Param;
import feign.RequestLine;

/**
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月20日 上午11:31:29
 */
public interface GitlabApi {
    /**
     * 根据用户名密码获取token
     *
     * @param grantType
     *            "password", 表示是根据用户名密码获取token
     * @param username
     *            用户名
     * @param password
     *            密码
     * @return String
     * @throws GitlabException
     * @date: 2018年12月20日 下午8:51:32
     */
    @RequestLine("POST /oauth/token?grant_type={grant_type}&username={username}&password={password}")
    public JSONObject getToken(@Param("grant_type") String grantType, @Param("username") String username,
            @Param("password") String password) throws GitlabException;

    /**
     * 获取项目所有的分支
     *
     * @param id
     *            项目ID
     * @param accessToken
     *            token
     * @param prePage
     *            一页多少条
     * @param page
     *            显示第page也
     * @return JSONArray
     * @throws GitlabException
     * @date: 2018年12月20日 下午8:56:40
     */
    @RequestLine("GET /projects/{id}/repository/branches?access_token={accessToken}&per_page={perPage}&page={page}")
    public JSONArray getBranchsByProjectIdAndToken(@Param("id") int id, @Param("accessToken") String accessToken,
            @Param("perPage") int perPage, @Param("page") int page) throws GitlabException;

    /**
     * 获取所有有权限的项目
     *
     * @param accessToken
     *            token
     * @param prePage
     *            一页多少条
     * @param page
     *            显示第page也
     * @return JSONArray
     * @throws GitlabException
     * @date: 2018年12月20日 下午8:57:08
     */
    @RequestLine("GET /projects?access_token={accessToken}&owned=true&per_page={perPage}&page={page}")
    public JSONArray getProkectsByToken(@Param("accessToken") String accessToken, @Param("perPage") int perPage,
            @Param("page") int page) throws GitlabException;

    /**
     * 获取项目所有tag
     *
     * @param id
     * @param accessToken
     * @param perPage
     * @param page
     * @return
     * @throws GitlabException
     *             JSONArray
     * @date: 2019年7月24日 上午10:50:29
     */
    @RequestLine("GET /projects/{id}/repository/tags?access_token={accessToken}&per_page={perPage}&page={page}")
    public JSONArray getTagsByProjectIdAndToken(@Param("id") int id, @Param("accessToken") String accessToken,
            @Param("perPage") int perPage, @Param("page") int page) throws GitlabException;

}
