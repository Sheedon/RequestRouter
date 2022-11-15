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
package org.sheedon.rrouter.core

import android.util.SparseArray

/**
 * 请求行为的创建工厂类
 * 1. 创建获取实际请求集合
 * 2. 加载请求调度策略，后续按照请求调度策略方式，从请求集合中取请求，执行请求行为
 * 3. 提供销毁动作，依次执行请求销毁
 *
 * @param <RequestCard>
 * @param <ResponseModel>
</ResponseModel></RequestCard> */
abstract class RequestFactory<RequestCard, ResponseModel> {
    /**
     * 创建请求策略集合，请求执行代理类需调用该方法，用于加载真实请求集合
     *
     * @param callback 反馈监听器
     * @return SparseArray<Request></Request> < RequestCard>>
     */
    abstract fun createRequestStrategies(callback: StrategyCallback<ResponseModel>): SparseArray<Request<RequestCard>>

    /**
     * 加载请求策略类型
     * 由实际创建的请求策略提供策略类型
     * 例如 [DefaultStrategyHandler.STRATEGY]
     *
     * @return 策略类型
     */
    abstract fun onLoadRequestStrategyType(): Int

    /**
     * 请求取消操作
     */
    abstract fun onCancel()

    /**
     * 操作中止的销毁操作
     */
    abstract fun onDestroy()
}