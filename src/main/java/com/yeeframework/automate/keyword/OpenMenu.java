package com.yeeframework.automate.keyword;

import com.yeeframework.automate.ActionType;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.action.OpenFormAction;
import com.yeeframework.automate.action.OpenMenuAction;
import com.yeeframework.automate.action.OpenSubMenuAction;
import com.yeeframework.automate.execution.Workflow;
import com.yeeframework.automate.execution.WorkflowConfig;
import com.yeeframework.automate.execution.WorkflowEntry;
import com.yeeframework.automate.util.InjectionUtils;
import com.yeeframework.automate.util.ReflectionUtils;

public class OpenMenu implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.keyword.Keywords.OPEN_MENU;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		Class<?> clazz = wc.getHandler(we.getVariable());
		
		Object handler = ReflectionUtils.instanceObject(clazz);
		InjectionUtils.setObject(handler);
		
		Menu menu = wc.getMenu(we.getVariable());
		OpenMenuAction menuAction = new OpenMenuAction(null, menu.getMenu());
		workflow.action(menuAction);
		
		OpenSubMenuAction subMenuAction = new OpenSubMenuAction(menuAction, menu.getSubMenu(), menu.getMenuId());
		workflow.action(subMenuAction);
		
		OpenFormAction formAction = new OpenFormAction((menu.getSubMenu() != null ? subMenuAction : menuAction), menu.getMenuId(), menu.getForm());
		((MenuAwareness) formAction).setMenu(menu);
		workflow.action(formAction);
	
		workflow.scopedAction();
		
		ActionType action = wc.getAction(we.getActionType());
		if (action != null) {
			action.run(handler, we, workflow);
		} else {
			throw new Exception("Action is not found");
		}
		
		workflow.resetScopedAction();
	}
	
	
}
