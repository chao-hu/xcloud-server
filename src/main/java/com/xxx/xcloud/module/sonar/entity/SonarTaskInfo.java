package com.xxx.xcloud.module.sonar.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * 
 * @author mengaijun
 * @date: 2019年5月20日 上午11:24:05
 */
@Entity
public class SonarTaskInfo {

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    private String taskName;

    private String codeReposName;

    private Byte status;

    private Boolean available;

    private Double healthDegree;

    private Integer codeLineNumbers;

    private Integer questionNumbers;

    private Date checkTime;

    private Date checkDurationTime;
    
	public SonarTaskInfo(String id, String taskName, String codeReposName, Byte status, Boolean available,
            Double healthDegree, Integer codeLineNumbers, Integer questionNumbers, Date checkTime,
            Date checkDurationTime) {
        this.id = id;
        this.taskName = taskName;
        this.codeReposName = codeReposName;
        this.status = status;
        this.available = available;
        this.healthDegree = healthDegree;
        this.codeLineNumbers = codeLineNumbers;
        this.questionNumbers = questionNumbers;
        this.checkTime = checkTime;
        this.checkDurationTime = checkDurationTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCodeReposName() {
        return codeReposName;
    }

    public void setCodeReposName(String codeReposName) {
        this.codeReposName = codeReposName;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Double getHealthDegree() {
        return healthDegree;
    }

    public void setHealthDegree(Double healthDegree) {
        this.healthDegree = healthDegree;
    }

    public Integer getCodeLineNumbers() {
        return codeLineNumbers;
    }

    public void setCodeLineNumbers(Integer codeLineNumbers) {
        this.codeLineNumbers = codeLineNumbers;
    }

    public Integer getQuestionNumbers() {
        return questionNumbers;
    }

    public void setQuestionNumbers(Integer questionNumbers) {
        this.questionNumbers = questionNumbers;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public Date getCheckDurationTime() {
        return checkDurationTime;
    }

    public void setCheckDurationTime(Date checkDurationTime) {
        this.checkDurationTime = checkDurationTime;
    }

}
