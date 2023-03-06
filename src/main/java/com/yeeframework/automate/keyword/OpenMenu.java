package com.yeeframework.automate.keyword;

import com.yeeframework.automate.ActionType;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.action.OpenMenuLevel3Action;
import com.yeeframework.automate.action.OpenMenuLevel1Action;
import com.yeeframework.automate.action.OpenMenuLevel2Action;
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
		if (menu.getMenuLevel1() != null) {
			OpenMenuLevel1Action menuLevel1 = new OpenMenuLevel1Action(null, menu.getMenuLevel1());
			((MenuAwareness) menuLevel1).setMenu(menu);
			workflow.action(menuLevel1);

			if (menu.getMenuLevel2() != null) {
				OpenMenuLevel2Action menuLevel2 = new OpenMenuLevel2Action(menuLevel1, menu.getMenuLevel2());
				workflow.action(menuLevel2);

				if (menu.getMenuLevel3() != null) {
					OpenMenuLevel3Action menuLevel3 = new OpenMenuLevel3Action(menuLevel2, menu.getMenuLevel3());
					workflow.action(menuLevel3);					
				}
			}
		}
		
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
