package com.yeeframework.automate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestCaseObject {

	@JacksonXmlProperty(localName = "scenario")
	String scenario;
	
	@JacksonXmlProperty(localName = "trigger-time")
	String triggerTime;
	
	@JacksonXmlProperty(localName = "description")
	String description;
	
	@JsonIgnore
	TestCasePath testCasePath;
	
	@JsonIgnore
	String key;
	
	public TestCaseObject() {
	}

	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public String getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(String triggerTime) {
		this.triggerTime = triggerTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setTestCasePath(TestCasePath testCasePath) {
		this.testCasePath = testCasePath;
	}
	
	public TestCasePath getTestCasePath() {
		return testCasePath;
	}

	@Override
	public String toString() {
		return "TestCaseObject [scenario=" + scenario + ", triggerTime=" + triggerTime + ", description=" + description
				+ "]";
	}
}
