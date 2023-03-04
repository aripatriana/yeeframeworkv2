package com.yeeframework.automate.util;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class DataTypeUtils {

	public static final int TYPE_OF_COLUMN = 1;
	public static final int TYPE_OF_VARIABLE = 2;
	public static final int TYPE_OF_ARGUMENT = 3;
	
	public static boolean checkType(String val, int type) {
		if (type == TYPE_OF_VARIABLE) {
			return val.startsWith("@");
		} else if (type == TYPE_OF_ARGUMENT || isPrimitiveDataTypes(val)) {
			return val.startsWith("'");
		} else if (type == TYPE_OF_COLUMN) {
			return !val.startsWith("@") && !val.startsWith("'");
	
		}
		return false;
	}
	
	public static boolean checkIsColumn(String var) {
		return checkType(var, TYPE_OF_COLUMN);
	}
	
	public static String checkColumnPrefix(String var) {
		if (checkIsColumn(var)) {
			String[] c = var.split("\\.");
			if (c.length> 1) {
				var = c[1];
			}
		}
		return var;
	}
	
	public static final boolean isPrimitiveDataTypes(String arg) {
		try {
			Integer.valueOf(arg);
			// checking a lot of data type can be put here 
			return true;
		} catch (Exception e) {
			//
		}
		
		try {
			Long.valueOf(arg);
			// checking a lot of data type can be put here 
			return true;
		} catch (Exception e) {
			//
		}
		
		try {
			new BigDecimal(arg);
			// checking a lot of data type can be put here 
			return true;
		} catch (Exception e) {
			//
		}
		
		try {
			if (!arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false"))
				throw new Exception();
			// checking additional data type can be put here 
			return true;
		} catch (Exception e) {
			//
		}
		return false;
	}
	
	public static final Object parse(Object obj) {
		if (isPrimitiveDataTypes(obj.toString()))
			return obj.toString();
		else
			return StringUtils.quote(String.valueOf(obj));
	}
}
