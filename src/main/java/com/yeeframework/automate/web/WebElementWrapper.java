package com.yeeframework.automate.web;

import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Function;
import com.yeeframework.automate.Checkpoint;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.screen.WindowScreen;
import com.yeeframework.automate.util.Sleep;

/**
 * Base class for any object that need the browser manager capabilities
 * deprecated since change to yeeframework
 * @author ari.patriana
 *
 */
@Deprecated
public abstract class WebElementWrapper {

	Logger log = LoggerFactory.getLogger(WebElementWrapper.class);
	
	private final int TIMEOUT_IN_SECOND = 15;
	
	private static int INPUT_TIMEOUT = 3;
	
	public static int SEARCH_TIMEOUT_IN_MILIS = 3000; 
	
	public static final String DEFAULT_MAIN = "main";
	
	public static final String DEFAULT_MODAL = "//div[@class='modal fade modal-wide in']";
	
	public static final String DEFAULT_MODAL_CONFIRMATION = "//div[contains(@class, 'modal') and @role='dialog' and contains(@style,'block') and ./div[contains(@class, 'modal-sm')]]";
	
	public static final String DEFAULT_TOOLTIP = "//div/div[contains(@id,'tooltip') and contains(@class,'tooltip')]";


	protected WebDriver wd;
	
	@Autowired
	protected WindowScreen ws;
	
	@Autowired
	protected Checkpoint cp;
	
	@Value("delay")
	private static String delay;
	
	public WebElementWrapper() {
		this(DriverManager.getDefaultDriver());
	}
	
	public WebElementWrapper(WebDriver wd) {
		this.wd = wd;
		this.ws = new WindowScreen(wd);
	}
	
	public void takeElementsAsCheckPoint(WebElement wl, WebExchange we) {
		cp.takeElements(wl, we);
	}
		
	public void takeElementsAsCheckPoint(WebExchange we) {
		cp.takeElements(wd, we);
	}

	public void captureFullWindow() {
		delayInput();
		try {
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public void captureWindow() {
		delayInput();
		try {
			ws.capture(WindowScreen.CAPTURE_CURRENT_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public void captureFullModal(String elementId) {
		delayInput();
		try {
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW, elementId);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public void captureFailedFullWindow() {
		delayInput();
		try {
			ws.setRemark("failed");
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public void captureFailedWindow() {
		delayInput();
		try {
			ws.setRemark("failed");
			ws.capture(WindowScreen.CAPTURE_CURRENT_WINDOW);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public void captureFailedFullModal(String elementId) {
		delayInput();
		try {
			ws.setRemark("failed");
			ws.capture(WindowScreen.CAPTURE_FULL_WINDOW, elementId);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	
	public WebDriver getDriver() {
		return wd;
	}
		
	protected WebElement findElementById(String id) {
		return findElementById(id, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementById(String id, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
	}
	
	protected WebElement findElementByXpath(String xpath) {
		return findElementByXpath(xpath, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByXpath(String xpath, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
	}
	
	protected WebElement findElementByName(String name) {
		return findElementByName(name, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByName(String name, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name)));
	}
	
	protected WebElement findElementByClassName(String className) {
		return findElementByClassName(className, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByClassName(String className, int timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(),timeout);
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
	}
	
	
	protected WebElement findElementById(WebElement webElement, final String id) {
		return findElementById(webElement, id, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementById(WebElement webElement, final String id, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
	    return wait.until(new Function<WebElement, WebElement>() {
               public WebElement apply(WebElement d) {
                   return d.findElement(By.id(id));
               }
        });
	}
	
	protected WebElement findElementByXpath(WebElement webElement, final String xpath) {
		return findElementByXpath(webElement, xpath, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByXpath(WebElement webElement, final String xpath, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.xpath(xpath));
            }
		});
	}
	
	protected WebElement findElementByName(WebElement webElement, final String name) {
		return findElementByName(webElement, name, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByName(WebElement webElement, final String name, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.name(name));
            }
		});
	}
	
	protected WebElement findElementByClassName(WebElement webElement, final String className) {
		return findElementByClassName(webElement, className, TIMEOUT_IN_SECOND);
	}
	
	protected WebElement findElementByClassName(WebElement webElement, final String className, int timeout) {
		WebElementWait wait = new WebElementWait(webElement, timeout);
		return wait.until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement d) {
                return d.findElement(By.className(className));
            }
		});
	}


	protected void delayInput() {
		if (delay != null)
			Sleep.wait(Float.valueOf((Float.valueOf(delay) * 1000)).intValue());
	}
	
	protected void setFocusOn(String id) {
		try {
			WebElement element = findElementById(id);
			if (element.isEnabled() && element.isDisplayed()) {
				delayInput();
				if("input".equals(element.getTagName())) {
					element.sendKeys("");
				} else{
					JavascriptExecutor jse = (JavascriptExecutor) getDriver();
					jse.executeScript("document.getElementById('"+id+"').focus();");
				}
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void setInputFieldLike(String id, String value) {
		try {
			WebElement we = findElementByXpath("//input[contains(@id,'" + id + "')]",INPUT_TIMEOUT); 
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.clear();
				we.sendKeys(value);
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void setInputField(String id, String value) {
		try {
			WebElement we = findElementById(id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				we.clear();
				we.sendKeys(value);
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void setDatepickerField(String id, String value) {
		try {
			WebElement we = findElementById(id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				we.clear();
				we.sendKeys(value);
				
				try {
					WebElement we1 = findElementByXpath("//td[contains(@class,'ui-datepicker-current-day')]", INPUT_TIMEOUT);
					if (we1.isDisplayed() && we1.isEnabled()) {
						we1.click();
					} else {
						log.info("Element " + id + " is not enabled/not displayed for value " + value);
					}
				} catch (Exception e) {
					log.info("Element " + id + " is not found for value " + value);
				}
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}		
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void selectDropdown(String id, String textValue) {
		try {
			WebElement we = findElementByXpath("//span[@aria-labelledby='select2-" + id + "-container']",INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				
				we.click();
				//findElementByXpath("//div[contains(@id,'" + id + "')]//div//span").click();
				Sleep.wait(100);
				
				try {
					WebElement we1 = findElementByXpath("//ul[contains(@id,'" + id + "')]//li[text()='" + textValue + "']", INPUT_TIMEOUT);
					if (we1.isEnabled() && we1.isDisplayed()) {
						we1.click();
					} else {
						log.info("Element " + id + " is not enabled/not displayed for value " + textValue);
					}
				} catch (TimeoutException e) {
					log.info("Element " + id + " is not found for value " + textValue);
				}
				Sleep.wait(200);
			} else {
				log.info("Element " + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element " + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void clickButtonLookup(String id) {
		try {
			WebElement we = findElementById("buttonTo_" + id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.click();
				Sleep.wait(1000);
			} else {
				log.info("Element buttonTo_" + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element buttonTo_" + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
	}
	
	protected void clickButtonLike(WebElement webElement, String id) {
		try {
			WebElement we = findElementByXpath(webElement, "//button[contains(@id,'" + id + "')]",INPUT_TIMEOUT);
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
	
	protected void clickButton(WebElement webElement, String id) {
		try {
			WebElement we = findElementById(webElement, id,INPUT_TIMEOUT);
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

	protected void clickButtonLike(String id) {
		try {
			WebElement we = findElementByXpath("//button[contains(@id,'" + id + "')]",INPUT_TIMEOUT);
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
	
	protected void clickButton(String id) {
		try {
			WebElement we = findElementById(id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				findElementById(id).click();
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
	
	/**
	 * Input text pada field lookup tanpa membuka dialog pencariannya
	 * @param id
	 * @param value
	 */
	protected void selectSimpleLookupSearch(String id, String value) {
		setInputField("textInput_" + id, value);
	}
	
	/**
	 * Input text pada field lookup dengan membuka dialog pencariannya
	 * @param id
	 * @param value
	 */
	protected void selectLookupSearch(String id, String value) {
		try {
			WebElement we = findElementById("buttonTo_" + id,INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.click();
				Sleep.wait(2000);
				
				findElementByXpath("//div[@id='myModal_" + id + "']//div//div//div[@class='modal-body']//div//div//div[contains(@class,'search')]//input").sendKeys(value);
				Sleep.wait(1000);
				
				int index = 0;
				
				try {
					WebElement webElement = findElementByXpath("//table[contains(@id, '" + id + "')]//tbody//tr[./td[2]/text()='" + value + "']");
					index = Integer.valueOf(webElement.getAttribute("data-index"));
					
					findElementByXpath("//input[contains(@name, '" + id + "radio') and @type='radio' and @data-index='" + index + "']").click();	
				} catch (StaleElementReferenceException e) {
					Sleep.wait(3000);	
					findElementByXpath("//input[contains(@name, '" + id + "radio') and @type='radio' and @data-index='" + index + "']").click();	
				}
				
				findElementById("buttonSave_" + id).click();
				Sleep.wait(200);
			} else {
				log.info("Element buttonTo_" + id + " is not enabled/not displayed");
			}
		} catch (TimeoutException e) {
			log.info("Element buttonTo_" + id + " is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element " + id + " is not found");
		}
		
	}
	
	/**
	 * Digunakan untuk mencari data pada tabel pencarian yang terdapat pada modal terpisah
	 * @param modalId
	 * @param tableId
	 * @param value
	 */
	protected void clickCustomTableSearch(String modalId, String tableId, String value) {
		delayInput();
		
		findElementByXpath("//div[@id='" + modalId + "']//div//div//div//div//div[contains(@class,'search')]//input").sendKeys(value);
		waitTableRender("tbl");
		
		int index = 0;
		try {
			waitTableRender(tableId);
			WebElement webElemenet = findElementByXpath("//table[contains(@id,'" + tableId + "')]//tbody//tr[./td/text()='" + value +"']");
			index = Integer.valueOf(webElemenet.getAttribute("data-index"));
			findElementByXpath("//table[contains(@id,'" + tableId + "')]//tbody//tr[@data-index='" + index + "']//td//input[@type='checkbox' and @data-index='" + index + "']").click();
		} catch (StaleElementReferenceException e) {
			waitTableRender("tbl");	
			findElementByXpath("//table[contains(@id,'" + tableId + "')]//tbody//tr[@data-index='" + index + "']//td//input[@type='checkbox' and @data-index='" + index + "']").click();
		}
	}
	
	/**
	 * Klik checkbox row pertama pada tabel pencarian
	 * @param id
	 */
	protected void clickRadioTableSearch(String id) {
		delayInput();
		waitTableRender(id);
		clickRadioTableSearch(id, 0);
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	protected void clickRadioTableSearch(String id, int index) {
		delayInput();
		waitTableRender(id);
		findElementByXpath("//table[@id='" + id + "']/tbody/tr[@data-index='" + index +"']/td/input[@type='radio']").click();
		waitTableRender("tbl");
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuain dengan query yang match
	 * @param id
	 * @param query
	 */
	protected void clickRadioTableSearch(String id, String query) {
		delayInput();
		waitTableRender(id);
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickRadioTableSearch(id, Integer.valueOf(index));
	}
	
	
	/**
	 * Klik checkbox row pertama pada tabel pencarian
	 * @param id
	 */
	protected void clickCheckBoxTableSearch(String id) {
		delayInput();
		waitTableRender(id);
		clickCheckBoxTableSearch(id, 0);
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	protected void clickCheckBoxTableSearch(String id, int index) {
		delayInput();
		waitTableRender(id);
		findElementByXpath("//table[@id='" + id + "']/tbody/tr[@data-index='" + index +"']/td/input[@type='checkbox']").click();
		waitTableRender("tbl");
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuain dengan query yang match
	 * @param id
	 * @param query
	 */
	protected void clickCheckBoxTableSearch(String id, String query) {
		delayInput();
		waitTableRender(id);
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickCheckBoxTableSearch(id, Integer.valueOf(index));
	}
	
	/**
	 * Digunakan untuk klik data row pertama pada tabel pencarian 
	 * @param id
	 */
	protected void clickTableSearch(String id) {
		delayInput();
		waitTableRender(id);
		clickTableSearch(id, 0);
	}
	
	/**
	 * Digunakan untuk klik data row pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	protected void clickTableSearch(String id, int index) {
		delayInput();
		waitTableRender(id);
		findElementByXpath("//table[contains(@id,'" + id + "')]//tbody//tr[@data-index='" + index + "']//td//a").click();
		waitTableRender("tbl");
	}
	
	
	/**
	 * Digunakan untuk klik data row pada tabel pencarian sesuai dengan query yang match
	 * @param id
	 * @param index
	 */
	protected void clickTableSearch(String id, String query) {
		delayInput();
		waitTableRender(id);
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickTableSearch(id, Integer.valueOf(index));
	}
	
	
	/**
	 * Digunakan untuk klik[action type] data row pertama pada tabel pencarian 
	 * @param id
	 */
	protected void clickTableSearches(String id, int indexActionType) {
		delayInput();
		waitTableRender(id);
		clickTableSearches(id, 0, indexActionType);
	}
	
	/**
	 * Digunakan untuk klik[action type] data row pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	protected void clickTableSearches(String id, int index, int indexActionType) {
		delayInput();
		waitTableRender(id);
		findElementByXpath("//table[contains(@id,'" + id + "')]//tbody//tr[@data-index='" + index + "']//td//a["+indexActionType+"]").click();
		waitTableRender("tbl");
	}
	
	
	/**
	 * Digunakan untuk klik[action type] data row pada tabel pencarian sesuai dengan query yang match
	 * @param id
	 * @param index
	 */
	protected void clickTableSearches(String id, String query, int indexActionType) {
		delayInput();
		waitTableRender(id);
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickTableSearches(id, Integer.valueOf(index), indexActionType);
	}
	
	protected boolean isElementExist(String id) {
		try {
			findElementById(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	protected boolean searchOnTable(String id, String query) {
		
		try {
			waitTableRender(id);
			findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']", 1);
			return true;
		} catch (Exception e) {
			// do nothing
		}
		return false;
	}
	
	protected void clickPageNo(String formId, String value) {
		delayInput();
		findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//span[@class='page-list']//span//button").click();
		findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//span[@class='page-list']//span//ul//li//a[text()='" + value + "']").click();
		waitTableRender("tbl");
	}
	
	protected void clickPageFirst(String formId) {
		try {
			WebElement webElement = findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-first')]//a");
			if (webElement.isEnabled()) {
				delayInput();
				webElement.click();
				waitTableRender("tbl");
			}
		} catch (TimeoutException e) {
			log.info("Element page-first is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element page-first is not found");
		}
	}
	
	protected void clickPageLast() {
		try {
			WebElement liElement = findElementByXpath("//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-last')]");
			if (!liElement.getAttribute("class").contains("disabled")) {
				delayInput();
				WebElement aElement = findElementByXpath("//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-last')]//a");
				aElement.click();
				waitTableRender("tbl");
			}
		} catch (TimeoutException e) {
			log.info("Element page-last is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element page-last is not found");
		}
	}
	
	protected void clickPageLast(String formId) {
		try {
			WebElement liElement = findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-last')]");
			if (!liElement.getAttribute("class").contains("disabled")) {
				delayInput();
				WebElement aElement = findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//ul[@class='pagination']//li[contains(@class,'page-last')]//a");
				aElement.click();		
				waitTableRender("tbl");
			}
		} catch (TimeoutException e) {
			log.info("Element page-last is not found");
		} catch (InvalidElementStateException e) {
			log.info("Element page-last is not found");
		}
	}
	
	protected void waitTableRender(String id) {
		try {
			WebElement element = findElementByXpath("//div[@class='fixed-table-body' and ./table[@id='" + id + "']]/div",1);
			if (element != null) {
				String display = element.getCssValue("display");
				Sleep.setTimeout(5 * 6000);
				while (display != null & display.equalsIgnoreCase("block")) {
					element = findElementByXpath("//div[@class='fixed-table-body' and ./table[@id='" + id + "']]/div",1);
					display = element.getCssValue("display");
					log.info("Wait until result list rendered");
					Sleep.wait(1000);
					Sleep.throwIfTimeout();
				}
				Sleep.setTimeout(0);
			}
		} catch (TimeoutException e) {
			// do nothing
		} catch (java.util.concurrent.TimeoutException e) {
			log.info("Element " + id + " is not found, e " + e.getMessage());
		}
	}

	public String getTextByIdLike(String id) {
		return findElementByXpath("//p[contains(@id,'" + id + "')]").getText();
	}

	public String getTextDropdownLike(String id) {
		return findElementByXpath("//div[contains(@id,'" + id + "') and not(contains(@style,'none'))]//span[contains(@id,'" + id + "')]").getText();		
	}
	
	public String getTextDropdown(String id) {
		return findElementByXpath("//div[@id='detailInstructionTypeFull' and not(contains(@style,'none'))]//span[@id='detailInstructionTypeFull']").getText();		
	}
	
	public String getTextById(String id) {
		return findElementById(id).getText();
	}
	
	public String getTextByName(String name) {
		return findElementByName(name).getText();
	}
	
	public String getTextByXPath(String xpath) {
		return findElementByXpath(xpath).getText();
	}
	
	public String getTextById(WebElement webElement, String id) {
		return findElementById(webElement, id).getText();
	}
	
	public String getTextByName(WebElement webElement, String name) {
		return findElementByName(webElement, name).getText();
	}
	
	public String getTextByXPath(WebElement webElement, String xpath) {
		return findElementByXpath(webElement, xpath).getText();
	}

	public WebElement getModalConfirmationElement(int timeout) {
		return findElementByXpath(DEFAULT_MODAL_CONFIRMATION, timeout);
	}
	
	public WebElement getModalConfirmationElement() {
		return findElementByXpath(DEFAULT_MODAL_CONFIRMATION);
	}

	public WebElement getModalElement() {
		return findElementByXpath(DEFAULT_MODAL);
	}
	
	public WebElement getModalElement(int timeout) {
		return findElementByXpath(DEFAULT_MODAL, timeout);
	}
	
	public WebElement getTooltipElement() {
		return findElementByXpath(DEFAULT_TOOLTIP);
	}

	public String getModalConfirmationId() {
		return getModalConfirmationElement().getAttribute("id");
	}
	
	public String getModalConfirmationId(int timeout) {
		return getModalConfirmationElement(timeout).getAttribute("id");
	}
	
	public String getModalId(int timeout) {
		return getModalElement(timeout).getAttribute("id");
	}
	
	public String getModalId() {
		return getModalElement().getAttribute("id");
	}
	
}
