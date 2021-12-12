package org.sheedon.rrouter.facade.annotation;

import org.sheedon.rrouter.strategy.parameter.DefaultRequestType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as 「request strategy」
 * 「request strategy」's type
 * <p>
 * There are two ways to use:
 * The first one, default is 0, represents the actual type of the overloaded method to
 * obtain the request strategy type。
 * The second, custom request strategy implementation,
 * must fill in requestStrategy(),
 * and cannot be 0 (it needs to be {@link DefaultRequestType.TYPE_LOCAL_REQUEST}
 * or {@link DefaultRequestType.TYPE_LOCAL_REQUEST})
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 11:22 下午
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestStrategy {

    /**
     * 「request strategy」's type
     * <p>
     * There are two ways to use:
     * The first one, default is 0, represents the actual type of the overloaded method to
     * obtain the request strategy type。
     * The second, custom request strategy implementation,
     * must fill in requestStrategy(),
     * and cannot be 0 (it needs to be {@link DefaultRequestType.TYPE_LOCAL_REQUEST}
     * or {@link DefaultRequestType.TYPE_LOCAL_REQUEST})
     */
    @IntRange(from = 0, to = DefaultRequestType.TYPE_LOCAL_REQUEST)
    int requestStrategy() default 0;
}
