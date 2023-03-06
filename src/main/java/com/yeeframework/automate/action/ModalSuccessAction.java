package com.yeeframework.automate.action;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.Callback;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.web.AbstractCallback;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebExchange;


/**
 * The action for handling the modal page response 
 * 
 * @author ari.patriana
 *
 */
public class ModalSuccessAction implements Actionable {
	Logger log = LoggerFactory.getLogger(ModalSuccessAction.class);
	

	public static final String DEFUALT_SUCCESS_ID = "saveSuccess";
	
	public static final String DEFAULT_FAILED_ID = "txFailed";
	
	public static final String DEFAULT_MAIN = "main";
	
	public static final String DEFAULT_MODAL = "//div[@class='modal fade modal-wide in']";
	
	public static final String DEFAULT_TOOLTIP = "//div/div[contains(@id,'tooltip') and contains(@class,'tooltip')]";
	
	@PropertyValue(value = "timeout.modal.callback")
	private String timeoutModalCallback;
	
	@Autowired
	private Callback callback;
	private String successId;
	private String[] failedId;
	
	public ModalSuccessAction(String successId, String failedId, AbstractCallback callback) {
		callback.setSuccessId(successId);
		callback.setFailedId(new String[] {failedId});
		
		this.callback = callback;
		this.successId = successId;
		this.failedId = new String[] {failedId};
	}
	
	public ModalSuccessAction(String successId, String[] failedId, AbstractCallback callback) {
		callback.setSuccessId(successId);
		callback.setFailedId(failedId);
		
		this.callback = callback;
		this.successId = successId;
		this.failedId = failedId;
		
		InjectionUtils.setObject(callback);
	}
	
	public ModalSuccessAction(AbstractCallback callback) {
		this(DEFUALT_SUCCESS_ID, DEFAULT_FAILED_ID, callback);
	}
	
	public ModalSuccessAction(String successId, AbstractCallback callback) {
		this(successId, DEFAULT_FAILED_ID, callback);
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		log.info("Waiting modal success open");
		int totalThread = failedId.length+2; 
		try {
			ExecutorService executor = Executors.newFixedThreadPool(totalThread);
			CountDownLatch countDownOk = new CountDownLatch(totalThread);
			CountDownLatch countDownLatch = new CountDownLatch(totalThread);
			ConcurrentHashMap<Boolean, String> modalSuccess = new ConcurrentHashMap<Boolean, String>();
			
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						WebDriverWait wait = new WebDriverWait(DriverManager.getDefaultDriver(),Integer.valueOf(timeoutModalCallback));
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(successId)));
						modalSuccess.put(Boolean.TRUE, successId);
						countDownOk.countDown();	
						log.info("Modal success open - " + successId);
					} catch (TimeoutException | NoSuchSessionException e1) {
						// do nothing
					} finally {
						countDownLatch.countDown();			
					}
				}
			});
			
			for (String failedModalId : failedId) {
				executor.execute(new Runnable() {
					
					@Override
					public void run() {
						try {
							WebDriverWait wait = new WebDriverWait(DriverManager.getDefaultDriver(),Integer.valueOf(timeoutModalCallback));
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(failedModalId)));
							modalSuccess.put(Boolean.FALSE, failedModalId);
							countDownOk.countDown();	
							log.info("Modal failed open - " + failedModalId);
						} catch (TimeoutException | NoSuchSessionException e1) {
							// do nothing
						} finally {
							countDownLatch.countDown();					
						}
					}
				});
			}
			
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						WebDriverWait wait = new WebDriverWait(DriverManager.getDefaultDriver(),Integer.valueOf(timeoutModalCallback));
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DEFAULT_TOOLTIP)));
						
						// sleep 3 seconds, to make sure for modal failed to open
						Sleep.wait(3000);
						if (modalSuccess.size() ==0 ) {
							try {
								modalSuccess.put(Boolean.FALSE, WebCommon.findElementByXpath(DEFAULT_MODAL, 1).getAttribute("id"));
							} catch (Exception e) {
								modalSuccess.put(Boolean.FALSE, DEFAULT_MAIN);
							}

							countDownOk.countDown();	
							log.info("Tooltip error open");
						}
						
						
					} catch (TimeoutException | NoSuchSessionException e1) {
						// do nothing
					} finally {
						countDownLatch.countDown();			
					}
					
				}
			});
			
			for (;;) {
				if (countDownOk.getCount() == (totalThread-1) || countDownLatch.getCount() == 0) {
					// shutdown thread
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							executor.shutdown();
							// Wait until all threads are finish
							// safe mode 
							try {
								executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e1) {
								log.error("Failed to wait termination while all thread not yet finished");
							}
							
						}
					}).start();
					
					if (countDownLatch.getCount() == 0)
						throw new FailedTransactionException("All window modal not open");
					break;
				}

				Sleep.wait(100);
			}
			
			// wait until modal fully open
			Sleep.wait(1000);
			
			if (modalSuccess.containsKey(Boolean.TRUE)) {
				callback.callback(WebCommon.findElementById(modalSuccess.get(Boolean.TRUE)), webExchange);
			} else {
				callback.callback(WebCommon.findElementById(modalSuccess.get(Boolean.FALSE)), webExchange);				
			}
	
		} catch (FailedTransactionException e) {
			throw e;
		} catch (ModalFailedException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
		
	}

}
