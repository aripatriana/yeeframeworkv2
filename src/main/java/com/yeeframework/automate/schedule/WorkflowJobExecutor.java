package com.yeeframework.automate.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.RunTestWorkflow;

public class WorkflowJobExecutor implements Job {

	private final Logger log = LoggerFactory.getLogger(WorkflowJobExecutor.class);
	
	RunTestWorkflow runTestWorkflow;
	
	public WorkflowJobExecutor() {
	}
	
	public WorkflowJobExecutor(RunTestWorkflow runTestWorkflow) {
		this.runTestWorkflow = runTestWorkflow;
	}
	
	public void setRunTestWorkflow(RunTestWorkflow runTestWorkflow) {
		this.runTestWorkflow = runTestWorkflow;
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Ready to run job {}", context.getTrigger().getJobKey().getName());
		ThreadJobWorker.newThreadJobRunnable(runTestWorkflow, context);
	}

}
