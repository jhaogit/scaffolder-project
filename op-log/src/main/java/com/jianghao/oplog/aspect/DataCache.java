package com.jianghao.oplog.aspect;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.List;
import java.util.Set;

/**
 * @author ：jh
 * @date ：Created in 2020/8/4 9:51
 * @description：
 */

@Setter
@Getter
public class DataCache {
    /**
     * 数据操作类型  UPDATE/DELETE/INSERT
     */
    private SqlCommandType opType;
    /**
     * 操作表名
     */
    private String tableName;
    /**
     * 表对应的实体类描述
     */
    private String tableNameDesc;
    /**
     * 表主键对应的实体类属性名称集合
     */
    private Set<String> keyNames;
    /**
     * sql集合
     */
    private List<String> sqlList;
    /**
     * 实体类class
     */
    private Class<?> entityType;
    /**
     * 历史数据集合
     */
    private List<?> oldData;
    /**
     * 新数据集合
     */
    private List<?> newData;
}