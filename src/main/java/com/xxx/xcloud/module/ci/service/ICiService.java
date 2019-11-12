package com.xxx.xcloud.module.ci.service;

import com.xxx.xcloud.module.ci.entity.Ci;
import com.xxx.xcloud.module.ci.entity.CiFile;
import com.xxx.xcloud.module.ci.entity.CiRecord;
import com.xxx.xcloud.module.ci.entity.CodeInfo;
import com.xxx.xcloud.module.ci.model.CiDetail;
import com.xxx.xcloud.module.image.model.ImageDetail;
import org.apache.commons.codec.language.bm.Lang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xujiangpeng
 * @ClassName:
 * @Description: 镜像构建接口
 * @date
 * @todo ImageBuildService\sourceBuild\packageBuild
 */
public interface ICiService {

    /**
     * 新增构建任务(基于源代码)
     *
     * @param ci
     * @throws
     * @Title: addSourceBuild
     * @Description:
     */
    Ci addCodeCi(Ci ci, CodeInfo codeInfo, CiFile ciFile);

    /**
     * 新增构建任务(基于部署包)
     *
     * @param ci
     * @throws
     * @Title: addSourceBuild
     * @Description:
     */
    Ci addDockerfileCi(Ci ci, CiFile ciFile);

    /**
     * 启动构建
     *
     * @param ciId      记录ID
     * @param createdBy 构建者
     * @return boolean 是否启动成功
     * @date: 2018年12月3日 下午2:50:05
     */
    boolean startCi(String ciId, String createdBy);

    /**
     * 停止构建
     *
     * @param ciId 记录ID
     * @return boolean 是否停止成功
     * @date: 2018年12月3日 下午2:50:05
     */
    boolean stopCi(String ciId);

    /**
     * 修改dockerfile构建
     *
     * @param ciModify
     * @param ciFileModify
     * @return boolean 是否成功
     * @date: 2018年12月18日 下午4:50:17
     */
    boolean modifyDockerfileCi(Ci ciModify, CiFile ciFileModify);

    /**
     * 修改构建
     *
     * @param ciModify
     * @param codeInfoModify
     * @param ciFileModify
     * @return boolean 修改是否成功
     * @date: 2018年12月3日 下午3:04:56
     */
    boolean modifyCodeCi(Ci ciModify, CodeInfo codeInfoModify, CiFile ciFileModify);

    /**
     * 删除构建
     *
     * @param ciId 记录ID
     * @return boolean 是否删除成功
     * @date: 2018年12月3日 下午2:50:05
     */
    boolean deleteCi(String ciId);

    /**
     * 删除单条构建记录
     *
     * @param id 构件记录ID
     * @return boolean 是否删除成功
     * @date: 2018年12月17日 下午2:36:52
     */
    boolean deleteCiRecord(String id);

    /**
     * 获取Ci列表
     *
     * @param tenantName 租户
     * @param projectId  项目ID
     * @param ciName
     * @param pageable   分页参数
     * @return Page<Ci>
     * @date: 2018年12月3日 下午3:13:36
     */
    Page<Ci> getCis(String tenantName, String projectId, String ciName, Pageable pageable);

    /**
     * 获取Ci列表
     *
     * @param tenantName 租户
     * @param projectId  项目ID
     * @param ciType     构建类型
     * @param ciName
     * @param pageable   分页参数
     * @return Page<Ci>
     * @date: 2018年12月3日 下午3:13:36
     */
    Page<Ci> getCis(String tenantName, String projectId, byte ciType, String ciName, Pageable pageable);

    /**
     * 根据租户名称和项目ID获取构建列表
     *
     * @param tenantName
     * @param projectId
     * @return
     */
    List<Ci> getCiList(String tenantName, String projectId);

    /**
     * 获取所有的构建任务
     *
     * @return
     * @Description
     */
    List<Ci> getAllCi();

    /**
     * 根据ID获取Ci
     *
     * @param ciId
     * @return Ci
     * @date: 2018年12月3日 下午3:15:21
     */
    Ci getCi(String ciId);

    /**
     * 获取最新的构建日志信息
     *
     * @param ciId
     * @return CiRecord 构建日志
     * @date: 2018年12月3日 下午3:23:30
     */
    CiRecord getCiRecordLatest(String ciId);

    // /**
    // * 获取所有构建日志信息
    // *
    // * @param ciId
    // * @return List<CiRecord> 构建日志
    // * @date: 2018年12月3日 下午3:23:30
    // */
    // List<CiRecord> getCiRecords(String ciId);

    /**
     * 获取构建日志信息分页
     *
     * @param ciId
     * @param pageable
     * @return Page<CiRecord> 构建日志分页
     * @date: 2018年12月3日 下午3:23:30
     */
    Page<CiRecord> getCiRecords(String ciId, Pageable pageable);

    /**
     * 获取构建记录详情
     *
     * @param ciId
     * @return CiDetail
     * @date: 2018年12月3日 下午3:25:48
     */
    CiDetail getCiDetail(String ciId);

    /**
     * 获取构建记录的构建详情
     *
     * @param ciId
     * @return CiDetail
     * @date: 2018年12月17日 上午11:28:30
     */
    CiDetail getCiConstructDetail(String ciId);

    /**
     * 事务内更新构建信息, 参数不为空, 更新; 为空, 不操作
     *
     * @param ci
     * @param ciFile
     * @param codeInfo
     * @param ciRecord
     * @param imageDetail
     * @date: 2018年12月17日 上午10:13:38
     */
    void updateCiInfoTransactional(Ci ci, CiFile ciFile, CodeInfo codeInfo, CiRecord ciRecord, ImageDetail imageDetail);

    /**
     * 在CiRecord记录中的logPrint字段添加一行记录信息
     *
     * @param ciRecord 构建记录
     * @param msg      记录信息
     * @date: 2018年12月14日 下午8:21:26
     */
    void addLogPrint(CiRecord ciRecord, String msg);

    /**
     * 获取语言版本信息
     *
     * @param langType java|go|python, 默认为java
     * @return List<Lang>
     * @date: 2018年12月21日 下午8:54:17
     */
    List<Lang> getLangsByType(String langType);

    /**
     * 根据构建名称和类型获取Ci对象(可以验证构建名称是否已经存在)
     *
     * @param tenantName
     * @param ciName     构件名称
     * @param ciType     构建类型
     * @return Ci
     * @date: 2018年12月14日 下午5:13:15
     */
    public Ci getCiByCiNameType(String tenantName, String ciName, Byte ciType);


    /**
     * 获取所有需要定时执行的构建
     *
     * @return List<Ci>
     * @date: 2019年1月3日 下午4:03:42
     */
    List<Ci> getCisCronIsNotNull();

    /**
     * 添加代码构建定时任务
     *
     * @param ci
     * @return boolean
     * @date: 2019年1月3日 下午2:53:19
     */
    boolean addCodeCiQuartz(Ci ci);

    /**
     * 根据镜像名称和版本信息获取构建信息
     *
     * @param tenantName
     * @param imageName
     * @param imageVersion
     * @return Ci
     * @date: 2019年1月7日 下午2:36:30
     */
    Ci getCiByImageNameAndVersion(String tenantName, String imageName, String imageVersion);



    /**
     * 禁用构建
     *
     * @param id 记录ID
     * @return boolean
     * @date: 2019年4月22日 上午9:29:16
     */
    boolean disableCi(String id);

    /**
     * 启用构建
     *
     * @param id 记录ID
     * @return boolean
     * @date: 2019年4月22日 上午9:31:08
     */
    boolean enableCi(String id);

    /**
     * 保存Jenkins的job信息
     *
     * @param job
     * @param originalJobName 为空：表示新建job；
     *                        不为空：（1）originalJobName等于更新的job的名称，更新（2）不等于，更新job名称，更新新的job
     * @param jobType
     * @date: 2019年4月16日 下午2:36:36
     */
    void saveJenkinsJob(com.xxx.xcloud.module.devops.model.Job job, String originalJobName, String jobType);

    /**
     * 获取状态在构建中的任务
     *
     * @return List<Ci>
     * @date: 2019年6月19日 上午9:43:51
     */
    List<Ci> getCisStatusIng();

    /**
     * 重启构建任务
     *
     * @param ci
     * @date: 2019年6月19日 上午10:05:48
     */
    void restartCi(Ci ci);

    /**
     * 根据CiId返回CiFile对象
     *
     * @param ciId
     * @return
     * @Description
     */
    CiFile getCiFileByCiId(String ciId);

    /**
     * 获取镜像构建次数统计信息（当天）
     *
     * @param tenantNameSet
     * @param imageNameSet
     * @return List<Map < String, Object>>
     * @date: 2019年8月19日 下午3:16:05
     */
    List<Map<String, Object>> getCiStatisticsToday(Set<String> tenantNameSet, Set<String> imageNameSet);

    /**
     * 获取镜像构建次数统计信息
     *
     * @param tenantNameSet
     * @param imageNameSet
     * @return List<Map < String, Object>>
     * @date: 2019年8月19日 下午3:16:05
     */
    List<Map<String, Object>> getCiStatistics(Set<String> tenantNameSet, Set<String> imageNameSet);

    /**
     * 获取服务的构建统计信息
     *
     * @param serviceId
     * @return Map<String, Object>
     * @date: 2019年9月3日 上午10:14:22
     */
    Map<String, Object> getCiStatistics(String serviceId);
}
