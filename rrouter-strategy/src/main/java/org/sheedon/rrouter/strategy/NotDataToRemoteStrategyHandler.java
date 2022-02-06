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
 * 优先本地，无数据取网络
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:44 下午
 */
public class NotDataToRemoteStrategyHandler extends BaseStrategyHandler {

    @Override
    public int[] loadRequestProcess() {
        return new int[]{StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST,
                StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST};
    }

    /**
     * 类型为优先本地请求，请求错误，才网络请求 {@link StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 第一次请求，则做网络请求，第二次进来走本地请求
     * 且当前状态必须为默认状态，否则请求失败
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     请求卡片
     * @return 是否处理成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard requestCard) {
        return super.handleRealRequestStrategy(processChain, requestStrategies, requestCard);
    }


    /**
     * 类型为优先本地请求，请求错误，才网络请求 {@link StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 当前进度为 {@link ProcessChain.STATUS_REQUESTING}，则执行反馈操作
     * 设置状态 {@link ProcessChain.STATUS_COMPLETED}
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param responseModel   结果model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 结果model类型
     * @return 是否处理成功
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(ProcessChain processChain,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel model, String message,
                                                                 boolean isSuccess) {
        // 状态并非「发送中」，则反馈执行失败
        if (processChain.getCurrentStatus() != ProcessChain.STATUS_REQUESTING) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        if(isSuccess){
            processChain.updateAllStatusToCompleted();
        }else{
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
        }
        if(processChain.isAllCompleted()){
            handleCallback(callback, model, message, isSuccess);
        }
        return true;
    }
}
