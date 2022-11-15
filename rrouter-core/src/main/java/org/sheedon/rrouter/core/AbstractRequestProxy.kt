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
import java.lang.NullPointerException

/**
 * 抽象请求代理类,数据请求统一通过该类代为执行，请求模块解耦，
 * 业务模块只需要处理「请求参数」和「反馈结果」，无需考虑数据采用什么方式获取。
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 8:05 下午
 */
abstract class AbstractRequestProxy<RequestCard, ResponseModel>(
    private var request: RequestFactory<RequestCard, ResponseModel>,// 请求工厂
    private var callback: DataSource.Callback<ResponseModel>?// 反馈监听器
) : RequestProxy.RequestProxyFactory() {

    // 真实调度的请求组
    private var requestStrategies: SparseArray<Request<RequestCard>>?

    // 进度
    private var chain: ProcessChain?

    // 请求卡片
    private var requestCard: RequestCard? = null

    private val strategyCallback by lazy {
        StrategyInternalCallback()
    }

    init {

        // 通过请求工厂创建真实请求策略集合
        requestStrategies = request.createRequestStrategies(strategyCallback)
        if (requestStrategies!!.size() == 0) {
            throw NullPointerException("requestStrategies is null or empty!")
        }
        val handler = bindStrategyHandler()

        // 加载请求策略,填充流程链，设置状态为未开始
        val strategyArray = handler.loadRequestProcess(request.onLoadRequestStrategyType())
        chain = ProcessChain(*strategyArray!!)
    }

    /**
     * 绑定策略执行者
     */
    final override fun bindStrategyHandler(): StrategyHandle.Responsibilities {
        val repository: ConfigRepository = RRouter.getInstance().getConfigRepository()
        return repository.getStrategyHandler()
    }

    /**
     * 执行请求操作，需要任意满足以下两个条件之一
     * 1. 上一次请求调度完成
     * 2. 这次请求数据与上一次的请求数据不一致
     *
     *
     *
     * 需要请求的条件下
     * 1. 重置状态
     * 2. 核实并且填充请求类 RequestCard
     * 3. 执行调度
     *
     *
     */
    override fun request() {
        if (chain!!.getCurrentStatus() == ProcessChain.STATUS_REQUESTING) {
            // 当前进度请求中，则核实是否数据是否更改，更改才重新请求
            val requestCard = onCreateRequestCard()
            if (requestCard != this.requestCard) {
                request.onCancel()
            } else {
                return
            }
        }

        // 重置进度
        chain!!.reset()
        // 其他流程 - 未开始/已完成
        // 核实拿到 requestCard
        checkRequestCard()
        // 请求执行
        requestDispatch()
    }

    /**
     * 获取请求卡片，并且copy到当前requestCard
     */
    @Suppress("UNCHECKED_CAST")
    private fun checkRequestCard() {
        val requestCard = onCreateRequestCard()
        if (requestCard is DataCloneable) {
            try {
                this.requestCard = (requestCard as DataCloneable).clone() as RequestCard
            } catch (e: CloneNotSupportedException) {
                this.requestCard = requestCard
            }
        } else {
            this.requestCard = requestCard
        }
    }

    /**
     * 策略执行器 根据请求策略方式，执行请求操作
     */
    private fun requestDispatch() {
        val handler = bindStrategyHandler()
        val strategyType = request.onLoadRequestStrategyType()

        // 获取当前状态
        val isSuccess = handler.handleRequestStrategy(
            strategyType, chain!!,
            requestStrategies!!, requestCard!!
        )
        if (!isSuccess) {
            strategyCallback.onDataNotAvailable("request failure")
        }
    }

    /**
     * 创建请求Card
     * 由创建的请求类中动态添加
     */
    protected abstract fun onCreateRequestCard(): RequestCard

    /**
     * 策略反馈监听器
     */
    inner class StrategyInternalCallback : StrategyCallback<ResponseModel> {
        /**
         * 数据加载成功
         * @param t 反馈数据
         */
        override fun onDataLoaded(t: ResponseModel) {
            handle(t, "", true, chain)
        }

        /**
         * 数据加载失败
         * @param message 描述信息
         */
        override fun onDataNotAvailable(message: String?) {
            handle(null, message, false, chain)
        }

        /**
         * 反馈结果处理
         * @param responseModel 反馈数据Model
         * @param message 描述信息
         * @param isSuccess 是否成功
         */
        private fun handle(
            responseModel: ResponseModel?, message: String?, isSuccess: Boolean,
            chain: ProcessChain?
        ) {
            if (callback == null) return


            // 加载策略执行者，无执行者，则直接反馈给callback，否则提交给执行器去执行
            val handler = bindStrategyHandler()

            // 加载请求策略类型
            val type = request.onLoadRequestStrategyType()


            // 执行反馈处理
            val handleSuccess = handler.handleCallbackStrategy(
                type, chain!!,
                callback, responseModel!!, message, isSuccess
            )

            // 当前状态为完成，则代表执行完成
            if (chain.getCurrentStatus() == ProcessChain.STATUS_COMPLETED) {
                return
            }

            // 执行未完成，执行下一个
            if (handleSuccess) {
                requestDispatch()
                return
            }

            // 无执行器执行
            notifyCallback(isSuccess, responseModel, message)
        }

        private fun notifyCallback(
            isSuccess: Boolean,
            responseModel: ResponseModel,
            message: String?
        ) {
            // 无执行器执行
            if (isSuccess) {
                callback?.onDataLoaded(responseModel)
            } else {
                callback?.onDataNotAvailable(message)
            }
        }
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        request.onDestroy()
        callback = null
        requestStrategies = null
        chain = null
    }
}