package com.xxx.xcloud.module.sonar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author mengaijun
 * @Description: sonar检查常量
 * @date: 2018年12月25日 下午6:10:15
 */
public class CodeCheckConstant {
	/**
     * 检查状态：1未检查2检查中3完成4失败5禁用
     */
	public static final byte CODE_CHECK_STATUS_WAIT = 1;
	public static final byte CODE_CHECK_STATUS_ING = 2;
	public static final byte CODE_CHECK_STATUS_SUCCESS = 3;
	public static final byte CODE_CHECK_STATUS_FAIL = 4;
    public static final byte CODE_CHECK_STATUS_DISABLED = 5;
	
	/**
	 * 代码检查定时任务组
	 */
	public static final String CODE_CHECK_QUARTZ_GROUP = "code_check_quartz_group_";
	
    /**
     * 规则集来源
     */
    public static final String QUALITYFILE_SOURCE_SYSTEM = "1";
    public static final String QUALITYFILE_SOURCE_TENANT = "2";

    /**
     * sonar规则严重程度：INFO,MINOR,MAJOR,CRITICAL,BLOCKER
     */
    public static final Set<String> SONAR_RULE_SEVERITIE_SET;
    static {
        SONAR_RULE_SEVERITIE_SET = new HashSet<>(8);
        SONAR_RULE_SEVERITIE_SET.add("INFO");
        SONAR_RULE_SEVERITIE_SET.add("MINOR");
        SONAR_RULE_SEVERITIE_SET.add("MAJOR");
        SONAR_RULE_SEVERITIE_SET.add("CRITICAL");
        SONAR_RULE_SEVERITIE_SET.add("BLOCKER");
    }
    /**
     * sonar规则类型：CODE_SMELL,BUG,VULNERABILITY
     */
    public static final Set<String> SONAR_RULE_TYPE_SET;
    static {
        SONAR_RULE_TYPE_SET = new HashSet<>(4);
        SONAR_RULE_TYPE_SET.add("CODE_SMELL");
        SONAR_RULE_TYPE_SET.add("BUG");
        SONAR_RULE_TYPE_SET.add("VULNERABILITY");
    }

    /**
     * sonar支持的语言和语言对应的规则总数
     */
    public static final Map<String, Integer> SONAR_LANG_RUNENUM_MAP;
    static {
        SONAR_LANG_RUNENUM_MAP = new HashMap<>(16);
        SONAR_LANG_RUNENUM_MAP.put("cs", null);
        SONAR_LANG_RUNENUM_MAP.put("flex", null);
        SONAR_LANG_RUNENUM_MAP.put("go", null);
        SONAR_LANG_RUNENUM_MAP.put("java", null);
        SONAR_LANG_RUNENUM_MAP.put("js", null);
        SONAR_LANG_RUNENUM_MAP.put("php", null);
        SONAR_LANG_RUNENUM_MAP.put("py", null);
        SONAR_LANG_RUNENUM_MAP.put("ts", null);
        SONAR_LANG_RUNENUM_MAP.put("xml", null);
    }
}
