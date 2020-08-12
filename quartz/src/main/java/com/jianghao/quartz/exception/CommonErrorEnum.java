package com.jianghao.quartz.exception;

import lombok.Getter;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 21:46
 * @description：公共异常枚举
 */

public enum CommonErrorEnum implements ICommonError{

    SUCCESS(200, true, "SUCCESS"),
    UN_KNOWN_ERROR(500, false, "SERVER ERROR,PLEASE CONTACT THE ADMINISTRATOR!"),
    DATA_OP_ERROR(9001, false, "DATABASE OPERATION ERROR,PLEASE CONTACT THE ADMINISTRATOR!"),
    AUTH_ERROR(9002, false, "NO PERMISSION!"),
    PARAM_MISSING(9003,false,"PARAM MISSING!"),
    HTTP_CONNECT_ERROR(9004,false,"HTTP CONNECT ERROR!"),
    ;

    @Getter
    private Integer code;
    @Getter
    private Boolean success;
    @Getter
    private String message;

    CommonErrorEnum(Integer code, Boolean success, String message) {
        this.code = code;
        this.success = success;
        this.message = message;
    }
}
