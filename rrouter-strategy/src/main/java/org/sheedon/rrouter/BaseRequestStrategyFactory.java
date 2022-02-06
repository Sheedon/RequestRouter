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
package org.sheedon.rrouter;

import android.util.SparseArray;

import androidx.annotation.Nullable;

import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;

/**
 * 基础请求策略实现工厂
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:55 上午
 */
public class BaseRequestStrategyFactory<RequestCard, ResponseModel>
        extends RequestFactory<RequestCard, ResponseModel> {

    // 请求策略
    private SparseArray<Request<RequestCard>> requestStrategies;


    public BaseRequestStrategyFactory() {
    }

    @Nullable
    @Override
    public SparseArray<Request<RequestCard>> createRequestStrategies(
            StrategyCallback<ResponseModel> callback) {
        if (requestStrategies == null) {
            requestStrategies = new SparseArray<>();

            requestStrategies.put(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST,
                    onCreateRealLocalRequestStrategy(callback));
            requestStrategies.put(StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST,
                    onCreateRealRemoteRequestStrategy(callback));
        }
        return requestStrategies;
    }

    /**
     * 加载请求策略类型
     * 由实际创建的请求策略提供策略类型
     * 例如 {@link org.sheedon.repository.DefaultStrategyHandler.STRATEGY}
     *
     * @return 策略类型
     */
    @Override
    public int onLoadRequestStrategyType() {
        return StrategyConfig.STRATEGY.TYPE_ONLY_REMOTE;
    }


    /**
     * 创建真实的本地请求策略
     *
     * @param callback 反馈监听器
     * @return Request<RequestCard, ResponseModel>
     */
    protected Request<RequestCard> onCreateRealLocalRequestStrategy(
            StrategyCallback<ResponseModel> callback) {
        return null;
    }

    /**
     * 创建真实的网络请求策略
     *
     * @param callback 反馈监听器
     * @return Request<RequestCard, ResponseModel>
     */
    protected Request<RequestCard> onCreateRealRemoteRequestStrategy(
            StrategyCallback<ResponseModel> callback) {
        return null;
    }

    @Override
    public void onCancel() {
        if (requestStrategies != null) {
            destroyByKey(StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST);
            destroyByKey(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST);
        }
    }

    /**
     * 根据key 销毁请求
     *
     * @param key 请求策略key
     */
    protected void cancelByKey(int key) {
        Request<RequestCard> request = requestStrategies.get(key);
        if (request != null) {
            request.onCancel();
        }
    }

    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        if (requestStrategies != null) {
            destroyByKey(StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST);
            destroyByKey(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST);
            requestStrategies.clear();
        }
        requestStrategies = null;
    }

    /**
     * 根据key 销毁请求
     *
     * @param key 请求策略key
     */
    protected void destroyByKey(int key) {
        Request<RequestCard> request = requestStrategies.get(key);
        if (request != null) {
            request.onDestroy();
            requestStrategies.remove(key);
        }
    }
}
