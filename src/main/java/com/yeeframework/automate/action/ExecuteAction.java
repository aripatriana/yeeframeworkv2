package com.yeeframework.automate.action;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Constants;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.entry.ArgsEntry;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.report.SnapshotEntry;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.util.SimpleEntry;
import com.yeeframework.automate.web.WebExchange;


public class ExecuteAction implements Actionable {

	Logger log = LoggerFactory.getLogger(ExecuteAction.class);
	
	private ArgsEntry ae;
	
	private SimpleEntry<Class<?>, Object[]> function;
	
	@PropertyValue(Constants.CURRENT_TESTCASE_ID)
	private String testcase;
	
	@PropertyValue(Constants.CURRENT_TESTSCEN_ID)
	private String scen;
	
	public ExecuteAction(SimpleEntry<Class<?>, Object[]> function, ArgsEntry ae) {
		this.function = function;
		this.ae = ae;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		log.info("Query -> " + ae.getQuery());
		
		if (ae.getParameters() != null && ae.getParameters().size() > 0) {
			if (webExchange.getCountSession() == 0) {
				ReportMonitor.logError(webExchange.get(Constants.CURRENT_TESTCASE_ID).toString(),
						webExchange.get(Constants.CURRENT_TESTSCEN_ID).toString(), "The session is needed when executing the query using a variable, use loadFile()");
				throw new FailedTransactionException("The session is needed when executing the query using a variable, use loadFile()");
			}
			
			// distinct module
			Set<String> module = new HashSet<String>();
			for (String variable : ae.getParameters()) {
				if (variable.startsWith("@"+WebExchange.PREFIX_TYPE_DATA)) {
					module.add(variable.split("\\.")[1]);
				}
			}
						
			for (int i=0; i<webExchange.getCountSession(); i++) {
				try {
					String sessionId = webExchange.createSession(i);
					if (!webExchange.isSessionFailed(sessionId)) {
						webExchange.setCurrentSession(sessionId);
					
						// log data monitor
						for (String m : module) {
							webExchange.setCurrentSession(i);
							Map<String, Object> metadata = webExchange.getMetaData(m, i);
							ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get(Constants.CURRENT_TESTCASE_ID).toString(),
									webExchange.get(Constants.CURRENT_TESTSCEN_ID).toString(), null, metadata);
						}
						
						execute(webExchange);		
					}
				} catch (FailedTransactionException e) {
					webExchange.addFailedSession(webExchange.getCurrentSession());
					log.error("Failed for transaction ", e);
					ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get(Constants.CURRENT_TESTCASE_ID).toString(),
							webExchange.get(Constants.CURRENT_TESTSCEN_ID).toString(), null, null, 
							e.getMessage(), ReportManager.FAILED);
				}
			}				
		} else {
			try {
				execute(webExchange);
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
				log.error("Failed for transaction ", e);
				ReportMonitor.logError(webExchange.get(Constants.CURRENT_TESTCASE_ID).toString(),
						webExchange.get(Constants.CURRENT_TESTSCEN_ID).toString(), e.getMessage());
			}
		}
	}
	
	private void execute(WebExchange webExchange) throws FailedTransactionException {
		try {
			if (ae.getQuery() != null) {
				for (Map<String,Object> query : ae.getParsedQuery(webExchange)) {
					log.info("Execute Function " + ae.getFunction() + " with Query -> " + query);
					Object object = ReflectionUtils.instanceObject(function.getKey());
					InjectionUtils.setObjectWithCustom(object, query);
					((Actionable) object).submit(webExchange);
					
					ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
							SnapshotEntry.SNAPSHOT_AS_RAWTEXT, ae.getScript(), null, ReportManager.PASSED);
				}	
			} else {
				Object object = ReflectionUtils.instanceObject(function.getKey(), function.getValue());
				InjectionUtils.setObject(object);
				((Actionable) object).submit(webExchange);;
			
			}
		} catch (Exception e) {
			throw new FailedTransactionException("Failed execute function " + e.getMessage());
		}
	}
}
