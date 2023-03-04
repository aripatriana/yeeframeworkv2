package com.yeeframework.automate.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindElement {
	WebDriver wd;
	WebElement we;
	
	public FindElement() {
	}
	
	public FindElement(WebDriver wd) {
		this.wd = wd;
	}
	
	public FindElement(WebElement we) {
		this.we = we;
	}
	
	public WebElement findElement(By by) {
		try {
			if (wd != null) {
				return wd.findElement(by);
			}
			
			if (we != null) {
				return we.findElement(by);
			}
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}
	
	public List<WebElement> findElements(By by) {
		try {
			if (wd != null) {
				return wd.findElements(by);
			}
			
			if (we != null) {
				return we.findElements(by);
			}
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}
	
	public List<WebElement> findVisibleElements(String[] xpaths) {
		for (String xpath : xpaths) {
			List<WebElement> elements = findElements(By.xpath(xpath));
			if (elements != null)
				return elements;
			
		}
		return null;
	}
	
	public WebElement findVisibleElement(String[] xpaths) {
		if (xpaths != null) {
			for (String xpath : xpaths) {
				WebElement element = findElement(By.xpath(xpath));
				if (element != null)
					return element;
				
			}
		}
		return null;
	}
}