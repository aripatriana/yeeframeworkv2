package com.yeeframework.automate.typeform;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.FormActionable;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.web.WebExchange;

/**
 * The action is managed by the session
 * @author ari.patriana
 *
 */
public class ManagedFormAction implements FormActionable {

	private LinkedList<Actionable> actionableList = new LinkedList<Actionable>();
	private Class<?> inheritClass;
	private Map<String, Object> metadata = new HashMap<String, Object>();
	
	public ManagedFormAction(Class<?> inheritClass) {
		this.inheritClass = inheritClass;
	}
	
	public Class<?> getInheritClass() {
		return inheritClass;
	}
	
	public void addActionable(Actionable actionable) {
		actionableList.add(actionable);
	}
	
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		for (Actionable actionable : actionableList) {
			InjectionUtils.setObject(inheritClass, actionable, metadata);
			actionable.submit(webExchange);
		}
	}
}
