package com.cycrix.jsonparser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonArray {
//	public enum FailBehavior {
//		Throw, Break, Pass 
//	}
//	
	public static final int FAIL_BEHAVIOR_THROW = 1;
	public static final int FAIL_BEHAVIOR_BREAK = 2;
	public static final int FAIL_BEHAVIOR_PASS 	= 3;
	
	public boolean optional() default false;
	public boolean ignore() default false;
	public int onFail() default FAIL_BEHAVIOR_PASS;
}

