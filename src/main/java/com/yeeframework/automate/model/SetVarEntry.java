package com.yeeframework.automate.model;

import com.yeeframework.automate.web.WebExchange;

public class SetVarEntry {

	private String variable;
	private String value;
	private QueryEntry query;
	private String script;
	
	
	public SetVarEntry() {
	}
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
		
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value.replace("'", "");
	}

	public QueryEntry getQuery() {
		return query;
	}

	public void setQuery(QueryEntry query) {
		this.query = query;
	}

	public String[] getParsedQuery(WebExchange webExchange) throws Exception {
		return query.getParsedQuery(webExchange);
		
	}
}
