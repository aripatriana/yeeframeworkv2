package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Constants;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.AssertQueryAction;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.model.QueryEntry;
import com.yeeframework.automate.reader.QueryReader;
import com.yeeframework.automate.reader.TemplateReader;

public class AssertAggregate implements Keyword {

	@PropertyValue(Constants.CURRENT_TESTCASE_ID)
	private String testCaseId;
		
	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.ASSERT_AGGREGATE;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {			
			TemplateReader tr = new TemplateReader(wc.getWorkflowQuery(testCaseId, we.getVariable()));
			QueryReader qr = new QueryReader(tr.read().toString());
			QueryEntry qe = qr.read();
			AssertQueryAction actionable = new AssertQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}
}
