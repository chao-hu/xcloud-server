package com.xxx.xcloud.module.devops.publishers.service;

import com.xxx.xcloud.module.devops.model.Job;
import com.xxx.xcloud.module.devops.publishers.pojo.Publishers;

/**
 * @author daien
 * @date 2019年3月15日
 */
public interface PublishersService {

    Publishers getPublishers(Job jobModel);

}
