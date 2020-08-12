package com.jianghao.quartz.exception;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 20:17
 * @description：
 */

public class ResultVO<T> implements Serializable {

    /**
     * 请求状态码，200表示请求响应成功
     */
    private int code;
    /**
     * 业务状态，true表示业务请求成功，与code的区别在于：
     * code只表示当前的request请求是否成功
     * success表示业务处理是否成功
     * 例如：请求下单，该请求已受理，但是因为条件不满足导致业务失败，则code为200，但success为false
     */
    private Boolean success;
    /**
     * 响应信息，用来说明响应情况
     */
    private String message;
    /**
     * 业务响应信息，用来说明响应情况
     */
    private String subMessage;
    /**
     * 响应的具体数据
     */
    private T data;

    public static ResultVO success() {
        ResultVO rv = new ResultVO();
        rv.setCode(CommonErrorEnum.SUCCESS.getCode());
        rv.setSuccess(CommonErrorEnum.SUCCESS.getSuccess());
        rv.setMessage(CommonErrorEnum.SUCCESS.getMessage());
        rv.setData(new HashMap<>());
        return rv;
    }

    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> rv = new ResultVO();
        rv.setCode(CommonErrorEnum.SUCCESS.getCode());
        rv.setSuccess(CommonErrorEnum.SUCCESS.getSuccess());
        rv.setMessage(CommonErrorEnum.SUCCESS.getMessage());
        rv.setData(data);
        return rv;
    }

    public static ResultVO fail(String message) {
        ResultVO rv = new ResultVO();
        rv.setCode(CommonErrorEnum.UN_KNOWN_ERROR.getCode());
        rv.setSuccess(CommonErrorEnum.UN_KNOWN_ERROR.getSuccess());
        rv.setMessage(message);
        rv.setData(new HashMap<>());
        return rv;
    }

    public static ResultVO fail(ICommonError commonError) {
        ResultVO rv = new ResultVO();
        rv.setCode(commonError.getCode());
        rv.setSuccess(commonError.getSuccess());
        rv.setMessage(commonError.getMessage());
        rv.setData(new HashMap<>());
        return rv;
    }

    public static ResultVO fail(ICommonError commonError,String subMessage) {
        ResultVO rv = new ResultVO();
        rv.setCode(commonError.getCode());
        rv.setSuccess(commonError.getSuccess());
        rv.setMessage(commonError.getMessage());
        rv.setSubMessage(subMessage);
        rv.setData(new HashMap<>());
        return rv;
    }

    public static ResultVO fail(BizException exception) {
        ResultVO rv = new ResultVO();
        rv.setCode(exception.getCode());
        rv.setSuccess(exception.getSuccess());
        rv.setMessage(exception.getMessage());
        rv.setSubMessage(exception.getSubMessage());
        rv.setData(new HashMap<>());
        return rv;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubMessage() {
        return subMessage;
    }

    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}