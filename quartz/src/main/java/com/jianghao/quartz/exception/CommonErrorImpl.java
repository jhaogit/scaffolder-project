package com.jianghao.quartz.exception;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 20:46
 * @description：
 */

public class CommonErrorImpl implements ICommonError {

    private Integer code;
    private Boolean success;
    private String message;

    public CommonErrorImpl() {
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return null;
    }

    @Override
    public Boolean getSuccess() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}