package com.xxx.xcloud.module.component.model.es;
import java.util.Map;

/**
 * @ClassName: EsStatus
 * @Description: EsStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsStatus {
    private String phase;
    private EsKibana kibana;
    private EsExporter exporter;
    private Map<String, EsInstance> instances;
    private String resourceupdateneedrestart;
    private String parameterupdateneedrestart;

    public Map<String, EsInstance> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, EsInstance> instances) {
        this.instances = instances;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getResourceupdateneedrestart() {
        return resourceupdateneedrestart;
    }

    public void setResourceupdateneedrestart(String resourceupdateneedrestart) {
        this.resourceupdateneedrestart = resourceupdateneedrestart;
    }

    public String getParameterupdateneedrestart() {
        return parameterupdateneedrestart;
    }

    public void setParameterupdateneedrestart(String parameterupdateneedrestart) {
        this.parameterupdateneedrestart = parameterupdateneedrestart;
    }

    public EsKibana getKibana() {
        return kibana;
    }

    public void setKibana(EsKibana kibana) {
        this.kibana = kibana;
    }

    public EsExporter getExporter() {
        return exporter;
    }

    public void setExporter(EsExporter exporter) {
        this.exporter = exporter;
    }

}
