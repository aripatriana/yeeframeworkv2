package com.yeeframework.automate.driver;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverClosable extends ChromeDriver implements WebDriverClosable {

	private boolean close = false;

	public ChromeDriverClosable(ChromeOptions options) {
		super(ChromeDriverService.createDefaultService(), options);
	}

	public ChromeDriverClosable(ChromeDriverService service) {
		super(service, new ChromeOptions());
	}

	@Deprecated
	public ChromeDriverClosable(Capabilities capabilities) {
		super(ChromeDriverService.createDefaultService(), capabilities);
	}

	@SuppressWarnings("deprecation")
	public ChromeDriverClosable(ChromeDriverService service, ChromeOptions options) {
		super(service, (Capabilities) options);
	}

	@Override
	public void close() {
		super.close();
		close = true;
	}

	@Override
	public boolean isClosed() {
		return close;
	}

	@Override
	public void quit() {
		super.quit();
		close();
	}

}
