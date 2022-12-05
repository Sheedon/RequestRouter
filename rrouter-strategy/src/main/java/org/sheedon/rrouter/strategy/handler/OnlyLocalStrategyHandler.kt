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
package org.sheedon.rrouter.strategy.handler

import android.util.SparseArray
import org.sheedon.rrouter.core.DataSource
import org.sheedon.rrouter.core.ProcessChain
import org.sheedon.rrouter.core.Request
import org.sheedon.rrouter.strategy.StrategyConfig

/**
 * 单一本地请求执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:15 下午
 */
class OnlyLocalStrategyHandler : BaseStrategyHandler() {
    /**
     * 请求流程，单一本地请求
     */
    override fun loadRequestProcess(): IntArray {
        return intArrayOf(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST)
    }

    /**
     * 执行真实请求行为
     * 类型为单一本地请求 [StrategyConfig.STRATEGY.TYPE_ONLY_LOCAL]，
     * 且当前状态为默认状态，则获取本地请求代理并做请求，否则请求调度失败
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     RequestCard
     * @return 是否请求成功
    </RequestCard> */
    override fun <RequestCard> handleRealRequestStrategy(
        processChain: ProcessChain,
        requestStrategies: SparseArray<Request<RequestCard>>,
        card: RequestCard?
    ): Boolean {
        return super.handleRealRequestStrategy(processChain, requestStrategies, card)
    }

    /**
     * 类型为单一本地请求 [StrategyConfig.STRATEGY.TYPE_ONLY_LOCAL]，
     * 当前进度为本地请求 [ProcessChain.STATUS_REQUESTING]，则执行反馈操作
     * 设置状态 [ProcessChain.STATUS_COMPLETED]
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param model           反馈model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 是否反馈成功
    </ResponseModel> */
    override fun <ResponseModel> handleRealCallbackStrategy(
        processChain: ProcessChain,
        callback: DataSource.Callback<ResponseModel>?,
        model: ResponseModel?, message: String?,
        isSuccess: Boolean
    ): Boolean {
        return super.handleRealCallbackStrategy(
            processChain,
            callback,
            model,
            message,
            isSuccess
        )
    }
}