package org.sheedon.rrouter.facade.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as request data adapter
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/22 4:12 下午
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestDataAdapter {
}
