package com.jianghao.quartz.service;

import com.jianghao.quartz.entity.QuartzEntity;
import com.jianghao.quartz.entity.Result;
import org.quartz.SchedulerException;
import java.util.List;

public interface IJobService {
	
    List<QuartzEntity> listQuartzEntity(QuartzEntity quartz, Integer pageNo, Integer pageSize) throws SchedulerException;

    Result add(QuartzEntity quartz);

    Result list(QuartzEntity quartz,Integer pageNo,Integer pageSize) throws SchedulerException;

    Result trigger(QuartzEntity quartz);

    Result pause(QuartzEntity quartz);

    Result resume(QuartzEntity quartz);

    Result remove(QuartzEntity quartz);

}
