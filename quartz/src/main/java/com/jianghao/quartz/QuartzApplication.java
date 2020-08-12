package com.jianghao.quartz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(value = "com.jianghao")
@MapperScan("com.jianghao.quartz.orm.mapper")
@EnableScheduling
public class QuartzApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(QuartzApplication.class, args);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
