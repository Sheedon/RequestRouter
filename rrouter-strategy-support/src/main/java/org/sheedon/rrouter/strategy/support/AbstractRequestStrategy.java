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
package org.sheedon.rrouter.strategy.support;

import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;

import io.reactivex.rxjava3.core.Observable;

/**
 * 抽象请求策略支持
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 10:37 下午
 */
public abstract class AbstractRequestStrategy<RequestCard, ResponseModel>
        implements Request<RequestCard> {

    protected StrategyCallback<ResponseModel> callback;

    public AbstractRequestStrategy(StrategyCallback<ResponseModel> callback) {
        this.callback = callback;
    }

    /**
     * 加载API 方法
     */
    protected abstract Observable<ResponseModel> onLoadMethod(RequestCard requestCard);
}
