package com.jianghao.oplog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：jh
 * @date ：Created in 2020/8/5 10:48
 * @description：日志注解
 */

@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataLog {
    /*
     * 是否记录日志
     */
    boolean isLog()default false;
    /*
     * 是否主键字段
     */
    boolean isKey()default false;
    /*
     * 主键对应的表字段
     */
    String keyColumn()default "";
    /*
     * 属性说明
     */
    String note()default "";
    /*
     * 字段映射 规范 replace = {"待审核_0","未通过_1","已通过_2"}
     */
    String[] replace()default "";
}