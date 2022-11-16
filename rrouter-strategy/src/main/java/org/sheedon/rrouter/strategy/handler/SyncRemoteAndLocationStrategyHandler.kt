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
 * 同步请求网络和本地策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:48 下午
 */
class SyncRemoteAndLocationStrategyHandler : BaseStrategyHandler() {
    private val lock = Any()

    /**
     * 请求流程，本地/网络同时访问
     */
    override fun loadRequestProcess(): IntArray {
        return intArrayOf(
            StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST,
            StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST
        )
    }

    /**
     * 类型为本地网络同步请求 [StrategyConfig.STRATEGY.TYPE_SYNC_REMOTE_AND_LOCATION]，
     * 则依次本地网络请求，设置进度
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param card       请求卡片
     * @param <RequestCard>     RequestCard
     * @return 是否请求成功
    </RequestCard> */
    override fun <RequestCard> handleRealRequestStrategy(
        processChain: ProcessChain,
        requestStrategies: SparseArray<Request<RequestCard>>,
        card: RequestCard
    ): Boolean {
        // 拿到当前进度对应的请求
        val localRequest = requestStrategies[StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST]
        val netRequest = requestStrategies[StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST]
        // 请求不存在，则请求失败
        if (localRequest == null && netRequest == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }

        // 状态并非「未发送」，则请求失败
        if (processChain.getStatus(0) != ProcessChain.STATUS_NORMAL
            && processChain.getStatus(1) != ProcessChain.STATUS_NORMAL
        ) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED)
            return false
        }
        request(localRequest, processChain, 0, card)
        request(netRequest, processChain, 1, card)
        return true
    }

    /**
     * 执行请求操作
     *
     * @param request       请求项
     * @param processChain  流程链
     * @param index         任务坐标
     * @param requestCard   请求卡片
     * @param <RequestCard> 请求类型
    </RequestCard> */
    private fun <RequestCard> request(
        request: Request<RequestCard>?,
        processChain: ProcessChain, index: Int,
        requestCard: RequestCard
    ) {
        if (request != null && processChain.getStatus(index) == ProcessChain.STATUS_NORMAL) {
            // 请求任务
            processChain.updateOfIndex(index, ProcessChain.STATUS_REQUESTING)
            request.request(requestCard)
        }
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
        model: ResponseModel?,
        message: String?,
        isSuccess: Boolean
    ): Boolean {
        // 当前状态是默认，意味着流程错误，不往下执行
        return if (processChain.getCurrentStatus() == ProcessChain.STATUS_NORMAL) {
            false
        } else handleRealCallbackStrategy(
            processChain, callback,
            model, message, isSuccess
        )
    }

    /**
     * 类型为并行请求 [StrategyConfig.STRATEGY.TYPE_SYNC_REMOTE_AND_LOCATION]，
     * 两者都是完成，则反馈失败，状态为提交中，则返回数据
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param model   反馈model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 反馈类型
     * @return 是否反馈成功
    </ResponseModel> */
    override fun <ResponseModel> handleRealCallbackStrategy(
        processChain: ProcessChain,
        callback: DataSource.Callback<ResponseModel>?,
        model: ResponseModel?, message: String?,
        isSuccess: Boolean
    ): Boolean {
        synchronized(lock) {
            return if (handleCallback(
                    processChain, 0, callback,
                    model, message, isSuccess
                )
            ) {
                true
            } else handleCallback(
                processChain, 1, callback,
                model, message, isSuccess
            )
        }
    }

    /**
     * 反馈处理
     *
     * @param processChain    流程链
     * @param index           坐标
     * @param callback        反馈持有者
     * @param responseModel   反馈内容
     * @param message         消息
     * @param isSuccess       是否成功
     * @param <ResponseModel> 反馈类型
     * @return 是否反馈成功
    </ResponseModel> */
    private fun <ResponseModel> handleCallback(
        processChain: ProcessChain, index: Int,
        callback: DataSource.Callback<ResponseModel>?,
        responseModel: ResponseModel?, message: String?,
        isSuccess: Boolean
    ): Boolean {
        if (processChain.getStatus(index) == ProcessChain.STATUS_REQUESTING) {
            processChain.updateOfIndex(index, ProcessChain.STATUS_COMPLETED)
            handleCallback(callback, responseModel, message, isSuccess)
            return true
        }
        return false
    }
}