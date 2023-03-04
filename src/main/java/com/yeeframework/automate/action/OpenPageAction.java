package com.yeeframework.automate.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.web.WebExchange;

public class OpenPageAction implements Actionable {
	
	Logger log = LoggerFactory.getLogger(OpenPageAction.class);
	private String url;
	
	public OpenPageAction(String url) {
		Assert.notNull(url, "url is must not be null");
		
		this.url = url;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		log.info("Open Page " + url);
		
		DriverManager.getDefaultDriver().get(url);
	}

}
