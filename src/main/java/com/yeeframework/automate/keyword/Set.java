package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.SetVariableAction;
import com.yeeframework.automate.entry.SetVarEntry;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.reader.SetVariableReader;

public class Set implements Keyword {
	
	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.SET;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		SetVariableReader reader = new SetVariableReader(we.getVariable());
		SetVarEntry set = reader.read();
		SetVariableAction seta = new SetVariableAction(set);
		workflow.action(seta);
	}
}
