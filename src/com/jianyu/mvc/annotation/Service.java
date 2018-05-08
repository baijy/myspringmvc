package com.jianyu.mvc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author BaiJianyu <br>
 * @date 2018年5月8日下午3:41:58 <br>
 * Better late than never. <br>
 */
@Target({ElementType.TYPE})  
@Retention(RetentionPolicy.RUNTIME)  
@Documented
public @interface Service {  
    String value() default "";  
}  
