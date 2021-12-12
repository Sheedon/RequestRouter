package org.sheedon.rrouter.compiler.handler.converter.center;

import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.facade.model.Converter;

import java.io.IOException;

import javax.lang.model.element.Element;

/**
 * 解析处理职责
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/6 11:04 下午
 */
public interface AnalysisCenter<F extends Element, T> extends Converter<F, T> {

    /**
     * 解析方法，转化为指定的结构
     * @param holdClass 路由持有类
     * @param f F
     * @return 是否被处理
     * @throws IOException
     */
    @SuppressWarnings("JavaDoc")
    boolean analysis(RouterHoldClass holdClass, F f) throws IOException;

}
