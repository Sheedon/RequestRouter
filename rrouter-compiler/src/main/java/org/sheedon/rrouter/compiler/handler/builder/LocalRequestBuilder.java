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
package org.sheedon.rrouter.compiler.handler.builder;

import org.sheedon.rrouter.compiler.Contract;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

/**
 * 本地请求策略构造器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/8 11:07 下午
 */
public class LocalRequestBuilder extends AbstractRequestBuilder {

    // 抽象本地请求类名
    private final static String ABSTRACT_LOCAL_REQUEST_CLASS = "AbstractLocalRequestStrategy";

    public LocalRequestBuilder(Messager messager, Elements elements, Filer filer) {
        super(messager, elements, filer);
    }

    @Override
    protected String loadMethod() {
        return "onLoadLocalMethod";
    }

    @Override
    protected String createRequestClassName(String routerClassName) {
        String className = routerClassName.replace(Contract.ROUTER, Contract.SEPARATOR);
        return className + Contract.LOCAL_REQUEST;
    }

    @Override
    protected String loadAbstractClassName() {
        return ABSTRACT_LOCAL_REQUEST_CLASS;
    }
}
