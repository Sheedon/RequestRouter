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

import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.strategy.model.IRspModel;

import io.reactivex.rxjava3.core.Observable;

/**
 * 默认空请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 11:07 下午
 */
public class NullRequestStrategy extends AbstractRequestStrategy<Object, Object> {
    public NullRequestStrategy(StrategyCallback<Object> callback) {
        super(callback);
    }

    @Override
    protected Observable<IRspModel<Object>> onLoadMethod(Object o) {
        return null;
    }


    @Override
    public void request(Object o) {

    }

    @Override
    public int onRequestType() {
        return 0;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onDestroy() {

    }
}
