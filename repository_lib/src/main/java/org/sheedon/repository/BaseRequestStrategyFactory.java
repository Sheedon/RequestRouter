package org.sheedon.repository;

import androidx.annotation.Nullable;

import org.sheedon.repository.strategy.StrategyConfig;

import java.util.Queue;

/**
 * 基础请求策略实现工厂
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:55 上午
 */
public class BaseRequestStrategyFactory<RequestCard, ResponseModel>
        extends Request.Factory<RequestCard, ResponseModel> {

    // 请求策略
    private Queue<Request<RequestCard>> requestStrategiesQueue;


    public BaseRequestStrategyFactory() {
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Queue<Request<RequestCard>> createRequestStrategies(
            StrategyHandle.StrategyCallback<ResponseModel> callback,
            StrategyHandle.Responsibilities handler, int strategyType) {
        if (requestStrategiesQueue == null) {
            requestStrategiesQueue = handler.loadRequestQueue(strategyType, onCreateRealLocalRequestStrategy(callback),
                    onCreateRealNetworkRequestStrategy(callback));
        }
        return requestStrategiesQueue;
    }


    @Nullable
    @Override
    public Queue<Request<RequestCard>> onGetRequestStrategies() {
        return requestStrategiesQueue;
    }

    /**
     * 加载请求策略类型
     * 由实际创建的请求策略提供策略类型
     * 例如 {@link com.landeng.data_repository_lib.DefaultStrategyHandler.STRATEGY}
     *
     * @return 策略类型
     */
    @Override
    public int onLoadRequestStrategyType() {
        return StrategyConfig.STRATEGY.TYPE_ONLY_NETWORK;
    }


    /**
     * 创建真实的本地请求策略
     *
     * @param callback 反馈监听器
     * @return Request<RequestCard, ResponseModel>
     */
    protected Request<RequestCard> onCreateRealLocalRequestStrategy(
            StrategyHandle.StrategyCallback<ResponseModel> callback) {
        return null;
    }

    /**
     * 创建真实的网络请求策略
     *
     * @param callback 反馈监听器
     * @return Request<RequestCard, ResponseModel>
     */
    protected Request<RequestCard> onCreateRealNetworkRequestStrategy(
            StrategyHandle.StrategyCallback<ResponseModel> callback) {
        return null;
    }

    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        if (requestStrategiesQueue == null)
            return;

        if (requestStrategiesQueue.size() == 0) {
            requestStrategiesQueue = null;
            return;
        }

        Request<RequestCard> request;
        while ((request = requestStrategiesQueue.poll()) != null) {
            request.onDestroy();
        }
        requestStrategiesQueue = null;
    }
}
