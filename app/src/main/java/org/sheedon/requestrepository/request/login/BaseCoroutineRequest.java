package org.sheedon.requestrepository.request.login;

import org.sheedon.rrouter.core.AbstractRequestProxy;
import org.sheedon.rrouter.core.DataSource;
import org.sheedon.rrouter.strategy.BaseRequestStrategyFactory;

import kotlinx.coroutines.CoroutineScope;

/**
 * 基础请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 1:50 下午
 */
public abstract class BaseCoroutineRequest<RequestCard, ResponseModel> {

    // 反馈监听器
    protected DataSource.Callback<ResponseModel> callback;
    // 请求代理
    protected AbstractRequestProxy<RequestCard, ResponseModel> proxy;

    public BaseCoroutineRequest(DataSource.Callback<ResponseModel> callback,
                                CoroutineScope coroutineScope) {
        this.callback = callback;
        proxy = new AbstractRequestProxy<RequestCard,
                ResponseModel>(createRequestStrategyFactory(coroutineScope),
                createProxyCallback()) {
            @Override
            protected RequestCard onCreateRequestCard() {
                return loadRequestCard();
            }
        };
    }

    /**
     * 加载请求卡片
     */
    protected abstract RequestCard loadRequestCard();

    /**
     * 创建请求策略工厂
     * @param coroutineScope
     */
    protected abstract BaseRequestStrategyFactory<RequestCard, ResponseModel>
    createRequestStrategyFactory(CoroutineScope coroutineScope);


    /**
     * 创建代理反馈监听器
     */
    protected DataSource.Callback<ResponseModel> createProxyCallback() {
        return new DataSource.Callback<ResponseModel>() {
            @Override
            public void onDataNotAvailable(String message) {
                if (callback != null)
                    callback.onDataNotAvailable(message);
            }

            @Override
            public void onDataLoaded(ResponseModel responseModel) {
                if (callback != null)
                    callback.onDataLoaded(responseModel);
            }
        };
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (proxy != null) {
            proxy.onDestroy();
        }
        proxy = null;
        callback = null;
    }
}
