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
package org.sheedon.rrouter.strategy

import kotlinx.coroutines.CoroutineScope
import org.sheedon.rrouter.core.StrategyCallback

/**
 * 网络请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:41 上午
 */
abstract class AbstractRemoteRequestStrategy<RequestCard, ResponseModel>(
    coroutineScope: CoroutineScope,
    callback: StrategyCallback<ResponseModel>?
) : BaseRequestStrategy<RequestCard, ResponseModel>(coroutineScope, callback) {
    /**
     * 请求类型 - 网络请求
     */
    override fun onRequestType(): Int {
        return StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST
    }
}