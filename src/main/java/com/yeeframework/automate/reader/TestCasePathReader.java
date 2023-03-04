package com.yeeframework.automate.reader;

import com.yeeframework.automate.entry.TestCasePath;
import com.yeeframework.automate.execution.WorkflowConfig;

public class TestCasePathReader {

	private WorkflowConfig workflowConfig;
	private String testCasePath;
	
	public TestCasePathReader(String testCasePath, WorkflowConfig workflowConfig) {
		this.testCasePath = testCasePath;
		this.workflowConfig = workflowConfig;
	}
	
	public TestCasePath read() {
		String[] testCaseArrs = testCasePath.split("\\,");
		TestCasePath testCasePath = new TestCasePath();
		for (String testCase : testCaseArrs) {
			String[] scenPath = testCase.split("\\/");
			if (scenPath.length > 1) {
				testCasePath.addTestCase(scenPath[0], scenPath[0] + "_" + scenPath[1].replace(".y", ""));
			} else {
				for (String testScen : workflowConfig.getWorkflowMapScens(scenPath[0])) {
					testCasePath.addTestCase(scenPath[0], testScen);
				}
			}
		}
		return testCasePath;
		
	}
}
