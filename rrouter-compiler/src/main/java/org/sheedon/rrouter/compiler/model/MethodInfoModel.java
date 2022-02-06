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
package org.sheedon.rrouter.compiler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * 方法信息Model
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/29 11:41 下午
 */
public class MethodInfoModel {

    // 方法名
    private String methodName;
    // 反馈类型
    private TypeName returnType;
    // 方法参数
    private final List<TypeName> parameterClass;
    // 其他属性
    private Object obj;

    public MethodInfoModel() {
        parameterClass = new LinkedList<>();
    }

    /**
     * 设置方法名
     *
     * @param methodName 方法名
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 设置返回类型
     *
     * @param returnType 返回类型
     */
    public void setReturnType(TypeMirror returnType) {
        this.returnType = ClassName.get(returnType);
    }

    /**
     * 添加参数类
     *
     * @param parameterType 参数类型
     */
    public void addParameterClass(TypeMirror parameterType) {
        parameterClass.add(ClassName.get(parameterType));
    }

    public void setObj(Object obj){
        this.obj = obj;
    }

    public Object getObj() {
        return obj;
    }

    public String getMethodName() {
        return methodName;
    }

    public TypeName getReturnType() {
        return returnType;
    }

    public List<TypeName> getParameterClass() {
        return parameterClass;
    }
}
