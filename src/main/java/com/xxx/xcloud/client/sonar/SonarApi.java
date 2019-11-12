package com.xxx.xcloud.client.sonar;

import com.xxx.xcloud.client.sonar.exception.SonarException;
import com.xxx.xcloud.client.sonar.model.*;
import com.xxx.xcloud.module.sonar.model.SonarCheckResultSummary;
import feign.Param;
import feign.RequestLine;

import java.util.List;

/**
 *
 * @author mengaijun
 * @Description: sonar接口
 * @date: 2018年12月10日 上午9:28:55
 */
public interface SonarApi {

	/**
	 * 获取sonar支持的语言
	 *
	 * @return List<SonarLanguage> sonar支持的语言
	 * @date: 2018年12月24日 下午4:25:52
	 * @throws SonarException
	 */
	@RequestLine("GET /languages/list")
	public SonarLanguages getLanguages() throws SonarException;

	/**
     * 查询所有规则集
     *
     * @return QualityProfiles
     * @date: 2018年12月20日 上午10:40:09
     * @throws SonarException
     */
    @RequestLine("GET /qualityprofiles/search")
    QualityProfiles searchQualityprofiles() throws SonarException;

    /**
     * 查询规则集
     *
     * @param language
     *            规则集语言
     * @param profileName
     * @param projectKey
     * @return QualityProfiles
     * @date: 2018年12月20日 上午10:40:09
     * @throws SonarException
     */
    @RequestLine("GET /qualityprofiles/search?language={language}&qualityProfile={qualityProfile}&project={project}")
    QualityProfiles searchQualityprofiles(@Param("language") String language,
            @Param("qualityProfile") String qualityProfile, @Param("project") String project) throws SonarException;

    /**
     * 根据信息查询规则集(参数不需要的话, 传null)
     *
     * @param language
     *            语言
     * @param profileKey
     * @param profileName
     * @return QualityProfileInheritance
     * @throws SonarException
     * @date: 2018年12月20日 上午10:40:22
     */
	@RequestLine("GET /qualityprofiles/inheritance?language={language}&profileKey={profileKey}&profileName={profileName}")
	public QualityProfileInheritance getQualityprofilesInheritance(@Param("language") String language,
            @Param("profileKey") String profileKey, @Param("profileName") String profileName)
			throws SonarException;
	/**
	 * 根据key查询规则集
	 *
	 * @param profileKey
	 * @return QualityProfileInheritance
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:22
	 */
	@RequestLine("GET /qualityprofiles/inheritance?profileKey={profileKey}")
	public QualityProfileInheritance getQualityprofilesInheritance(@Param("profileKey") String profileKey)
			throws SonarException;

	/**
	 * 根据语言和名称查询规则集
	 * @param language
	 * @param profileName
	 * @return QualityProfileInheritance
	 * @throws SonarException
	 * @date: 2019年3月7日 下午5:11:02
	 */
	@RequestLine("GET /qualityprofiles/inheritance?language={language}&profileName={profileName}")
	public QualityProfileInheritance getQualityprofilesInheritance(@Param("language") String language,
            @Param("profileName") String profileName)
			throws SonarException;


    /**
     * search
     *
     * @param qprofile
     *            规则集key
     * @param activation
     *            是否激活
     * @param p
     *            第p页, 默认为1
     * @param ps
     *            ps条记录, Must be greater than 0 and less than 500
     * @param s
     *            排序字段
     * @param severities
     *            INFO,MINOR,MAJOR,CRITICAL,BLOCKER
     * @param languages
     *             java，js，
     * @param types
     *             CODE_SMELL,BUG,VULNERABILITY
     * @return Rules
     * @throws SonarException
     * @date: 2018年12月20日 上午10:40:29
     */
    @RequestLine("GET /rules/search?qprofile={qprofile}&activation={activation}&p={p}&ps={ps}&severities={severities}&languages={languages}&types={types}")
    public Rules searchRules(@Param("qprofile") String qprofile, @Param("activation") String activation,
            @Param("p") Integer p, @Param("ps") Integer ps, @Param("severities") String severities,
            @Param("languages") String languages, @Param("types") String types);

    /**
     * 查询规则
     *
     * @param p
     * @param ps
     * @param languages
     * @param facets
     * @return Rules
     * @date: 2019年5月21日 下午3:58:38
     */
    @RequestLine("GET /rules/search?p={p}&ps={ps}&languages={languages}&facets={facets}")
    public Rules searchRules(@Param("p") Integer p, @Param("ps") Integer ps, @Param("languages") String languages,
            @Param("facets") String facets);

	/**
	 * 激活规则
	 *
	 * @param key
	 *            规则集key
	 * @param rule
	 *            规则key
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:33
	 */
	@RequestLine("POST /qualityprofiles/activate_rule?key={key}&rule={rule}")
	public void activateRule(@Param("key") String key, @Param("rule") String rule);

	/**
	 * 取消规则
	 *
	 * @param key
	 *            规则集key
	 * @param rule
	 *            规则key
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:37
	 */
	@RequestLine("POST /qualityprofiles/deactivate_rule?key={key}&rule={rule}")
    public void deactivateRule(@Param("key") String key, @Param("rule") String rule);


	/**
	 * 创建规则集
	 *
	 * @param language
	 *            规则集语言类型
	 * @param name
	 *            规则集名称
	 * @return QualityProfileInheritance 创建返回信息
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:45
	 */
	@RequestLine("POST /qualityprofiles/create?language={language}&name={name}")
	public QualityProfileInheritance createQualityProfiles(@Param("language") String language,
            @Param("name") String name) throws SonarException;

	/**
	 * 复制一份规则集
	 * @param fromKey 规则集key
	 * @param toName 复制的新的规则集的名称
	 * @throws SonarException
	 * void
	 * @date: 2019年3月7日 下午3:25:27
	 */
	@RequestLine("POST qualityprofiles/copy?fromKey={fromKey}&toName={toName}")
	public void copyQualityProfiles(@Param("fromKey") String fromKey, @Param("toName") String toName)throws SonarException ;

	/**
	 * 规则集重命名
	 * @param key 规则集key
	 * @param name 修改后的规则集的名称
	 * @throws SonarException
	 * void
	 * @date: 2019年3月7日 下午3:25:27
	 */
    @RequestLine("POST qualityprofiles/rename?key={key}&name={name}")
	public void renameQualityProfiles(@Param("key") String key, @Param("name") String name)throws SonarException ;

	/**
	 * 删除规则集记录
	 *
	 * @param profileKey
	 *            key
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:48
	 */
	@RequestLine("POST /qualityprofiles/delete?profileKey={profileKey}")
	public void deleteQualityProfiles(@Param("profileKey") String profileKey) throws SonarException;

	/**
	 * 根据语言查询
	 *
	 * @param language
	 * @return List<QualityProfile>
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:13
	 */
	@RequestLine("GET /qualityprofiles/search?language={language}")
	public List<SonarQualityProfile> searchQualityprofilesByLanguage(@Param("language") String language);

	/**
	 * 根据名称查询规则集
	 *
	 * @param profileName
	 * @return List<QualityProfile>
	 * @throws SonarException
	 * @date: 2018年12月20日 上午10:40:17
	 */
	@RequestLine("GET /qualityprofiles/search?profileName={profileName}")
	public List<SonarQualityProfile> searchQualityprofilesByProfileName(@Param("profileName") String profileName);

	/**
	 * 查询检查结果
	 * @param p 第p页, 从1开始
	 * @param ps 每页ps条
	 * @param projectKeys 项目
	 * @return List<Object>
	 * @throws SonarException
	 * @date: 2019年1月2日 下午2:50:39
	 */
	@RequestLine("GET /issues/search?p={p}&ps={ps}"
            + "&s=FILE_LINE&asc=true&additionalFields=_all&facets=types,resolutions,severities"
			+ "&resolved=false&types={types}"
            + "&severities={severities}" +
            "&componentKeys={componentKeys}")
    SonarCheckResult searchIssues(@Param("p") int p, @Param("ps") int ps, @Param("componentKeys") String componentKeys,
            @Param("types") String types, @Param("severities") String severities)throws SonarException;

    /**
     * 获取sonar检查结果（INFO,MINOR,MAJOR,CRITICAL,BLOCKER）
     * 
     * @param componentKeys
     * @return JSONObject
     * @date: 2019年8月14日 上午10:35:38
     */
    @RequestLine("GET /issues/search?p=1&ps=1" + "&componentKeys={componentKeys}" + "&facets=severities&resolved=false")
    SonarCheckResult searchIssues(@Param("componentKeys") String componentKeys);

    /**
     * 获取代码检查结果
     *
     * @param component
     * @throws SonarException
     *             void
     * @date: 2019年5月8日 下午6:03:08
     */
    @RequestLine("GET measures/component?componentKey={componentKey}"
            + "&metricKeys=bugs,vulnerabilities,code_smells,reliability_rating,ncloc"
            + "&additionalFields=metrics,periods")
    SonarCheckResultSummary getCheckResult(@Param("componentKey") String componentKey) throws SonarException;
}
