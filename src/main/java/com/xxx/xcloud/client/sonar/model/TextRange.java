package com.xxx.xcloud.client.sonar.model;

/**
 * 错误文件范围
 * 
 * @author mengaijun
 * @date: 2019年5月20日 上午10:49:32
 */
public class TextRange {
    /**
     * 起始行
     */
    private Integer startLine;
    private Integer endLine;
    /**
     * 起始列
     */
    private Integer startOffset;
    private Integer endOffset;

    public Integer getStartLine() {
        return startLine;
    }

    public void setStartLine(Integer startLine) {
        this.startLine = startLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public void setEndLine(Integer endLine) {
        this.endLine = endLine;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public Integer getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }

}
