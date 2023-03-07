package com.yeeframework.automate.schedule;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "scheduled")
public class ScheduledObject {
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "testcase")
	List<TestCaseObject> testCases;

	public ScheduledObject() {
	}
	
	public List<TestCaseObject> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<TestCaseObject> testCases) {
		this.testCases = testCases;
	}

	@Override
	public String toString() {
		return "ScheduledObject [testCases=" + testCases + "]";
	}
	
}
