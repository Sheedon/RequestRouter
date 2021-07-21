package org.sheedon.repository;

import org.sheedon.repository.strategy.StrategyConfig;

/**
 * 网络请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:41 上午
 */
public abstract class AbstractNetRequestStrategy<RequestCard, ResponseModel>
        extends BaseRequestStrategy<RequestCard, ResponseModel> {


    public AbstractNetRequestStrategy(StrategyHandle.StrategyCallback<ResponseModel> callback) {
        super(callback);
    }

    /**
     * 请求类型 - 网络请求
     */
    @Override
    public int onRequestType() {
        return StrategyConfig.REQUEST.TYPE_NETWORK_REQUEST;
    }

    @Override
    protected int onProgressType() {
        return StrategyConfig.PROGRESS.REQUEST_DOING;
    }
}
