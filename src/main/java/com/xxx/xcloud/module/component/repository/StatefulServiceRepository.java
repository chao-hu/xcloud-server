package com.xxx.xcloud.module.component.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.StatefulService;


/**
 * @ClassName: StatefulServiceRepository
 * @Description: 组件集群表jpa
 * @author lnn
 * @date 2019年11月21日
 *
 */
@Repository
public interface StatefulServiceRepository extends JpaRepository<StatefulService, String> {

    /**
     * serviceId
     * @param serviceId
     * @return
     */
    public Optional<StatefulService> findById(String serviceId);

    /**
     * serviceId，非serviceState状态
     * 
     * @param serviceId
     * @param state
     * @return
     */
    public StatefulService findByIdAndServiceStateNot(String serviceId, String state);

    /**
     * nameSpace，serviceId，非serviceState状态
     * 
     * @param namespace
     * @param serviceId
     * @param state
     * @return
     */

    public StatefulService findByNamespaceAndIdAndServiceStateNot(String namespace, String serviceId, String state);

    /**
     * nameSpace，appType，serviceName，serviceState状态
     * 
     * @param namespace
     * @param appType
     * @param serviceName
     * @param serviceState
     * @return
     */
    public List<StatefulService> findByNamespaceAndAppTypeAndServiceNameAndServiceState(String namespace,
            String appType, String serviceName, String serviceState);

    /**
     * nameSpace，appType，serviceName，非serviceState状态
     * 
     * @param namespace
     * @param appType
     * @param serviceName
     * @param serviceState
     * @return
     */
    public List<StatefulService> findByNamespaceAndAppTypeAndServiceNameAndServiceStateNot(String namespace,
            String appType, String serviceName, String serviceState);

    /**
     * 租户，组件类型，指定serviceState状态
     * 
     * @param namespace
     * @param appType
     * @param serviceState
     * @return
     */
    public List<StatefulService> findByNamespaceAndAppTypeAndServiceState(String namespace, String appType,
            String serviceState);

    /**
     * 租户，组件类型，指定serviceState状态
     * 
     * @param namespace
     * @param projectId
     * @param appType
     * @param serviceState
     * @return
     */
    public List<StatefulService> findByNamespaceAndProjectIdAndAppTypeAndServiceState(String namespace,
            String projectId, String appType, String serviceState);

    /**
     * 非serviceState状态，上次操作早于某个时间点的集群
     * 
     * @param state
     * @param time
     * @return
     */
    public List<StatefulService> findByServiceStateNotAndLastoptTimeBefore(String state, Date time);

    /**
     * serviceState，上次操作早于某个时间点的集群
     * 
     * @param state
     * @param time
     * @return
     */
    public List<StatefulService> findByServiceStateAndLastoptTimeBefore(String state, Date time);

    /**
     * 租户，非serviceState状态
     * 
     * @param namespace
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceStateNot(String namespace, String serviceState,
            Pageable pageable);

    /**
     * 租户，非serviceState状态
     * 
     * @param namespace
     * @param serviceState
     * @param appType
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndAppTypeAndServiceStateNot(String namespace, String appType,
            String serviceState, Pageable pageable);

    /**
     * 租户，项目id
     * 
     * @param namespace
     * @param serviceState
     * @param projectId
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceStateNot(String namespace, String projectId,
            String serviceState, Pageable pageable);

    /**
     * 租户，项目id
     * 
     * @param namespace
     * @param projectId
     * @param appType
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndAppTypeAndServiceStateNot(String namespace,
            String projectId, String appType, String serviceState, Pageable pageable);

    /**
     * 租户，指定serviceState
     * 
     * @param namespace
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceState(String namespace, String serviceState,
            Pageable pageable);

    /**
     * 租户，指定serviceState
     * 
     * @param namespace
     * @param serviceState
     * @param appType
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceStateAndAppType(String namespace, String serviceState,
            String appType, Pageable pageable);

    /**
     * 租户，集群名称包含,非serviceState状态
     * 
     * @param namespace
     * @param serviceName
     * @param clusterState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceNameContainingAndServiceStateNot(String namespace,
            String serviceName, String clusterState, Pageable pageable);

    /**
     * 租户，集群名称包含,非serviceState状态
     * 
     * @param namespace
     * @param serviceName
     * @param clusterState
     * @param appType
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceNameContainingAndAppTypeAndServiceStateNot(String namespace,
            String serviceName, String appType, String clusterState, Pageable pageable);

    /**
     * 租户，项目id，集群名称包含,非serviceState状态
     * 
     * @param namespace
     * @param projectId
     * @param serviceName
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceNameContainingAndServiceStateNot(String namespace,
            String projectId, String serviceName, String serviceState, Pageable pageable);

    /**
     * 租户，项目id，集群名称包含,非serviceState状态
     * 
     * @param namespace
     * @param projectId
     * @param serviceName
     * @param serviceState
     * @param appType
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceNameContainingAndAppTypeAndServiceStateNot(
            String namespace, String projectId, String serviceName, String appType, String serviceState,
            Pageable pageable);

    /**
     * 租户，项目id，指定serviceState
     * 
     * @param namespace
     * @param projectId
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceState(String namespace, String projectId,
            String serviceState, Pageable pageable);

    /**
     * 租户，项目id，指定serviceState
     * 
     * @param namespace
     * @param projectId
     * @param serviceState
     * @param appType
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceStateAndAppType(String namespace,
            String projectId, String serviceState, String appType, Pageable pageable);

    /**
     * 租户，集群名称包含，指定serviceState
     * 
     * @param namespace
     * @param serviceName
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceNameContainingAndServiceState(String namespace,
            String serviceName, String serviceState, Pageable pageable);

    /**
     * 租户，集群名称包含，指定serviceState
     * 
     * @param namespace
     * @param serviceName
     * @param serviceState
     * @param appType
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndServiceNameContainingAndServiceStateAndAppType(String namespace,
            String serviceName, String serviceState, String appType, Pageable pageable);

    /**
     * 租户，项目id，集群名称包含，指定serviceState状态
     * 
     * @param namespace
     * @param projectId
     * @param serviceName
     * @param serviceState
     * @param pageable
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceNameContainingAndServiceState(String namespace,
            String projectId, String serviceName, String serviceState, Pageable pageable);

    /**
     * 租户，项目id，集群名称包含，指定serviceState状态
     * 
     * @param namespace
     * @param projectId
     * @param serviceName
     * @param serviceState
     * @param pageable
     * @param appType
     * @return
     */
    public Page<StatefulService> findByNamespaceAndProjectIdAndServiceNameContainingAndServiceStateAndAppType(
            String namespace, String projectId, String serviceName, String serviceState, String appType,
            Pageable pageable);

    /**
     * 租户
     * 
     * @param namespace
     * @return
     */
    public List<StatefulService> findByNamespace(String namespace);

    /**
     * 通过租户删除service
     * 
     * @param namespace
     * @return
     */
    public void deleteByNamespace(String namespace);
}
