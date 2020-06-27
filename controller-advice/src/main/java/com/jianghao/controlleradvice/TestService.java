package com.jianghao.controlleradvice;

import com.jianghao.controlleradvice.exception.BizException;
import com.jianghao.controlleradvice.exception.CommonErrorEnum;
import org.springframework.stereotype.Service;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 20:52
 * @description：
 */

@Service
public class TestService {

    public String test1(){
        throw new BizException(CommonErrorEnum.UN_KNOWN_ERROR,"蒋浩测试业务异常");
    }
}