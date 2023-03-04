package com.yeeframework.automate;

import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;

public interface Keyword {

	public String script();
	
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception;
}
