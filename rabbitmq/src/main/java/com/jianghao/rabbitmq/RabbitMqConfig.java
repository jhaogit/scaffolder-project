package com.jianghao.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    /**
     * 交换机
     * 注意这里的交换机类型：DirectExchange
     * @return
     */
    @Bean
    public DirectExchange testExchange(){
        Map<String, Object> args = new HashMap<>();
        args.put("test-exchange-type", "direct");
        return new DirectExchange("test_exchange",true, false,args);
    }

    /**
     * 队列
     * @return
     */
    @Bean
    public Queue testQueue(){
        return new Queue("test_queue",true);
    }

    /**
     * 给延时队列绑定交换机
     * @return
     */
    @Bean
    public Binding testBinding(Queue testQueue, DirectExchange testExchange){
        return BindingBuilder.bind(testQueue).to(testExchange).with("test_key");
    }
}