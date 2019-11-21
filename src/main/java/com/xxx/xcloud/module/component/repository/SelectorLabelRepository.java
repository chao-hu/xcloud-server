package com.xxx.xcloud.module.component.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.component.entity.SelectorLabel;

/**
 * 
 * <p>Description: SelectorLabel持久化接口</p>
 *
 * @author wangkebiao
 * @date 2019年3月13日
 */
@Repository
public interface SelectorLabelRepository extends JpaRepository<SelectorLabel, String> {
	
	/**
	 * 
	 * <p>Description: 获取当前类别启用的标签</p>
	 *
	 * @param type 服务&组件的类别
	 * @return List<SelectorLabel>
	 */
	@Query("select label from SelectorLabel label where label.type = ?1 and label.enable=1")
	public List<SelectorLabel> findByTypeAndEnableTrue(String type);
}
