package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.DockerfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author xujiangpeng
 * @ClassName:
 * @Description: (这里用一句话描述这个类的作用)
 * @date
 */
@Repository
public interface DockerfileTypeRepository extends JpaRepository<DockerfileType, String> {

}
