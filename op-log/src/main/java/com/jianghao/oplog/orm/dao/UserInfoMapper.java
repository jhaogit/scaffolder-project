package com.jianghao.oplog.orm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianghao.oplog.orm.po.UserInfo;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    int deleteByPrimaryKey(Integer id);

    int insert(UserInfo record);

    int insertSelective(UserInfo record);

    UserInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserInfo record);

    int updateByPrimaryKey(UserInfo record);

    int insertListSelective(List<UserInfo> list);
}