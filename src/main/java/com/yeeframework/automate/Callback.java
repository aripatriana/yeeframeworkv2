package com.yeeframework.automate;

import org.openqa.selenium.WebElement;

import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.web.WebExchange;

/**
 * Callback interface
 * 
 * @author ari.patriana
 *
 */
public interface Callback  {

	public void callback(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException;

}
