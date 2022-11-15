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

import kotlinx.coroutines.*
import org.sheedon.rrouter.core.Converter
import org.sheedon.rrouter.core.IRspModel
import org.sheedon.rrouter.core.RRouter
import org.sheedon.rrouter.core.StrategyCallback

/**
 * 基础请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:33 上午
 */
abstract class BaseRequestStrategy<RequestCard, ResponseModel>(
    private val coroutineScope: CoroutineScope,
    callback: StrategyCallback<ResponseModel>?
) : AbstractRequestStrategy<RequestCard, ResponseModel>(callback) {
    private var disposable: Job? = null
    private var factory: Converter<Any, IRspModel<*>>?
    private var errorMessage: String

    init {
        @Suppress("UNCHECKED_CAST")
        factory = RRouter.getInstance().rspConverter as Converter<Any, IRspModel<*>>
        errorMessage = RRouter.getInstance().configRepository.getErrorMessage()
    }

    /**
     * 请求操作
     *
     * @param requestCard 请求卡片
     */
    override fun request(requestCard: RequestCard) {
        val job = disposable
        if (job != null && !job.isCompleted) {
            job.cancel()
        }

        val launch = coroutineScope.async(Dispatchers.IO) {
            try {
                val rspModel = onLoadMethod(requestCard)

                if (callback == null) return@async
                if (rspModel == null) {
                    callback?.onDataNotAvailable(errorMessage)
                    return@async
                }
                val iRspModel: IRspModel<*> = factory!!.convert(rspModel)
                if (iRspModel.isSuccess()) {
                    callback?.onDataLoaded(rspModel)
                    return@async
                }
                val message = iRspModel.getMessage()
                callback?.onDataNotAvailable(message)
                onSuccessComplete()
            } catch (e: Exception) {
                callback?.onDataNotAvailable(e.message)
            }

        }
    }

    /**
     * 成功返回结果
     */
    protected fun onSuccessComplete() {}

    /**
     * 取消
     */
    override fun onCancel() {
        val job = disposable
        if (job != null && !job.isCompleted) {
            job.cancel()
        }
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        callback = null
        factory = null
        val job = disposable
        if (job != null && !job.isCompleted) {
            job.cancel()
        }
        disposable = null
    }
}