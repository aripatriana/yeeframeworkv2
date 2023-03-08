package com.yeeframework.automate.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Assertion;
import com.yeeframework.automate.Constants;
import com.yeeframework.automate.Statement;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ReachTimeoutException;
import com.yeeframework.automate.model.QueryEntry;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.report.SnapshotEntry;
import com.yeeframework.automate.util.DBQuery;
import com.yeeframework.automate.util.DataTypeUtils;
import com.yeeframework.automate.util.MapUtils;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.util.StringUtils;
import com.yeeframework.automate.web.WebExchange;


/**
 * The action for delay process
 * 
 * @author ari.patriana
 *
 */
public class AwaitAction implements Actionable {

	Logger log = LoggerFactory.getLogger(AwaitAction.class);
	
	@PropertyValue(Constants.CURRENT_TESTCASE_ID)
	private String testcase;
	
	@PropertyValue(Constants.CURRENT_TESTSCEN_ID)
	private String scen;
	
	@PropertyValue(value = "timeout.default")
	private Integer defautTimeout;
	
	private Integer timeoutInSecond = 300;
	
	private Integer delay;
	private QueryEntry qe;
	
	public AwaitAction() {
		if (defautTimeout != null) 
			timeoutInSecond = defautTimeout;
	}
	public AwaitAction(Integer delay) {
		this();
		this.delay = delay;
	}
	
	public AwaitAction(QueryEntry qe) {
		this();
		this.qe = qe;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		if (delay != null) {
			log.info("Await for " + delay + "s");
			Sleep.wait(delay*1000);
			ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
					SnapshotEntry.SNAPSHOT_AS_RAWTEXT, "delay -> " + delay + "s", null, ReportManager.PASSED);
		} else {
			log.info("Query -> " + qe.getQuery());
			
			try {
				assertQuery(webExchange);
				
				ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
						SnapshotEntry.SNAPSHOT_AS_RAWTEXT, "await with condition -> " + qe.getQuery(), null, ReportManager.PASSED);
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
				log.error("Failed for transaction ", e);
				ReportMonitor.logError(webExchange.get(Constants.CURRENT_TESTCASE_ID).toString(),
						webExchange.get(Constants.CURRENT_TESTSCEN_ID).toString(), e.getMessage());
			}
		}
	}
	
	private void assertQuery(WebExchange webExchange) throws FailedTransactionException {
		if (qe.getStatements().size() > 1)
			throw new FailedTransactionException("Multiple statement is not allowed here!"); 
		long start = System.currentTimeMillis();
		String[] columns = new String[qe.getColumns().size()];
		columns = qe.getColumns().toArray(columns);
		
		List<String[]> result = new ArrayList<String[]>();
		try {
			int i = 0;
			for (String query : qe.getParsedQuery(webExchange)) {

				for (int j=0; j< columns.length; j++) {
					columns[j] = parseParameter(columns[j], webExchange);
				}
				
				while(true) {
					log.info("Execute Query -> " + query);
					result = DBQuery.selectQuery(query, columns, String.class);
			
					Assertion assertion = new Assertion();
					assertion.setQuery(query);
					for (String[] res : result) {
						Map<String, String> resultMap = MapUtils.copyAsMap(columns, res, String.class, String.class);
						for (Statement state : qe.getStatements(i).values()) {
							Statement statement = new Statement(state);
							if (statement.getEquality() != null) {
								if (statement.isArg1(DataTypeUtils.TYPE_OF_COLUMN)) {
									statement.setVal1(resultMap.get(parseParameter(statement.getArg1(), webExchange)));
								} else if (statement.isArg1(DataTypeUtils.TYPE_OF_VARIABLE)) {
									if (statement.getArg1().contains(QueryEntry.SQUARE_BRACKET)) {
										statement.setVal1(StringUtils.nvl(parseExclusiveVariable(statement.getArg1(), webExchange), "null"));
									} else {
										statement.setVal1(StringUtils.nvl(webExchange.get(statement.getArg1()),"null"));
									}
								} else {
									statement.setVal1(statement.getArg1());
								}
								if (statement.isArg2(DataTypeUtils.TYPE_OF_COLUMN)) {
									statement.setVal2(resultMap.get(parseParameter(statement.getArg2(), webExchange)));
								} else if (statement.isArg2(DataTypeUtils.TYPE_OF_VARIABLE)) {
									if (statement.getArg2().contains(QueryEntry.SQUARE_BRACKET)) {
										statement.setVal2(StringUtils.nvl(parseExclusiveVariable(statement.getArg2(), webExchange), "null"));
									} else {
										statement.setVal2(StringUtils.nvl(webExchange.get(statement.getArg2()),"null"));
									}
								} else {
									statement.setVal2(statement.getArg2());
								}
									
								assertion.addStatement(statement);
							}
						}
						i++;
					}
					
					if (assertion.isTrue() && result.size() > 0) {
						break;
					} else {
						long diff = System.currentTimeMillis() - start;
						if (diff > (timeoutInSecond * 1000)) {
							throw new ReachTimeoutException("Reach time out after " + timeoutInSecond + " seconds for " + query);
						}
						
						Sleep.wait(1000);
					}
				}

			}
		}  catch (ReachTimeoutException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			log.error("Failed execute query ", e);
			throw new FailedTransactionException(e.getMessage());
		}
	}
	
	private String parseParameter(String argument, WebExchange webExchange) {
		if (argument.startsWith("@")) {
			return StringUtils.quote(StringUtils.nvl(webExchange.get(argument),"null").toString());
		} else if (argument.contains("(") && argument.contains(")")) {
			for (String p : qe.getParameters()) {
				argument = argument.replace(p, StringUtils.quote(String.valueOf(webExchange.get(p))));	
			}
			return argument.replace(" ", "");
		} else {
			return argument;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private String parseExclusiveVariable(String argument, WebExchange webExchange) throws Exception {
		String result = null;
		Map<String, List<String>> squared = new HashMap<String, List<String>>();
		if(argument.contains(QueryEntry.SQUARE_BRACKET)) {
			String[] s = argument.split("\\" + QueryEntry.SQUARE_BRACKET);
			if (s.length > 2)
				throw new Exception("Not valid argument for " + argument);
			List<String> r = squared.get(s[0]);
			if (r == null) r = new ArrayList<String>();
			if (s.length == 2)
				r.add(s[1].replace(".","").trim());
			squared.put(s[0], r);
		}
		
		// []
		if (!squared.isEmpty()) {
			for (Entry<String, List<String>> e : squared.entrySet()) {
				Object o = webExchange.get(e.getKey()+QueryEntry.SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list for " + e.getKey()+QueryEntry.SQUARE_BRACKET);
				
				if (o != null) {
					List<Object> l = (List<Object>) o;
					if (e.getValue() != null && !e.getValue().isEmpty()) {
						for (String v : e.getValue()) {
							List<Object> values = MapUtils.mapAsList((List<Map<String, Object>>) (List<?>)l, v);
							result = MapUtils.listAsString(values, ",");
						}
					} else {
						result = MapUtils.listAsString(l, ",");
					}
				}
			}
		}
		
		return result;
	}

	
}
