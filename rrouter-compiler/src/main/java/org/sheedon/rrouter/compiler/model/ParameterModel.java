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

/**
 * 构造方法参数类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/26 4:18 下午
 */
public class ParameterModel {

    // 类名-全类名
    private String className;
    // 字段名
    private String fieldName;

    private ParameterModel() {

    }

    /**
     * 构造参数信息Model
     *
     * @param className 字段的全类名
     * @param fieldName 字段名
     * @return ParameterModel
     */
    public static ParameterModel build(String className, String fieldName) {
        ParameterModel model = new ParameterModel();
        model.className = className;
        model.fieldName = fieldName;
        return model;
    }

    /**
     * 获取全类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取字段名称
     */
    public String getFieldName() {
        return fieldName;
    }
}
