package com.yeeframework.automate.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yeeframework.automate.util.MapUtils;
import com.yeeframework.automate.util.QueryParseUtils;
import com.yeeframework.automate.web.WebExchange;

public class ArgsEntry {

	private Map<String, Object> args = new HashMap<String, Object>();
	private List<String> parameters = new ArrayList<String>();
	private String query;
	private String function;
	private String script;
	
	public ArgsEntry() {
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public String getScript() {
		return script;
	}
	
	public void setFunction(String function) {
		this.function = function;
	}
	
	public String getFunction() {
		return function;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setArgs(Map<String, Object> args) {
		this.args = args;
	}
	
	public Map<String, Object> getArgs() {
		return args;
	}
	
	public void addParameter(String parameter) {
		parameters.add(parameter);
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object>[] getParsedQuery(WebExchange webExchange) throws Exception {
		
		String[] queries = QueryParseUtils.parseArgs(MapUtils.mapAsString(getArgs(),"#"), getParameters(), webExchange);
		Map<String, Object>[] map = (Map<String, Object>[])  new Map[queries.length];
		for (int i=0; i<queries.length;i++) {
			map[i] = MapUtils.stringAsMap(queries[i], "#");
		}
		return map;
	}
}
