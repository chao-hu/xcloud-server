package com.xxx.xcloud.client.sonar.model;

/**
 * 
 * @author mengaijun
 * @Description: 检查问题详情
 * @date: 2019年1月2日 下午3:41:41
 */
public class Issues {
	/**
	 * 错误文件
	 */
	private String component;
	
	/**
	 * 错误信息
	 */
	private String message;
	
	/**
	 * 问题级别
	 */
	private String severity;
	
	/**
	 * 问题类别
	 */
	private String type;
	
	/**
     * 规则信息()
     */
	private String rule;

    private String key;

    /**
     * http://172.16.11.133:9000/project/issues?id=devops-test_sonar_testbdos&open=AWqvLn12qTi9mCcqK0EE&resolved=false
     */
    private String questionUrl;

    /**
     * 问题行数信息
     */
    private TextRange textRange;

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getQuestionUrl() {
        return questionUrl;
    }

    public void setQuestionUrl(String questionUrl) {
        this.questionUrl = questionUrl;
    }

    public TextRange getTextRange() {
        return textRange;
    }

    public void setTextRange(TextRange textRange) {
        this.textRange = textRange;
    }
	
}
