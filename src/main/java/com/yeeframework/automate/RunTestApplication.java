package com.yeeframework.automate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.action.ExecuteCmdAction;
import com.yeeframework.automate.entry.ArgsEntry;
import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowConfigAwareness;
import com.yeeframework.automate.execution.WorkflowConfigInitializer;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.io.FileIO;
import com.yeeframework.automate.keyword.ActionTypes;
import com.yeeframework.automate.keyword.Assert;
import com.yeeframework.automate.keyword.AssertAggregate;
import com.yeeframework.automate.keyword.AssertQuery;
import com.yeeframework.automate.keyword.Await;
import com.yeeframework.automate.keyword.ClearSession;
import com.yeeframework.automate.keyword.Execute;
import com.yeeframework.automate.keyword.ExecuteQuery;
import com.yeeframework.automate.keyword.Keywords;
import com.yeeframework.automate.keyword.LoadFile;
import com.yeeframework.automate.keyword.Login;
import com.yeeframework.automate.keyword.Logout;
import com.yeeframework.automate.keyword.OpenMenu;
import com.yeeframework.automate.keyword.Relogin;
import com.yeeframework.automate.keyword.SelectProduct;
import com.yeeframework.automate.keyword.a.Approve;
import com.yeeframework.automate.keyword.a.ApproveDetail;
import com.yeeframework.automate.keyword.a.Check;
import com.yeeframework.automate.keyword.a.CheckDetail;
import com.yeeframework.automate.keyword.a.MultipleApprove;
import com.yeeframework.automate.keyword.a.MultipleCheck;
import com.yeeframework.automate.keyword.a.Search;
import com.yeeframework.automate.keyword.a.Validate;
import com.yeeframework.automate.reader.ArgsReader;
import com.yeeframework.automate.reader.QueryReader;
import com.yeeframework.automate.reader.ScheduledReader;
import com.yeeframework.automate.reader.TemplateReader;
import com.yeeframework.automate.reader.TestCasePathReader;
import com.yeeframework.automate.reader.WorkflowYReader;
import com.yeeframework.automate.schedule.SchedulerJobFactory;
import com.yeeframework.automate.schedule.WorkflowScheduler;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.util.LoginInfo;
import com.yeeframework.automate.util.MapUtils;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.util.SimpleEntry;
import com.yeeframework.automate.util.StringUtils;
import com.yeeframework.automate.web.WebExchange;

import ch.qos.logback.classic.util.ContextInitializer;

/**
 * The main class to start the system, everything start from run(,,);
 * 
 * @author ari.patriana
 *
 */
public class RunTestApplication {

	private static final Logger log = LoggerFactory.getLogger(RunTestApplication.class);
	private static final String CONFIG_FILENAME = "config.properties";
	private static final String SCHEDULED_FILENAME = "scheduled.xml";
	private static final String USER_FILENAME = "user.properties";
	private static final String LOGBACK_FILE_PATH = "src/main/resources/logback.xml";
	private static final String CONFIG_FILE_PATH = "/config/" + CONFIG_FILENAME;
	private static final String USER_FILE_PATH = "/config/" + USER_FILENAME;
	private static final String SCHEDULED_PATH = "/config/" + SCHEDULED_FILENAME;
	private static final String DRIVER_FILE_PATH = "/lib/driver/bin/chromedriver.exe";

	public final static String SUFFIX_USER = "user";
	public final static String PREFIX_MEMBER_CODE = "memberCode";
	public final static String PREFIX_USERNAME = "username";
	public final static String PREFIX_PASSWORD = "password";
	public final static String PREFIX_KEYFILE = "keyFile";
	public final static String PREFIX_TOKEN = "token";
	
	private static final String[] FILE_EXTENSTIONS = new String[] {".y", ".xlx", ".xlsx", ".sql", ".element"};
	
	private static String extractParam(String[] args, String key, String defaultValue) {
		for (String arg : args) {
			if (arg.startsWith(key)) {
				return arg.substring(key.length(), arg.length());
			}
		}
		return defaultValue;
	}
	
	public static void run(Class<? extends RunTestWorkflow> clazz, String[] args) {
		System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, LOGBACK_FILE_PATH);
		
		WorkflowConfig workflowConfig = null;
		try {
			System.setProperty("user.dir", extractParam(args, "-Dbase.dir=", System.getProperty("user.dir")));
			
			// default path location
			String driverPathFile = StringUtils.path(System.getProperty("user.dir"), DRIVER_FILE_PATH);
			String configPathFile = StringUtils.path(System.getProperty("user.dir"), CONFIG_FILE_PATH);
			String userPathFile = StringUtils.path(System.getProperty("user.dir"), USER_FILE_PATH);
			String scheduledFile = StringUtils.path(System.getProperty("user.dir"), SCHEDULED_PATH);

			// custom path location 
			driverPathFile = extractParam(args, "-Ddriver.path=", driverPathFile);
			configPathFile = extractParam(args, "-Dconfig.path=", configPathFile);
			userPathFile = extractParam(args, "-Duser.path=", userPathFile);
			scheduledFile = extractParam(args, "-Dscheduled.path=", scheduledFile);
			
			// testcase mode : standalone, scheduled, and client/server
			String testCaseMode = extractParam(args, "-Dtestcase.mode=", null);
			String testCashPath = extractParam(args, "-Dtestcase.path=", null);
			String moduleName = null;
			
			cleanUpTempDir();
			
			setConfig(new String[] {configPathFile, userPathFile}, moduleName);

			setDriver(driverPathFile);
			
			workflowConfig = new WorkflowConfig();
			setWorkflowy(workflowConfig);
			setScheduled(new String[] {scheduledFile}, workflowConfig);
			
			RunTestWorkflow workflow = (RunTestWorkflow) ReflectionUtils.instanceObject(clazz);
			
			if (workflow != null) {
				// setup default worklflowconfig initialization
				WorkflowConfigInitializer defaultWofkflowConfigInit = new RunTestApplication.BuiltInWorkflowConfigInitializer();
				defaultWofkflowConfigInit.configure(workflowConfig);
				
				if (workflow instanceof WorkflowConfigInitializer) {
					((WorkflowConfigInitializer)workflow).configure(workflowConfig);
					verifyWorkflowy(workflowConfig);
				}
				if (workflow instanceof WorkflowConfigAwareness) {
					((WorkflowConfigAwareness) workflow).setWorkflowConfig(workflowConfig);
				}
				
				InjectionUtils.setObjectWithCustom(workflow, ConfigLoader.getConfigMap());
				
				if (testCaseMode == null) {
					workflow.testWorkflow();									
				} else {
					if (testCaseMode.equalsIgnoreCase("standalone")) {
						if (testCashPath == null) {
							workflow.testWorkflow();
						} else {
							TestCasePathReader r = new TestCasePathReader(testCashPath, workflowConfig);
							workflow.testWorkflow(r.read());
						}
					} else if (testCaseMode.equalsIgnoreCase("scheduled")) {
						WorkflowScheduler ws = new WorkflowScheduler(new SchedulerJobFactory(workflow), workflowConfig.getSchedules());
						ws.schedule();						
					} else if (testCaseMode.equalsIgnoreCase("client")) {
						// to do client server
					}
				}
			}
		} catch (InterruptedException e) {
			DriverManager.close();
		}catch (Exception e) {
			e.printStackTrace();
			log.error("ERROR ", e);
		} finally {
			if (workflowConfig != null)
				workflowConfig.clear();
			ConfigLoader.clear();
			ContextLoader.clear();
		}
	}
	
	private static void cleanUpTempDir() {
		try {
			FileUtils.cleanDirectory(new File(StringUtils.path(System.getProperty("user.dir"),"tmp")));		
		} catch (IOException e1) {
			log.error("ERROR ", e1);
		}
	}
	
	private static void setDriver(String driverPathFile) {
		if (driverPathFile != null) {
			log.info("Driver Path : " + driverPathFile);
			
			DriverManager.setDriverPath(driverPathFile);
		}
		
		Object headless = ConfigLoader.getConfig("browser.headless");
		if (headless != null) {
			log.info("Headless mode : " + headless);
			
			DriverManager.setHeadlessMode(headless.toString());
		}
	}
	
	private static void setConfig(String[] configPathFiles, String moduleName) throws IOException {
		Map<String, Object> systemData = new HashMap<String, Object>();
		
		systemData.put("{base_dir}", StringUtils.path(System.getProperty("user.dir")));
		systemData.put("{tmp_dir}", StringUtils.path(System.getProperty("user.dir"),"tmp"));
		systemData.put("{log_dir}", StringUtils.path(System.getProperty("user.dir") + "log"));
		systemData.put("{config_dir}", StringUtils.path(System.getProperty("user.dir"),"config"));
		systemData.put("{keyfile_dir}", StringUtils.path(System.getProperty("user.dir"),"config","keyfile"));
		systemData.put("{template_dir}", StringUtils.path(System.getProperty("user.dir"),"config","template"));
		systemData.put("{element_dir}", StringUtils.path(System.getProperty("user.dir"),"config","element"));
		systemData.put("{testcase_dir}", (moduleName != null ? moduleName : StringUtils.path(System.getProperty("user.dir"),"testcase")));
		systemData.put("{report_dir}", (moduleName != null ? moduleName : StringUtils.path(System.getProperty("user.dir"),"report")));
		ConfigLoader.getConfigMap().putAll(systemData);
		
	
		// element
		Map<String , LinkedList<File>> mapFiles = new HashMap<String, LinkedList<File>>();
		try {
			URL url = RunTestApplication.class.getClassLoader().getResource("element");
			File file = new File(url.toURI());
			searchFile(file.listFiles(), "elements", mapFiles);
		} catch (Exception e) {
			log.warn("Failed to load defined elements");
		}
		
		try {
			File workflowDir = new File(ConfigLoader.getConfig("{element_dir}").toString());
			searchFile(workflowDir.listFiles(), "elements", mapFiles);
			for (File file : MapUtils.combineValueAsList(mapFiles.values())) {
				Map<String, Object> elements = FileIO.loadMapValueFile(file, "=");
				MapUtils.concatMapKey(file.getName().replace(".element", "") + ".", elements);
				ConfigLoader.setElementMap(file.getName().replace(".element", ""), elements);
			}
		} catch (Exception e) {
			// do nothing
		}
		
		// config
		String config = StringUtils.findContains(configPathFiles, CONFIG_FILENAME);
		log.info("Config Properties : " + config);
		Map<String, Object> metadata = new HashMap<String, Object>();
		Properties prop = FileIO.loadProperties(config);
		for (final String name: prop.stringPropertyNames()) {
			String value = prop.getProperty(name);
			if (value.toString().contains("{") && value.toString().contains("}")) {
				value = replaceSystemVariable(systemData, value);
			}
		    metadata.put(name, value);
		}
		ConfigLoader.getConfigMap().putAll(metadata);
		
		// user
		String user = StringUtils.findContains(configPathFiles, USER_FILENAME);
		log.info("User Properties : " + user);
		Map<String, Map<String, Object>> loginUser = new HashMap<String, Map<String, Object>>();
		prop = FileIO.loadProperties(user);
		
		for (final String name: prop.stringPropertyNames()) {
			String value = prop.getProperty(name);
			if (value.toString().contains("{") && value.toString().contains("}")) {
				value = replaceSystemVariable(systemData, value);
			}
			
			String key = name.replace("." + PREFIX_MEMBER_CODE, "")
					.replace("." + PREFIX_USERNAME, "")
					.replace("." + PREFIX_PASSWORD, "")
					.replace("." + PREFIX_KEYFILE, "")
					.replace("." + PREFIX_TOKEN, "");
			
			Map<String, Object> login = loginUser.get(key);
			if (login == null) login = new HashMap<String, Object>();
			login.put(name, value);
			if (name.contains("keyFile")) {
				login.put(name.replace("keyFile", "token"), new String(Files.readAllBytes(Paths.get(value))));
				login.put(name, StringUtils.path(value));
			}
			loginUser.put(key, login);
		}
		ConfigLoader.getLoginInfo().putAll(loginUser);
	}
	
	private static String replaceSystemVariable(Map<String, Object> systemData, String value) {
		for (Entry<String, Object> entry : systemData.entrySet()) {
			if (value.contains(entry.getKey())) {
				value = value.replace(entry.getKey(), entry.getValue().toString());
				break;
			}
		}
		return value;
	}
	
	private static void setWorkflowy(WorkflowConfig workflowConfig) throws Exception {

		File workflowDir = new File(ConfigLoader.getConfig("{testcase_dir}").toString());
		
		log.info("Load workflow files " + workflowDir.getAbsolutePath());
		
		LinkedHashMap<String , LinkedList<File>> mapFiles = new LinkedHashMap<String, LinkedList<File>>();
		searchFile(workflowDir.listFiles(), "testcase", mapFiles);
		
		for (Entry<String, LinkedList<File>> testScens : mapFiles.entrySet()) {
			int i = 0, w = 0;
			for (File file : testScens.getValue()) {
				
				if (!file.isHidden()) {
					if (file.getName().endsWith(".y")) {
						String testScenId = testScens.getKey() + "_" + file.getName().replace(".y", "");
						log.info("Load testScenId file " + testScenId);
						w++;
						WorkflowYReader reader = new WorkflowYReader(file);
						LinkedList<WorkflowEntry> workFlowEntries = reader.read();
						for (WorkflowEntry entry : workFlowEntries) {
							if (entry.checkKeyword(Keywords.LOAD_FILE)) entry.setVariable(testScens.getKey());
						}
						workflowConfig.addWorkflowEntry(testScenId, workFlowEntries);
						workflowConfig.addWorkflowScan(testScens.getKey(), testScenId);
						workflowConfig.addWorkflowFile(testScenId, file);
					} else if (file.getName().endsWith(".xlsx")
							|| file.getName().endsWith(".xlx")) {
						log.info("Load data file " + testScens.getKey() + " -> " + file.getName());
						workflowConfig.addWorkflowData(testScens.getKey(), file);
						i++;
					} else if (file.getName().endsWith(".sql")) {
						log.info("Load sql file " + testScens.getKey() + " -> " + file.getName());
						workflowConfig.addWorkflowQuery(testScens.getKey(), file);
					}
				}
			}			
			
			if (i == 0 || w == 0 || i > 1) {
				throw new Exception("Tscen required file incomplete");
			}
			
		}
	}
	
	private static void setScheduled(String[] configPathFiles, WorkflowConfig workflowConfig) {
		String pathXml = StringUtils.findContains(configPathFiles, SCHEDULED_FILENAME);
//		try {
			ScheduledReader scheduledReader = new ScheduledReader(new File(pathXml));
//			ScheduledEntry scheduledEntry = scheduledReader.read();
//			for (ScheduledTestCase t : scheduledEntry.getTestCases()) {
//				TestCasePathReader r = new TestCasePathReader(t.getScenario(), workflowConfig);
//				t.setTestCasePath(r.read());
//				workflowConfig.addScheduled(t.getScenario(), t);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	private static void verifyWorkflowy(WorkflowConfig workflowConfig) throws ScriptInvalidException {
		
	
		
		
		for (Entry<String, LinkedList<WorkflowEntry>> entryList : workflowConfig.getWorkflowEntries().entrySet()) {
			String fileName = workflowConfig.getWorkflowFile(entryList.getKey()).getName();
			Set<String> moduleIdList = new LinkedHashSet<String>();
			for (WorkflowEntry entry : entryList.getValue()) {
				if (!entry.checkKeywords(new ArrayList<String>(Keywords.getKeywords())))
					throw new ScriptInvalidException("Invalid script for " + entry.getKeyword() + " in " + fileName);
		
			
				if (!entry.checkActionTypes(new ArrayList<String>(ActionTypes.getActions())))
					throw new ScriptInvalidException("Invalid action type for " + entry.getActionType() + " in " + fileName);
		
				
				if (entry.checkKeyword(Keywords.OPEN_MENU)) {
					Menu menu = workflowConfig.getMenu(entry.getVariable());
					if (menu == null) {
						throw new ScriptInvalidException("Menu not found for " + entry.getVariable() + " in " + fileName);
					}
					moduleIdList.add(menu.getModuleId());
				} else if (entry.checkKeyword(Keywords.EXECUTE)) {
					ArgsReader ar = new ArgsReader(entry.getVariable());
					ArgsEntry ae = ar.read();
					List<String> params = ae.getParameters();
					for (String p : params) {
						if (p.startsWith("@" + WebExchange.PREFIX_TYPE_DATA)
								|| p.startsWith("@" + WebExchange.PREFIX_TYPE_ELEMENT)) {
							moduleIdList.add(p.split("\\.")[1]);
						}
					}
					SimpleEntry<Class<?>, Object[]> function = workflowConfig.getFunction(ae.getFunction());
					if (function == null) {
						throw new ScriptInvalidException("Function not found for " + ae.getFunction() + " in " + fileName);
					}
				} else if (entry.checkKeyword(Keywords.ASSERT)) {
					String[] params = StringUtils.parseStatement(entry.getVariable(), Statement.MARK);
					for (String p : params) {
						if (p.startsWith("@" + WebExchange.PREFIX_TYPE_DATA)
								|| p.startsWith("@" + WebExchange.PREFIX_TYPE_ELEMENT)) {
							moduleIdList.add(p.split("\\.")[1]);
						}
					}
				} else if (entry.checkKeyword(Keywords.ASSERT_QUERY)) {
					QueryReader qr = new QueryReader(entry.getVariable());
					QueryEntry qe = qr.read();
					List<String> params = qe.getVariables();
					for (String p : params) {
						if (p.startsWith("@" + WebExchange.PREFIX_TYPE_DATA)
								|| p.startsWith("@" + WebExchange.PREFIX_TYPE_ELEMENT)) {
							moduleIdList.add(p.split("\\.")[1]);
						}
					}
				} else if (entry.checkKeyword(Keywords.ASSERT_AGGREGATE)) {
					File file = workflowConfig.getWorkflowQuery(workflowConfig.getWorkflowMapKey(entryList.getKey()), entry.getVariable());
					if (file == null) {
						throw new ScriptInvalidException("File not found for " + entry.getVariable() + " in " + fileName);
					}
					TemplateReader tr = new TemplateReader(file);
					QueryReader qr = new QueryReader(tr.read().toString());
					QueryEntry qe = qr.read();
					List<String> params = qe.getVariables();
					for (String p : params) {
						if (p.startsWith("@" + WebExchange.PREFIX_TYPE_DATA)
								|| p.startsWith("@" + WebExchange.PREFIX_TYPE_ELEMENT)) {
							moduleIdList.add(p.split("\\.")[1]);
						}
					}
				} else if (entry.checkKeyword(Keywords.EXECUTE_QUERY)) {
					QueryReader qr = new QueryReader(entry.getVariable());
					QueryEntry qe = qr.read();
					List<String> params = qe.getVariables();
					for (String p : params) {
						if (p.startsWith("@" + WebExchange.PREFIX_TYPE_DATA)
								|| p.startsWith("@" + WebExchange.PREFIX_TYPE_ELEMENT)) {
							moduleIdList.add(p.split("\\.")[1]);
						}
					}
				} else if (entry.checkKeyword(Keywords.LOGIN) || entry.checkKeyword(Keywords.RELOGIN)) {
					String variable = LoginInfo.parseVariable(entry.getVariable());
					Map<String, Object> login = ConfigLoader.getLoginInfo(variable);
					if (login == null) {
						throw new ScriptInvalidException("Login info not found for " + entry.getVariable() + " in " + fileName);
					}
					if (!StringUtils.match(LoginInfo.parsePrefixVariable(entry.getVariable()), new String[] {"it","cm"})) {
						throw new ScriptInvalidException("Prefix login info not found for " + entry.getVariable() + " in " + fileName);
					}
					
					if (login.get(variable + "." + PREFIX_MEMBER_CODE) == null
							|| login.get(variable + "." + PREFIX_USERNAME) == null
							|| login.get(variable + "." + PREFIX_PASSWORD) == null
							|| login.get(variable + "." + PREFIX_KEYFILE) == null) {
						throw new ScriptInvalidException("Login info not completed for " + entry.getVariable() + " in " + fileName);
					}
				}
			}
			workflowConfig.checkModule(moduleIdList);
			workflowConfig.addWorkflowModule(entryList.getKey(), moduleIdList);
		}
	}
	
	private static void searchFile(File[] files, String dir, Map<String, LinkedList<File>> mapFiles) {
		for (File file : files) {
			if (file.isDirectory()) {
				searchFile(file.listFiles(), file.getName(), mapFiles);
			}
			
			if (file.isFile()) {
				if (StringUtils.endsWith(file.getName(), FILE_EXTENSTIONS)) {
					LinkedList<File> fileList = mapFiles.get(dir);
					if (fileList == null) fileList = new LinkedList<File>();
					fileList.add(file);
					mapFiles.put(dir, fileList);
				};
			}
		}
	}
	

	static class BuiltInWorkflowConfigInitializer implements WorkflowConfigInitializer {
		
		@Override
		public void configure(WorkflowConfig workflowConfig) {
			workflowConfig.addKeyword(Assert.class);
			workflowConfig.addKeyword(AssertAggregate.class);
			workflowConfig.addKeyword(AssertQuery.class);
			workflowConfig.addKeyword(ClearSession.class);
			workflowConfig.addKeyword(Await.class);
			workflowConfig.addKeyword(Execute.class);
			workflowConfig.addKeyword(ExecuteQuery.class);
			workflowConfig.addKeyword(LoadFile.class);
			workflowConfig.addKeyword(Login.class);
			workflowConfig.addKeyword(Logout.class);
			workflowConfig.addKeyword(OpenMenu.class);
			workflowConfig.addKeyword(Relogin.class);
			workflowConfig.addKeyword(SelectProduct.class);
			workflowConfig.addKeyword(com.yeeframework.automate.keyword.Set.class);
			workflowConfig.addKeyword(com.yeeframework.automate.keyword.Set.class);
			
			workflowConfig.addAction(Validate.class);
			workflowConfig.addAction(Search.class);
			workflowConfig.addAction(Check.class);
			workflowConfig.addAction(CheckDetail.class);
			workflowConfig.addAction(Approve.class);
			workflowConfig.addAction(ApproveDetail.class);
			workflowConfig.addAction(MultipleCheck.class);
			workflowConfig.addAction(MultipleApprove.class);
			
			workflowConfig.addFunction("cmd", ExecuteCmdAction.class);
			
		}
	}
	
}
