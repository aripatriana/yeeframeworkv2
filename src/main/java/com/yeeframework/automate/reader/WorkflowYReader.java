package com.yeeframework.automate.reader;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.util.StringUtils;

/**
 * Translate the script inside y file and convert it to the WorkflowEntry that system can read
 * 
 * @author ari.patriana
 *
 */
public class WorkflowYReader {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WorkflowYReader.class);
	
	private SimpleFileReader fileReader;
	private TextCompiler compiler;
	private static String fileName;
	public WorkflowYReader(File file) {
		fileReader = new SimpleFileReader(file);
		compiler = new TextCompiler();
		fileName = file.getName();
	}
	
	public static class TextCompiler {
	
		private List<String> scripts = new LinkedList<String>();
		private StringBuffer tmp = new StringBuffer();
		
		public void compile(String rawText) throws ScriptInvalidException {
			rawText = rawText.trim();
			tmp.append(rawText);
			if (rawText.endsWith(";")) {
				scripts.add(convertToSystemFormat(check(tmp.toString())));
				tmp = new StringBuffer();
			} else if (rawText.endsWith("\"") || rawText.endsWith("+")) {
				tmp.append("#");
			} else {
				throw new ScriptInvalidException("Invalid script for " + rawText + " in " + fileName);
			}
		}
		
		public String convertToSystemFormat(String script) {
			if (script.startsWith("@")) {
				return "set(\"" + (script.endsWith(";") ? script.replace(";", "") : script) + "\");";
			} else {
				return script;
			}
		}
		
		private String check(String script) throws ScriptInvalidException {
			if (!script.contains("#")) return script;
			
			String checked = script.replace(" ", "").replace("#", "");
			checked = checked.replace("\"+\"","");
			checked = checked.replace("\\\"", "");
			checked = StringUtils.replaceCharForward(checked, '"', "");
			checked = StringUtils.replaceCharBackward(checked, '"', "");
			
			if (StringUtils.containsCharForward(checked, '"') > 0)
				throw new ScriptInvalidException("Invalid script for " + script.replace("#", "") + " in " + fileName);

			String[] parsed = script.split("#");
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<parsed.length; i++) {
				String temp = parsed[i].trim();
				if (i==0) {
					temp = StringUtils.trimBackward(StringUtils.replaceCharBackward(temp, '+', "", 0));
					temp = StringUtils.replaceCharBackward(temp, '"', "", 0);
				} else if (i==parsed.length-1) {
					temp = StringUtils.trimForward(StringUtils.replaceCharForward(temp, '+', "", 0));
					temp = StringUtils.replaceCharForward(temp, '"', "", 0);
				} else {
					temp = StringUtils.trimForward(StringUtils.replaceCharForward(temp, '+', "", 0));
					temp = StringUtils.replaceCharForward(temp, '"', "", 0);
					temp = StringUtils.trimBackward(StringUtils.replaceCharBackward(temp, '+', "", 0));
					temp = StringUtils.replaceCharBackward(temp, '"', "", 0);
				}
				sb.append(temp);
			}
			return sb.toString();
		}

		public List<String> getScripts() throws ScriptInvalidException {
			if (!tmp.toString().isEmpty()) throw new ScriptInvalidException("Invalid script for " + tmp.toString().replace("#", "") + " in " + fileName);
			return scripts;
		}
		
	}
	
	public LinkedList<WorkflowEntry> read() throws ScriptInvalidException {
		while(fileReader.iterate()) {
			String script = fileReader.read();
			script = script.replace("\t", "");
			if (!script.startsWith("//") && !script.isEmpty()) {
				compiler.compile(script);				
			}
		}
		fileReader.close();
		
		LinkedList<WorkflowEntry> workflowEntries = new LinkedList<WorkflowEntry>();
		for (String script : compiler.getScripts()) {
			workflowEntries.add(translate(script));			
		}
		return workflowEntries;
	}
	
	private WorkflowEntry translate(String script) throws ScriptInvalidException {
		WorkflowEntry workflowEntry = new WorkflowEntry();
		
		// check semicolon
		if (!script.endsWith(";")) throw new ScriptInvalidException("Missing semicolon " + script + " in " + fileName);
		script = StringUtils.replaceCharBackward(script, ';', "", 0);
		
		// check variable
		String variable = detectVariable(script);
		checkVariable(variable);
		workflowEntry.setVariable(variable);
		
		// check script
		String simpleScript = script;
		if (variable != null)
			simpleScript = script.replace("\"" + variable + "\"", "");
		String[] simpleScripts = simpleScript.split("\\.");

		if (simpleScripts.length == 2) {
			String actionType = simpleScripts[1].replace("()", "");
			if (simpleScripts[1].equals(actionType)) 
				throw new ScriptInvalidException("Missing or invalid bracket for " + script + " in " + fileName);
			workflowEntry.setActionType(actionType);
		}
		
		String basicScript = simpleScripts[0].replace("()", "");
		if (simpleScripts[0].equals(basicScript)) 
			throw new ScriptInvalidException("Missing or invalid bracket for " + script + " in " + fileName);
		workflowEntry.setKeyword(basicScript);
	
		return workflowEntry;
	}
	
	private void checkVariable(String script) throws ScriptInvalidException {
		if (script == null)
			return;
		Map<String, Integer> counter = new HashMap<String, Integer>(); 
		for (int i=0; i<script.length(); i++) {
			if (script.charAt(i) == '(')
				counter.put("c", (counter.get("c") == null ? 1 : counter.get("c") + 1));
			if (script.charAt(i) == ')')
				counter.put("c", (counter.get("c") == null ? -1 : counter.get("c") - 1));
			if (script.charAt(i) == '\'')
				counter.put("sq", (counter.get("sq") == null ? -1 : counter.get("sq") + 1));
		}
		if (counter.get("c") != null && (counter.get("c") != 0))
			throw new ScriptInvalidException("Missing or invalid bracket script for " + script + " in " + fileName);
		if (counter.get("sq") != null && counter.get("sq") % 2 != 0)
			throw new ScriptInvalidException("Invalid single quote for " + script + " in " + fileName);
	}
	
	private String detectVariable(String script) throws ScriptInvalidException {
		boolean findout = false;
		int start = 0;
		int end = 0;
		String temp = script.replace("\\\"", "##");
		for (int i=0; i < temp.length(); i++) {
			if (temp.charAt(i) == '"') {
				if (!findout) {
					start = i+1;
					findout = true;
				} else {
					if (findout && end != 0) {
						throw new ScriptInvalidException("Invalid quote variable for " + script + " in " + fileName);
					} 
					end = i;
				}
			}
		}
		
		if (start >0 && end == 0)
			throw new ScriptInvalidException("Invalid quote variable for " + script + " in " + fileName);
		
		if (start == 0 && end == 0) {
			return null;
		} 

		return script.substring(start, end);
	}
}
