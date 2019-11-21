package com.xxx.xcloud.module.component.service.worker.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.MemoryCpu;
import com.xxx.xcloud.module.component.model.ftp.FtpCluster;

@Service
@Scope("prototype")
public class FtpClusterChangeResourceWorker extends BaseFtpClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(FtpClusterChangeResourceWorker.class);

    @Override
    public void execute() {

        LOG.info("===============FtpClusterChangeResourceWorker====================");

        String serviceId = data.get("serviceId");

        String tenantName = data.get("tenantName");

        // 1、获取旧的service
        StatefulService service = null;
        service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("修改资源时根据serviceId获取statefulService为null");
            return;
        }

        checkChangeResource(tenantName, service);

        componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(tenantName, service.getId(),
                service.getServiceName());

    }

    private void checkChangeResource(String tenantName, StatefulService service) {
        String cpuString = data.get("cpu");
        String memoryString = data.get("memory") + CommonConst.UNIT_GI;
        String capacityString = data.get("capacity") + CommonConst.UNIT_GI;

        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(CommonConst.THREAD_SLEEP_TIME);
                LOG.info("已经用时：" + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                LOG.error("线程休眠异常！", e);
                break;
            }
            if (System.currentTimeMillis() - start > CommonConst.COMPONENT_OPERATION_TIMEOUT) {
                LOG.error("等待修改资源超时，service：" + JSON.toJSONString(service));
                break;
            }
            FtpCluster ftpCluster = componentOperationsClientUtil.getFtpCluster(tenantName, service.getServiceName());
            LOG.info("判断ftpCluster中数据是否存在");
            LOG.info("ftpCluster:" + JSON.toJSONString(ftpCluster));

            if (null != ftpCluster && null != ftpCluster.getSpec() && null != ftpCluster.getSpec().getResources()
                    && ftpCluster.getStatus().isResourceupdateneedrestart()) {
                MemoryCpu resourceYaml = ftpCluster.getSpec().getResources().getLimits();
                String cpuYaml = resourceYaml.getCpu();
                String memoryYaml = resourceYaml.getMemory();
                String capacityYaml = ftpCluster.getSpec().getCapacity();

                if (cpuString.equals(cpuYaml) && memoryString.equals(memoryYaml)
                        && capacityString.equals(capacityYaml)) {
                    try {
                        // 修改标志位,使用最新的service
                        updateServiceExtendedField(tenantName, service.getId(), service.getServiceName());
                        LOG.info("ftp集群修改资源成功，修改service扩展字段标志位成功，资源或配置是否生效："
                                + !ftpCluster.getStatus().isResourceupdateneedrestart());

                    } catch (Exception e) {
                        LOG.error("ftp集群修改资源成功，修改service扩展字段标志位失败，资源或配置是否生效："
                                + !ftpCluster.getStatus().isResourceupdateneedrestart());

                    }
                    break;
                }

            }

        }

    }

}
