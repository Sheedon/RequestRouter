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
package org.sheedon.rrouter.rxjava

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import org.sheedon.rrouter.core.*

/**
 * 抽象请求策略支持
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 10:37 下午
 */
abstract class AbstractRequestStrategy<RequestCard, ResponseModel>(
    protected var callback: StrategyCallback<ResponseModel>?
) : Request<RequestCard> {

    private var disposable: Disposable? = null
    private var factory: Converter<ResponseModel, IRspModel<*>>?
    private var errorMessage: String

    init {
        @Suppress("UNCHECKED_CAST")
        factory = RRouter.getInstance().rspConverter as Converter<ResponseModel, IRspModel<*>>
        errorMessage = RRouter.getInstance().configRepository.getErrorMessage()
    }

    /**
     * 请求操作
     *
     * @param requestCard 请求卡片
     */
    @Suppress("UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS",
        "UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS"
    )
    override fun request(requestCard: RequestCard) {
        val job = disposable
        if (job != null && !job.isDisposed) {
            job.dispose()
        }

        disposable = onLoadMethod(requestCard)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Consumer<ResponseModel> {
                override fun accept(rspModel: ResponseModel) {
                    if (callback == null) return

                    val iRspModel: IRspModel<*> = factory!!.convert(rspModel)

                    if (iRspModel.isSuccess()) {
                        callback?.onDataLoaded(rspModel)
                    }else{
                        val message = iRspModel.getMessage()
                        callback?.onDataNotAvailable(message)
                    }
                    onSuccessComplete()
                }

            }) {
                callback?.onDataNotAvailable(it.message)
            }
    }

    /**
     * 加载API 方法
     */
    @Suppress("UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS")
    protected abstract fun onLoadMethod(requestCard: RequestCard): Observable<ResponseModel>

    /**
     * 成功返回结果
     */
    protected open fun onSuccessComplete() {}

    /**
     * 取消
     */
    override fun onCancel() {
        val job = disposable
        if (job != null && !job.isDisposed) {
            job.dispose()
        }
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        callback = null
        factory = null
        val job = disposable
        if (job != null && !job.isDisposed) {
            job.dispose()
        }
        disposable = null
    }
}