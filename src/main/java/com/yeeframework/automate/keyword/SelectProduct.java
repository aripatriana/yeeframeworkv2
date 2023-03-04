package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.ProductSelectorAction;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;

public class SelectProduct implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.SELECT_PRODUCT;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		workflow.getWebExchange().put("productType", we.getVariable());
		workflow
		.action(new ProductSelectorAction(we.getVariable()));
	}
}
