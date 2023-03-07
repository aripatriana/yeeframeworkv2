package com.yeeframework.automate.schedule;

import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowScheduler {

	private final Logger log = LoggerFactory.getLogger(WorkflowScheduler.class);
	
	SchedulerJobFactory schedulerJobFactory;
	List<TestCaseObject> testCases;
	
	public WorkflowScheduler(SchedulerJobFactory schedulerJobFactory, List<TestCaseObject> testCases) {
		this.schedulerJobFactory = schedulerJobFactory;
		this.testCases = testCases;
	}

	public void schedule() {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();

			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.setJobFactory(schedulerJobFactory);
			scheduler.start();
			
			ThreadJobWorker worker = new ThreadJobWorker(1);
			worker.start();
			
			for (TestCaseObject testCase : testCases) {
				log.info("Add schedule {}", testCase);
				
				JobDataMap jobDataMap = new JobDataMap();
				jobDataMap.put("testCashPath", testCase.getTestCasePath());
				jobDataMap.put("key", testCase.getKey());
				
				JobDetail job = JobBuilder.newJob(WorkflowJobExecutor.class)
						  .withIdentity("job_" + testCase.getKey(), testCase.getKey())
						  .setJobData(jobDataMap)
						  .build();
				
				CronTrigger trigger = TriggerBuilder.newTrigger()
						  .withIdentity("trigger_" + testCase.getKey(), testCase.getScenario())
						  .withSchedule(CronScheduleBuilder.cronSchedule(testCase.getTriggerTime()))
						  .forJob("job_" + testCase.getKey(), testCase.getKey())
						  .build();
				
				scheduler.scheduleJob(job, trigger);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
