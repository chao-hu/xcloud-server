package com.xxx.xcloud.module.ingress.apiutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxx.xcloud.client.kubernetes.KubernetesClientFactory;
import com.xxx.xcloud.common.ReturnCode;
import com.xxx.xcloud.common.exception.ErrorMessageException;

import io.fabric8.kubernetes.api.model.extensions.Ingress;

/**
 * Description:IngressApi <br/>
 * date: 2019年9月20日 上午9:57:37 <br/>
 * 
 * @author LYJ
 * @version
 * @since JDK 1.8
 */
public class IngressApi {

	private static final Logger LOG = LoggerFactory.getLogger(IngressApi.class);

	/**
	 * Description:查询Ingress <br/>
	 * 
	 * @author LYJ
	 * @param ingressName
	 *            ingress名称
	 * @param tenantName
	 *            租户名称
	 * @return Ingress
	 */
	public static Ingress getIngress(String ingressName, String tenantName) {
		Ingress k8sIngress = null;
		try {
			k8sIngress = KubernetesClientFactory.getClient().extensions().ingresses().inNamespace(tenantName)
					.withName(ingressName).get();
		} catch (Exception e) {
			k8sIngress = null;
			LOG.error("获取Ingress: " + ingressName + " 失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_GET_INGRESS_FAILED,
					"获取Ingress: " + ingressName + " 失败! error msg:{}");
		}
		return k8sIngress;
	}

	/**
	 * Description:创建Ingress <br/>
	 *
	 * @author LYJ
	 * @param ingress
	 *            k8s资源对象
	 * @param tenantName
	 *            租户名称
	 * @return Ingress
	 */
	public static Ingress createIngress(Ingress ingress, String tenantName) {
		Ingress k8sIngress = null;
		try {
			k8sIngress = KubernetesClientFactory.getClient().extensions().ingresses().inNamespace(tenantName)
					.create(ingress);
		} catch (Exception e) {
			k8sIngress = null;
			LOG.error("服务代理: " + ingress.getMetadata().getName() + " 创建失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
					"服务代理: " + ingress.getMetadata().getName() + " 创建失败!");
		}
		if (null == k8sIngress) {
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
					"服务代理: " + ingress.getMetadata().getName() + " 创建失败!");
		}
		return k8sIngress;
	}

	/**
	 * Description:创建或修改Ingress <br/>
	 *
	 * @author LYJ
	 * @param ingress
	 *            k8s资源对象
	 * @param tenantName
	 *            租户名称
	 * @return Ingress
	 */
	public static Ingress createOrReplaceIngress(Ingress ingress, String tenantName) {
		Ingress k8sIngress = null;
		try {
			k8sIngress = KubernetesClientFactory.getClient().extensions().ingresses().inNamespace(tenantName)
					.createOrReplace(ingress);
		} catch (Exception e) {
			k8sIngress = null;
			LOG.error("服务代理: " + ingress.getMetadata().getName() + " 创建或修改失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
					"服务代理: " + ingress.getMetadata().getName() + " 创建或修改失败!");
		}
		if (null == k8sIngress) {
			throw new ErrorMessageException(ReturnCode.CODE_K8S_CREATE_INGRESS_FAILED,
					"服务代理: " + ingress.getMetadata().getName() + " 创建或修改失败!");
		}
		return k8sIngress;
	}

	/**
	 * Description:删除Ingress <br/>
	 *
	 * @author LYJ
	 * @param ingressName
	 *            ingress名称
	 * @param tenantName
	 *            租户名称
	 */
	public static void deleteIngress(String ingressName, String tenantName) {
		try {
			Boolean deleteIngress = KubernetesClientFactory.getClient().extensions().ingresses().inNamespace(tenantName)
					.withName(ingressName).cascading(true).delete();
			if (deleteIngress) {
				LOG.info("服务代理: " + ingressName + " 删除成功!");
			} else {
				throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_INGRESS_FAILED,
						"服务代理: " + ingressName + " 删除失败!");
			}
		} catch (Exception e) {
			LOG.error("服务代理: " + ingressName + " 删除失败! error msg:{}", e);
			throw new ErrorMessageException(ReturnCode.CODE_K8S_DELETE_INGRESS_FAILED,
					"服务代理: " + ingressName + " 删除失败!");
		}
	}
}
