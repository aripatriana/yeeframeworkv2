package com.yeeframework.automate.schedule;

import java.util.Map;
import java.util.Map.Entry;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.yeeframework.automate.entry.ScheduledEntry.ScheduledTestCase;

public class WorkflowScheduler {

	SchedulerJobFactory schedulerJobFactory;
	Map<String, ScheduledTestCase> testCases;
	
	public WorkflowScheduler(SchedulerJobFactory schedulerJobFactory, Map<String, ScheduledTestCase> testCases) {
		this.schedulerJobFactory = schedulerJobFactory;
		this.testCases = testCases;
	}

	public void schedule() {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();

			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.setJobFactory(schedulerJobFactory);
			
			scheduler.start();
			
			for (Entry<String, ScheduledTestCase> testCase : testCases.entrySet()) {
				JobDataMap jobDataMap = new JobDataMap();
//				jobDataMap.put("testCashPath", testCase.getValue().getTestCasePath());
				
				JobDetail job = JobBuilder.newJob(WorkflowJobExecutor.class)
						  .withIdentity("job_" + testCase.getKey(), testCase.getKey())
						  .setJobData(jobDataMap)
						  .build();
				
//				CronTrigger trigger = TriggerBuilder.newTrigger()
//						  .withIdentity("trigger_" + testCase.getKey(), testCase.getKey())
//						  .withSchedule(CronScheduleBuilder.cronSchedule(testCase.getValue().getTriggerTime()))
//						  .forJob("job_" + testCase.getKey(), testCase.getKey())
//						  .build();
				
//				scheduler.scheduleJob(job, trigger);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}
}
