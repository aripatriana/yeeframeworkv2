package com.yeeframework.automate.execution;


import java.util.LinkedList;
import java.util.Map;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.Constants;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.FormActionable;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.MultipleFormActionable;
import com.yeeframework.automate.action.ModalSuccessAction;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.typeform.ManagedFormAction;
import com.yeeframework.automate.typeform.ManagedMultipleFormAction;
import com.yeeframework.automate.util.DBQuery;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.web.WebCommon;
import com.yeeframework.automate.web.WebExchange;
import com.yeeframework.automate.web.WebUI;


/**
 * Main workflow
 * 
 * @author ari.patriana
 *
 */
public class Workflow {

	Logger log = LoggerFactory.getLogger(Workflow.class);
	private WebExchange webExchange;
	private LinkedList<Actionable> actionables;
	private boolean scopedAction = false;
	private int scopedActionIndex = 0;
	
	public Workflow(WebExchange webExchange) {
		this.webExchange = webExchange;
		this.actionables = new LinkedList<Actionable>();
	}
	
	public WebExchange getWebExchange() {
		return webExchange;
	}
	
	public Menu getActiveMenu() {
		return (Menu) webExchange.get(Constants.CURRENT_MENU);
	}
	
	public void scopedAction() {
		scopedAction = true;
		scopedActionIndex = 0;
	}
	
	public void resetScopedAction() {
		scopedAction = false;
	}
	
	private void setActiveMenu(Menu activeMenu) {
		webExchange.put(Constants.CURRENT_MENU, activeMenu);
		webExchange.put(Constants.CURRENT_MODULE_ID, activeMenu.getModuleId());
		webExchange.put(Constants.CURRENT_MENU_ID, activeMenu.getId());
	}

	public static Workflow configure() {
		WebExchange webExchange = new WebExchange();
		webExchange.putAll(ConfigLoader.getConfigMap());
		webExchange.addElements(ConfigLoader.getElementMap());
		for (Map<String, Object> login : ConfigLoader.getLoginInfos()) {
			webExchange.putAll(login);			
		}
		
		ContextLoader.setWebExchange(webExchange);
		return new Workflow(webExchange);
	}
	
	public Workflow executeImmediate(Actionable actionable) {
		try {
			if (InjectionUtils.isWorkbookPersistentPresent(actionable)) {
				if (InjectionUtils.isWorkbookSessionPresent(actionable)) {
					InjectionUtils.setObjectLocal(actionable);
					executeSafeActionable(actionable);
				} else {
					InjectionUtils.setObject(actionable);
					executeSafeActionable(actionable);	
				}
			} else {
				InjectionUtils.setObject(actionable);
				executeSafeActionable(actionable);
			}
		} catch (FailedTransactionException e) {
			log.error("Failed for transaction ", e);
		} catch (ModalFailedException e) {
			log.error("Modal failed ", e);
		}
		return this;
	}
	
	public Workflow action(Actionable actionable) {
		if (scopedAction) {
			if (scopedActionIndex == 0) {
				ManagedFormAction scoped = null;
				if (actionable instanceof FormActionable) {
					scoped = new ManagedFormAction(actionable.getClass());
				} else if (actionable instanceof MultipleFormActionable) {
					scoped = new ManagedMultipleFormAction(actionable.getClass());
				}
				
				if (scoped != null) {
					scoped.addActionable(actionable);
					actionables.add(scoped);						
				} else {
					log.warn("Managed Action is missing for " + actionable);
				}
			} else {
				Actionable act = actionables.getLast();
				if (act != null)
					((ManagedFormAction) act).addActionable(actionable);
				else 
					log.warn("Managed Action is missing for " + actionable);
			}
			scopedActionIndex++;
		} else {
			actionables.add(actionable);	
		}
		
		return this;
	}
	
	public Workflow action(Actionable actionable, boolean executeImmediate) {
		if (executeImmediate) {
			executeImmediate(actionable);
		} else {
			if (scopedAction) {
				if (scopedActionIndex == 0) {
					ManagedFormAction scoped = null;
					if (actionable instanceof FormActionable) {
						scoped = new ManagedFormAction(actionable.getClass());
					} else if (actionable instanceof MultipleFormActionable) {
						scoped = new ManagedMultipleFormAction(actionable.getClass());
					}
					
					if (scoped != null) {
						scoped.addActionable(actionable);
						actionables.add(scoped);						
					} else {
						log.warn("Managed Action is missing for " + actionable);
					}
				} else {
					Actionable act = actionables.getLast();
					if (act != null)
						((ManagedFormAction) act).addActionable(actionable);
					else 
						log.warn("Managed Action is missing for " + actionable);
				}
				scopedActionIndex++;
			} else {
				actionables.add(actionable);	
			}
		}
		return this;
	}

	public Workflow executeWorkflow() throws Exception {
		if (actionables.size() == 0) {
			throw new RuntimeException("No process performed");	
		}
		
		if (webExchange.getTotalMetaData()==0 && webExchange.isRetention() 
				&& webExchange.getMandatoryModules().size() > 0) {
			throw new RuntimeException("No row data to be processed");
		}
		
		try {
			webExchange.initSession(webExchange.getMetaDataSize());

			ReportMonitor.getScenEntry(webExchange.get(Constants.CURRENT_TESTSCEN_ID).toString()).setNumOfData(webExchange.getMetaDataSize());
			
			log.info("Total data-row " + webExchange.getTotalMetaData());
			
		
			for (Actionable actionable : actionables) {
				if (actionable instanceof MenuAwareness) {
					setActiveMenu(((MenuAwareness) actionable).getMenu());
				}
				
				if (actionable instanceof FormActionable && webExchange.getMetaDataSize(getActiveMenu().getModuleId()) == 0) {
					// sebuah form action harus memiliki data untuk diproses
					// jika tidak ada data yg diproses akan diabaikan
					log.info("Skip to process action for " + getActiveMenu().getId() +" data size " + webExchange.getMetaDataSize(getActiveMenu().getModuleId()));
				} else {
					// execute actionable if any session active, if all session failed no further process performed
					// sesi merepresentasikan data, jika ada 3 data yang akan diproses maka akan ada 3 session
					// tetapi jika data tersebut gagal selama menjalankan skenario, sesi tersebut akan ditakeout
					// dan tidak akan dilakukan proses selanjutnya
					if (webExchange.getSessionList().size() > 0
							&& (webExchange.getSessionList().size() - webExchange.getFailedSessionList().size() > 0)) {
						executeActionableWithSession(actionable);						
					}							
				}
			}
		} catch (Exception e) { 
			log.info("Transaction interrupted ");
			log.error("ERROR ", e);
			throw e;
		} finally {
			webExchange.setRetention(Boolean.FALSE);
			webExchange.clearMetaData();
		}
		
		return this;
	}
	
	public void executeActionableNoSession(Actionable actionable, Map<String, Object> metadata) throws Exception {
		if (InjectionUtils.isWorkbookPersistentPresent(actionable)) {
			// execute map serializable
			if (InjectionUtils.isWorkbookSessionPresent(actionable)) {
				InjectionUtils.setObjectLocal(actionable);
				executeSafeActionable(actionable);
			} else {
				InjectionUtils.setObjectWithCustom(actionable, metadata);
				executeSafeActionable(actionable);
			}
		} else {
			// execute common action
			InjectionUtils.setObject(actionable);
			executeSafeActionable(actionable);
		}
	}
	
	public void executeActionableWithSession(Actionable actionable) throws Exception {
		// cek apakah class action mengimplementasikan @annotation Workbook
		if (InjectionUtils.isWorkbookPersistentPresent(actionable)) {
			
			// cek apakah map-type yang digunakan adalah WorkbookType.SESSION
			if (InjectionUtils.isWorkbookSessionPresent(actionable)) {
				
				// ManagedMultipleFormAction
				if (actionable instanceof ManagedMultipleFormAction) {
					try {
						InjectionUtils.setObjectLocal(actionable);
						
						// execute action
						executeSafeActionable(actionable);
						DriverManager.getDefaultDriver().navigate().refresh();
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
								getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), null, null);
					} catch (FailedTransactionException | IndexOutOfBoundsException e) {
						webExchange.addListFailedSession(webExchange.getSessionList());
						log.info("Transaction is not completed, skipped for further processes");
						log.error("Failed for transaction ", e);
						
						captureFailedWindow(actionable);
						DriverManager.getDefaultDriver().navigate().refresh();
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
								getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), null, null, e.getMessage(), ReportManager.FAILED);
					} catch (ModalFailedException e) {
						log.info("Modal failed, skipped for further processes");
//						webExchange.addListFailedSession(webExchange.getSessionList());
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
								getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), null, null, e.getMessage(), ReportManager.PASSED);
					}
				} else {
					int i = 0;
					while(true) {
						String sessionId = webExchange.createSession(i);
						if (!webExchange.isSessionFailed(sessionId)) {
							log.info("Execute data-row index " + i + " with session " + sessionId);
							webExchange.setCurrentSession(sessionId);
							
							Map<String, Object> metadata = null;
							try {	
								metadata = webExchange.getMetaData(getActiveMenu().getModuleId(), i);
								
								if (actionable instanceof ManagedFormAction) {
									InjectionUtils.setObjectLocal(actionable);
									((ManagedFormAction) actionable).setMetadata(metadata);
								} else {
									InjectionUtils.setObjectLocal(actionable);	
								}
							
								// execute action
								executeSafeActionable(actionable);
								DriverManager.getDefaultDriver().navigate().refresh();
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
										getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), getWebExchange().getLocalSystemMap(), metadata);
							} catch (FailedTransactionException | IndexOutOfBoundsException e) {
								log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
								log.error("Failed for transaction ", e);

								webExchange.addFailedSession(sessionId);
								captureFailedWindow(actionable);
								DriverManager.getDefaultDriver().navigate().refresh();
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
										getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), getWebExchange().getLocalSystemMap(), metadata, e.getMessage(), ReportManager.FAILED);
							} catch (ModalFailedException e) {
								log.info("Modal failed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
//								webExchange.addFailedSession(sessionId);
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
										getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), getWebExchange().getLocalSystemMap(), metadata, e.getMessage(), ReportManager.PASSED);
							}
						}
						i++;
						
						if (webExchange.getSessionList().size() <= i) {
							webExchange.clearCachedSession();
							break;
						}
					}
				}

			} else {
				// MapType.RETENTION
				int i = 0;
				while(true) {
					String sessionId = webExchange.createSession(i);
					if (!webExchange.isSessionFailed(sessionId)) {
						Map<String, Object> metadata = null;
						
						log.info("Execute data-row index " + i + " with session " + sessionId);
					
						try {
							metadata = webExchange.getMetaData(getActiveMenu().getModuleId(),i, true);
							
							if (actionable instanceof ManagedFormAction) {
								InjectionUtils.setObject(actionable);
								((ManagedFormAction) actionable).setMetadata(metadata);
							} else {
								InjectionUtils.setObjectWithCustom(actionable, metadata);	
							}
							
							executeSafeActionable(actionable);
							DriverManager.getDefaultDriver().navigate().refresh();
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
									getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), getWebExchange().getLocalSystemMap(), metadata);
						} catch (FailedTransactionException | IndexOutOfBoundsException e) {
							log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
							log.error("Failed for transaction ", e);
							
							webExchange.addFailedSession(sessionId);
							captureFailedWindow(actionable);
							DriverManager.getDefaultDriver().navigate().refresh();
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(), getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
									getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), getWebExchange().getLocalSystemMap(), metadata, e.getMessage(), ReportManager.FAILED);
						} catch (ModalFailedException e) {
							log.info("Modal failed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
//							webExchange.addFailedSession(sessionId);
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(), getWebExchange().get(Constants.CURRENT_TESTCASE_ID).toString(),
									getWebExchange().get(Constants.CURRENT_TESTSCEN_ID).toString(), getWebExchange().getLocalSystemMap(), metadata, e.getMessage(), ReportManager.PASSED);
						}
					}
					i++;
					
					if (webExchange.getListMetaData(getActiveMenu().getModuleId()).size() <= i) {
						webExchange.clearCachedSession();
						break;
					}
				}
			}
		} else {
			InjectionUtils.setObject(actionable);
			executeSafeActionable(actionable);	
		}
	}
	
	public void executeSafeActionable(Actionable actionable) throws FailedTransactionException, ModalFailedException {
		int retry = 1;
		try {
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementNotInteractableException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {
			retryWhenException(actionable, ++retry);
		}
	}
	
	private void retryWhenException(Actionable actionable, int retry) throws FailedTransactionException, ModalFailedException {
		try {
			log.info("Something happened, be calm! we still loving you!");

			DriverManager.getDefaultDriver().navigate().refresh();
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementNotInteractableException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {			
			if (retry < Constants.MAX_RETRY_LOAD_PAGE) {
				retryWhenException(actionable, ++retry);
			} else {
				log.error("Failed for transaction ", e);
				throw new FailedTransactionException("Failed for transaction, " + e.getMessage());
			}	
		}
	}
		
	public Workflow waitUntil(ModalSuccessAction actionable) {
		if (scopedAction) {
			if (scopedActionIndex == 0) {
				ManagedFormAction scoped = null;
				if (actionable instanceof FormActionable) {
					scoped = new ManagedFormAction(actionable.getClass());
				} else if (actionable instanceof MultipleFormActionable) {
					scoped = new ManagedMultipleFormAction(actionable.getClass());
				}
				
				if (scoped != null) {
					scoped.addActionable(actionable);
					actionables.add(scoped);						
				} else {
					log.warn("Managed Action is missing for " + actionable);
				}
			} else {
				Actionable act = actionables.getLast();
				if (act != null)
					((ManagedFormAction) act).addActionable(actionable);
				else
					log.warn("Managed Action is missing for " + actionable);
			}
		} else {
			actionables.add(actionable);	
		}
			 
		return this;
	}
	
	private void captureFailedWindow(Actionable actionable) {
		try {
			try {
				WebUI.getModalConfirmationId(1);
				WebCommon.captureFailedWindow();
			} catch (Exception e) {
				WebCommon.captureFailedFullModal(WebUI.getModalId(1));
			}
		} catch (Exception e1) {
			WebCommon.captureFailedFullWindow();
		}
	}
	

	
	public Workflow clearSession() {
		log.info("Clear session");
		DBQuery.close();
		webExchange.clear();
		return this;
	}
	

}
