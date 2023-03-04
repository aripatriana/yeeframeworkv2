package com.yeeframework.automate.execution;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Constants;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.action.LogoutFormAction;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.util.InjectionUtils;

/**
 * The execution of workflow comes from here
 * 
 * @author ari.patriana
 *
 */
public class WorkflowExecutor {

	Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
	
	public void execute(String testCaseId, String testScenId, Workflow workflow, WorkflowConfig config) throws Exception {
		ReportMonitor.scenInprogress(testCaseId, testScenId);
		
        MDC.put("testcase", testScenId);
        
		try {
			log.info("Execute testScenId " + testScenId);
			
			ContextLoader.getWebExchange().put(Constants.CURRENT_TESTSCEN_ID, testScenId);
			configureModules(config, testScenId);
			
			for (WorkflowEntry entry : config.getWorkflowEntries(testScenId)) {
				Keyword keyword = config.getKeyword(entry.getKeyword());
				InjectionUtils.setObject(keyword);
				
				keyword.run(config, entry, workflow);
			}
			
			workflow.executeWorkflow();
			 
			ReportMonitor.completeScen(testScenId);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
			// scenario halted caused by exception
			ReportMonitor.scenHalted(testCaseId, testScenId, e.getMessage());
		} finally {
			// if exception occured in any state of the workflow, must be ensured to logout the system
			 if (workflow.getWebExchange().get("token") != null) {
				 try {
					 workflow.executeImmediate(new LogoutFormAction());	 
				 } catch (Exception e) {
					 // if exception keeps stubborn then close driver
				 }
			 }
			 
			 // close driver cause new scenario will make new driver available
			 DriverManager.close();
			 workflow.clearSession();
				
			 MDC.remove("testcase");
		}
	}
	
	public void configureModules(WorkflowConfig wc, String testScenId) {
		if (wc.getWorkflowModule(testScenId) != null) {
			ContextLoader.getWebExchange().setModules(wc.getWorkflowModule(testScenId));
			ContextLoader.getWebExchange().setMandatoryModules(wc.getMandatoryModules(testScenId));
		}
	}
	
}

