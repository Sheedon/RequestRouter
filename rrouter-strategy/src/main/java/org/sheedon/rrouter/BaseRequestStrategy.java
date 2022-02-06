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

import android.content.Context;

import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.strategy.support.AbstractRequestStrategy;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 基础请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:33 上午
 */
public abstract class BaseRequestStrategy<RequestCard, ResponseModel>
        extends AbstractRequestStrategy<RequestCard, ResponseModel>{

    private Disposable disposable;
    private Context context;

    public BaseRequestStrategy(StrategyCallback<ResponseModel> callback) {
        super(callback);
        this.context = RRouter.getInstance().getContext();
    }

    /**
     * 请求操作
     *
     * @param requestCard 请求卡片
     */
    @Override
    public void request(RequestCard requestCard) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = onLoadMethod(requestCard)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rspModel -> {
                    if (callback == null)
                        return;

                    if (rspModel == null) {
                        callback.onDataNotAvailable(context.getString(org.sheedon.rrouter.strategy.R.string.data_back_error));
                        return;
                    }

                    if (rspModel.isSuccess()) {
                        callback.onDataLoaded(rspModel.getData());
                        return;
                    }

                    callback.onDataNotAvailable(rspModel.getMessage());
                    onSuccessComplete();
                }, throwable -> {
                    if (callback == null)
                        return;

                    callback.onDataNotAvailable(throwable.getMessage());
                });
    }

    /**
     * 成功返回结果
     */
    protected void onSuccessComplete() {

    }

    /**
     * 取消
     */
    @Override
    public void onCancel() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        callback = null;
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = null;
        context = null;
    }
}
