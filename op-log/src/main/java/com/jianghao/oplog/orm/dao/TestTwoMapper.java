package com.jianghao.oplog.orm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianghao.oplog.orm.po.TestTwo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestTwoMapper extends BaseMapper<TestTwo> {
    int deleteByPrimaryKey(@Param("oneId") Integer oneId, @Param("twoId") Integer twoId);

    int insert(TestTwo record);

    int insertSelective(TestTwo record);

    TestTwo selectByPrimaryKey(@Param("oneId") Integer oneId, @Param("twoId") Integer twoId);

    int updateByPrimaryKeySelective(TestTwo record);

    int updateByPrimaryKey(TestTwo record);

    Integer updateListByPrimaryKeySelective(@Param("list") List<TestTwo> list);

    int insertListSelective(List<TestTwo> testTwos);
}