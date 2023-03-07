package com.yeeframework.automate.reader;

import java.util.List;

import com.yeeframework.automate.entry.TestCasePath;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.WorkflowConfig;

public class TestCasePathReader {

	private WorkflowConfig workflowConfig;
	private String testCasePath;
	
	public TestCasePathReader(String testCasePath, WorkflowConfig workflowConfig) {
		this.testCasePath = testCasePath;
		this.workflowConfig = workflowConfig;
	}
	
	public TestCasePath read() throws ScriptInvalidException {
		String[] testCaseArrs = testCasePath.split("\\,");
		TestCasePath testCasePath = new TestCasePath();
		for (String testCase : testCaseArrs) {
			String[] scenPath = testCase.split("\\/");
			if (scenPath.length > 1) {
				String testScenId = scenPath[0] + "_" + scenPath[1].replace(".y", "");
				if (!workflowConfig.containWorkflowKey(testScenId)) 
					throw new ScriptInvalidException("TestCase is invalid / not found -> " + testScenId);
				
				testCasePath.addTestCase(scenPath[0], scenPath[0] + "_" + scenPath[1].replace(".y", ""));
			} else {
				List<String> testScenIdList = workflowConfig.getWorkflowMapScens(scenPath[0]);
				if (testScenIdList == null || testScenIdList.size() == 0)
					throw new ScriptInvalidException("TestCase is invalid or dosen't have any test scenario -> " + scenPath[0]);
				
				for (String testScen : workflowConfig.getWorkflowMapScens(scenPath[0])) {
					testCasePath.addTestCase(scenPath[0], testScen);
				}
			}
		}
		return testCasePath;
		
	}
}
