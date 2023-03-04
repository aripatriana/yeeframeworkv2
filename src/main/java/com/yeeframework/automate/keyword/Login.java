package com.yeeframework.automate.keyword;

import java.util.Map;

import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.RunTestApplication;
import com.yeeframework.automate.action.LoginFormAction;
import com.yeeframework.automate.action.OpenPageAction;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.util.LoginInfo;

public class Login implements Keyword {

	@PropertyValue("login.url.it")
	private String loginUrl;
	
	@PropertyValue("login.url.cm")
	private String loginUrlCm;
	
	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.LOGIN;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		String prefix = LoginInfo.parsePrefixVariable(we.getVariable());
		final String loginUrl = ("it".equals(prefix)) ? this.loginUrl : this.loginUrlCm;	
		
		 workflow
			.action(new OpenPageAction(loginUrl))
			.action(new LoginFormAction(getLoginInfo(LoginInfo.parseVariable(we.getVariable()))));
	}
	
	public LoginInfo getLoginInfo(String variable) {
		Map<String, Object> loginUser = ConfigLoader.getLoginInfo(variable);
		return new LoginInfo(loginUser.get(variable + "." + RunTestApplication.PREFIX_MEMBER_CODE).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_USERNAME).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_PASSWORD).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_KEYFILE).toString());
	}
}
