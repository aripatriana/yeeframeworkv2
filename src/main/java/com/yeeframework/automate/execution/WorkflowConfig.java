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
import com.yeeframework.automate.schedule.TestCaseObject;
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
	private Map<String, SimpleEntry<Class<?>, Object[]>> functionMap = new HashMap<String, SimpleEntry<Class<?>,Object[]>>();
	private Map<String, Class<? extends ActionHandler>> handlerMap = new HashMap<String, Class<? extends ActionHandler>>();
	private Map<String, LinkedList<WorkflowEntry>> workflowEntries = new HashMap<String, LinkedList<WorkflowEntry>>();
	private Map<String, File> workflowDatas = new HashMap<String, File>();
	private Map<String, Map<String, File>> workflowQueries = new HashMap<String, Map<String, File>>();
	private Map<String, Menu> menuMap = new HashMap<String, Menu>();
	private Set<String> modules = new HashSet<String>();
	private Map<String, Set<String>> workflowModules = new HashMap<String, Set<String>>();
	private LinkedList<String> workflowKeys = new LinkedList<String>();
	private LinkedList<String> workflowScens = new LinkedList<String>();
	private Map<String, List<String>> workflowMapScens = new HashMap<String, List<String>>();
	private Map<String, String> workflowMapKeys = new HashMap<String, String>();
	private Map<String, File> workflowFiles = new HashMap<String, File>();
	private Map<String, Keyword> keywords = new HashMap<String, Keyword>();
	private Map<String, ActionType> actions = new HashMap<String, ActionType>();
	private List<TestCaseObject> schedules = new LinkedList<TestCaseObject>();
	private Set<String> mandatoryModules = new HashSet<String>();
	
	public void addScheduledTestCase(TestCaseObject testCase) {
		schedules.add(testCase);
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
			if (menu.isMandatoryCheck())
				mandatoryModules.add(menu.getModuleId());
		}
	}
	
	public Set<String> getAllMandatoryModules() {
		return mandatoryModules;
	}
	
	public Set<String> getMandatoryModules(String testScenId) {
		Set<String> mandatories = new HashSet<String>();
		for (String a : mandatoryModules) {
			for (String b : getWorkflowModule(testScenId)) {
				if (a.equalsIgnoreCase(b))
					mandatories.add(a);
			}
		}
		return mandatories;
	}
	
	public void setMandatoryModules(Set<String> mandatoryModules) {
		this.mandatoryModules = mandatoryModules;
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
	
	public void addWorkflowEntry(String workflowKey, LinkedList<WorkflowEntry> workflowEntry) {
		workflowKeys.add(workflowKey);
		workflowEntries.put(workflowKey, workflowEntry);
	}
	
	public void addWorkflowScan(String workflowScan, String workflowKey) {
		if (!workflowScens.contains(workflowScan))
			workflowScens.add(workflowScan);
		List<String> keys = workflowMapScens.get(workflowScan);
		if (keys == null)
			keys = new LinkedList<String>();
		keys.add(workflowKey);
		workflowMapScens.put(workflowScan, keys);
		workflowMapKeys.put(workflowKey, workflowScan);
	}
	
	public Map<String, String> getWorkflowMapKeys() {
		return workflowMapKeys;
	}
	
	public String getWorkflowMapKey(String key) {
		return workflowMapKeys.get(key);
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
		return workflowKeys;
	}
	
	public boolean containWorkflowKey(String worklowKey) {
		return workflowKeys.contains(worklowKey);
	}
	
	public List<String> getWorkflowKey(String scen) {
		return workflowMapScens.get(scen);
	}
	
	public Menu getMenu(String  id) {
		return menuMap.get(id);
	}
	
	public LinkedList<String> getWorkflowScens() {
		return workflowScens;
	}
	
	public boolean containWorkflowScen(String workflowScen) {
		return workflowScens.contains(workflowScen);
	}
	
	public Map<String, List<String>> getWorkflowMapScens() {
		return workflowMapScens;
	}
	
	public List<String> getWorkflowMapScens(String scen) {
		return workflowMapScens.get(scen);
	}
	
	public void addWorkflowModule(String scen, Set<String> moduleId) {
		workflowModules.put(scen, moduleId);
	}
	
	public Map<String, Set<String>> getWorkflowModules() {
		return workflowModules;
	}
	
	public Set<String> getWorkflowModule(String scen) {
		return workflowModules.get(scen);
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
	
	public LinkedList<String> getWorkflowKeys() {
		return workflowKeys;
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
		workflowKeys.clear();
		workflowScens.clear();
		workflowMapScens.clear();
		workflowQueries.clear();
		workflowFiles.clear();
		
	}
}
