package com.jianghao.limit.service;
/**
 * @author ：jh
 * @date ：Created in 2020/6/27 17:20
 * @description：限流类型
 */
public enum LimitType {

    /**
     * 自定义key
     */
    CUSTOMER,

    /**
     * 请求者IP
     */
    IP;

}
