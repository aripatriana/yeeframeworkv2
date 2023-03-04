package com.yeeframework.automate.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.reader.BufferedFileReader;
import com.yeeframework.automate.util.StringUtils;

public class FileIO {

	private static final Logger log = LoggerFactory.getLogger(FileIO.class);
	
	public static Map<String, Object> loadMapValueFile(File path, String separator) {
		BufferedFileReader reader = new BufferedFileReader(path);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		while(reader.iterate()) {
			String text = reader.read();
			String last = StringUtils.removeCharIndex(text, separator, 0);
			if (!text.isEmpty() && !last.isEmpty())
				data.put(text.replace(separator+last, ""), last);
		}
		return data;		
	}
	public static Map<String, Object> loadMapValueFile(String path, String separator) {
		return loadMapValueFile(new File(path), separator);
	}
	
	public static void main(String[] args) {
		FileIO.loadMapValueFile("D:\\error.txt", "=");
	}
	
	public static Properties loadProperties(String path) {
		File file = new File(path); 
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			log.error("Error load properties ", e);
		} catch (IOException e) {
			log.error("Error load properties ", e);
		}
		return prop;
	}
	
	public static void write(String path, Map<String, Object> data) {
		try {
			StringBuffer sb = new StringBuffer();
			for (Entry<String, Object> e : data.entrySet()) {
				if (!sb.toString().isEmpty()) {
					sb.append(",");
				}
				sb.append(e.getKey() + "=" + e.getValue().toString());
			}
			write(new FileWriter(path), sb.toString());
		} catch (IOException e) {
			log.error("Error writer file ", e);
			e.printStackTrace();
		}
	}
	
	public static void write(FileWriter writer, String data) {
		try {
			writer.write(data);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			log.error("Error writer file ", e);
			e.printStackTrace();
		}
	}
	
	public static void searchFile(File[] files, String dir, Map<String, LinkedList<File>> mapFiles, String[] extensions) {
		for (File file : files) {
			if (file.isDirectory()) {
				searchFile(file.listFiles(), file.getName(), mapFiles, extensions);
			}
			
			if (file.isFile()) {
				if (StringUtils.endsWith(file.getName(), extensions)) {
					LinkedList<File> fileList = mapFiles.get(dir);
					if (fileList == null) fileList = new LinkedList<File>();
					fileList.add(file);
					mapFiles.put(dir, fileList);
				};
			}
		}
	}
}
