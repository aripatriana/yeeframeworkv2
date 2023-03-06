package com.yeeframework.automate.action;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebExchange;

/**
 * The action for open menu level 3 page
 * 
 * @author ari.patriana
 *
 */
public class OpenMenuLevel3Action implements Actionable {

	Logger log = LoggerFactory.getLogger(OpenMenuLevel3Action.class);
	private OpenMenuLevel2Action prevMenu;
	private String menuName;
	int timeout = 1;
	
	public OpenMenuLevel3Action(OpenMenuLevel2Action prevMenu, String menuName) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		log.info("Open Menu Level 3 {} ", menuName);
		Sleep.wait(500);
		try {
			WebDriverWait wait = new WebDriverWait(WebCommon.getDriver(),timeout);
			WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul//li[./a//span[text()='" + prevMenu.getPrevMenu().getMenuName() + "']]//li[./a//span[text()='" + prevMenu.getMenuName() + "']]//li/a[./span[text()='" + getMenuName() + "']]")));
			webElement.click();
			
			Sleep.wait(3000);
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				prevMenu.submit(webExchange);
				this.submit(webExchange);
			} else {
				WebCommon.getDriver().navigate().refresh();
				submit(webExchange);				
			}
		}			
	}

}
