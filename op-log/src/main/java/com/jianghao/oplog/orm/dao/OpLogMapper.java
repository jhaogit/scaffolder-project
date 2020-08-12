package com.jianghao.oplog.orm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianghao.oplog.orm.po.OpLog;
import org.springframework.stereotype.Repository;

@Repository
public interface OpLogMapper extends BaseMapper<OpLog> {
    int deleteByPrimaryKey(Integer id);

    int insert(OpLog record);

    int insertSelective(OpLog record);

    OpLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OpLog record);

    int updateByPrimaryKey(OpLog record);
}