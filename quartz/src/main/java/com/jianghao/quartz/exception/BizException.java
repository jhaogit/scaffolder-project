package com.jianghao.quartz.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 20:40
 * @description：
 */

@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed", "commonError"})
public class BizException extends RuntimeException implements Serializable {

    private ICommonError commonError;
    private String subMessage;

    public BizException(ICommonError error, String subMessage) {
        this.subMessage = subMessage;
        this.commonError = error;
    }

    public BizException(ICommonError error) {
        this.commonError = error;
    }

    public ICommonError getCommonError() {
        return this.commonError;
    }

    public void setCommonError(ICommonError commonError) {
        this.commonError = commonError;
    }

    public Integer getCode() {
        return this.commonError.getCode();
    }

    public Boolean getSuccess() {
        return this.commonError.getSuccess();
    }

    public String getMessage() {
        return this.commonError.getMessage();
    }

    public String getSubMessage() {
        return subMessage;
    }

    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }
}