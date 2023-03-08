package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.ExecuteAction;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.model.ArgsEntry;
import com.yeeframework.automate.reader.ArgsReader;

public class Execute implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.EXECUTE;
	}

	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			ArgsReader ar = new ArgsReader(we.getVariable());
			ArgsEntry ae = ar.read();

			ExecuteAction actionable = new ExecuteAction(wc.getFunction(ae.getFunction()), ae);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}

}
