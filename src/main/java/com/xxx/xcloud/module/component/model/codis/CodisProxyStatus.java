package com.xxx.xcloud.module.component.model.codis;

/**
 * @ClassName: CodisProxyStatus
 * @Description: CodisProxyStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisProxyStatus extends CodisDashboardStatus {
	private boolean needRestart;

	public boolean isNeedRestart() {
		return needRestart;
	}

	public void setNeedRestart(boolean needRestart) {
		this.needRestart = needRestart;
	}

}
