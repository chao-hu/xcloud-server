package com.xxx.xcloud.module.ceph.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxx.xcloud.module.ceph.entity.CephRbd;
import com.xxx.xcloud.module.ceph.entity.CephSnap;
import com.xxx.xcloud.module.ceph.entity.ServiceCephRbd;
import com.xxx.xcloud.module.ceph.entity.SnapStrategy;

/**
 * 
 * <p>
 * Description: 块存储操作接口
 *     创建租户时，需要调用createCephRbdPool方法为租户创建块存储的Pool
 *     删除租户时，需要调用destroyCephRbdPool释放租户块存储Pool
 * </p>
 *
 * @author wangkebiao
 * @date 2019年10月30日
 */
public interface CephRbdService {

    /**
     * 创建块存储Pool
     * @Title: createCephRbdPool
     * @Description: 根据指定的租户名称，创建块存储Pool,每个租户有一个与租户同名的Pool
     * @param tenantName 租户名
     * @return boolean 
     * @throws
     */
    boolean createCephRbdPool(String tenantName);

    /**
     * 删除Pool
     * @Title: destroyCephRbdPool
     * @Description: 根据租户名称删除Pool
     * @param tenantName 租户名称
     * @return boolean 
     * @throws
     */
    boolean destroyCephRbdPool(String tenantName);

    /**
     * 新增块存储
     * @Title: add
     * @Description: 新增块存储 
     * @param tenantName 租户名
     * @param createdBy 创建者
     * @param projectId 项目信息
     * @param rbdName 块名称
     * @param size 大小（单位为M）
     * @param description 块存储描述
     * @return boolean 
     * @throws
     */
    boolean add(String tenantName, String createdBy, String projectId, String rbdName, double size, String description);

    /**
     * 获取块存储详情
     * @Title: get
     * @Description: 获取块存储详情
     * @param cephRbdId 块存储id
     * @return CephRbd 
     * @throws
     */
    CephRbd get(String cephRbdId);

    /**
     * 块存储列表(模糊查询)
     * @Title: list
     * @Description: 块存储列表(模糊查询)
     * @param tenantName 租户名
     * @param name 块存储名称
     * @param projectId 项目信息
     * @param pageable
     * @return Page<CephRbd> 
     * @throws
     */
    Page<CephRbd> list(String tenantName, String name, String projectId, Pageable pageable);

    /**
     * 可用的块存储列表（即没有被服务挂载）
     * @Title: listAvailable
     * @Description: 可用的块存储列表（即没有被服务挂载）
     * @param tenantName 租户名称
     * @return List<CephRbd> 
     * @throws
     */
    List<CephRbd> listAvailable(String tenantName);

    /**
     * 项目内可用的块存储列表（即没有被服务挂载）
     * @Title: listAvailableInProject
     * @Description: 项目内可用的块存储列表（即没有被服务挂载）
     * @param projectId 项目信息
     * @return List<CephRbd> 
     * @throws
     */
    List<CephRbd> listAvailableInProject(String projectId);

    /**
     * 删除块存储
     * @Title: delete
     * @Description: 删除块存储
     * @param cephRbdId 块存储id
     * @return boolean 
     * @throws
     */
    boolean delete(String cephRbdId);

    /**
     * 块存储扩容（只能增加容量）
     * @Title: resize
     * @Description: 块存储扩容（只能增加容量）
     * @param cephRbdId  块存储Id
     * @param size 大小（单位为M）
     * @return boolean 
     * @throws
     */
    boolean resize(String cephRbdId, double size);

    /**
     * 创建快照
     * @Title: createSnap
     * @Description: 创建快照
     * @param cephRbdId 块存储ID
     * @param snapName 快照名称
     * @param description  快照描述
     * @return boolean 
     * @throws
     */
    boolean createSnap(String cephRbdId, String snapName, String description);

    /**
     * 删除快照
     * @Title: deleteSnap
     * @Description: 删除快照
     * @param cephRbdId 块存储ID
     * @param snapId 快照ID
     * @return boolean 
     * @throws
     */
    boolean deleteSnap(String cephRbdId, String snapId);

    /**
     * 快照回滚
     * @Title: snapRollBack
     * @Description: 快照回滚
     * @param cephRbdId 块存储ID
     * @param snapId 快照ID
     * @return boolean 
     * @throws
     */
    boolean snapRollBack(String cephRbdId, String snapId);

    /**
     * 指定块的快照列表
     * @Title: snapList
     * @Description: 指定块的快照列表
     * @param cephRbdId 块存储ID
     * @return List<CephSnap> 
     * @throws
     */
    List<CephSnap> snapList(String cephRbdId);

    /**
     * 快照策略详情
     * @Title: getSnapStrategy
     * @Description: 快照策略详情
     * @param cephRbdId 块存储ID
     * @return SnapStrategy 
     * @throws
     */
    SnapStrategy getSnapStrategy(String cephRbdId);

    /**
     * 增加快照策略
     * @Title: addSnapStrategy
     * @Description: 增加快照策略
     * @param cephRbdId 块存储ID
     * @param week 周几字符串（周日至周六分别由0,1.....6代表，例：周三周四与周日由字符串"3,4,0"表示）
     * @param time 时间（0点至23点，例：7点8点22点23点0点由字符串“7,8,22,23,0”表示）
     * @param endDate 截止日期
     * @param status  状态 ：0/1（0：停止状态，1：运行状态）
     * @return boolean 
     * @throws
     */
    boolean addSnapStrategy(String cephRbdId, String week, String time, Date endDate, int status);

    /**
     * 更新快照策略
     * @Title: updateSnapStrategy
     * @Description: 更新快照策略
     * @param cephRbdId 块存储ID
     * @param week 周几字符串（周一至周日分别由0,1.....6代表，例：周三周四与周日由字符串"2,3,6"表示）
     * @param time 时间（0点至23点，例：7点8点22点23点0点由字符串“7,8,22,23,0”表示）
     * @param endDate  截止日期
     * @param status 状态 ：0/1（0：停止状态，1：运行状态）
     * @return boolean 
     * @throws
     */
    boolean updateSnapStrategy(String cephRbdId, String week, String time, Date endDate, int status);

    /**
     * 删除快照策略
     * @Title: deleteSnapStrategy
     * @Description: 删除快照策略
     * @param cephRbdId 块存储ID
     * @return boolean 
     * @throws
     */
    boolean deleteSnapStrategy(String cephRbdId);

    /**
     * 获取服务挂载的块存储
     * @Title: mountInService
     * @Description: 获取服务挂载的块存储
     * @param serviceId 服务ID
     * @return List<ServiceCephRbd> 
     * @throws
     */
    List<ServiceCephRbd> mountInService(String serviceId);

    /**
     * 保存块存储在指定服务的挂载
     * @Title: mountSave
     * @Description: 保存块存储在指定服务的挂载
     * @param id 主键ID
     * @param serviceId 服务id 
     * @param cephRbdId 块存储ID
     * @param mountPath 挂载路径
     * @throws
     */
    void mountSave(String id, String serviceId, String cephRbdId, String mountPath);

    /**
     * 取消块存储在指定服务的挂载
     * @Title: mountCancel
     * @Description: 取消块存储在指定服务的挂载
     * @param serviceId 服务id
     * @param cephRbdId 快存储id
     * @throws
     */
    void mountCancel(String serviceId, String cephRbdId);

    /**
     * 清除指定服务的块存储挂载
     * @Title: mountClear
     * @Description: 清除指定服务的块存储挂载
     * @param serviceId 服务id 
     * @throws
     */
    void mountClear(String serviceId);
}
