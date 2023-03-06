package com.yeeframework.automate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Join {
	
	public Class<?> clazz();
	
	public String name() default "";

}
