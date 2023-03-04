package com.yeeframework.automate.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebElementWrapper;
import com.yeeframework.automate.web.WebExchange;

/**
 * The action for select the product choosen
 *  
 * @author ari.patriana
 *
 */
@SuppressWarnings("deprecation")
public class ProductSelectorAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(ProductSelectorAction.class);
	
	private String productType;
	
	public ProductSelectorAction(String productType) {
		this.productType = productType;
	}
	
	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getProductType() {
		return productType;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {

		 if (webExchange.get("token") == null)
			 throw new FailedTransactionException("Workflow halted caused by login failed");
		 
		log.info("Open Product " + getProductType());
		
		Sleep.wait(1000);
		WebCommon.findElementByXpath("//div[@class='divProductTypeSelector']/span[@id='project-selector']").click();
		WebCommon.findElementByXpath("//*[contains(@href,'" + getProductType() + "')]").click();
	}

}
