package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.AwaitAction;
import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.reader.QueryReader;

public class Await implements Keyword {
	
	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.AWAIT;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			workflow
				.action(new AwaitAction(Integer.valueOf(we.getVariable())));
		} catch (Exception e) {
			try {
				QueryReader qr = new QueryReader(we.getVariable());
				QueryEntry qe = qr.read();
				workflow
					.action(new AwaitAction(qe));
			} catch (ScriptInvalidException e1) {
				throw e1;
			}
		}
	}

}
