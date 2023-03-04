package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.LogoutFormAction;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;

public class Logout implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.LOGOUT;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		workflow.action(new LogoutFormAction());
	}
}
