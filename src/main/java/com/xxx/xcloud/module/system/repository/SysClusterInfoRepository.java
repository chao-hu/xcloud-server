package com.xxx.xcloud.module.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xxx.xcloud.module.system.entity.SysClusterInfo;

/**
 * @ClassName: SysClusterInfoRepository
 * @Description: SysClusterRepository
 * @author huchao
 * @date 2019年10月24日
 *
 */
public interface SysClusterInfoRepository extends JpaRepository<SysClusterInfo, String> {

}
