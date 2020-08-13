package com.jianghao.oplog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：jh
 * @date ：Created in 2020/8/13 15:48
 * @description：程序启动初始化字段需要枚举映射的实体类
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogReplace {

}