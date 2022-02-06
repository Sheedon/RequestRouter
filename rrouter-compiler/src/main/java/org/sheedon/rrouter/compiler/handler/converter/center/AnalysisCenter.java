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
