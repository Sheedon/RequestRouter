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
import kotlin.NullPointerException

/**
 * 默认策略执行者
 * 采用适配器工厂，按类型获取策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 6:56 下午
 */
internal class DefaultStrategyHandler private constructor() :
    StrategyHandle.ResponsibilityFactory() {

    // 策略执行工厂
    private var handlerFactory: StrategyHandle.Factory? = null
    override fun setHandlerFactory(handlerFactory: StrategyHandle.Factory?) {
        this.handlerFactory = handlerFactory
    }

    override fun loadRequestProcess(strategyType: Int): IntArray {
        val handler = handlerFactory?.loadStrategyHandler(strategyType)
            ?: throw NullPointerException("please set strategy handler")
        return handler.loadRequestProcess()
    }

    /**
     * 请求策略执行
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     RequestCard
     * @return 执行是否成功
    </RequestCard> */
    override fun <RequestCard> handleRequestStrategy(
        requestStrategyType: Int,
        processChain: ProcessChain,
        requestStrategies: SparseArray<Request<RequestCard>>,
        card: RequestCard
    ): Boolean {
        val handler = handlerFactory?.loadStrategyHandler(requestStrategyType)
        if (handler == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }
        return handler.handleRequestStrategy(processChain, requestStrategies, card)
    }

    /**
     * 反馈策略待执行方法
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 执行完成的进度
    </ResponseModel> */
    override fun <ResponseModel> handleCallbackStrategy(
        requestStrategyType: Int,
        processChain: ProcessChain,
        callback: DataSource.Callback<ResponseModel>?,
        responseModel: ResponseModel?, message: String?,
        isSuccess: Boolean
    ): Boolean {
        val handler = handlerFactory?.loadStrategyHandler(requestStrategyType)
        if (handler == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }
        return handler.handleCallbackStrategy(
            processChain, callback, responseModel,
            message, isSuccess
        )
    }

    companion object {
        // 单例
        var HANDLER = DefaultStrategyHandler()
    }
}