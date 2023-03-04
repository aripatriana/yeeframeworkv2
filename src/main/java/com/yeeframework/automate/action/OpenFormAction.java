package com.yeeframework.automate.action;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebElementWrapper;
import com.yeeframework.automate.web.WebExchange;

/**
 * The action for open the form page
 * 
 * @author ari.patriana
 *
 */
@SuppressWarnings("deprecation")
public class OpenFormAction extends WebElementWrapper implements Actionable, MenuAwareness {

	Logger log = LoggerFactory.getLogger(OpenFormAction.class);
	private Actionable prevMenu;
	private String menuId;
	private String form;
	private Menu menu;
	int timeout = 1;
	
	public OpenFormAction(Actionable prevMenu, String menuId, String form) {
		this.prevMenu = prevMenu;
		this.menuId = menuId;
		this.form = form;
	}
	
	public String getMenuId() {
		return menuId;
	}
	
	public String getForm() {
		return form;
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	@Override
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		log.info("Open Form " + form);
		Sleep.wait(500);
		try {
			WebDriverWait wait = new WebDriverWait(WebCommon.getDriver(),timeout);
			WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id='" + getMenuId() + "']//li//a[./span[text()='" + getForm() + "']]")));
			webElement.click();
			
			Sleep.wait(3000);
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				if (prevMenu instanceof OpenMenuAction 
						|| prevMenu instanceof OpenSubMenuAction) {
					try {
						prevMenu.submit(webExchange);
					} catch (FailedTransactionException | ModalFailedException e1) {
						// do nothing
					}
				}
				this.submit(webExchange);
			} else {
				WebCommon.getDriver().navigate().refresh();
				submit(webExchange);				
			}
		}			
	}

}
