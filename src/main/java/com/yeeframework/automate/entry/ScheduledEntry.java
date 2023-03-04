package com.yeeframework.automate.entry;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "scheduled")
public class ScheduledEntry {
	
	@JacksonXmlElementWrapper(useWrapping = true)
	@JacksonXmlProperty(localName = "testcase")
	List<ScheduledTestCase> testCases;

	public ScheduledEntry() {
	}
	
	public List<ScheduledTestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<ScheduledTestCase> testCases) {
		this.testCases = testCases;
	}
	
	public class ScheduledTestCase {

		@JsonProperty(value = "scenario")
		String scenario;
		
		public ScheduledTestCase() {
			// TODO Auto-generated constructor stub
		}
		
		public ScheduledTestCase(String scenario) {
		}
		
		public void setScenario(String scenario) {
			this.scenario = scenario;
		}
		
		public String getScenario() {
			return scenario;
		}
		
		
	}
	
}
