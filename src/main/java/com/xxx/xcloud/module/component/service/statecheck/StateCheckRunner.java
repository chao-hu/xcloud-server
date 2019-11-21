package com.xxx.xcloud.module.component.service.statecheck;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xxx.xcloud.module.component.consts.CommonConst;
import com.xxx.xcloud.module.component.entity.StatefulService;
import com.xxx.xcloud.module.component.util.ComponentOperationsClientUtil;
import com.xxx.xcloud.module.component.util.ComponentOperationsDataBaseUtil;


/**
 * @ClassName: StateCheckRunner
 * @Description: StateCheckRunner
 * @author lnn
 * @date 2019年11月21日
 *
 */
@Component
@Order(value = 3)
public class StateCheckRunner extends BaseStateCheckRunner {
    private static Logger LOG = LoggerFactory.getLogger(StateCheckRunner.class);

    private List<StatefulService> services = null;

    @Autowired
    private ComponentOperationsClientUtil componentOperationsClientUtil;

    @Autowired
    private ComponentOperationsDataBaseUtil componentOperationsDataBaseUtil;

    @Override
    protected void stateCheck() {

        // 查找上一次操作距离现在15分钟并且状态为waiting的service，强制改成failed状态
        try {
            LOG.info("查找上一次操作距离现在15分钟并且状态为waiting的service，强制改成failed状态");
            changeStateToFailed();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("stateCheckRunner异常，修改15分钟之前处于waiting状态的集群异常！", e.getMessage());
        }
        // 查找最后一次操作是5分钟之前的所有service
        long compareTime = System.currentTimeMillis() - 5 * CommonConst.MINUTE;
        Date date = new Date(compareTime);
        try {
            services = statefulServiceRepository
                    .findByServiceStateNotAndLastoptTimeBefore(CommonConst.STATE_CLUSTER_DELETED, date);
            LOG.info("集群表中的数据：" + JSON.toJSONString(services));
        } catch (Exception e) {
            LOG.error("健康检查查询数据库失败", e);
        }
        LOG.info("查询出来最后一次操作时间大于5分钟的services：" + JSON.toJSONString(services));
        if (null == services || services.isEmpty()) {
            return;
        }

        // 遍历所有service
        for (StatefulService statefulService : services) {
            try {
                switch (statefulService.getAppType()) {
                case CommonConst.APPTYPE_REDIS:
                    componentOperationsClientUtil.changeRedisClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_MYSQL:
                    componentOperationsClientUtil.changeMysqlClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_STORM:
                    componentOperationsClientUtil.changeStormClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_KAFKA:
                    componentOperationsClientUtil.changeKafkaClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_FTP:
                    componentOperationsClientUtil.changeFtpClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_CODIS:
                    componentOperationsClientUtil.changeCodisClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_ES:
                    componentOperationsClientUtil.changeEsClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_MEMCACHED:
                    componentOperationsClientUtil.changeMemcachedClusterAndNodesStateByYaml(
                            statefulService.getNamespace(), statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_PROMETHEUS:
                    componentOperationsClientUtil.changePrometheusClusterAndNodesStateByYaml(
                            statefulService.getNamespace(), statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_POSTGRESQL:
                    componentOperationsClientUtil.changePostgresqlClusterAndNodesStateByYaml(
                            statefulService.getNamespace(), statefulService.getId(), statefulService.getServiceName());
                    break;
                case CommonConst.APPTYPE_ZK:
                    componentOperationsClientUtil.changeZkClusterAndNodesStateByYaml(statefulService.getNamespace(),
                            statefulService.getId(), statefulService.getServiceName());
                    break;
                default:
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("stateCheckRunner异常，同步5分钟前的集群和节点状态异常！", e.getMessage());
            }
        }

    }

    /**
     * 查找上一次操作距离现在15分钟并且状态为waiting的service，强制改成failed状态
     */
    private void changeStateToFailed() {
        long waitingTime = System.currentTimeMillis() - 15 * 60 * 1000;
        Date date = new Date(waitingTime);
        try {
            services = statefulServiceRepository
                    .findByServiceStateAndLastoptTimeBefore(CommonConst.STATE_CLUSTER_WAITING, date);
        } catch (Exception e) {
            LOG.error("健康检查查询数据库失败！", e);
        }

        LOG.info("查询出来最后一次操作时间大于15分钟并且状态为waiting的services：" + JSON.toJSONString(services));
        if (null == services || services.isEmpty()) {
            return;
        }

        for (StatefulService statefulService : services) {
            try {
                componentOperationsDataBaseUtil.updateClusterState(statefulService.getId(), CommonConst.STATE_CLUSTER_FAILED,
                        null, null);
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("stateCheckRunner异常，修改15分钟之前处于waiting状态的集群为failed异常！", e.getMessage());
            }
        }

    }
}
