package org.sheedon.rrouter.facade.model;

/**
 * Feedback data converter to convert "format F" into "format T"
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/22 4:28 下午
 */
public interface Converter<F, T> {

    T convert(F value);
}
