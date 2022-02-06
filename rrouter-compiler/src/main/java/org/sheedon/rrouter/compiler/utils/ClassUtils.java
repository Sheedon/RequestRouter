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
package org.sheedon.rrouter.compiler.utils;

import com.squareup.javapoet.ClassName;

import org.sheedon.rrouter.compiler.Contract;

/**
 * 类处理工具
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/8 1:32 下午
 */
public class ClassUtils {

    /**
     * 通过全类名 转化为 ClassName
     *
     * @param qualifiedName 全类名
     * @return ClassName
     */
    public static ClassName convertByQualifiedName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(Contract.POINT);
        return ClassName.get(qualifiedName.substring(0, index), qualifiedName.substring(index + 1));
    }

    /**
     * 转化为小驼峰命名
     */
    public static String convertLittleCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        String firstLetter = name.substring(0, 1);
        return firstLetter.toLowerCase() + name.substring(1);
    }

    /**
     * 转化为大驼峰命名
     */
    public static String convertBigCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        String firstLetter = name.substring(0, 1);
        return firstLetter.toUpperCase() + name.substring(1);
    }
}
