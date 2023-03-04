package com.yeeframework.automate;

import com.yeeframework.automate.web.WebElementWrapper;

/**
 * Abstract class for default driver used for browser
 * 
 * @author ari.patriana
 *
 */
@Deprecated
public abstract class DefaultBaseDriver extends WebElementWrapper {

	public DefaultBaseDriver() {
		super(DriverManager.getDefaultDriver());
	}
}
