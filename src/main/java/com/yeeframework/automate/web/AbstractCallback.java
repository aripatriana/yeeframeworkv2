package com.yeeframework.automate.web;

import org.openqa.selenium.WebElement;

import com.yeeframework.automate.Callback;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;

/**
 * The abstract implementation of Callback
 * 
 * @author ari.patriana
 *
 */
public abstract class AbstractCallback implements Callback {

	protected String successId;
	
	protected String[] failedId;
	
	public AbstractCallback() {
	}
	
	public AbstractCallback(String successId, String[] failedId) {
		this.successId = successId;
		this.failedId = failedId;
	}
	
	public void setSuccessId(String successId) {
		this.successId = successId;
	}
	
	public String getSuccessId() {
		return successId;
	}
	
	public void setFailedId(String[] failedId) {
		this.failedId = failedId;
	}
	
	public String[] getFailedId() {
		return failedId;
	}
	
	@Override
	public void callback(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		if (webElement.getAttribute("id") != null && webElement.getAttribute("id").equals(successId)) {
			webExchange.put("@response_modal", "success");
			ok(webElement, webExchange);
		} else {
			webExchange.put("@response_modal", "failed");
			notOk(webElement, webExchange);
		}
	}
	
	public abstract void ok(WebElement webElement, WebExchange webExchange) throws FailedTransactionException;
	
	public abstract void notOk(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException;
}
