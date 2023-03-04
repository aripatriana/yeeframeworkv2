package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.Statement;
import com.yeeframework.automate.action.AssertStatementAction;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.util.StringUtils;

public class Assert implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.ASSERT;
	}

	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			String[] result = StringUtils.parseStatement(we.getVariable(), Statement.MARK);
			AssertStatementAction actionable = new AssertStatementAction(new Statement(result[0], result[1], result[2]));
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}

}
