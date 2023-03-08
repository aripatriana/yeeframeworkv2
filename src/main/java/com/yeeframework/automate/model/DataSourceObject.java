package com.yeeframework.automate.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DataSourceObject {

	@JacksonXmlProperty(localName = "connection-url")
	private String connectionUrl;
	
	@JacksonXmlProperty(localName = "driver-class")
	private String driverClass;
	
	@JacksonXmlProperty(localName = "username")
	private String username;
	
	@JacksonXmlProperty(localName = "password")
	private String password;

	public String getConnectionUrl() {
		return connectionUrl;
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("datasource.connection-url", getConnectionUrl());
		map.put("datasource.driver-class", getDriverClass());
		map.put("datasource.username", getUsername());
		map.put("datasource.password", getPassword());
		return map;
	}

	@Override
	public String toString() {
		return "DataSourceObject [connectionUrl=" + connectionUrl + ", driverClass=" + driverClass + ", username="
				+ username + ", password=" + password + "]";
	}
}
