package com.jianghao.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ：jh
 * @date ：Created in 2020/7/10 15:19
 * @description：
 */
@RestController
@RequestMapping("/mq")
public class MqDemo {

    @Autowired
    MqSendUtil mqSendUtil;

    @GetMapping("/send")
    public void mqSend(){
        mqSendUtil.sendDelayMq("test_queue","test_exchange","test_key","mqtestinfo");
    }

}