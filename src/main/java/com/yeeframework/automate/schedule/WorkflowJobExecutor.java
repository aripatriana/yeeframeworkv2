package com.yeeframework.automate.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.yeeframework.automate.RunTestWorkflow;
import com.yeeframework.automate.entry.TestCasePath;

public class WorkflowJobExecutor implements Job {

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
		runTestWorkflow.testWorkflow((TestCasePath)context.getJobDetail().getJobDataMap().get("testCashPath"));
	}

}
