package com.yeeframework.automate;

import java.awt.Toolkit;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import com.yeeframework.automate.driver.ChromeDriverClosable;

/**
 * Manage the browser driver
 * 
 * @author ari.patriana
 *
 */
public class DriverManager {

	private static WebDriver wd;
	
	private static String driverPath = "D:/System/WebDriver/bin/chromedriver.exe";

	private static String headless;
	
	public static void setDriverPath(String driverPath) {
		DriverManager.driverPath = driverPath;
	}
	
	public static void setHeadlessMode(String headless) {
		DriverManager.headless = headless;
	}
	
	public static WebDriver getChromeDriver() {
		if (wd == null) {
			System.setProperty("webdriver.chrome.driver", driverPath);
			ChromeOptions option = new ChromeOptions();
			option.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			option.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
			if ("true".equalsIgnoreCase(headless)) {
				option.addArguments("--headless");
				wd =  new ChromeDriverClosable(option);
				wd.manage().window().setPosition(new Point(0,0));
				wd.manage().window().setSize(new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
			} else {
				wd =  new ChromeDriverClosable(option);
				wd.manage().window().maximize();
			}
		}
			
		return wd;
	}
	
	public static WebDriver getDefaultDriver() {
		return getChromeDriver();
	}
	
	public static void close() {
		try {
			if (wd != null) wd.close();
		} catch (WebDriverException e) {
			// do nothing
		}
		wd = null;
	}
}
