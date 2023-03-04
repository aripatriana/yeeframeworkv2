package com.yeeframework.automate.util;

import java.util.Map;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.typeform.ManagedFormAction;

public class InjectionUtils {

	public static void setObject(Class<?> inheritClass, Actionable actionable, Map<String, Object> metadata) {
		if (ContextLoader.isWorkbookPersistentPresent(inheritClass)) {
			// execute map serializable
			if (ContextLoader.isWorkbookSessionPresent(actionable)) {
				ContextLoader.setObjectSession(actionable);
			} else {
				ContextLoader.setObjectWithCustom(actionable, metadata);
			}
		} else {		
			ContextLoader.setObject(actionable);
		}
	}
	
	public static boolean isWorkbookPersistentPresent(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isWorkbookPersistentPresent(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isWorkbookPersistentPresent(object);
	}
	
	public static boolean isWorkbookSessionPresent(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isWorkbookSessionPresent(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isWorkbookSessionPresent(object);
	}
	
	public static void setObject(Object object) {
		ContextLoader.setObject(object);
	}
	
	public static void setObjectLocal(Object object) {
		ContextLoader.setObjectSession(object);
	}
	
	public static void setObjectWithCustom(Object object, Map<String, Object> metadata) {
		ContextLoader.setObjectWithCustom(object, metadata);
	}
	
	public static void setObjectSessionWithCustom(Object object, Map<String, Object> metadata) {
		ContextLoader.setObjectSessionWithCustom(object, metadata);
	}
}
