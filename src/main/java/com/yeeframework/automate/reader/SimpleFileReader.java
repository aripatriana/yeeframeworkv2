package com.yeeframework.automate.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.FileReader;

/**
 * Read any file and process it as simple format
 *  
 * @author ari.patriana
 *
 */
public class SimpleFileReader implements FileReader<String> {

	private Logger log = LoggerFactory.getLogger(SimpleFileReader.class);
	
	private File file;
	private Scanner scanner;
	private int size;
	public SimpleFileReader(File file) {
		this.file = file;
		try {
			size= 0;
			scanner = new Scanner(file, "UTF-8");
		} catch (FileNotFoundException e) {
			log.error("ERROR ", e);
		}
	}
	
	@Override
	public File getFile() {
		return file;
	}

	@Override
	public boolean skipHeader() {
		return false;
	}

	@Override
	public String getHeader() {
		return null;
	}

	@Override
	public boolean iterate() {
		if (scanner == null) return false;
		return scanner.hasNextLine();
	}

	@Override
	public String read() {
		if (scanner == null) return null;
		size++;
		return scanner.nextLine();
	}
	
	@Override
	public void close() {
		scanner.close();
	}
	
	@Override
	public int getSize() {
		return size;
	}

}
