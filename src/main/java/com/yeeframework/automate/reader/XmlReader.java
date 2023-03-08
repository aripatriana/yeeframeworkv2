package com.yeeframework.automate.reader;

import java.io.File;
import java.io.IOException;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlReader {

	private ObjectMapper xmlMapper;
	private File pathXmlFile;
	
	public XmlReader(File pathXmlFile) {
		JacksonXmlModule xmlModule = new JacksonXmlModule();
//		xmlModule.setDefaultUseWrapper(false);
		xmlMapper = new XmlMapper(xmlModule);
		
		this.pathXmlFile = pathXmlFile;
	}
	
	public <T> T read(Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		Assert.notNull(pathXmlFile, "path xml is null");
		
		return xmlMapper.readValue(pathXmlFile, clazz);
	}
}
