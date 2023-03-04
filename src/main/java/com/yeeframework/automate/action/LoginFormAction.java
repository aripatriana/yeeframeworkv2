package com.yeeframework.automate.action;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.util.LoginInfo;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebElementWrapper;
import com.yeeframework.automate.web.WebExchange;


/**
 * The action for access login
 * 
 * @author ari.patriana
 *
 */
@SuppressWarnings("deprecation")
public class LoginFormAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(LoginFormAction.class);
	
	private static final int MAX_RETRY_LOGIN = 3;
	private String memberCode;
	private String username;
	private String password;
	private String keyFile;
	private String token;
	private int retry = 0;
	
	public LoginFormAction(LoginInfo loginInfo) {
		this(loginInfo.getMemberCode(), loginInfo.getUsername(), loginInfo.getPassword(), loginInfo.getKeyFile());
	}
	
	public LoginFormAction(String memberCode, String username, String password, String keyFile) {
		this.memberCode = memberCode;
		this.username = username;
		this.password = password;
		this.keyFile = keyFile;
		try {
			this.token = new String(Files.readAllBytes(Paths.get(keyFile)));
		} catch (IOException e) {
			log.error("Error ", e);
		}
	}
	
	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}
	
	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getKeyFile() {
		return keyFile;
	}
	
	public String getMemberCode() {
		return memberCode;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {
		if (webExchange.get("token") != null) return;
		log.info("Open Login Page with " +  getMemberCode() + "/" + getUsername());
		
		Sleep.wait(1000);
		
		WebCommon.findElementById("memberCode").sendKeys(getMemberCode());
		WebCommon.findElementByName("username").sendKeys(getUsername());
		WebCommon.findElementByName("password").sendKeys(getPassword());
		WebCommon.findElementById("keyFile").sendKeys(getKeyFile());
		
		WebCommon.captureFullWindow();
		
		WebCommon.findElementByXpath("//button[text()='Sign In']").click();		
		
		try {
			WebCommon.findElementByXpath("//form//section[@class='error']", 1);
			
			try {
				WebCommon.findElementByXpath("//form//fieldset//section//div//div//p[contains(text(),'Bad credentials')]",1);
				throw new FailedTransactionException("Bad credentials for memberCode=" + getMemberCode() + " username=" + getUsername() + " password=" + getPassword());
			} catch (TimeoutException e) {
				// do nothing
			}
			
			if (retry < MAX_RETRY_LOGIN) {
				DriverManager.getDefaultDriver().navigate().refresh();
				retry++;
				this.submit(webExchange);				
			} else {
				throw new FailedTransactionException("Exceed maximum login attempt(" + MAX_RETRY_LOGIN+ ") for memberCode=" + getMemberCode() + " username=" + getUsername() + " password=" + getPassword());
			}
		} catch (TimeoutException e) {
			// do nothing
		}		
		
		webExchange.put("username", getUsername());
		webExchange.put("memberCode", getMemberCode());
		webExchange.put("password", getPassword());
		webExchange.put("token", getToken());	
		
		log.info("Login success");
	}

	
}
