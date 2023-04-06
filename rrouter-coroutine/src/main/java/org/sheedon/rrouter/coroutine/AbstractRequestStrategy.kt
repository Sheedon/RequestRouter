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
package org.sheedon.rrouter.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import org.sheedon.rrouter.core.*

/**
 * 抽象请求策略支持
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 10:37 下午
 */
abstract class AbstractRequestStrategy<RequestCard, ResponseModel>(
    protected val coroutineScope: CoroutineScope,
    protected var callback: StrategyCallback<ResponseModel>?
) : Request<RequestCard> {

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
    override fun request(requestCard: RequestCard?) {
        val job = disposable
        if (job != null && !job.isCompleted) {
            job.cancel()
        }

        disposable = coroutineScope.async(Dispatchers.IO) {
            try {
                val rspModel = onLoadMethod(requestCard)

                if (callback == null) return@async
                if (rspModel == null) {
                    callback?.onDataNotAvailable(errorMessage)
                    return@async
                }
                val iRspModel: IRspModel<*> = factory!!.convert(rspModel)
                if (iRspModel.checkSuccess()) {
                    callback?.onDataLoaded(rspModel)
                    return@async
                }
                val message = iRspModel.loadMessage()
                callback?.onDataNotAvailable(message)
                onSuccessComplete()
            } catch (e: Exception) {
                if (e.message?.contains(CODE_CANCEL) == true) {
                    callback?.onDataNotAvailable("")
                } else {
                    callback?.onDataNotAvailable(e.message)
                }
            }

        }
    }

    /**
     * 加载API 方法
     */
    protected abstract suspend fun onLoadMethod(requestCard: RequestCard?): ResponseModel?

    /**
     * 成功返回结果
     */
    protected open fun onSuccessComplete() {}

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

    companion object {
        // 取消code
        const val CODE_CANCEL = " 600 "
    }
}