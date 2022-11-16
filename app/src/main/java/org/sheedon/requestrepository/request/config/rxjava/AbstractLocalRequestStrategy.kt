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
package org.sheedon.requestrepository.request.config.rxjava

import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.rrouter.rxjava.AbstractRequestStrategy
import org.sheedon.rrouter.strategy.StrategyConfig

/**
 * 默认本地请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:36 上午
 */
abstract class AbstractLocalRequestStrategy<RequestCard, ResponseModel>(
    callback: StrategyCallback<ResponseModel>?
) : AbstractRequestStrategy<RequestCard, ResponseModel>(callback) {
    /**
     * 请求类型 - 本地请求
     */
    override fun onRequestType(): Int {
        return StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST
    }
}