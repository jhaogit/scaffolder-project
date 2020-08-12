package com.jianghao.quartz.controller;

import com.jianghao.quartz.entity.QuartzEntity;
import com.jianghao.quartz.entity.Result;
import com.jianghao.quartz.service.IJobService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
public class JobController {

	private final static Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private IJobService jobService;

	@PostMapping("/add")
	public Result save(QuartzEntity quartz){
		LOGGER.info("新增任务");
		return jobService.add(quartz);
	}

	@PostMapping("/list")
	public Result list(QuartzEntity quartz,Integer pageNo,Integer pageSize) throws SchedulerException {
		LOGGER.info("任务列表");
		return jobService.list(quartz,pageNo,pageSize);
	}

	@PostMapping("/trigger")
	public  Result trigger(QuartzEntity quartz) {
		LOGGER.info("执行任务");
		return jobService.trigger(quartz);
	}

	@PostMapping("/pause")
	public  Result pause(QuartzEntity quartz) {
		LOGGER.info("停止任务");
		return jobService.pause(quartz);
	}

	@PostMapping("/resume")
	public  Result resume(QuartzEntity quartz) {
		LOGGER.info("恢复任务");
		return jobService.resume(quartz);
	}

	@PostMapping("/remove")
	public  Result remove(QuartzEntity quartz) {
		LOGGER.info("移除任务");
		return jobService.remove(quartz);
	}
}
