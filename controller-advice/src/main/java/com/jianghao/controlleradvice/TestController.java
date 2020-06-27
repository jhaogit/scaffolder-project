package com.jianghao.controlleradvice;

import com.jianghao.controlleradvice.exception.BizException;
import com.jianghao.controlleradvice.exception.CommonErrorEnum;
import com.jianghao.controlleradvice.exception.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 20:52
 * @description：
 */

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    TestService service;

    @GetMapping("/1")
    public ResultVO success(){
        return ResultVO.success();
    }

    @GetMapping("/2")
    public ResultVO fail(){
        throw new BizException(CommonErrorEnum.UN_KNOWN_ERROR);
    }

    @GetMapping("/3")
    public ResultVO failMessage(){
        throw new BizException(CommonErrorEnum.UN_KNOWN_ERROR,"蒋浩测试业务异常");
    }

    @PostMapping("/4")
    public ResultVO failMessageService(@Valid @RequestBody TestReq req){
        service.test1();
        return ResultVO.success();
    }
}