package com.yeeframework.automate.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "config")
public class ConfigObject {

	@JacksonXmlProperty(localName = "system")
	private SystemObject system;
	
	@JacksonXmlElementWrapper(useWrapping = true)
	@JacksonXmlProperty(localName = "extension")
	private List<PropertyObject> extensions;

	public SystemObject getSystem() {
		return system;
	}

	public void setSystem(SystemObject system) {
		this.system = system;
	}

	public List<PropertyObject> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<PropertyObject> extensions) {
		this.extensions = extensions;
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(system.toMap());
		for(PropertyObject property : extensions) {
			map.put(property.getName(), property.getValue());
		}
		return map;
	}

	@Override
	public String toString() {
		return "ConfigObject [system=" + system + ", extensions=" + extensions + "]";
	}
	
}
