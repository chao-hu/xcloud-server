/*
 * 文件名：PodTopo.java
 * 版权：Copyright by bonc
 * 描述：
 * 修改人：zhoutao
 * 修改时间：2016年10月11日
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */

package com.xxx.xcloud.module.ceph.pojo;

import java.util.Date;

import lombok.Data;


/**
 * @ClassName: FileUploadProgress
 * @Description: 文件上传
 * @author wangkebiao
 * @date 2019年11月11日
 *
 */
@Data
public class FileUploadProgress {

	/**
	 * uuid;
	 */
	private String uuid;

	/**
	 * fileName
	 */
	private String fileName;

	/**
	 * size;
	 */
	private long size;

	/**
	 * read;
	 */
	private long read;

	/**
	 * creatTime;
	 */
	private Date creatTime;
	
}
