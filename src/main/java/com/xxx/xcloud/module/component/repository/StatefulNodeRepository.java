package com.xxx.xcloud.module.component.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.StatefulNode;

/**
 * 组件节点表jpa
 * 
 * @author LiuYue
 * @date 2018年12月12日
 */
@Repository
public interface StatefulNodeRepository extends JpaRepository<StatefulNode, String> {

    /**
     * 获取某个集群下指定名字的节点
     * 
     * @param serviceId
     * @param nodeName
     * @return
     */
    public StatefulNode findByServiceIdAndNodeName(String serviceId, String nodeName);

    /**
     * 获取某个集群下指定名字，nodeState状态的节点
     * 
     * @param serviceId
     * @param nodeName
     * @param nodeState
     * @return
     */
    public StatefulNode findByServiceIdAndNodeNameAndNodeState(String serviceId, String nodeName, String nodeState);

    /**
     * 获取某个集群下指定名字，非nodeState状态的节点
     * 
     * @param serviceId
     * @param nodeName
     * @param nodeState
     * @return
     */
    public StatefulNode findByServiceIdAndNodeNameAndNodeStateNot(String serviceId, String nodeName, String nodeState);

    /**
     * 获取指定nodeId的节点
     * 
     * @param nodeId
     * @param nodeState
     * @return
     */
    public StatefulNode findByIdAndNodeStateNot(String nodeId, String nodeState);

    /**
     * serviceId,非nodeState
     * 
     * @param serviceId
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndNodeStateNot(String serviceId, String nodeState);

    /**
     * 获取所有非nodeState状态的节点
     * 
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByNodeStateNot(String nodeState);

    /**
     * serviceId,指定nodeState
     * 
     * @param serviceId
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndNodeState(String serviceId, String nodeState);

    /**
     * serviceId，role，非nodeState
     * 
     * @param serviceId
     * @param role
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndRoleAndNodeStateNot(String serviceId, String role, String nodeState);

    /**
     * serviceId，role，nodeState
     * 
     * @param serviceId
     * @param role
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndRoleAndNodeState(String serviceId, String role, String nodeState);

    /**
     * serviceId,nodeNameContaining,nodeState
     * 
     * @param serviceId
     * @param nodeName
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndNodeNameContainingAndNodeState(String serviceId, String nodeName,
            String nodeState);

    /**
     * serviceId,nodeNameContaining,非nodeState
     * 
     * @param serviceId
     * @param nodeName
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndNodeNameContainingAndNodeStateNot(String serviceId, String nodeName,
            String nodeState);

    /**
     * serviceId,nodeNameContaining,role,指定nodeState
     * 
     * @param serviceId
     * @param nodeName
     * @param role
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndNodeNameContainingAndRoleAndNodeState(String serviceId, String nodeName,
            String role, String nodeState);

    /**
     * serviceId,nodeNameContaining,role,非nodeState
     * 
     * @param serviceId
     * @param nodeName
     * @param role
     * @param nodeState
     * @return
     */
    public List<StatefulNode> findByServiceIdAndNodeNameContainingAndRoleAndNodeStateNot(String serviceId,
            String nodeName, String role, String nodeState);

    /**
     * 通过serviceId删除记录
     * @param serviceId
     * @return
     */
    void deleteByServiceId(String serviceId);

}
