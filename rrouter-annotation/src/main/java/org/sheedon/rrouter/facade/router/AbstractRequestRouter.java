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
package org.sheedon.rrouter.facade.router;

import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.facade.annotation.Provider;
import org.sheedon.rrouter.facade.model.Converter;
import org.sheedon.rrouter.facade.model.RequestBodyAdapter;
import org.sheedon.rrouter.strategy.parameter.DefaultStrategy;
import org.sheedon.rrouter.strategy.support.AbstractRequestStrategy;

import io.reactivex.rxjava3.core.Observable;

/**
 * Abstract request router, which defines the request routing standard implementation of the method
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/21 4:39 下午
 */
public class AbstractRequestRouter<RequestCard, ResponseModel> {

    @Provider
    protected AbstractRequestRouter() {

    }

    /**
     * Request strategy type,
     * If you need to use the overloaded method,
     * add an annotation {@link org.sheedon.rrouter.facade.annotation.StrategyType}
     *
     * @StrategyType
     */
    @SuppressWarnings("JavaDoc")
    protected int requestStrategy() {
        return DefaultStrategy.TYPE_ONLY_REMOTE;
    }

    /**
     * Execute "request action" through request data adapter
     * Can only be used in subclasses, modification is not recommended
     *
     * @param adapter Request data adapter
     */
    public void request(RequestBodyAdapter<RequestCard> adapter) {
        if (adapter == null) {
            return;
        }

        RequestCard requestBody = adapter.getRequestBody();
        request(requestBody);
    }

    /**
     * Execute "request action" through request's card
     * Can only be used in subclasses, modification is not recommended
     *
     * @param card RequestCard
     */
    public void request(RequestCard card) {

    }

    /**
     * Build a remote request class
     * If you need to use add annotations {@link org.sheedon.rrouter.facade.annotation.RequestStrategy}
     * remoteRequestClass()/onLoadRemoteMethod() , Only one comment can be added
     *
     * @return AbstractRequestStrategy's Class
     */
    public AbstractRequestStrategy<RequestCard, ResponseModel> remoteRequestClass(StrategyCallback<ResponseModel> callback) {
        return null;
    }

    /**
     * Load remote method scheduling, request card request,
     * feedback the result Observable<IRspModel<ResponseModel>>
     * If you need to use add annotations {@link org.sheedon.rrouter.facade.annotation.RequestStrategy}
     * remoteRequestClass()/onLoadRemoteMethod() , Only one comment can be added
     *
     * @param requestCard request body
     * @return Observable<IRspModel < ResponseModel>>
     */
    public Observable<ResponseModel> onLoadRemoteMethod(RequestCard requestCard) {
        return null;
    }


    /**
     * Build a local request class
     * If you need to use add annotations {@link org.sheedon.rrouter.facade.annotation.RequestStrategy}
     * localRequestClass()/onLoadLocalMethod() , Only one comment can be added
     *
     * @return AbstractRequestStrategy's Class
     */
    public AbstractRequestStrategy<RequestCard, ResponseModel> localRequestClass(StrategyCallback<ResponseModel> callback) {
        return null;
    }

    /**
     * Load local method scheduling, request card request,
     * feedback the result Observable<IRspModel<ResponseModel>>
     * If you need to use add annotations {@link org.sheedon.rrouter.facade.annotation.RequestStrategy}
     * localRequestClass()/onLoadLocalMethod() , Only one comment can be added
     *
     * @param requestCard request body
     * @return Observable<IRspModel < ResponseModel>>
     */
    public Observable<ResponseModel> onLoadLocalMethod(RequestCard requestCard) {
        return null;
    }

    /**
     * request data adapter method
     * If you need to use add annotations {@link org.sheedon.rrouter.facade.annotation.RequestDataAdapter}
     */
    public RequestBodyAdapter<RequestCard> requestAdapter() {
        return null;
    }

    /**
     * Result conversion method
     * If you need to use add annotations {@link org.sheedon.rrouter.facade.annotation.CallbackDataAdapter}
     *
     * @param model    Feedback result
     * @param <Result> Results to be displayed
     * @return Converter<ResponseModel, ?>
     */
    protected Converter<ResponseModel, ?> convertAdapter() {
        return null;
    }

    /**
     * Response result handler, operate and process data uniformly in the routing class,
     * so as not to repeatedly write consistent logic in all objects that hold the routing class
     */
    public IResponseDispatcher<ResponseModel> dispatcher() {
        return null;
    }

    /**
     * destroy request action
     */
    public void onDestroy() {
    }
}
