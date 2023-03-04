package com.yeeframework.automate.keyword.a;

import com.yeeframework.automate.ActionType;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.util.ReflectionUtils;

public class MultipleApprove implements ActionType {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.ActionTypes.MULTIPLE_APPROVE;
	}

	@Override
	public void run(Object handler, WorkflowEntry we, Workflow workflow) throws Exception {
		ReflectionUtils.invokeMethod(handler, com.yeeframework.automate.keyword.ActionTypes.MULTIPLE_APPROVE, Workflow.class, workflow);
	}

}
