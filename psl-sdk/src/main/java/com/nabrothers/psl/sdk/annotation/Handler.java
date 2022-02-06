package com.nabrothers.psl.sdk.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Handler {
    String command();

    String info() default "";
}
