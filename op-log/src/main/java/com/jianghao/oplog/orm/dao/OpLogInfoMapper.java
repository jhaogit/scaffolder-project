package com.jianghao.oplog.orm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianghao.oplog.orm.po.OpLogInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface OpLogInfoMapper extends BaseMapper<OpLogInfo> {
    int deleteByPrimaryKey(Integer id);

    int insert(OpLogInfo record);

    int insertSelective(OpLogInfo record);

    OpLogInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OpLogInfo record);

    int updateByPrimaryKey(OpLogInfo record);
}