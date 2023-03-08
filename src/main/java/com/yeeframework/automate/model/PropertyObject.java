package com.yeeframework.automate.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PropertyObject {

	@JacksonXmlProperty(localName = "name", isAttribute = true)
	private String name;
	
	@JacksonXmlProperty(localName = "value", isAttribute = true)
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "PropertyObject [name=" + name + ", value=" + value + "]";
	}
	
}
