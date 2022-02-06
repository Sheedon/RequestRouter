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
package org.sheedon.rrouter.strategy;

import android.util.SparseArray;

import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.rrouter.ProcessChain;
import org.sheedon.rrouter.StrategyConfig;
import org.sheedon.rrouter.core.support.Request;

/**
 * 单一本地请求执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:15 下午
 */
public class OnlyLocalStrategyHandler extends BaseStrategyHandler {


    /**
     * 请求流程，单一本地请求
     */
    @Override
    public int[] loadRequestProcess() {
        return new int[]{StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST};
    }


    /**
     * 执行真实请求行为
     * 类型为单一本地请求 {@link StrategyConfig.STRATEGY.TYPE_ONLY_LOCAL}，
     * 且当前状态为默认状态，则获取本地请求代理并做请求，否则请求调度失败
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param requestCard       请求卡片
     * @param <RequestCard>     RequestCard
     * @return 是否请求成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard requestCard) {
        return super.handleRealRequestStrategy(processChain, requestStrategies, requestCard);
    }

    /**
     * 类型为单一本地请求 {@link StrategyConfig.STRATEGY.TYPE_ONLY_LOCAL}，
     * 当前进度为本地请求 {@link ProcessChain.STATUS_REQUESTING}，则执行反馈操作
     * 设置状态 {@link ProcessChain.STATUS_COMPLETED}
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param responseModel   反馈model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 是否反馈成功
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(ProcessChain processChain,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel responseModel, String message,
                                                                 boolean isSuccess) {

        return super.handleRealCallbackStrategy(processChain, callback, responseModel, message, isSuccess);
    }
}
