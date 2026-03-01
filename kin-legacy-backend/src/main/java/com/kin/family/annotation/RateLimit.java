package com.kin.family.annotation;

import com.kin.family.constant.RateLimitType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int value() default 10;
    int duration() default 60;
    RateLimitType type() default RateLimitType.IP;
}
