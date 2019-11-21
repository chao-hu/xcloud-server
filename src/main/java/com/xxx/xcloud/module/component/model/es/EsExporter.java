package com.xxx.xcloud.module.component.model.es;

/**
 * @ClassName: EsExporter
 * @Description: EsExporter
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsExporter {

    private String name;
    private String instancePhase;
    private int kibanaInterPort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKibanaInterPort() {
        return kibanaInterPort;
    }

    public void setKibanaInterPort(int kibanaInterPort) {
        this.kibanaInterPort = kibanaInterPort;
    }

    public String getInstancePhase() {
        return instancePhase;
    }

    public void setInstancePhase(String instancePhase) {
        this.instancePhase = instancePhase;
    }

}