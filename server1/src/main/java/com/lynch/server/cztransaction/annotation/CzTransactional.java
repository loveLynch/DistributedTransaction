package com.lynch.server.cztransaction.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CzTransactional {
    boolean isStart() default false;
    boolean isEnd() default false;
}
