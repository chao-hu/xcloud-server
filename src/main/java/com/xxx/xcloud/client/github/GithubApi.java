package com.xxx.xcloud.client.github;

import com.alibaba.fastjson.JSONArray;
import com.xxx.xcloud.client.github.model.GitHubBranch;
import com.xxx.xcloud.client.github.model.GithubUser;
import feign.Param;
import feign.RequestLine;

import java.util.List;

/**
 * 
 * @author mengaijun
 * @date: 2019年1月3日 下午6:39:31
 */
public interface GithubApi {

	/**
	 * 根据token获取用户信息
	 * @param token
	 * @return GithubUser 
	 * @date: 2019年1月2日 下午5:39:19
	 */
	@RequestLine("GET /user?access_token={access_token}")
	public GithubUser getUserInfoByToken(@Param("access_token")String token);
	
	/**
	 * 获取所有项目
	 * @param username 用户
	 * @param token
	 * @return JSONArray 
	 * @date: 2019年1月2日 下午5:31:11
	 */
	@RequestLine("GET /users/{username}/repos?access_token={access_token}")
	public JSONArray getReposes(@Param("username")String username, @Param("access_token")String token);
	
	/**
	 * 获取项目所有分支
	 * @param username
	 * @param reponame
	 * @param token
	 * @return List<GitHubBranch>
	 * @date: 2019年1月2日 下午5:31:24
	 */
	@RequestLine("GET /repos/{username}/{reponame}/branches?access_token={access_token}")
	public List<GitHubBranch> getBranches(@Param("username")String username, @Param("reponame")String reponame,
			@Param("access_token")String token);
}
