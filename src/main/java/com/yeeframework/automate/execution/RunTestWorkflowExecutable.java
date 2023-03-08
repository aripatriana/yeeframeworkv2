package com.yeeframework.automate.execution;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.Constants;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.RunTestWorkflow;
import com.yeeframework.automate.model.TestCasePath;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.report.ScenEntry;
import com.yeeframework.automate.report.TestCaseEntry;
import com.yeeframework.automate.util.InjectionUtils;

/**
 * Default implementation of RunTestWorkflow
 * 
 * @author ari.patriana
 *
 */
public class RunTestWorkflowExecutable implements RunTestWorkflow, WorkflowConfigAwareness {

	Logger log = LoggerFactory.getLogger(RunTestWorkflowExecutable.class);
	
	WorkflowConfig workflowConfig;
	
	@Override
	public void setWorkflowConfig(WorkflowConfig workflowConfig) {
		this.workflowConfig = workflowConfig;
	}
	
	public void testWorkflow(TestCasePath testCasePath) {
		testWorkflow(testCasePath.getTestCases(), testCasePath.getTestCasesMap());
	}
	
	@Override
	public void testWorkflow() {
		testWorkflow(workflowConfig.getWorkflowTestCases(), workflowConfig.getWorkflowMapTestCases());
	}
	
	public void testWorkflow(List<String> testCasesId, Map<String, List<String>> testScensId) {
		long startExeDate = System.currentTimeMillis();
		try {
			configureInitReport(testCasesId, testScensId);
			
			for (String testCaseId : testCasesId) {
				try {
					Workflow workflow = Workflow.configure();
					ContextLoader.getWebExchange().put(Constants.CURRENT_TESTCASE_ID, testCaseId);
					ContextLoader.getWebExchange().put(Constants.START_TIME_MILIS_ID, startExeDate);
					
					WorkflowExecutor executor = new WorkflowExecutor();
					InjectionUtils.setObject(executor);
			
					for (String testScenId : testScensId.get(testCaseId)) {
						executor.execute(testCaseId, testScenId, workflow, workflowConfig);
					}
					
					ReportMonitor.completeTestCase(testCaseId);
				} catch (Exception e) {
					log.error("FATAL ERROR ", e);
					
					ReportMonitor.testCaseHalted(testCaseId, e.getMessage());
				}			
			}
		} catch (Exception e) {
			log.error("FATAL ERROR ", e);
		} finally {
			try {
				ReportManager report = new ReportManager(String.valueOf(startExeDate));
				InjectionUtils.setObjectSessionWithCustom(report, ConfigLoader.getConfigMap());
				
				report.createReport();
			} catch (IOException e) {
				log.error("FATAL ERROR ", e);
			}		

			log.info("Finished in "  + (System.currentTimeMillis()-startExeDate)/1000  + " seconds");
		}
	}
	
	private void configureInitReport(List<String> testCasesId, Map<String, List<String>> testScensId) {
		ReportMonitor.init();
		for (String testCaseId : testCasesId) {
			LinkedList<ScenEntry> scenEntries = new LinkedList<ScenEntry>();
			for (String testScenId : testScensId.get(testCaseId)) {
				ScenEntry scenEntry = new ScenEntry();
				scenEntry.setTestCaseId(testCaseId);
				scenEntry.setTscanId(testScenId);
				scenEntry.setStatus(ReportManager.NOTYET);
				scenEntries.add(scenEntry);
			}
			
			TestCaseEntry testCaseEntry = new TestCaseEntry();
			testCaseEntry.setTestCaseId(testCaseId);
			testCaseEntry.setStatus(ReportManager.NOTYET);
			testCaseEntry.setNumOfScen(scenEntries.size());
			ReportMonitor.addTestCaseEntry(testCaseEntry, scenEntries);
		}
	}
}
