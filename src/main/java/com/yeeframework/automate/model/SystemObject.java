package com.yeeframework.automate.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class SystemObject {

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "property")
	private List<PropertyObject> properties;
	
	@JacksonXmlProperty(localName = "datasource")
	private DataSourceObject datasource;

	public List<PropertyObject> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyObject> properties) {
		this.properties = properties;
	}

	public DataSourceObject getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSourceObject datasource) {
		this.datasource = datasource;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(datasource.toMap());
		for(PropertyObject property : properties) {
			map.put(property.getName(), property.getValue());
		}
		return map;
	}
	@Override
	public String toString() {
		return "SystemObject [properties=" + properties + ", datasource=" + datasource + "]";
	}
	
}
