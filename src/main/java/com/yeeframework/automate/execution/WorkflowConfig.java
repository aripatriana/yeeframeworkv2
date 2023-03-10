package com.yeeframework.automate.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.ActionHandler;
import com.yeeframework.automate.ActionType;
import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.model.TestCaseObject;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.util.SimpleEntry;

/**
 * Hold any workflow configuration
 * 
 * @author ari.patriana
 *
 */
public class WorkflowConfig {

	private static final Logger log = LoggerFactory.getLogger(WorkflowConfig.class);
	
	// mapping antara nama fungsi dan Fungsinya dalam bentuk SimpleEntry, fungsiName->->FungsiClass->Args
	private Map<String, SimpleEntry<Class<?>, Object[]>> functionMap = new HashMap<String, SimpleEntry<Class<?>,Object[]>>();
	
	// mapping antara menuId dan ActioHandler, menuId->ActionHandler
	private Map<String, Class<? extends ActionHandler>> handlerMap = new HashMap<String, Class<? extends ActionHandler>>();
	
	// mapping antara testScenId dan WorkflowEntry, testScenId->WorkflowEntry
	private Map<String, LinkedList<WorkflowEntry>> workflowEntries = new HashMap<String, LinkedList<WorkflowEntry>>();
	
	// mapping antara testCaseId dan Data Excell, testCaseId->Excel
	private Map<String, File> workflowDatas = new HashMap<String, File>();
	
	// mapping antara testCaseId dan mapping fileName dan Filenya, testCaseId->->fileName->File
	private Map<String, Map<String, File>> workflowQueries = new HashMap<String, Map<String, File>>();
	
	// mapping menuId dan Menu, menuId->Menu
	private Map<String, Menu> menuMap = new HashMap<String, Menu>();
	
	// list semua modul yang dikonfigure pada saat startup
	private Set<String> modules = new HashSet<String>();
	
	// mapping antara testScenId dan modulId2 yang digunakan dalam canvas, testScenId->List(module)
	private Map<String, Set<String>> workflowModules = new HashMap<String, Set<String>>();
	
	// list testScenId yang terdaftar
	private LinkedList<String> workflowTestScens = new LinkedList<String>();
	
	// list testCaseId yang terdaftar
	private LinkedList<String> workflowTestCases = new LinkedList<String>();
	
	// mapping antara testCaseId dan list testScenId, testCaseId->List(testScenId)
	private Map<String, List<String>> workflowMapTestCases = new HashMap<String, List<String>>();
	
	// mapping antara testScenId dan testCaseId, testScenId->testCaseId
	private Map<String, String> workflowMapTestScens = new HashMap<String, String>();
	
	// mapping file test scenario berdasarkan testScenId, testScenId->File
	private Map<String, File> workflowFiles = new HashMap<String, File>();
	
	// list Keyword yang terdaftar, keyword->Keyword
	private Map<String, Keyword> keywords = new HashMap<String, Keyword>();
	
	// list ActionType yang terdaftar, script->ActionType
	private Map<String, ActionType> actions = new HashMap<String, ActionType>();
	
	// list TestCase yang masuk dalam scheduler
	private List<TestCaseObject> schedules = new LinkedList<TestCaseObject>();
	
	// mandatory modules adalah data Modul/Menu yang memiliki mapping terhadap sheet Excell
	private Map<String, List<Class<?>>> testCaseEntities = new HashMap<String, List<Class<?>>>();
	
	public void addScheduledTestCase(TestCaseObject testCase) {
		schedules.add(testCase);
	}
	
	public Set<String> getModules() {
		return modules;
	}
	
	public boolean containModule(String module) {
		return modules.contains(module);
	}
	
	public List<TestCaseObject> getScheduledTestCase() {
		return schedules;
	}
	
	public void addAction(Class<? extends ActionType>  action) {
		Object o = ReflectionUtils.instanceObject(action);
		com.yeeframework.automate.keyword.ActionTypes.addAction(((ActionType)o).script());
		addAction(((ActionType)o).script(), (ActionType)o);
	}
	
	public void addAction(String script, ActionType action) {
		actions.put(script, action);
	}
	
	public ActionType getAction(String script) {
		return actions.get(script);
	}
	
	public Map<String, ActionType> getActions() {
		return actions;
	}
	
	public void addKeyword(Class<? extends Keyword> keyword) {
		Object o = ReflectionUtils.instanceObject(keyword);
		com.yeeframework.automate.keyword.Keywords.addKeyword(((Keyword)o).script());
		addKeyword(((Keyword)o).script(), (Keyword)o);
	}
	
	public void addKeyword(String script, Keyword keyword) {
		keywords.put(script, keyword);
	}
	
	public Keyword getKeyword(String script) {
		return keywords.get(script);
	}
	
	public Map<String, Keyword> getKeywords() {
		return keywords;
	}
	
	public Map<String, SimpleEntry<Class<?>, Object[]>> getFunctionMap() {
		return functionMap;
	}
	
	public void addFunction(String functionKey, Class<? extends Actionable> actionable, Object[] args) {
		functionMap.put(functionKey, new SimpleEntry<Class<?>, Object[]>(actionable, args));
	}

	public void addFunction(String functionKey, Class<? extends Actionable> actionable) {
		functionMap.put(functionKey, new SimpleEntry<Class<?>, Object[]>(actionable, null));
	}
	
	public Map<String, Class<? extends ActionHandler>> getHandlerMap() {
		return handlerMap;
	}
	
	public void addHandler(Menu[] menuList, Class<? extends ActionHandler> actionable) {
		for (Menu menu : menuList) {		
			modules.add(menu.getModuleId());
			menuMap.put(menu.getId(), menu);
			handlerMap.put(menu.getId(), actionable);
		}
	}
	
	public Set<String> getMandatoryModules(String testScenId) {
		Set<String> mandatories = new HashSet<String>();
		for (String a : testCaseEntities.keySet()) {
			for (String b : getWorkflowModule(testScenId)) {
				if (a.equalsIgnoreCase(b))
					mandatories.add(a);
			}
		}
		return mandatories;
	}

	public void addTestCaseEntity(String moduleId, Class<?> clazz) {
		List<Class<?>> c = testCaseEntities.get(moduleId);
		if (c == null)
			c = new ArrayList<Class<?>>();
		c.add(clazz);
		testCaseEntities.put(moduleId, c);
	}
	
	public Map<String, List<Class<?>>> getTestCaseEntities() {
		return testCaseEntities;
	}
	
	public Class<? extends ActionHandler> getHandler(String id) {
		return handlerMap.get(id);
	}
	
	public SimpleEntry<Class<?>, Object[]> getFunction(String functionKey) {
		return functionMap.get(functionKey);
	}
	
	public boolean isFunctionExists(String functionKey) {
		return functionMap.containsKey(functionKey);
	}
	
	public void addWorkflowEntry(String workflowTestScenId, LinkedList<WorkflowEntry> workflowEntry) {
		workflowTestScens.add(workflowTestScenId);
		workflowEntries.put(workflowTestScenId, workflowEntry);
	}
	
	public void addWorkflowTestCase(String workflowTestCaseId, String workflowTestScenId) {
		if (!workflowTestCases.contains(workflowTestCaseId))
			workflowTestCases.add(workflowTestCaseId);
		List<String> testScens = workflowMapTestCases.get(workflowTestCaseId);
		if (testScens == null)
			testScens = new LinkedList<String>();
		testScens.add(workflowTestScenId);
		workflowMapTestCases.put(workflowTestCaseId, testScens);
		workflowMapTestScens.put(workflowTestScenId, workflowTestCaseId);
	}
	
	public Map<String, String> getWorkflowMapTestScens() {
		return workflowMapTestScens;
	}
	
	public String getWorkflowMapTestScen(String key) {
		return workflowMapTestScens.get(key);
	}
	
	public void addWorkflowData(String workflowScan, File file) {
		workflowDatas.put(workflowScan, file);
	}
	
	public void addWorkflowQuery(String workflowScan, File file) {
		Map<String, File> files = workflowQueries.get(workflowScan);
		if (files == null)
			files = new HashMap<String, File>();
		files.put(file.getName().replace(".sql", ""), file);
		workflowQueries.put(workflowScan, files);
	}
	
	public Map<String, Map<String, File>> getWorkflowQueries() {
		return workflowQueries;
	}
	
	public File getWorkflowQuery(String scen, String filename) {
		if (!workflowQueries.containsKey(scen))
			return null;
		return workflowQueries.get(scen).get(filename);
	}
	
	public Map<String, File> getWorkflowDatas() {
		return workflowDatas;
	}
	
	public File getWorkflowData(String scen) {
		return workflowDatas.get(scen);
	}
	
	public LinkedList<WorkflowEntry> getWorkflowEntries(String workflowKey) {
		return workflowEntries.get(workflowKey);
	}
	
	public Map<String, LinkedList<WorkflowEntry>> getWorkflowEntries() {
		return workflowEntries;
	}
	
	public LinkedList<String> getWorkflowKey() {
		return workflowTestScens;
	}
	
	public boolean containWorkflowTestScen(String testScenId) {
		return workflowTestScens.contains(testScenId);
	}
	
	public List<String> getWorkflowTestCase(String testCaseId) {
		return workflowMapTestCases.get(testCaseId);
	}
	
	public Menu getMenu(String  id) {
		return menuMap.get(id);
	}
	
	public LinkedList<String> getWorkflowTestCases() {
		return workflowTestCases;
	}
	
	public boolean containWorkflowTestCases(String workflowScen) {
		return workflowTestCases.contains(workflowScen);
	}
	
	public Map<String, List<String>> getWorkflowMapTestCases() {
		return workflowMapTestCases;
	}
	
	public List<String> getWorkflowMapTestCase(String testCaseId) {
		return workflowMapTestCases.get(testCaseId);
	}
	
	public void addWorkflowModule(String testCaseId, Set<String> moduleId) {
		workflowModules.put(testCaseId, moduleId);
	}
	
	public Map<String, Set<String>> getWorkflowModules() {
		return workflowModules;
	}
	
	public Set<String> getWorkflowModule(String testCaseId) {
		return workflowModules.get(testCaseId);
	}
	
	public void checkModule(Set<String> modules) throws ScriptInvalidException {
		List<String> notExists = new ArrayList<String>();
		for (String module : modules) {
			if (!this.modules.contains(module))
				notExists.add(module);
		}
		
		// prefer to note warning than exceptio
		if (notExists.size() > 0)
			log.warn("These modules are not built-in eat, " + notExists.toString());
			//throw new ScriptInvalidException("Module not exists for " + notExists.toString());
	}
	
	public LinkedList<String> getWorkflowTestScens() {
		return workflowTestScens;
	}
	
	public void setWorkflowFiles(Map<String, File> workflowFiles) {
		this.workflowFiles = workflowFiles;
	}
	
	public void addWorkflowFile(String workflowKey, File file) {
		this.workflowFiles.put(workflowKey, file);
	}
	
	public Map<String, File> getWorkflowFiles() {
		return workflowFiles;
	}
	
	public File getWorkflowFile(String workflowKey) {
		return workflowFiles.get(workflowKey);
	}
	
	public void clear() {
		functionMap.clear();
		handlerMap.clear();
		workflowEntries.clear();
		workflowDatas.clear();
		menuMap.clear();
		workflowModules.clear();
		workflowTestScens.clear();
		workflowTestCases.clear();
		workflowMapTestCases.clear();
		workflowQueries.clear();
		workflowFiles.clear();
		
	}
}
