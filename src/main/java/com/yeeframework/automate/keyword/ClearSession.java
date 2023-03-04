package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.web.WebExchange;

public class ClearSession implements Keyword {
	
	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.CLEAR_SESSION;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			workflow.action(new Actionable() {
				
				@Override
				public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
					workflow.clearSession();
				}
			});
		} catch (Exception e) {
			throw e;
		}
	}
	

}
