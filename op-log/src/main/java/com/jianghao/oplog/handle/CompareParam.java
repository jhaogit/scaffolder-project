package com.jianghao.oplog.handle;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ：jh
 * @date ：Created in 2020/8/5 15:33
 * @description：对比两个属性结果
 */

@Setter
@Getter
public class CompareParam {

    /**
     * 字段名
     */
    private String fieldName;
    /**
     * 字段注释
     */
    private String fieldComment;
    /**
     * 字段旧值
     */
    private Object oldValue;
    /**d
     * 字段新值
     */
    private Object newValue;
}
