/**
 *
 */
package com.xxx.xcloud.common;

/**
 * @author ruzz
 *
 */
public class ApiResult {
    int code;
    Object data;
    String message;

    public ApiResult() {
        super();
    }

    public ApiResult(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public ApiResult(int code, Object data, String message) {
        super();
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
