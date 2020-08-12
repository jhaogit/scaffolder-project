package com.jianghao.quartz.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 22:00
 * @description：全局异常处理
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**-------- 通用未知异常处理 --------**/
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultVO error(Exception e) {
        log.error("System Error:message-[{}]",e);
        return ResultVO.fail(CommonErrorEnum.UN_KNOWN_ERROR);
    }

    /**-------- 参数缺失异常处理 --------**/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultVO error(MethodArgumentNotValidException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String subMessage = objectError.getDefaultMessage();
        log.error("Param Error:message-[{}]",subMessage);
        return ResultVO.fail(CommonErrorEnum.PARAM_MISSING,subMessage);
    }

    /**-------- 请求体缺失异常处理 --------**/
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResultVO error(HttpMessageNotReadableException e) {
        log.error("Param Error:message-[Required request body is missing]");
        return ResultVO.fail(e.getMessage());
    }

    /**-------- 请求方式异常处理 --------**/
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResultVO error(HttpRequestMethodNotSupportedException e) {
        log.error("Http Method Error:message-[{}]",e);
        return ResultVO.fail(e.getMessage());
    }

    /**-------- 自定义定异常处理方法 --------**/
    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResultVO error(BizException e) {
        log.error("Biz Error:message-[{}],subMessage[{}]",e.getMessage(),e.getSubMessage());
        log.error("Biz Error:StackTrace-[{}]",e.getStackTrace());
        return ResultVO.fail(e);
    }
}