package com.yeeframework.automate.keyword;

import com.yeeframework.automate.FileRetention;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.exception.XlsSheetStyleException;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.reader.MultiLayerXlsFileReader;

public class LoadFile implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.LOAD_FILE;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			FileRetention retention = new FileRetention(new MultiLayerXlsFileReader(wc.getWorkflowData(we.getVariable())));
			workflow.getWebExchange().setRetention(Boolean.TRUE);
			retention.perform(workflow.getWebExchange());
		} catch (XlsSheetStyleException e) {
			throw new Exception(e);
		}
	}
}
