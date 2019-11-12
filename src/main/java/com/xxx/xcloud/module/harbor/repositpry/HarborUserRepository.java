package com.xxx.xcloud.module.harbor.repositpry;

import com.xxx.xcloud.module.harbor.entity.HarborUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HarborUserRepository extends JpaRepository<HarborUser, Integer> {

    public HarborUser findByTenantName(String tenantName);

}
