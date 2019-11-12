package com.xxx.xcloud.module.devops.scm.service.impl;

import com.xxx.xcloud.common.XcloudProperties;
import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.module.devops.common.DevopsException;
import com.xxx.xcloud.module.devops.model.ScmModel;
import com.xxx.xcloud.module.devops.model.SvnModel;
import com.xxx.xcloud.module.devops.scm.pojo.Locations;
import com.xxx.xcloud.module.devops.scm.pojo.ModuleLocation;
import com.xxx.xcloud.module.devops.scm.pojo.Scm;
import com.xxx.xcloud.module.devops.scm.pojo.WorkspaceUpdater;
import com.xxx.xcloud.module.devops.scm.service.ScmFactory;
import org.springframework.stereotype.Component;

/**
 * @author daien
 * @date 2019年3月15日
 */
@Component("svnFactory")
public class SvnFactory implements ScmFactory {

    private static final String CLAZZ = "hudson.scm.SubversionSCM";

    @Override
    public Scm getScm(ScmModel scmModel) throws DevopsException {
        // check
        SvnModel svnModel = scmModel.getSvnModel();
        if (svnModel == null) {
            throw new DevopsException(500, "Svn配置信息为空");
        }

        String remote = svnModel.getUrl();
        String local = svnModel.getLocal();
        String credentialId = svnModel.getCredentialId();

        ModuleLocation moduleLocation = new ModuleLocation();
        moduleLocation.setRemote(remote);
        moduleLocation.setLocal(local);
        moduleLocation.setCredentialsId(credentialId);
        moduleLocation.setIgnoreExternalsOption(true);
        moduleLocation.setDepthOption("infinity");
        moduleLocation.setCancelProcessOnExternalsFail(true);

        Locations locations = new Locations();
        locations.setModuleLocation(moduleLocation);

        WorkspaceUpdater workspaceUpdater = new WorkspaceUpdater();

        Scm scm = new Scm();
        scm.setClazz(CLAZZ);
        scm.setPlugin(XcloudProperties.getConfigMap().get(Global.DEVOPS_PLUGIN_SVN));
        scm.setIgnoreDirPropChanges(false);
        scm.setFilterChangelog(false);
        scm.setQuietOperation(true);
        scm.setWorkspaceUpdater(workspaceUpdater);
        scm.setLocations(locations);

        return scm;
    }

}
