package org.sheedon.rrouter.facade.annotation;


import org.sheedon.rrouter.strategy.parameter.DefaultStrategy;
import org.sheedon.rrouter.strategy.support.AbstractRequestStrategy;
import org.sheedon.rrouter.strategy.support.NullRequestStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a page can be route by Request-Router.
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 9:22 下午
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface RRouter {


    /**
     * request strategy type
     */
    @IntRange(from = DefaultStrategy.TYPE_ONLY_REMOTE, to = DefaultStrategy.TYPE_ONLY_LOCAL)
    int requestStrategy() default DefaultStrategy.TYPE_ONLY_REMOTE;

    /**
     * local request class
     * DefaultRequestStrategy.class equivalent Void.class
     */
    Class<? extends AbstractRequestStrategy<?, ?>> localRequestClass() default NullRequestStrategy.class;

    /**
     * remote request class
     * DefaultRequestStrategy.class equivalent Void.class
     */
    Class<? extends AbstractRequestStrategy<?, ?>> remoteRequestClass() default NullRequestStrategy.class;


    /**
     * used current request strategy type
     */
    boolean used() default false;
}
