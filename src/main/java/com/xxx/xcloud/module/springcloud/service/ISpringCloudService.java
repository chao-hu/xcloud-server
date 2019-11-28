package com.xxx.xcloud.module.springcloud.service;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.module.springcloud.entity.SpringCloudService;

/**
 * @ClassName: ISpringCloudService
 * @Description: spring cloud 服务接口
 * @author lnn
 * @date 2019年11月26日
 *
 */
public interface ISpringCloudService {

    public ApiResult create(String serviceId);

    public ApiResult stop(String serviceId);

    public ApiResult start(String serviceId);

    public ApiResult delete(String serviceId);

    public SpringCloudService findById(String serviceId) throws Exception;

}
