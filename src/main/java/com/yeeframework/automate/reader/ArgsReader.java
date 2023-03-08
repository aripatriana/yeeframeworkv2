package com.yeeframework.automate.reader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.model.ArgsEntry;
import com.yeeframework.automate.model.QueryEntry;
import com.yeeframework.automate.util.StringUtils;

public class ArgsReader {
	
	private String variable;
	
	public ArgsReader(String variable) {
		this.variable = variable;
	}
	
	public ArgsEntry read() throws ScriptInvalidException {
		if (variable.contains("\\\""))
			variable = variable.replace("\\\"", "\"");
		ArgsEntry ae = new ArgsEntry();
		ae.setScript(variable);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		if (variable.trim().endsWith(";"))
			throw new ScriptInvalidException("Invalid character, semicolon not allowed in an arguments " + variable);
		if (variable.contains("?")) {
			ae.setFunction(variable.substring(0, variable.indexOf("?")));
			ae.setQuery(variable.substring(variable.indexOf("?")+1, variable.length()));
			String[] args = ae.getQuery().split("&");
			for (String p : args) {
				if (!p.contains("="))
					throw new ScriptInvalidException("Invalid argument in an execute function " + variable + " -> " + p);
				String split[] = StringUtils.split(p, "=");
				data.put(split[0], split[1]);
			}
			ae.setArgs(data);
			
		} else {
			ae.setFunction(variable);
		}
		
		if (ae.getQuery() != null)
			parseParameter(ae, ae.getQuery());
		return ae;
	}
	
	public void parseParameter(ArgsEntry qe, String query) {
		Map<String, String> temp = new HashMap<String, String>();
		query = StringUtils.replaceById(query, QueryEntry.BRACKETS, temp);

		for (int i=0; i<query.length(); i++) {
			if (query.charAt(i) == '@') {
				String param = StringUtils.substringUntil(query.substring(i, query.length()), new char[] {')',' ','\''});
				for (Entry<String, String> id : temp.entrySet()) {
					param = param.replace(id.getKey(), id.getValue());
				}
				qe.addParameter(param.trim());
			} 
		}
	}

}
