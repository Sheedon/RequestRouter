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
package org.sheedon.rrouter.compiler.processor;

import org.sheedon.rrouter.compiler.model.holder.RequestHoldClass;
import org.sheedon.rrouter.facade.annotation.RRouter;
import org.sheedon.rrouter.facade.annotation.Request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * request field proxy processor,aims to record the class name of the Request Field class
 * and the full class name and field name of the @Request annotation
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/24 6:56 下午
 */
final class RequestFieldProcessor {

    private final Messager mMessager;
    private final Types mTypeUtils;
    // 持有请求类的Class类信息
    private final Map<String, RequestHoldClass> holderClasses = new HashMap<>();
    // 字段类型的全类名，用于路由类核实是否需要创建对应的类，若需核实的类在这里不存在，则无需创建
    private final Set<String> fieldQualifiedNames = new HashSet<>();

    RequestFieldProcessor(Messager mMessager, Types mTypeUtils) {
        this.mMessager = mMessager;
        this.mTypeUtils = mTypeUtils;
    }

    /**
     * 开始运行解析动作
     */
    void run(RoundEnvironment environment) {
        Set<? extends Element> elements = environment.getElementsAnnotatedWith(Request.class);

        // 无数据则结束
        if (elements == null || elements.isEmpty()) {
            return;
        }

        // 遍历，插入到 请求类的Map中
        for (Element element : elements) {
            if (element instanceof VariableElement) {
                boolean isInsert = insertHolderClass((VariableElement) element);
                if (!isInsert) {
                    return;
                }
            }
        }
    }

    /**
     * 插入Holder Class 类
     */
    private boolean insertHolderClass(VariableElement element) {
        // 获取持有「请求路由」的类，提取这个类的全类名和类名，
        // 生成根据类名生成RequestHoldClass 放入键为qualifiedName 的map位置上
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = typeElement.getQualifiedName().toString();
        String simpleName = typeElement.getSimpleName().toString();
        RequestHoldClass holdClass = holderClasses.computeIfAbsent(qualifiedName,
                value -> new RequestHoldClass(simpleName));

        // 将字段填入 按字段类名和字段名填入RequestHoldClass中
        String fieldQualifiedName = element.asType().toString();
        String fieldName = element.getSimpleName().toString();
        holdClass.addField(fieldQualifiedName, fieldName);

        // 核实持有的路由类是否符合条件 持有@RRouter
        TypeMirror typeMirror = element.asType();
        TypeElement routerElement = (TypeElement) mTypeUtils.asElement(typeMirror);
        RRouter rRouter = routerElement.getAnnotation(RRouter.class);
        if (rRouter == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Please add annotation @RRouter in " +
                            routerElement.getQualifiedName().toString(),
                    routerElement);
            return false;
        }


        fieldQualifiedNames.add(fieldQualifiedName);
        return true;
    }

    public Map<String, RequestHoldClass> getHolderClasses() {
        return holderClasses;
    }

    /**
     * 获取字段类的全类名
     */
    Set<String> getFieldQualifiedNames() {
        return fieldQualifiedNames;
    }
}
