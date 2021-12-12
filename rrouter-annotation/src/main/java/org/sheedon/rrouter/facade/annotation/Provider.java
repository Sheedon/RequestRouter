package org.sheedon.rrouter.facade.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Request class constructor method provider
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/22 3:14 下午
 */
@Target({ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface Provider {
}
