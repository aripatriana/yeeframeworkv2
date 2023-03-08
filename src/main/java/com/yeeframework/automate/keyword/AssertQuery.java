package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.AssertQueryAction;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.model.QueryEntry;
import com.yeeframework.automate.reader.QueryReader;

public class AssertQuery implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.ASSERT_QUERY;
	}

	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			QueryReader qr = new QueryReader(we.getVariable());
			QueryEntry qe = qr.read();
			AssertQueryAction actionable = new AssertQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}

	
	
}
