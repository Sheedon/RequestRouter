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
import org.sheedon.rrouter.core.StrategyHandle
import org.sheedon.rrouter.core.ProcessChain
import org.sheedon.rrouter.core.Request

/**
 * 基础策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:33 下午
 */
abstract class BaseStrategyHandler : StrategyHandle {
    /**
     * 处理请求代理
     *
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     请求卡片
     * @return 是否处理成功
    </RequestCard> */
    override fun <RequestCard> handleRequestStrategy(
        processChain: ProcessChain,
        requestStrategies: SparseArray<Request<RequestCard>>,
        card: RequestCard
    ): Boolean {
        return handleRealRequestStrategy(processChain, requestStrategies, card)
    }

    /**
     * 真实处理请求代理
     *
     * @param processChain      请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     请求卡片类型
     * @return 是否调用成功
    </RequestCard> */
    protected open fun <RequestCard> handleRealRequestStrategy(
        processChain: ProcessChain,
        requestStrategies: SparseArray<Request<RequestCard>>,
        card: RequestCard
    ): Boolean {
        // 拿到当前进度对应的请求
        val request = requestStrategies[processChain.getProcess()]
        // 请求不存在，则请求失败
        if (request == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }

        // 状态并非「未发送」，则请求失败
        if (processChain.getCurrentStatus() != ProcessChain.STATUS_NORMAL) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }

        // 请求任务
        processChain.updateCurrentStatus(ProcessChain.STATUS_REQUESTING)
        request.request(card)
        return true
    }

    /**
     * 处理反馈代理
     *
     * @param callback        反馈监听
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 结果model类型
     * @return 是否处理成功
    </ResponseModel> */
    override fun <ResponseModel> handleCallbackStrategy(
        processChain: ProcessChain,
        callback: DataSource.Callback<ResponseModel>?,
        model: ResponseModel,
        message: String?,
        isSuccess: Boolean
    ): Boolean {

        // 当前状态已完成，不做额外反馈处理
        if (processChain.getCurrentStatus() == ProcessChain.STATUS_COMPLETED) {
            return false
        }

        // 当前状态是默认，意味着流程错误，不往下执行
        return if (processChain.getCurrentStatus() == ProcessChain.STATUS_NORMAL) {
            false
        } else handleRealCallbackStrategy(
            processChain, callback,
            model, message, isSuccess
        )

        // 真实执行
    }

    /**
     * 真实处理反馈代理
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param model           数据
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 反馈model类型
     * @return 是否处理成功
    </ResponseModel> */
    protected open fun <ResponseModel> handleRealCallbackStrategy(
        processChain: ProcessChain,
        callback: DataSource.Callback<ResponseModel>?,
        model: ResponseModel,
        message: String?,
        isSuccess: Boolean
    ): Boolean {
        // 状态并非「发送中」，则反馈执行失败
        if (processChain.getCurrentStatus() != ProcessChain.STATUS_REQUESTING) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }
        processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
        handleCallback(callback, model, message, isSuccess)
        return true
    }

    /**
     * 处理反馈结果
     *
     * @param callback        反馈监听器
     * @param model           反馈Model
     * @param message         描述信息
     * @param isSuccess       是否为成功数据反馈
     * @param <ResponseModel> 反馈数据类型
    </ResponseModel> */
    protected open fun <ResponseModel> handleCallback(
        callback: DataSource.Callback<ResponseModel>?,
        model: ResponseModel, message: String?,
        isSuccess: Boolean
    ) {
        if (isSuccess) {
            callback?.onDataLoaded(model)
            return
        }
        callback?.onDataNotAvailable(message)
    }
}