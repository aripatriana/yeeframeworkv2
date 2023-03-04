package com.yeeframework.automate.web;

import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Constants;
import com.yeeframework.automate.util.Sleep;

public class WebUI extends WebCommon {
	
	private static Logger log = LoggerFactory.getLogger(WebUI.class);
	
	public static String DEFAULT_MAIN = "main";
	
	public static String DEFAULT_MODAL = "//div[@class='modal fade modal-wide in']";
	
	public static String DEFAULT_MODAL_CONFIRMATION = "//div[contains(@class, 'modal') and @role='dialog' and contains(@style,'block') and ./div[contains(@class, 'modal-sm')]]";
	
	public static String DEFAULT_TOOLTIP = "//div/div[contains(@id,'tooltip') and contains(@class,'tooltip')]";

	
	public static void clickButton(String id) {
		clickButton(findElementById(id,Constants.WEB_INPUT_TIMEOUT));
	}
	public static void setInput(String id, String value) {
		setInput(findElementById(id,Constants.WEB_INPUT_TIMEOUT), value);
	}
	
	public static void setInputLike(String id, String value) {
		setInput(findElementByXpath("//input[contains(@id,'" + id + "')]",Constants.WEB_INPUT_TIMEOUT), value);
	}
	
	public static void setDatepicker(String id, String value) {
		try {
			WebElement we = findElementById(id,Constants.WEB_INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				we.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				we.clear();
				we.sendKeys(value);
				
				try {
					WebElement we1 = findElementByXpath("//td[contains(@class,'ui-datepicker-current-day')]", Constants.WEB_INPUT_TIMEOUT);
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
	
	public static void selectDropdown(String id, String textValue) {
		try {
			WebElement we = findElementByXpath("//span[@aria-labelledby='select2-" + id + "-container']",Constants.WEB_INPUT_TIMEOUT);
			if (we.isEnabled() && we.isDisplayed()) {
				delayInput();
				
				we.click();
				//findElementByXpath("//div[contains(@id,'" + id + "')]//div//span").click();
				Sleep.wait(100);
				
				try {
					WebElement we1 = findElementByXpath("//ul[contains(@id,'" + id + "')]//li[text()='" + textValue + "']", Constants.WEB_INPUT_TIMEOUT);
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
	
	public static void clickButtonLookup(String id) {
		clickButton(findElementById("buttonTo_" + id, Constants.WEB_INPUT_TIMEOUT));
	}
	
	public static void clickButtonLike(WebElement webElement, String id) {
		clickButton(findElementByXpath(webElement, "//button[contains(@id,'" + id + "')]", Constants.WEB_INPUT_TIMEOUT));
	}

	public static void clickButtonLike(String id) {
		clickButton(findElementByXpath("//button[contains(@id,'" + id + "')]", Constants.WEB_INPUT_TIMEOUT));
	}
	
	
	/**
	 * Input text pada field lookup tanpa membuka dialog pencariannya
	 * @param id
	 * @param value
	 */
	public static void selectSimpleLookupSearch(String id, String value) {
		setInput(findElementById("textInput_" + id), value);
	}
	
	/**
	 * Input text pada field lookup dengan membuka dialog pencariannya
	 * @param id
	 * @param value
	 */
	public static void selectLookupSearch(String id, String value) {
		try {
			WebElement we = findElementById("buttonTo_" + id, Constants.WEB_INPUT_TIMEOUT);
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
	public static void clickCustomTableSearch(String modalId, String tableId, String value) {
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
	public static void clickRadioTableSearch(String id) {
		delayInput();
		waitTableRender(id);
		clickRadioTableSearch(id, 0);
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	public static void clickRadioTableSearch(String id, int index) {
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
	public static void clickRadioTableSearch(String id, String query) {
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
	public static void clickCheckBoxTableSearch(String id) {
		delayInput();
		waitTableRender(id);
		clickCheckBoxTableSearch(id, 0);
	}
	
	/**
	 * Klik checkbox pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	public static void clickCheckBoxTableSearch(String id, int index) {
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
	public static void clickCheckBoxTableSearch(String id, String query) {
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
	public static void clickTableSearch(String id) {
		delayInput();
		waitTableRender(id);
		clickTableSearch(id, 0);
	}
	
	/**
	 * Digunakan untuk klik data row pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	public static void clickTableSearch(String id, int index) {
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
	public static void clickTableSearch(String id, String query) {
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
	protected static void clickTableSearches(String id, int indexActionType) {
		delayInput();
		waitTableRender(id);
		clickTableSearches(id, 0, indexActionType);
	}
	
	/**
	 * Digunakan untuk klik[action type] data row pada tabel pencarian sesuai dengan index yg dipassing
	 * @param id
	 * @param index
	 */
	public static void clickTableSearches(String id, int index, int indexActionType) {
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
	public static void clickTableSearches(String id, String query, int indexActionType) {
		delayInput();
		waitTableRender(id);
		WebElement webElement = findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']");
		String index = webElement.getAttribute("data-index");
		clickTableSearches(id, Integer.valueOf(index), indexActionType);
	}
	
	public static boolean existsOnTable(String id, String query) {
		
		try {
			waitTableRender(id);
			findElementByXpath("//table[@id='" + id + "']/tbody/tr[./td/text()='" + query+ "']", 1);
			return true;
		} catch (Exception e) {
			// do nothing
		}
		return false;
	}
	
	public static void clickPageNo(String formId, String value) {
		delayInput();
		findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//span[@class='page-list']//span//button").click();
		findElementByXpath("//form[@id='" + formId + "']//div[@class='row']//div[not(@id)]//div[1]//div[4]//span[@class='page-list']//span//ul//li//a[text()='" + value + "']").click();
		waitTableRender("tbl");
	}
	
	public static void clickPageFirst(String formId) {
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
	
	public static void clickPageLast() {
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
	
	public static void clickPageLast(String formId) {
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
	
	public static void waitTableRender(String id) {
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

	public static String getTextByIdLike(String id) {
		return findElementByXpath("//p[contains(@id,'" + id + "')]").getText();
	}

	public static String getTextDropdownLike(String id) {
		return findElementByXpath("//div[contains(@id,'" + id + "') and not(contains(@style,'none'))]//span[contains(@id,'" + id + "')]").getText();		
	}
	
	public static String getTextDropdown(String id) {
		return findElementByXpath("//div[@id='detailInstructionTypeFull' and not(contains(@style,'none'))]//span[@id='detailInstructionTypeFull']").getText();		
	}

	public static WebElement getModalConfirmationElement(int timeout) {
		return findElementByXpath(DEFAULT_MODAL_CONFIRMATION, timeout);
	}
	
	public static WebElement getModalConfirmationElement() {
		return findElementByXpath(DEFAULT_MODAL_CONFIRMATION);
	}

	public static WebElement getModalElement() {
		return findElementByXpath(DEFAULT_MODAL);
	}
	
	public static WebElement getModalElement(int timeout) {
		return findElementByXpath(DEFAULT_MODAL, timeout);
	}
	
	public static WebElement getTooltipElement() {
		return findElementByXpath(DEFAULT_TOOLTIP);
	}

	public static String getModalConfirmationId() {
		return getModalConfirmationElement().getAttribute("id");
	}
	
	public static String getModalConfirmationId(int timeout) {
		return getModalConfirmationElement(timeout).getAttribute("id");
	}
	
	public static String getModalId(int timeout) {
		return getModalElement(timeout).getAttribute("id");
	}
	
	public static String getModalId() {
		return getModalElement().getAttribute("id");
	}
}
