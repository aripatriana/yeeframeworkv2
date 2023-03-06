package com.yeeframework.automate.entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yeeframework.automate.Statement;
import com.yeeframework.automate.util.DataTypeUtils;
import com.yeeframework.automate.util.MapUtils;
import com.yeeframework.automate.util.QueryParseUtils;
import com.yeeframework.automate.web.WebExchange;

public class QueryEntry {

	public static final String ROUND_BRACKET = "()";
	public static final String SQUARE_BRACKET = "[]";
	public static final String[] BRACKETS = new String[] {ROUND_BRACKET,SQUARE_BRACKET};
	
	private String query;
	private Map<String, Statement> statements = new LinkedHashMap<String, Statement>();
	private List<String> columns = new LinkedList<String>();
	private List<String> parameters = new ArrayList<String>();
	private List<String> variables = new ArrayList<String>();

	public QueryEntry() {
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void addColumn(String column) {
//		int counter = 0;
//		String columnIndex = column;
//		
//		while (MapUtils.findEquals(columns, columnIndex) != null) {
//			counter++;
//			columnIndex=column +"#"+counter;
//		}
//		
		columns.add(column);
	}
	public void addStatement(String var1, String var2, String equality) {
		if (var1 != null && equality != null)
			statements.put(var1, new Statement(var1, var2, equality));
		else
			statements.put(var1, null);
		
		if (var1 != null && DataTypeUtils.checkType(var1, DataTypeUtils.TYPE_OF_VARIABLE))
			variables.add(var1);
		if (var2 != null && DataTypeUtils.checkType(var2, DataTypeUtils.TYPE_OF_VARIABLE))
			variables.add(var2);
	}
	
	public List<String> getColumns() {
		return columns;
	}
	
	public List<String> getVariables() {
		return variables;
	}
	
//	public List<String> getColumns() {
//		List<String> columns = new LinkedList<String>();
//		for (String column : statements.keySet()) {
//			column = com.nusantara.automate.util.StringUtils.removeLastChar(column, "#");
//			columns.add(column);
//		}
//		return columns;
//	}
	
	public Map<String, Statement> getStatements() {
		Map<String, Statement> temp = new LinkedHashMap<String, Statement>();
		MapUtils.copyValueNotNull(statements, temp);
		return temp;
	}
	
	public Map<String, Statement> getStatements(int index) {
		Map<String, Statement> temp = new LinkedHashMap<String, Statement>();
		MapUtils.copyValueNotNull(statements, temp);
		Map<String, Statement> result = new LinkedHashMap<String, Statement>();
		
		for (Entry<String, Statement> entry : temp.entrySet()) {
			String key = entry.getKey();
			Statement statement = new Statement(entry.getValue());
			
			if (statement.getArg1().contains(ROUND_BRACKET)) {
				statement.setArg1(statement.getArg1().replace(ROUND_BRACKET, "[" +  index + "]"));
			}
			if (statement.getArg2().contains(ROUND_BRACKET)) {
				statement.setArg2(statement.getArg2().replace(ROUND_BRACKET, "[" +  index + "]"));
			}
			if (key.contains(ROUND_BRACKET)) {
				key = key.replace(ROUND_BRACKET, "[" + index + "]");
			}
			result.put(key, statement);
		}
		return result;
	}
	
	public void setStatements(Map<String, Statement> statements) {
		this.statements = statements;
	}
	
	public void addParameter(String parameter) {
		parameters.add(parameter);
		variables.add(parameter);
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	public String[] getParsedQuery(WebExchange webExchange) throws Exception {
		return QueryParseUtils.parseArgs(getQuery(), getParameters(), webExchange);
	}
}
