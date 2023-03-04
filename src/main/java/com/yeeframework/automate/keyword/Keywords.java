package com.yeeframework.automate.keyword;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic script that represented in y file
 * @author ari.patriana
 *
 */
public class Keywords {


	private final static Set<String> defaultKeywords = new HashSet<String>();
	
	public final static String LOGIN = "login";
	public final static String RELOGIN = "relogin";
	public final static String LOGOUT = "logout";
	public final static String LOAD_FILE = "loadFile";
	public final static String OPEN_MENU = "openMenu";
	public final static String EXECUTE = "execute";
	public final static String EXECUTE_QUERY = "executeQuery";
	public final static String CLEAR_SESSION = "clearSession";
	public final static String COMMENT = "comment";
	public final static String SELECT_PRODUCT = "selectProduct";
	public final static String ASSERT = "assert";
	public final static String ASSERT_QUERY = "assertQuery";
	public final static String ASSERT_AGGREGATE = "assertAggr";
	public final static String AWAIT = "await";
	public final static String SET = "set";
	
	
	
	static {
		defaultKeywords.add(LOGIN);
		defaultKeywords.add(RELOGIN);
		defaultKeywords.add(LOGOUT);
		defaultKeywords.add(LOAD_FILE);
		defaultKeywords.add(OPEN_MENU);
		defaultKeywords.add(EXECUTE);
		defaultKeywords.add(EXECUTE_QUERY);
		defaultKeywords.add(CLEAR_SESSION);
		defaultKeywords.add(SELECT_PRODUCT);
		defaultKeywords.add(ASSERT);
		defaultKeywords.add(ASSERT_QUERY);
		defaultKeywords.add(ASSERT_AGGREGATE);
		defaultKeywords.add(AWAIT);
		defaultKeywords.add(SET);
	}
	
	public static void addKeyword(String script) {
		defaultKeywords.add(script);
	}
	
	public static Set<String> getKeywords() {
		return defaultKeywords;
	}
	
}
