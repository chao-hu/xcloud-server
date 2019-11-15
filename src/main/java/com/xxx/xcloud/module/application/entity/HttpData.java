package com.xxx.xcloud.module.application.entity;

import java.util.Map;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午3:30:05
 */
public class HttpData{

    private Map<String, String> httpHeade;

    private String path;

    private Integer port;

    public Map<String, String> getHttpHeade() {
        return httpHeade;
    }

    public void setHttpHeade(Map<String, String> httpHeade) {
        this.httpHeade = httpHeade;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "httpData [httpHeade=" + httpHeade + ", path=" + path + ", port=" + port + "]";
    }

}
