package com.yeeframework.automate.web;

import org.openqa.selenium.WebElement;

import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;

/**
 * The implementation of callback that support browser manager
 * 
 * @author ari.patriana
 *
 */
public class WebCallback extends AbstractCallback {

	
	public WebCallback() {
	}
	
	public WebCallback(String successId, String[] failedId) {
		super(successId, failedId);
	}
	
	@Override
	public void ok(WebElement webElement, WebExchange webExchange) throws FailedTransactionException {
		// not yet implemented
	}
	
	public void notOk(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		try {
			if (WebCommon.findElementById(webElement, "failedOk",1) != null) {
				WebCommon.captureWindow();
				WebCommon.clickButton(webElement, "failedOk");
			}
		} catch (Exception e) {
			// do nothing
		}
		
		try {
			try {
				WebUI.getModalConfirmationId(1);
				WebUI.captureWindow();
			} catch (Exception e) {
				WebUI.captureFullModal(WebUI.getModalId(1));
			}
		} catch (Exception e1) {
			WebUI.captureFullWindow();
		}
		
		throw new ModalFailedException("Modal failed");
	}
}
