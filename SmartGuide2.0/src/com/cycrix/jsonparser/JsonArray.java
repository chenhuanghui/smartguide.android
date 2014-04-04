package com.cycrix.jsonparser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonArray {
	public enum FailBehavior {
		Throw, Break, Pass 
	}
	
	public boolean optional() default false;
	public boolean ignore() default false;
	public FailBehavior onFail() default FailBehavior.Pass;
}
