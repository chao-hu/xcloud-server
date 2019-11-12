/**
 * Project Name:EPM_PAAS_CLOUD
 * File Name:Rule.java
 * Package Name:com.bonc.epm.paas.sonar.model
 * Date:2018年2月9日下午2:34:48
 * Copyright (c) 2018, longkaixiang@bonc.com.cn All Rights Reserved.
 *
 */

package com.xxx.xcloud.client.sonar.model;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2018年12月10日 上午10:13:11
 */
public class Rules {
	/**
	 * 总记录数
	 */
    private Integer total;
    /**
     * 第p页
     */
    private Integer p;
    /**
     * 第p页有ps条
     */
    private Integer ps;
    private List<Rule> rules;
    private Map<String, List<Rule>> actives;
    private List<Facet> facets;
    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public Integer getP() {
        return p;
    }
    public void setP(Integer p) {
        this.p = p;
    }
    public Integer getPs() {
        return ps;
    }
    public void setPs(Integer ps) {
        this.ps = ps;
    }
    public List<Rule> getRules() {
        return rules;
    }
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
    public Map<String, List<Rule>> getActives() {
        return actives;
    }
    public void setActives(Map<String, List<Rule>> actives) {
        this.actives = actives;
    }
    public List<Facet> getFacets() {
        return facets;
    }
    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }
}
