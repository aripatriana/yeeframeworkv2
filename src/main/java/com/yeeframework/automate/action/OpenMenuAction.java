package com.yeeframework.automate.action;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebElementWrapper;
import com.yeeframework.automate.web.WebExchange;

/**
 * The action for open the menu trees
 * 
 * @author ari.patriana
 *
 */
@SuppressWarnings("deprecation")
public class OpenMenuAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(OpenMenuAction.class);
	OpenMenuAction prevMenu;
	String menuName;
	int timeout = 10;
	
	public OpenMenuAction(OpenMenuAction prevMenu, String menuName) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		if (menuName == null) return;
		Sleep.wait(500);
		log.info("Open Menu " + menuName);
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
