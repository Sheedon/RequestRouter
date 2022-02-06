/*
 * Copyright (C) 2022 Sheedon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sheedon.rrouter.compiler.handler.converter.strategies;

import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.handler.converter.center.AnalysisCenter;

import java.lang.annotation.Annotation;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;

/**
 * 抽象解析器，解析持有@RRouter的类方法，并且解析，设置到RouterHoldClass中
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/6 11:07 下午
 */
public abstract class AbstractConverter<F extends Element, T, A extends Annotation>
        implements AnalysisCenter<F, T> {

    // 错误输出类
    protected final Messager messager;
    // 类型处理工具类
    protected final Types types;

    public AbstractConverter(Messager messager, Types types) {
        this.messager = messager;
        this.types = types;
    }

    /**
     * 解析请求路由类的方法
     *
     * @param holdClass 路由持有类
     * @param f         解析信息
     * @return 是否被解析
     */
    @Override
    public boolean analysis(RouterHoldClass holdClass, F f) {
        if (f == null) {
            return false;
        }

        A annotation = f.getAnnotation(loadAnnotationClass());
        if (annotation == null) {
            return false;
        }

        return handleConvert(annotation, holdClass, f);
    }

    /**
     * 执行转化动作，调度由子类具体实现转化行为的convert()方法，得到转化结果
     * 由该结果传递到抽象方法 handleAnnotation() 处理注解信息，从而填充到 RouterHoldClass 中
     *
     * @param annotation 注解
     * @param holdClass  路由持有类
     * @param f          被解析信息
     * @return 是否被处理
     */
    protected boolean handleConvert(A annotation, RouterHoldClass holdClass, F f) {
        T convert = convert(f);
        return handleAnnotation(annotation, holdClass, convert, f);
    }

    /**
     * 处理注解信息
     *
     * @return 是否被处理
     */
    protected abstract boolean handleAnnotation(A annotation, RouterHoldClass holdClass, T convert, F f);


    /**
     * 获取需要解析的注解信息
     *
     * @param annotationType 注解类型
     * @param <A>            注解类
     * @return A 实际注解类
     */
    protected abstract Class<A> loadAnnotationClass();
}
