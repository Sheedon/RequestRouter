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
package org.sheedon.rrouter.core;

import android.app.Application;

import java.util.Objects;

/**
 * 请求路由客户端
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 10:49 下午
 */
public class RRouter {

    // 单例
    private final static RRouter sInstance = new RRouter();
    private static boolean sInstalled = false;
    // 基础参数配置项
    private ConfigRepository configRepository;
    // 应用上下文
    private Application context;

    private RRouter() {

    }

    public static void setUp(Application application, ConfigRepository repository) {
        if (sInstalled) {
            return;
        }

        sInstance.context = Objects.requireNonNull(application, "application == null");
        sInstance.configRepository = Objects.requireNonNull(repository, "repository == null");
        sInstalled = true;
    }

    static RRouter getInstance() {
        return sInstance;
    }

    public Application getContext() {
        return context;
    }

    static boolean isInstalled() {
        return sInstalled;
    }

    ConfigRepository getConfigRepository() {
        return Objects.requireNonNull(configRepository, "please RRouter initialize first");
    }

    Converter<?, IRspModel<?>> getRspConverter() {
        return Objects.requireNonNull(configRepository.getRspConverter(), "please RRouter initialize first");
    }
}
