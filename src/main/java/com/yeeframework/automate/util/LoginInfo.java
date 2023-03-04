package com.yeeframework.automate.util;

public class LoginInfo {

	private String memberCode;

	private String username;

	private String password;

	private String keyFile;
	
	public LoginInfo(String memberCode, String username, String password, String keyFile) {
		this.memberCode = memberCode;
		this.username = username;
		this.password = password;
		this.keyFile = keyFile;
	}

	public String getMemberCode() {
		return memberCode;
	}

	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}
	
	public static String parseVariable(String variable) {
		String prefix = StringUtils.substringUntil(variable, new String[] {"->"});
		if (prefix != null) {
			variable = variable.replace(prefix+"->", "");
		}
		return variable;
	}
	
	public static String parsePrefixVariable(String variable) {
		String prefix = StringUtils.substringUntil(variable, new String[] {"->"});
		if (prefix != null) {
			return prefix;
		}
		return "it";
	}
	
}
