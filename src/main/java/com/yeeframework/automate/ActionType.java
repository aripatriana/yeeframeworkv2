package com.yeeframework.automate;

import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowEntry;

public interface ActionType {

	public String script();
	
	public void run(Object handler, WorkflowEntry we, Workflow workflow) throws Exception;
	
}
