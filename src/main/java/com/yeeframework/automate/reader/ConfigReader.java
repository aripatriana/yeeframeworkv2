package com.yeeframework.automate.reader;

import java.io.File;
import java.io.IOException;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.yeeframework.automate.model.ScheduledObject;

public class ConfigReader {

	private ObjectMapper xmlMapper;
	private File pathXmlFile;
	
	public ConfigReader(File pathXmlFile) {
		JacksonXmlModule xmlModule = new JacksonXmlModule();
//		xmlModule.setDefaultUseWrapper(false);
		xmlMapper = new XmlMapper(xmlModule);
		
		this.pathXmlFile = pathXmlFile;
	}
	
	public ScheduledObject read() throws JsonParseException, JsonMappingException, IOException {
		Assert.notNull(pathXmlFile, "path xml is null");
		
		return xmlMapper.readValue(pathXmlFile, ScheduledObject.class);
	}
}
