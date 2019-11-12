package com.xxx.xcloud.module.devops.scm.service.impl;

import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.ScmModel;
import com.xxx.xcloud.module.devops.scm.pojo.Scm;
import com.xxx.xcloud.module.devops.scm.service.ScmFactory;
import com.xxx.xcloud.module.devops.scm.service.ScmService;
import org.springframework.stereotype.Service;

/**
 * @author daien
 * @date 2019年3月15日
 */
@Service
public class ScmServiceImpl implements ScmService {

    @Override
    public Scm getScm(ScmModel scmModel) throws DevopsException {
        if (scmModel == null) {
            return null;
        }
        ScmFactory scmFactory = ScmFactoryStrategy.getScmFactory(scmModel.getScmType());
        return scmFactory == null ? null : scmFactory.getScm(scmModel);
    }

}
