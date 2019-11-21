package com.xxx.xcloud.module.component.model.es;

/**
 * @ClassName: EsKibana
 * @Description: EsKibana
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class EsKibana {

    private String name;
    private String nodeName;
    private int kibanaExterPort;
    private int kibanaInterPort;
    private String instancePhase;
    private String kibanaExterHost;
    private String kibanaInterHost;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getKibanaExterPort() {
        return kibanaExterPort;
    }

    public void setKibanaExterPort(int kibanaExterPort) {
        this.kibanaExterPort = kibanaExterPort;
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

    public String getKibanaExterHost() {
        return kibanaExterHost;
    }

    public void setKibanaExterHost(String kibanaExterHost) {
        this.kibanaExterHost = kibanaExterHost;
    }

    public String getKibanaInterHost() {
        return kibanaInterHost;
    }

    public void setKibanaInterHost(String kibanaInterHost) {
        this.kibanaInterHost = kibanaInterHost;
    }

}