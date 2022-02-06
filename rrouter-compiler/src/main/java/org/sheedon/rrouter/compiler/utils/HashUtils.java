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

import java.util.ArrayList;
import java.util.List;

/**
 * 构造参数组 hash工具类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/26 6:02 下午
 */
public class HashUtils {

    private static final List<String> hashArray = new ArrayList<>();
    private static final Object lock = new Object();

    /**
     * 通过类名创建哈希码，哈希码为类名所在hashArray的坐标
     *
     * @param className 全类名
     * @return 哈希码
     */
    public static int hashCode(String className) {
        int index = hashArray.indexOf(className);
        if (index != -1) {
            return index;
        }

        synchronized (lock) {
            index = hashArray.indexOf(className);

            if (index != -1) {
                return index;
            }

            hashArray.add(className);
            return hashArray.size() - 1;
        }
    }

}
