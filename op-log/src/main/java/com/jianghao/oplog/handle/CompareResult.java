package com.jianghao.oplog.handle;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;

/**
 * @author ：jh
 * @date ：Created in 2020/8/5 15:33
 * @description：对比结果
 */

@Setter
@Getter
public class CompareResult {
    /**
     * 主键 两个对象的所有属性对比结果
     */
    private List<CompareParam> params;

    /**
     * 两个对象的主键 key:value
     */
    private Set<String> kyes;

}
