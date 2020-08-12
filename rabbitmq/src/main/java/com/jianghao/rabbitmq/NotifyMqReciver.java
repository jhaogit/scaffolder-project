package com.jianghao.rabbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Slf4j
public class NotifyMqReciver {
    @Autowired
    MqSendUtil mqSendUtil;
    @RabbitListener(queues = "test_queue")
    public void notify(String msg, Message message, Channel channel) throws IOException {
        try {
            log.info("消费者接受mq消息：【{}】",msg);
            //消费成功
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            //消费失败
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            throw new RuntimeException();
        }
    }
}