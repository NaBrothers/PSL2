package com.nabrothers.psl.sdk.annotation;

import com.nabrothers.psl.sdk.enums.TriggerType;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Handler {
    String command() default "";

    String info() default "";

    TriggerType trigger() default TriggerType.AT_ONLY;
}
