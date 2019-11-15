package com.xxx.xcloud.module.ingress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xxx.xcloud.module.ingress.entity.IngressProxy;

/**
 * @author  LYJ </br>
 * create time：2018年12月5日 下午3:26:33 </br>
 * @version 1.0
 * @since
 */
@Repository
public interface IngressProxyRepository extends JpaRepository<IngressProxy, String>{

}
