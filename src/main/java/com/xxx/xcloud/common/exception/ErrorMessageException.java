package com.xxx.xcloud.common.exception;

/**
 * @ClassName: ErrorMessageException
 * @Description: Api 出现错误或异常情况
 * @author huchao
 * @date 2019年10月24日
 *
 */
public class ErrorMessageException extends RuntimeException {

    private static final long serialVersionUID = 7521673271244669604L;

    protected int code;

    public ErrorMessageException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
