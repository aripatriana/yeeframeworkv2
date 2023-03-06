package com.yeeframework.automate.action;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebExchange;

/**
 * The action for open menu level 1 page
 * 
 * @author ari.patriana
 *
 */
public class OpenMenuLevel1Action implements Actionable, MenuAwareness  {

	Logger log = LoggerFactory.getLogger(OpenMenuLevel1Action.class);
	OpenMenuLevel1Action prevMenu;
	String menuName;
	Menu menu;
	int timeout = 10;
	
	public OpenMenuLevel1Action(OpenMenuLevel1Action prevMenu, String menuName) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	@Override
	public Menu getMenu() {
		return menu;
	}
	
	@Override
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public OpenMenuLevel1Action getPrevMenu() {
		return prevMenu;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		if (menuName == null) return;
		Sleep.wait(500);
		log.info("Open Menu Level 1 {}", menuName);
		try {
			WebCommon.findElementByXpath("//ul//li//a//span[text()='" + getMenuName() + "']", timeout).click();
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				prevMenu.setTimeout(1);
				prevMenu.submit(webExchange);
				this.submit(webExchange);
			} else {
				WebCommon.findElementByXpath("//a[@title='Collapse Menu']").click();
				try {
					WebCommon.findElementByXpath("//ul//li//a//span[text()='" + getMenuName() + "']", 1);
				} catch (TimeoutException e1) {
					WebCommon.getDriver().navigate().refresh();
					submit(webExchange);	
				}			
			}
		}
		
	}

}
