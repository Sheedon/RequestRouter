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
package org.sheedon.rrouter.compiler.model.holder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Request holding's Class information
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/25 3:35 下午
 */
public class RequestHoldClass {

    // 类名
    private final String className;
    // 一个类下面 路由字段 的 集合
    // 以该路由字段全类名为键，字段名为值
    private final Map<String, List<String>> routerFieldMap = new HashMap<>();

    public RequestHoldClass(String className) {
        this.className = className;
    }

    /**
     * 添加字段信息
     *
     * @param qualifiedName 字段全类名
     * @param fieldName     字段名称
     */
    public void addField(String qualifiedName, String fieldName) {
        routerFieldMap.computeIfAbsent(qualifiedName, value -> new LinkedList<>()).add(fieldName);
    }

    /**
     * 获取持有请求类的外部类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取字段信息
     * key 为字段全类名
     * value 为字段名称集合
     *
     * @return Map<String, List < String>>
     */
    public Map<String, List<String>> getRouterFieldMap() {
        return routerFieldMap;
    }
}
