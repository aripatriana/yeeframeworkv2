package com.yeeframework.automate.schedule;

import java.util.concurrent.CountDownLatch;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.RunTestWorkflow;
import com.yeeframework.automate.entry.TestCasePath;

public class ThreadJobRunnable implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(ThreadJobRunnable.class);
	
	RunTestWorkflow runTestWorkflow;
	JobExecutionContext context;
	CountDownLatch latch;
	
	public ThreadJobRunnable(RunTestWorkflow runTestWorkflow, JobExecutionContext context) {
		this.runTestWorkflow = runTestWorkflow;
		this.context = context;
		this.latch = new CountDownLatch(1);
	}
	
	@Override
	public void run() {
		log.info("Run scheduled testWorkflow {}", context.getJobDetail().getKey());
		runTestWorkflow.testWorkflow((TestCasePath) context.getJobDetail().getJobDataMap().get("testCashPath"));
		latch.countDown();
	}
	
	public boolean isDone() {
		return latch.getCount() == 0;
	}
}
