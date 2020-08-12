package com.jianghao.quartz.orm.mapper;

import com.jianghao.quartz.entity.QuartzEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrtzMapper {
    List<QuartzEntity> selectCondition(@Param("quartz") QuartzEntity quartz);
}
