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
 * 执行策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 9:18 下午
 */
interface StrategyHandle {
    /**
     * 加载请求流程，用于在 ProcessChain类 中填充流程进度
     * 加载的流程ID数组为「策略执行方式」所对应的请求策略顺序
     * 例如A请求执行方式对应的策略依次为：1⃣ -> 2⃣ -> 3⃣ -> 2⃣ -> 3⃣
     * 那么返回数组为「1，2，3，2，3」，ProcessChain类 会将该流程的进度都设置为 ProcessChain.STATUS_NORMAL
     */
    fun loadRequestProcess(): IntArray

    /**
     * 请求策略执行
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     RequestCard
     * @return 执行是否成功
    </RequestCard> */
    fun <RequestCard> handleRequestStrategy(
        processChain: ProcessChain,
        requestStrategies: SparseArray<Request<RequestCard>>,
        card: RequestCard
    ): Boolean

    /**
     * 反馈策略待执行方法
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param model           反馈结果
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 执行完成的进度
    </ResponseModel> */
    fun <ResponseModel> handleCallbackStrategy(
        processChain: ProcessChain,
        callback: DataSource.Callback<ResponseModel>?,
        model: ResponseModel,
        message: String?,
        isSuccess: Boolean
    ): Boolean

    /**
     * 策略执行器抽象工厂
     */
    abstract class Factory {
        /**
         * 加载策略处理集合
         */
        protected abstract fun loadStrategyHandlerArray(): SparseArray<StrategyHandle>

        /**
         * 加载策略执行者
         *
         * @param requestStrategyType 请求策略类型
         * @return StrategyHandler
         */
        fun loadStrategyHandler(requestStrategyType: Int): StrategyHandle? {
            val sparseArray = loadStrategyHandlerArray()
            return sparseArray[requestStrategyType]
        }
    }

    /**
     * 默认 组策略执行者 的职责
     */
    interface Responsibilities {
        /**
         * 加载请求流程，用于在 ProcessChain类 中填充流程进度
         * 加载的流程ID数组为「策略执行方式」所对应的请求策略顺序
         * 例如A请求执行方式对应的策略依次为：1⃣ -> 2⃣ -> 3⃣ -> 2⃣ -> 3⃣
         * 那么返回数组为「1，2，3，2，3」，ProcessChain类 会将该流程的进度都设置为 ProcessChain.STATUS_NORMAL
         *
         * @param strategyType 请求策略类型
         */
        fun loadRequestProcess(strategyType: Int): IntArray?

        // 执行请求策略
        fun <RequestCard> handleRequestStrategy(
            requestStrategyType: Int, processChain: ProcessChain,
            requestStrategies: SparseArray<Request<RequestCard>>,
            card: RequestCard
        ): Boolean

        // 执行反馈处理策略
        fun <ResponseModel> handleCallbackStrategy(
            requestStrategyType: Int, processChain: ProcessChain,
            callback: DataSource.Callback<ResponseModel>?,
            responseModel: ResponseModel, message: String?,
            isSuccess: Boolean
        ): Boolean
    }

    /**
     * 请求策略仓库工厂类
     */
    abstract class ResponsibilityFactory : Responsibilities {
        abstract fun setHandlerFactory(handlerFactory: Factory?)
    }
}