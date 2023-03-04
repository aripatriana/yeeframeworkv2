package com.yeeframework.automate.keyword;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic script that represented in y file
 * @author ari.patriana
 *
 */
public class ActionTypes {

	private final static Set<String> defaultActions = new HashSet<String>();
	
	public final static String REJECT = "reject";
	public final static String REJECT_DETAIL = "rejectDetail";
	public final static String MULTIPLE_REJECT = "rejectMultiple";
	public final static String APPROVE = "approve";
	public final static String APPROVE_DETAIL = "approveDetail";
	public final static String MULTIPLE_APPROVE = "approveMultiple";
	public final static String VALIDATE = "validate";
	public final static String UPLOAD = "upload";
	public final static String CHECK = "check";
	public final static String CHECK_DETAIL = "checkDetail";
	public final static String MULTIPLE_CHECK = "checkMultiple";
	public final static String SEARCH = "search";
	

	
	static {
		defaultActions.add(REJECT);
		defaultActions.add(REJECT_DETAIL);
		defaultActions.add(MULTIPLE_REJECT);
		defaultActions.add(APPROVE);
		defaultActions.add(APPROVE_DETAIL);
		defaultActions.add(MULTIPLE_APPROVE);
		defaultActions.add(VALIDATE);
		defaultActions.add(UPLOAD);
		defaultActions.add(CHECK);
		defaultActions.add(CHECK_DETAIL);
		defaultActions.add(MULTIPLE_CHECK);
		defaultActions.add(SEARCH);
	}
	
	public static void addAction(String script) {
		defaultActions.add(script);
	}
	
	public static Set<String> getActions() {
		return defaultActions;
	}
	
}
