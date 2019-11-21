package com.xxx.xcloud.module.component.model.codis;
import java.util.Map;

/**
 * @ClassName: CodisGroupsStatus
 * @Description: CodisGroupsStatus
 * @author lnn
 * @date 2019年11月15日
 *
 */
public class CodisGroupsStatus {

	private Map<String, CodisGroupStatus> groups;

	public Map<String, CodisGroupStatus> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, CodisGroupStatus> groups) {
		this.groups = groups;
	}

}
