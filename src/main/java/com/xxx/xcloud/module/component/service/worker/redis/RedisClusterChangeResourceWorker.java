package com.xxx.xcloud.module.component.service.worker.redis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.model.base.MemoryCpu;
import com.xxx.xcloud.module.component.model.redis.RedisCluster;

@Service
@Scope("prototype")
public class RedisClusterChangeResourceWorker extends BaseRedisClusterWorker {

    private static Logger LOG = LoggerFactory.getLogger(RedisClusterChangeResourceWorker.class);

    @Override
    public void execute() {

        LOG.info("===============RedisClusterChangeResourceWorker====================");

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

        componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(tenantName, service.getId(),
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
            RedisCluster redisCluster = componentOperationsClientUtil.getRedisCluster(tenantName,
                    service.getServiceName());
            LOG.info("判断redisCluster中数据是否存在");
            LOG.info("redisCluster:" + JSON.toJSONString(redisCluster));

            if (null != redisCluster && null != redisCluster.getSpec() && null != redisCluster.getSpec().getResources()
                    && redisCluster.getStatus().isNeedRestart()
                    && CommonConst.STATE_CLUSTER_RUNNING.equals(redisCluster.getStatus().getPhase())) {
                MemoryCpu resourceYaml = redisCluster.getSpec().getResources().getLimits();
                String cpuYaml = resourceYaml.getCpu();
                String memoryYaml = resourceYaml.getMemory();
                String capacityYaml = redisCluster.getSpec().getCapacity();

                if (cpuString.equals(cpuYaml) && memoryString.equals(memoryYaml)
                        && capacityString.equals(capacityYaml)) {
                    try {
                        // 修改标志位,使用最新的service
                        if (updateExtendedFieldByYaml(service.getId())) {
                            LOG.info("redis集群修改资源成功，修改service扩展字段标志位成功，资源或配置是否生效："
                                    + !redisCluster.getStatus().isNeedRestart());
                        } else {
                            LOG.error("redis集群修改资源成功，修改service扩展字段标志位失败，资源或配置是否生效："
                                    + !redisCluster.getStatus().isNeedRestart());
                        }
                        break;

                    } catch (Exception e) {
                        LOG.error("修改资源时，获取redisCluster的资源失败！");
                        break;
                    }
                }

            }

        }

    }

    private boolean updateExtendedFieldByYaml(String serviceId) {
        StatefulService service = componentOperationsDataBaseUtil.getStatefulServiceById(serviceId);
        if (null == service) {
            LOG.error("资源修改成功后根据serviceId获取statefulService为null");
            return false;
        }

        Map<String, String> extendedField = componentOperationsDataBaseUtil
                .getServiceExtendedField(service.getExtendedField());
        extendedField.put(CommonConst.RESOURCE_OR_CONFIG_EFFECTIVE, CommonConst.EFFECTIVE_FALSE);
        service.setExtendedField(JSON.toJSONString(extendedField));
        statefulServiceRepository.save(service);
        return true;

    }

}
