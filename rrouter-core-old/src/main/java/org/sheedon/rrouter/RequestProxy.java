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
package org.sheedon.rrouter;

/**
 * 请求策略代理基本接口
 * 需要履行的职责
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 4:33 下午
 */
public interface RequestProxy {

    /**
     * 代理请求入口
     */
    void request();

    /**
     * 销毁
     */
    void onDestroy();

    /**
     * 请求策略代理工厂类
     * 代理请求Class，多策略配置的请求操作
     */
    abstract class RequestProxyFactory implements RequestProxy {


        /**
         * 绑定策略执行器
         * 执行请求组策略
         */
        protected abstract StrategyHandle.Responsibilities bindStrategyHandler();
    }
}
