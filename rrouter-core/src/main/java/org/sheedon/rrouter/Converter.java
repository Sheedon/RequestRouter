package org.sheedon.rrouter;

import org.sheedon.rrouter.core.support.IRspModel;

/**
 * 数据转换
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2022/2/15 7:35 下午
 */
public interface Converter<T, F> {

    F convert(T t);


    class Factory {

        public Converter<?, IRspModel<?>> createCheckConverter() {
            return null;
        }

    }
}
