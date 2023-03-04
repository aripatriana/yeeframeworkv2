package com.yeeframework.automate.schedule;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.yeeframework.automate.RunTestWorkflow;

public class SchedulerJobFactory extends SimpleJobFactory {

	RunTestWorkflow runTestWorkflow;
	
	public SchedulerJobFactory(RunTestWorkflow runTestWorkflow) {
		this.runTestWorkflow = runTestWorkflow;
	}
	
	@Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler Scheduler) throws SchedulerException {
	 	WorkflowJobExecutor job = (WorkflowJobExecutor) super.newJob(bundle, Scheduler);
        job.setRunTestWorkflow(runTestWorkflow);
        return job;
    }
}
