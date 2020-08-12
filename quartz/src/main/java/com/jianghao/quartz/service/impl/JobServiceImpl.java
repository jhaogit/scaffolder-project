package com.jianghao.quartz.service.impl;

import com.github.pagehelper.PageHelper;
import com.jianghao.quartz.entity.QuartzEntity;
import com.jianghao.quartz.entity.Result;
import com.jianghao.quartz.orm.mapper.QrtzMapper;
import com.jianghao.quartz.service.IJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class JobServiceImpl implements IJobService {

    @Autowired
	QrtzMapper qrtzMapper;
    @Autowired
    private Scheduler scheduler;

    /**
     * @param quartz
     * @param pageNo
     * @param pageSize
     * @return java.util.List<com.jianghao.quartz.entity.QuartzEntity>
     * @Description: 查询任务列表
     */
	@Override
	public List<QuartzEntity> listQuartzEntity(QuartzEntity quartz,Integer pageNo, Integer pageSize) throws SchedulerException {
		PageHelper.startPage(pageNo, pageSize);
		List<QuartzEntity> list = qrtzMapper.selectCondition(quartz);
        for (QuartzEntity quartzEntity : list) {
            JobKey key = new JobKey(quartzEntity.getJobName(), quartzEntity.getJobGroup());
            JobDetail jobDetail = scheduler.getJobDetail(key);
            quartzEntity.setJobMethodName(jobDetail.getJobDataMap().getString("jobMethodName"));
        }
        //todo return new PageInfo(list);
        return list;
	}

	/**
	 * @param quartz
	 * @return com.jianghao.quartz.entity.Result
	 * @Description: 新增任务
	 */
    @Override
    public Result add(QuartzEntity quartz) {
        try {
            //获取Scheduler实例、废弃、使用自动注入的scheduler、否则spring的service将无法注入
            //Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            //如果是修改  展示旧的 任务
            if(quartz.getOldJobGroup()!=null){
                JobKey key = new JobKey(quartz.getOldJobName(),quartz.getOldJobGroup());
                scheduler.deleteJob(key);
            }
            Class cls = Class.forName(quartz.getJobClassName()) ;
            cls.newInstance();
            //构建job信息
            JobDetail job = JobBuilder.newJob(cls).withIdentity(quartz.getJobName(),
                    quartz.getJobGroup())
                    .withDescription(quartz.getDescription()).build();
            job.getJobDataMap().put("jobMethodName", quartz.getJobMethodName());
            // 触发时间点
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(quartz.getCronExpression());
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger"+quartz.getJobName(), quartz.getJobGroup())
                    .startNow().withSchedule(cronScheduleBuilder).build();
            //交由Scheduler安排触发
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }

    @Override
    public Result list(QuartzEntity quartz, Integer pageNo, Integer pageSize) throws SchedulerException {
        List<QuartzEntity> list = listQuartzEntity(quartz, pageNo, pageSize);
        return Result.ok(list);
    }

    /**
     * @param quartz
     * @return com.jianghao.quartz.entity.Result
     * @Description: 执行一次任务
     */
    @Override
    public Result trigger(QuartzEntity quartz) {
        try {
            JobKey key = new JobKey(quartz.getJobName(),quartz.getJobGroup());
            scheduler.triggerJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }

    /**
     * @param quartz
     * @return com.jianghao.quartz.entity.Result
     * @Description: 暂停任务
     */
    @Override
    public Result pause(QuartzEntity quartz) {

        try {
            JobKey key = new JobKey(quartz.getJobName(),quartz.getJobGroup());
            scheduler.pauseJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }

    /**
     * @param quartz
     * @return com.jianghao.quartz.entity.Result
     * @Description: 恢复任务
     */
    @Override
    public Result resume(QuartzEntity quartz) {
        try {
            JobKey key = new JobKey(quartz.getJobName(),quartz.getJobGroup());
            scheduler.resumeJob(key);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }

    /**
     * @param quartz
     * @return com.jianghao.quartz.entity.Result
     * @Description: 移除任务
     */
    @Override
    public Result remove(QuartzEntity quartz) {
        try {

            TriggerKey triggerKey = TriggerKey.triggerKey(quartz.getJobName(), quartz.getJobGroup());
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(quartz.getJobName(), quartz.getJobGroup()));
            log.info("removeJob:{}"+ JobKey.jobKey(quartz.getJobName()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
        return Result.ok();
    }
}
