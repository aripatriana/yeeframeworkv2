package com.yeeframework.automate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestCaseEntity {

	TestCaseEntityType type() default TestCaseEntityType.RETENTION;
	
	public String name() default "";
	
	public String description() default "";
}
