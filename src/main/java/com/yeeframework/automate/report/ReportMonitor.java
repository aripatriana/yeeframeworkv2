package com.yeeframework.automate.report;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReportMonitor {

	// group of skenario
	private static LinkedList<TestCaseEntry> testCaseEntries = new LinkedList<TestCaseEntry>();
	
	// list of detail skenario 
	private static Map<String, LinkedList<ScenEntry>> scenEntries = new HashMap<String, LinkedList<ScenEntry>>();
	
	// skenario by sken id
	private static Map<String, ScenEntry> scenEntriesByTscenId = new HashMap<String, ScenEntry>();
	
	// data by session id
	private static LinkedHashMap<String, DataEntry> dataEntryBySessionId = new LinkedHashMap<String, DataEntry>();
	
	// data by tscan id
	private static Map<String, LinkedList<DataEntry>> dataEntries = new HashMap<String, LinkedList<DataEntry>>();
	
	// image by tscand id
	private static Map<String, LinkedList<SnapshotEntry>> imageEntries = new HashMap<String, LinkedList<SnapshotEntry>>();
	
	public static void init() {
		testCaseEntries.clear();
		scenEntries.clear();
		scenEntriesByTscenId.clear();
		dataEntryBySessionId.clear();
		dataEntries.clear();
		imageEntries.clear();
	}
	
	public static void addTestCaseEntry(TestCaseEntry testCaseEntry, LinkedList<ScenEntry> scenEntryList) {
		for (ScenEntry scenEntry : scenEntryList) {
			scenEntriesByTscenId.put(scenEntry.getTscanId(), scenEntry);			
		}
		testCaseEntries.add(testCaseEntry);
		scenEntries.put(testCaseEntry.getTestCaseId(), scenEntryList);
	}
	
	public static ScenEntry getScenEntry(String tscenId) {
		return scenEntriesByTscenId.get(tscenId);
	}
	
	public static TestCaseEntry getTestCaseEntry(String testCaseId) {
		for (TestCaseEntry testCase : testCaseEntries) {
			if (testCase.getTestCaseId().equals(testCaseId)) {
				return testCase;
			}
		}
		return null;
	}
	
	public static ScenEntry getScenEntry(String testCaseId, String scenId) {
		for (ScenEntry scenEntry : scenEntries.get(testCaseId)) {
			if (scenEntry.getTscanId().equals(scenId)) {
				return scenEntry;
			}
		}
		return null;
	}
	
	public static void logDataEntry(List<String> sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata) {
		for (String session : sessionId) {
			logDataEntry(session, testCaseId, tscenId, sessionData, rawdata, null, ReportManager.PASSED);			
		}
	}
	
	public static void logDataEntry(String sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata) {
		logDataEntry(sessionId, testCaseId, tscenId, sessionData, rawdata, null, ReportManager.PASSED);
	}

	public static void logDataEntry(List<String> sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata, String errorLog, String status) {
		for (String session : sessionId) {
			logDataEntry(session, testCaseId, tscenId, sessionData, rawdata, errorLog, status);
		}
	}
	
	public static void logDataEntry(String sessionId, String testCaseId, String tscenId, Map<String, Object> sessionData, Map<String, Object> rawdata, String errorLog, String status) {
		DataEntry dataEntry = new DataEntry();
		dataEntry.addMetaData(rawdata);
		dataEntry.setScenId(tscenId);
		dataEntry.setSessionId(sessionId);
		dataEntry.setSessionData(sessionData);
		dataEntry.setStatus(status);
		dataEntry.appendErrorLog(errorLog);
		dataEntry.setTestCaseId(testCaseId);
		logDataEntry(sessionId, dataEntry);
	}
	
	private static void logDataEntry(String sessionId, DataEntry dataEntry) {
		DataEntry data = dataEntryBySessionId.get(sessionId);
		if (data == null) {
			LinkedList<DataEntry> dataEntryList = dataEntries.get(dataEntry.getScenId());
			if (dataEntryList == null)  dataEntryList = new LinkedList<DataEntry>();
			dataEntryList.add(dataEntry);
			
			dataEntries.put(dataEntry.getScenId(), dataEntryList);
			dataEntryBySessionId.put(sessionId, dataEntry);
		} else {
			if (data.equals(dataEntry)) {
				data.setStatus(dataEntry.getStatus());
				data.appendErrorLog(dataEntry.getErrorLog());
				if (dataEntry.getSessionData() != null)
					data.setSessionData(dataEntry.getSessionData());
				if(dataEntry.getMetaData() != null && dataEntry.getMetaData().size() > 0)
					if (!data.checkMetaData(dataEntry.getMetaData().get(0))) {
						data.addAllMetaData(dataEntry.getMetaData());
					}
			}
		}
	}
	
	public static void logError(String testCaseId, String tscenId, String errorMessage) {
		ScenEntry scenEntry =  getScenEntry(testCaseId, tscenId);
		scenEntry.appendErrorLog(errorMessage);
		scenEntry.setStatus(ReportManager.FAILED);
	}
	
	public static void logSnapshotEntry(String testCaseId, String scenId, String sessionId, String as, String rawText, String filePath, String status) {
		SnapshotEntry snapshotEntry = new SnapshotEntry();
		snapshotEntry.setTscenId(scenId);
		snapshotEntry.setTestCaseId(testCaseId);
		snapshotEntry.setSessionId(sessionId);
		snapshotEntry.setSnapshotAs(as);
		snapshotEntry.setRawText(rawText);
		snapshotEntry.setImgFile(filePath);
		snapshotEntry.setStatus(status);
		logSnapshotEntry(snapshotEntry);
	}
	
	public static void logSnapshotEntry(String testCaseId, String scenId, String sessionId, String filePath, String status) {
		SnapshotEntry snapshotEntry = new SnapshotEntry();
		snapshotEntry.setTscenId(scenId);
		snapshotEntry.setTestCaseId(testCaseId);
		snapshotEntry.setSessionId(sessionId);
		snapshotEntry.setImgFile(filePath);
		snapshotEntry.setStatus(status);
		logSnapshotEntry(snapshotEntry);	
	}
	
	private static void logSnapshotEntry(SnapshotEntry imageEntry) {
		LinkedList<SnapshotEntry> imageEntryTemp = imageEntries.get(imageEntry.getTscenId());
		if (imageEntryTemp == null) imageEntryTemp = new LinkedList<SnapshotEntry>();
		imageEntryTemp.add(imageEntry);
		imageEntries.put(imageEntry.getTscenId(), imageEntryTemp);
	}
	

	public static LinkedList<TestCaseEntry> getTestCaseEntries() {
		return testCaseEntries;
	}
	
	public static LinkedList<ScenEntry> getScenEntries(String testCaseId) {
		return scenEntries.get(testCaseId);
	}
	
	public static LinkedList<DataEntry> getDataEntries(String tscenId) {
		return dataEntries.get(tscenId);
	}
	
	
	public static LinkedList<SnapshotEntry> getImageEntries(String tscenId) {
		return imageEntries.get(tscenId);
	}
	
	
	// menyelesaikan grup skenario dg complete
	public static void completeTestCase(String testCaseId) {
		boolean testCaseFailed = false;
		boolean testCaseHalted = false;
		int numfailed = 0;
		int numdata = 0;
		LinkedList<ScenEntry> scenEntryList = scenEntries.get(testCaseId);
		if (scenEntryList != null) {
			for (ScenEntry scen : scenEntries.get(testCaseId)) {
				if (scen.getStatus().equals(ReportManager.FAILED)
						|| scen.getStatus().equals(ReportManager.HALTED)) {
					numfailed++;
					testCaseFailed = true;
				}
				if (scen.getStatus().equals(ReportManager.HALTED)) {
					testCaseHalted = true;
				}
				numdata = numdata + scen.getNumOfData();
			}			
		}
		
		TestCaseEntry testCase = getTestCaseEntry(testCaseId);
		testCase.setNumOfFailed(numfailed);
		testCase.setNumOfData(numdata);
		if (testCaseFailed)
			if (testCaseHalted)
				testCase.setStatus(ReportManager.HALTED);
			else
				testCase.setStatus(ReportManager.FAILED);
		else
			testCase.setStatus(ReportManager.PASSED);
	}
	
	
	// menyelesaikan skenario dg complete
	public static void completeScen(String tscenId) {
		ScenEntry scen = scenEntriesByTscenId.get(tscenId);
		if (scen != null) {
			StringBuffer sb = new StringBuffer();
			int numfailed = 0;
			if (scen.getStatus().equals(ReportManager.INPROGRESS)) {
				boolean scenFailed = false;
				if (dataEntries.get(tscenId) != null) {
					for (DataEntry dataEntry : dataEntries.get(tscenId)) {
						if (dataEntry.getStatus().equals(ReportManager.FAILED)
								||dataEntry.getStatus().equals(ReportManager.HALTED)) {
							numfailed++;
							scenFailed=true;
							sb.append(dataEntry.getErrorLog());
							sb.append(System.lineSeparator());
						}
					}					
				}
				
				if (imageEntries.get(tscenId) != null) {
					for (SnapshotEntry imageEntry : imageEntries.get(tscenId)) {
						if (imageEntry.getStatus().equals(ReportManager.FAILED)
								|| imageEntry.getStatus().equals(ReportManager.HALTED)) {
							scenFailed=true;
							break;
						}
					}					
				}
				
				scen.setFailedRow(numfailed);
				scen.appendErrorLog(sb.toString());
				if (scenFailed) {
					scen.setStatus(ReportManager.FAILED);
				} else {
					scen.setStatus(ReportManager.PASSED);
				}			
			}
		}
	}
	
	
	// menghentikan skenario, dan skenario2 lain dalam state inprogress/not yet
	public static void scenHalted(String testCaseId, String tscenId, String errorLog) {
		ScenEntry scen = scenEntriesByTscenId.get(tscenId);
		scen.appendErrorLog(errorLog);
		scen.setFailedRow(scen.getNumOfData());
		scen.setStatus(ReportManager.HALTED);
		
		LinkedList<DataEntry> dataEntryList = dataEntries.get(tscenId);
		if (dataEntryList != null) {
			for (DataEntry dataEntry : dataEntryList) {
				if (dataEntry.getStatus().equals(ReportManager.INPROGRESS)
						|| dataEntry.getStatus().equals(ReportManager.NOTYET))
					dataEntry.setStatus(ReportManager.HALTED);
			}
		}
		
		LinkedList<SnapshotEntry> imageEntryList = imageEntries.get(tscenId);
		if (imageEntryList != null) {
			for (SnapshotEntry imageEntry : imageEntryList) {
				if (imageEntry.getStatus().equals(ReportManager.INPROGRESS)
						|| imageEntry.getStatus().equals(ReportManager.NOTYET))
					imageEntry.setStatus(ReportManager.HALTED);
			}
		}
	}
	
	
	// menghentikan testcase, dan testcase2 lain dalam state inprogres/not yet
	public static void testCaseHalted(String testCaseId, String errorLog) {
		boolean setErrorLog = false;
		int numfailed = 0;
		int numdata = 0;
		LinkedList<ScenEntry> scenEntryList = scenEntries.get(testCaseId);
		if (scenEntryList != null) {
			for (ScenEntry scenEntry : scenEntries.get(testCaseId)) {
				if (scenEntry.getStatus().equals(ReportManager.FAILED)
						|| scenEntry.getStatus().equals(ReportManager.HALTED)) numfailed++;
				if (scenEntry.getStatus().equals(ReportManager.INPROGRESS)
						|| scenEntry.getStatus().equals(ReportManager.NOTYET)) {
					scenEntry.setStatus(ReportManager.HALTED);
					if (!setErrorLog)
						scenEntry.setErrorLog(errorLog);
					scenEntry.setFailedRow(scenEntry.getNumOfData());
					setErrorLog = true;
					numfailed++;
				}
				numdata = numdata + scenEntry.getNumOfData();
			}
		}
		
		TestCaseEntry testCase = getTestCaseEntry(testCaseId);
		testCase.setNumOfData(numdata);
		testCase.setNumOfFailed(numfailed);
		testCase.setStatus(ReportManager.HALTED);
	}
	
	// mengubah state menjadi inprogress
	public static void scenInprogress(String testCaseId, String scenId) {
		TestCaseEntry testCase = getTestCaseEntry(testCaseId);
		testCase.setStatus(ReportManager.HALTED);
		
		ScenEntry scen = scenEntriesByTscenId.get(scenId);
		scen.setStatus(ReportManager.INPROGRESS);
	}
	
}
