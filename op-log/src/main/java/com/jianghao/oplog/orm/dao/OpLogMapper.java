package com.jianghao.oplog.orm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianghao.oplog.orm.po.OpLog;
import com.jianghao.oplog.orm.po.OpLogInfo;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OpLogMapper extends BaseMapper<OpLog> {
    int deleteByPrimaryKey(Integer id);

    int insert(OpLog record);

    int insertSelective(OpLog record);

    OpLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OpLog record);

    int updateByPrimaryKey(OpLog record);

    void insertListSelective(List<OpLogInfo> opLogInfos);

}