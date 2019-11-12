package com.xxx.xcloud.client.sonar.model;

import java.util.List;

/**
 * @author mengaijun
 * @Description: sonar检查结果
 * @date: 2019年1月2日 下午3:40:32
 */
public class SonarCheckResult {
	/**
	 * 第p页
	 */
	private int p;
	/**
	 * 每页ps条
	 */
	private int ps;
	
	private int total;
	
	private List<Rule> rules;
	
	private List<Issues> issues;

    private List<Facet> facets;

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getPs() {
		return ps;
	}

	public void setPs(int ps) {
		this.ps = ps;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public List<Issues> getIssues() {
		return issues;
	}

	public void setIssues(List<Issues> issues) {
		this.issues = issues;
	}

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

}
