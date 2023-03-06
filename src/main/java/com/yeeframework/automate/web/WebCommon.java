package com.yeeframework.automate.web;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.yeeframework.automate.Checkpoint;
import com.yeeframework.automate.Constants;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.action.OpenMenuLevel3Action;
import com.yeeframework.automate.action.OpenMenuLevel1Action;
import com.yeeframework.automate.action.OpenMenuLevel2Action;
import com.yeeframework.automate.driver.WebDriverClosable;
import com.yeeframework.automate.screen.WindowScreen;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.util.Sleep;

public class WebCommon {

	private static Logger log = LoggerFactory.getLogger(WebCommon.class);
	
	protected static WebDriver wd;
	private static WindowScreen ws;
	private static Checkpoint cp;
	
	public static WebDriver getDriver() {
		if (wd == null || (wd != null && ((WebDriverClosable)wd).isClosed())) {
			wd = DriverManager.getDefaultDriver();
			ws = new WindowScreen(wd);
			InjectionUtils.setObject(ws);

			cp = new Checkpoint();
			InjectionUtils.setObject(cp);
		}
		return wd;
	}
	
	/* find element section */
	public static WebElement findElementById(String id) {
		return findElementById(id, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	public static WebElement findElementById(String id, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
	}
	
	public static WebElement findElementByXpath(String xpath) {
		return findElementByXpath(xpath, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	public static WebElement findElementByXpath(String xpath, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
	}
	
	public static WebElement findElementByName(String name) {
		return findElementByName(name, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	protected static WebElement findElementByName(String name, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name)));
	}
	
	protected static WebElement findElementByClassName(String className) {
		return findElementByClassName(className, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	protected static WebElement findElementByClassName(String className, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
	}
	
	
	public static WebElement findElementById(WebElement webElement, final String id) {
		return findElementById(webElement, id, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	public static WebElement findElementById(WebElement webElement, final String id, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
	    return wait.until(new Function<WebElement, WebElement>() {
               public WebElement apply(WebElement d) {
                   return d.findElement(By.id(id));
               }
        });
	}
	
	public static WebElement findElementByXpath(WebElement webElement, final String xpath) {
		return findElementByXpath(webElement, xpath, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	public static WebElement findElementByXpath(WebElement webElement, final String xpath, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.xpath(xpath));
            }
		});
	}
	
	public static WebElement findElementByName(WebElement webElement, final String name) {
		return findElementByName(webElement, name, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	public static WebElement findElementByName(WebElement webElement, final String name, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.name(name));
            }
		});
	}
	
	public static WebElement findElementByClassName(WebElement webElement, final String className) {
		return findElementByClassName(webElement, className, Constants.WEB_TIMEOUT_IN_SECOND);
	}
	
	public static WebElement findElementByClassName(WebElement webElement, final String className, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.className(className));
            }
		});
	}
	
	public static boolean isElementExist(String id) {
		try {
			findElementById(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void setInput(WebElement we, String value) {
		Assert.notNull(we);
		try {
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				we.clear();
				we.sendKeys(value);
				Sleep.wait(200);
			} else {
				log.info("Element " + we.getAttribute("id") + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + we.getAttribute("id") + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + we.getAttribute("id") + " is not found");
		}
	}
	
	public static void clickButton(WebElement parentElement, String id) {
		try {
			WebElement we = findElementById(parentElement, id, Constants.WEB_INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	public static void clickButton(WebElement we) {
		Assert.notNull(we);
		
		try {
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element " + we.getAttribute("id") + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + we.getAttribute("id") + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + we.getAttribute("id") + " is not found");
		}
	}
	
	public static void setWindowFocus(WebElement we) {
		Assert.notNull(we);
		
		try {
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				if("input".equals(we.getTagName())) {
					we.sendKeys("");
				} else{
					JavascriptExecutor jse = (JavascriptExecutor) wd;
					jse.executeScript("document.getElementById('" + we.getAttribute("id") + "').focus();");
				}
			}
		} catch (TimeoutException e) {
			log.info("Element " + we.getAttribute("id") + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + we.getAttribute("id") + " is not found");
		}
	}
	
	public static String getTextById(String id) {
		return findElementById(id).getText();
	}
	
	public static String getTextByName(String name) {
		return findElementByName(name).getText();
	}
	
	public static String getTextByXPath(String xpath) {
		return findElementByXpath(xpath).getText();
	}
	
	public static String getTextById(WebElement webElement, String id) {
		return findElementById(webElement, id).getText();
	}
	
	public static String getTextByName(WebElement webElement, String name) {
		return findElementByName(webElement, name).getText();
	}
	
	public static String getTextByXPath(WebElement webElement, String xpath) {
		return findElementByXpath(webElement, xpath).getText();
	}

	public static void invokeMenu(Menu menu) {
		WebExchange webExchange = ContextLoader.getWebExchange();

		if (menu.getMenuLevel1() != null) {
			OpenMenuLevel1Action menuLevel1 = new OpenMenuLevel1Action(null, menu.getMenuLevel1());
			((MenuAwareness) menuLevel1).setMenu(menu);
			menuLevel1.submit(webExchange);

			if (menu.getMenuLevel2() != null) {
				OpenMenuLevel2Action menuLevel2 = new OpenMenuLevel2Action(menuLevel1, menu.getMenuLevel2());
				menuLevel2.submit(webExchange);

				if (menu.getMenuLevel3() != null) {
					OpenMenuLevel3Action menuLevel3 = new OpenMenuLevel3Action(menuLevel2, menu.getMenuLevel3());
					menuLevel3.submit(webExchange);				
				}
			}
		}
	}
	

	public static void takeElementsAsCheckPoint(WebElement wl, WebExchange we) {
		cp.takeElements(wl, we);
	}
		
	public static void takeElementsAsCheckPoint(WebExchange we) {
		cp.takeElements(wd, we);
	}

	public static void captureFullWindow() {
		delayInput();
		try {
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public static void captureWindow() {
		delayInput();
		try {
			ws.capture(WindowScreen.CAPTURE_CURRENT_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public static void captureFullModal(String elementId) {
		delayInput();
		try {
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW, elementId);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public static void captureFailedFullWindow() {
		delayInput();
		try {
			ws.setRemark("failed");
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public static void captureFailedWindow() {
		delayInput();
		try {
			ws.setRemark("failed");
			ws.capture(WindowScreen.CAPTURE_CURRENT_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public static void captureFailedFullModal(String elementId) {
		delayInput();
		try {
			ws.setRemark("failed");
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW, elementId);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	protected static void delayInput() {
		Sleep.wait(Float.valueOf((Float.valueOf(Constants.WEB_ELEMENT_DELAY) * 1000)).intValue());
	}
}
