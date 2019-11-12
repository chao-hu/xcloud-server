package com.xxx.xcloud.module.ci.repository;

import com.xxx.xcloud.module.ci.entity.CiFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author mengaijun
 *
 * @date: 2018年12月7日 下午2:27:00
 */
@Repository
public interface CiFileRepository extends JpaRepository<CiFile, String> {
	/**
	 * 根据ciId获取
	 * @param ciId 
	 * @return CiFile 
	 * @date: 2019年1月3日 下午6:09:52
	 */
	@Query("select c from CiFile c where c.ciId=?1")
	CiFile getByCiId(String ciId);
	
	/**
	 * 根据ciId删除记录
	 * @param ciId ciId
	 * @date: 2019年1月3日 下午6:10:03
	 */
	@Query("delete from CiFile c where c.ciId=?1")
	@Modifying
	void deleteByCiId(String ciId);
}
