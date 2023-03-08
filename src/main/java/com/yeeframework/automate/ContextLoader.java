package com.yeeframework.automate;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yeeframework.automate.annotation.FetchSession;
import com.yeeframework.automate.annotation.MapAction;
import com.yeeframework.automate.annotation.MapActionList;
import com.yeeframework.automate.annotation.PropertyColumn;
import com.yeeframework.automate.annotation.Join;
import com.yeeframework.automate.annotation.JoinList;
import com.yeeframework.automate.annotation.TestCaseEntity;
import com.yeeframework.automate.annotation.PropertySession;
import com.yeeframework.automate.annotation.TestCaseEntityType;
import com.yeeframework.automate.annotation.PropertyValue;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.web.WebExchange;

/**
 * Manage context of object and it is used for direct injection
 * 
 * @author ari.patriana
 *
 */
@SuppressWarnings("deprecation")
public class ContextLoader {
	
	private static Logger log = LoggerFactory.getLogger(ContextLoader.class);
	
	private static WebExchange webExchange;
	
	public static void setWebExchange(WebExchange webExchange) {
		ContextLoader.webExchange = webExchange;
	}
	
	public static WebExchange getWebExchange() {
		return webExchange;
	}
	
	public static boolean isWorkbookPersistentPresent(Class<?> clazz) {
		return clazz.isAnnotationPresent(TestCaseEntity.class);
	}
	
	public static boolean isWorkbookPersistentPresent(Object object) {
		Class<?> clazz = object.getClass();
		return isWorkbookPersistentPresent(clazz);
	}
	
	public static boolean isWorkbookSessionPresent(Class<?> clazz) {
		if (clazz.isAnnotationPresent(TestCaseEntity.class)) {
			TestCaseEntityType type = clazz.getAnnotation(TestCaseEntity.class).type();
			return type.equals(TestCaseEntityType.SESSION);					
		}
		return false;
	}
	
	public static boolean isWorkbookSessionPresent(Object object) {
		Class<?> clazz = object.getClass();
		return isWorkbookSessionPresent(clazz);
	}
	
	public static void setObject(Object object) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (getWebExchange() != null) {
			map.putAll(getWebExchange().getAll());
			map.put(WebExchange.ALL_LOCAL_VARIABLE, getWebExchange().getAllListLocalSystemMap());
			map.put(WebExchange.LOCAL_VARIABLE, getWebExchange().getLocalSystemMap());		
			map.putAll(getWebExchange().getLocalSystemMap());
		}
		setObject(object, map);
	}
	
	public static void setObjectSession(Object object) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (getWebExchange() != null) {
			map.putAll(getWebExchange().getAll());
			map.put(WebExchange.ALL_LOCAL_VARIABLE, getWebExchange().getAllListLocalSystemMap());
			map.put(WebExchange.LOCAL_VARIABLE, getWebExchange().getLocalSystemMap());		
			map.putAll(getWebExchange().getLocalSystemMap());
		}
		setObject(object, map);
	}
	
	public static void setObjectWithCustom(Object object, Map<String, Object> metadata) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (getWebExchange() != null) {
			map.putAll(getWebExchange().getAll());
			map.put(WebExchange.ALL_LOCAL_VARIABLE, getWebExchange().getAllListLocalSystemMap());
			map.put(WebExchange.LOCAL_VARIABLE, getWebExchange().getLocalSystemMap());	
			map.putAll(getWebExchange().getLocalSystemMap());
		}

		if (metadata != null && metadata.size() > 0)
			map.putAll(metadata);
		
		setObject(object, map);
	}
	
	public static void setObjectSessionWithCustom(Object object, Map<String, Object> metadata) {
		Map<String, Object> map = new HashMap<String, Object>();
	
		if (getWebExchange() != null) {
			map.putAll(getWebExchange().getAll());
			map.put(WebExchange.ALL_LOCAL_VARIABLE, getWebExchange().getAllListLocalMap());
			map.put(WebExchange.LOCAL_VARIABLE, getWebExchange().getLocalSystemMap());	
			map.putAll(getWebExchange().getLocalSystemMap());
		}
		
		// if there is the same object within metadata and session, then the session will be overridden by metadata
		if (metadata != null && metadata.size() > 0)
			map.putAll(metadata);
		
		setObject(object, map);
	}

	
	private static void setObject(Object object, Map<String, Object> metadata) {
		Class<?> clazz = object.getClass();
		if (clazz.isAnnotationPresent(TestCaseEntity.class)) {		
			Map<String, String> fields = recognize(object, metadata);
			setFields(object, fields, metadata);
		} else {
			Map<String, String> fields = recognizeValue(object, metadata);
			setFields(object, fields, metadata);
		}
	}
	
	private static Map<String, String> recognizeValue(Object object, Map<String, Object> metadata) {
		return recognizeValue(object, object.getClass(), metadata);
	}
	
	private static Map<String, String> recognizeValue(Object object, Class<?> clazz, Map<String, Object> metadata) {
		Map<String, String> fields  = new HashMap<String, String>();    
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(PropertyValue.class)) {
            	 fields.put(field.getName(), field.getAnnotation(PropertyValue.class).value());
            } else if (field.isAnnotationPresent(FetchSession.class)) {
            	try {
                	if (!ReflectionUtils.checkAssignableFrom(field.getType(), List.class)) throw new InstantiationException("Exception for initialize field " + field.getName()  + " must be List");
                	ReflectionUtils.setProperty(object, field.getName(), metadata.get(WebExchange.ALL_LOCAL_VARIABLE));
            	} catch (InstantiationException e) {
            		log.error("ERROR ", e);
            	}
            } else if (field.isAnnotationPresent(Autowired.class)) {
            	try {
            		Object d = field.get(object);
            		if (d == null) {
	            		Class<?> c = field.getType();
						d = c.newInstance();
            		}
					setObject(d, metadata);
					ReflectionUtils.setProperty(object, field.getName(), d);
				} catch (IllegalAccessException e) {
					log.error("ERROR ", e);
				} catch (InstantiationException e) {
					log.error("ERROR ", e);
				}
            }
        }
        if (!Object.class.equals(clazz.getSuperclass())) {
        	Map<String, String> temp = recognizeValue(object, clazz.getSuperclass(), metadata);
        	if (temp != null && temp.size() > 0) {
        		fields.putAll(temp);
        	}
        }
        return fields;
	}

	private static Map<String, String> recognize(Object object, Map<String, Object> metadata) {
		return recognize(object, object.getClass(), metadata);
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Map<String, String> recognize(Object object, Class<?> clazz, Map<String, Object> metadata) {
		Map<String, String> fields  = new HashMap<String, String>();    
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(PropertyColumn.class)) {
                fields.put(field.getName(), field.getAnnotation(PropertyColumn.class).name());
            } else if (field.isAnnotationPresent(MapAction.class)) {
				try {
					Class<?> c = field.getAnnotation(MapAction.class).clazz();
					Object d = c.newInstance();
					setObject(d, (Map<String, Object>)metadata.get(field.getAnnotation(MapAction.class).name()));
					ReflectionUtils.setProperty(object, field.getName(), d);
				} catch (InstantiationException e) {
					log.error("ERROR ", e);
				} catch (IllegalAccessException e1) {
					log.error("ERROR ", e1);
				}
            } else if (field.isAnnotationPresent(Join.class)) {
				try {
					Class<?> c = field.getAnnotation(Join.class).clazz();
					Object d = c.newInstance();
					setObject(d, (Map<String, Object>)metadata.get(field.getAnnotation(Join.class).name()));
					ReflectionUtils.setProperty(object, field.getName(), d);
				} catch (InstantiationException e) {
					log.error("ERROR ", e);
				} catch (IllegalAccessException e1) {
					log.error("ERROR ", e1);
				}
			} else if (field.isAnnotationPresent(MapActionList.class)) {
            	try {
		            Class<?> c = field.getAnnotation(MapActionList.class).clazz();
		            LinkedList<Object> list = new LinkedList<Object>();

	            	if (metadata.get(field.getAnnotation(MapActionList.class).name()) != null) {
		            	for (LinkedHashMap<String, Object> md : (Collection<LinkedHashMap<String, Object>>)metadata.get(field.getAnnotation(MapActionList.class).name())) {
		            		Object d = c.newInstance();
		            		setObjectWithCustom(d, md);
		            		list.add(d);
		            	};
	            	}
		            ReflectionUtils.setProperty(object, field.getName(), list);
    			} catch (InstantiationException e) {
    				log.error("ERROR ", e);
				} catch (IllegalAccessException e1) {
					log.error("ERROR ", e1);
				}
            } else if (field.isAnnotationPresent(JoinList.class)) {
            	try {	
	            	Class<?> c = field.getAnnotation(JoinList.class).clazz();
	            	LinkedList<Object> list = new LinkedList<Object>();
	            	if (metadata.get(field.getAnnotation(JoinList.class).name()) != null) {
	            		Object obj = metadata.get(field.getAnnotation(JoinList.class).name());
	            		
	            		if (ReflectionUtils.checkAssignableFrom(c, String.class)) {
	            			list = (LinkedList<Object>)obj;
	            		} else {
	            			for (LinkedHashMap<String, Object> md : (Collection<LinkedHashMap<String, Object>>)obj) {
			            		Object d = c.newInstance();
			            		setObjectWithCustom(d, md);
			            		list.add(d);
			            	};
	            		}
		            }
		            ReflectionUtils.setProperty(object, field.getName(), list);
    			} catch (InstantiationException e) {
    				log.error("ERROR ", e);
				} catch (IllegalAccessException e1) {
					log.error("ERROR ", e1);
				}
            } else if (field.isAnnotationPresent(PropertySession.class)) {
            	Map<String, Object> session = (Map<String, Object>) metadata.get(WebExchange.LOCAL_VARIABLE);
            	if (session.get(field.getAnnotation(PropertySession.class).name()) != null) {
            		ReflectionUtils.setProperty(object, field.getName(), String.valueOf(session.get(field.getAnnotation(PropertySession.class).name())));
            	} else {
            		ReflectionUtils.setProperty(object, field.getName(), null);
            	}
            		
//            	 fields.put(field.getName(), field.getAnnotation(MapSession.class).name());
            	
            // map session is from now used to map from session value
            /*} else if (field.isAnnotationPresent(MapSession.class)) {
            	try {
                	if (!ReflectionUtils.checkAssignableFrom(field.getType(), Map.class)) throw new InstantiationException("Exception for initialize field " + field.getName()  + " must be Map");
                	ReflectionUtils.setProperty(object, field.getName(), metadata.get(WebExchange.LOCAL_VARIABLE));
            	} catch (InstantiationException e) {
            		log.error("ERROR ", e);
            	}*/
            } else if (field.isAnnotationPresent(PropertyValue.class)) {
            	 fields.put(field.getName(), field.getAnnotation(PropertyValue.class).value());
            } else if (field.isAnnotationPresent(FetchSession.class)) {
            	try {
                	if (!ReflectionUtils.checkAssignableFrom(field.getType(), List.class)) throw new InstantiationException("Exception for initialize field " + field.getName()  + " must be List");
                	ReflectionUtils.setProperty(object, field.getName(), metadata.get(WebExchange.ALL_LOCAL_VARIABLE));
            	} catch (InstantiationException e) {
            		log.error("ERROR ", e);
            	}
            } else if (field.isAnnotationPresent(Autowired.class)) {
            	try {
            		Object d = field.get(object);
            		if (d == null) {
	            		Class<?> c = field.getType();
						d = c.newInstance();
            		}
					setObject(d, metadata);
					ReflectionUtils.setProperty(object, field.getName(), d);
				} catch (IllegalAccessException e) {
					log.error("ERROR ", e);
				} catch (InstantiationException e) {
					log.error("ERROR ", e);
				}
            }
            
        }
        
        if (!Object.class.equals(clazz.getSuperclass())) {
        	Map<String, String> temp = recognize(object, clazz.getSuperclass(), metadata);
        	if (temp != null && temp.size() > 0) {
        		fields.putAll(temp);
        	}
        }
        return fields;
	}
	
	private static void setFields(Object object, Map<String, String> fields, Map<String, Object> metadata) {
		for (Entry<String, String> entry : fields.entrySet()) {
			Object value = metadata.get(entry.getValue());
			
			// handling if upper case map field exists
			// uppercase exists in nusantara versi 0.0.2
			if (value == null)
				value = metadata.get(entry.getValue().toLowerCase());
			// end
			
			if (value != null) {
				ReflectionUtils.setProperty(object, entry.getKey(), String.valueOf(value));
			} else {
				ReflectionUtils.setProperty(object, entry.getKey(), null);
			}	
		}        
	}
	
	public static void clear() {
		if (getWebExchange() != null) {
			getWebExchange().clear();
		}
	}

}
