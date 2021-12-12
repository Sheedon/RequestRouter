package org.sheedon.rrouter.facade.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for request field, which need autowired
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/22 11:11 上午
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Request {
}
