package com.yeeframework.automate.entry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestCasePath {

	private Map<String, List<String>> testCasesMap = new HashMap<String, List<String>>();
	private List<String> testCases = new LinkedList<String>();
	
	public void addTestCase(String testCaseId, String testScenId) {
		List<String> testScenList = testCasesMap.get(testCaseId);
		if (testScenList == null) {
			testScenList = new LinkedList<String>();
		}
		testScenList.add(testScenId.replace(".y", ""));
		testCasesMap.put(testCaseId, testScenList);
		
		if (!testCases.contains(testCaseId)) {
			testCases.add(testCaseId);
		}
	}
	
	public Map<String, List<String>> getTestCasesMap() {
		return testCasesMap;
	}
	
	public List<String> getTestScen(String testCase) {
		return testCasesMap.get(testCase);
	}
	
	public List<String> getTestCases() {
		return testCases;
	}
	
}
