package com.jianghao.rabbitmq;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
public class MqSendUtil implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback{

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sendDelayMq(String uuid,String exchange,String routingKey,String content){
        CorrelationData correlationData = new CorrelationData(uuid);
        log.info("消息发送者通知业务系统--->消息【{}】发送时间【{}】",uuid, DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                content,
                correlationData
        );
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String cause) {
        if (b) {
            log.info("消息发送者投递消息【{}】成功",correlationData.getId());
        } else {
            log.info("消息发送者投递消息【{}】失败",correlationData.getId());
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息消费失败：{}",message);
    }
}