package org.sheedon.repository;

import org.sheedon.repository.strategy.StrategyConfig;

/**
 * 默认本地请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:36 上午
 */
public abstract class AbstractLocalRequestStrategy<RequestCard, ResponseModel>
        extends BaseRequestStrategy<RequestCard, ResponseModel> {

    public AbstractLocalRequestStrategy(StrategyHandle.StrategyCallback<ResponseModel> callback) {
        super(callback);
    }

    /**
     * 请求类型 - 本地请求
     */
    @Override
    public int onRequestType() {
        return StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST;
    }

    @Override
    protected int onProgressType() {
        return StrategyConfig.PROGRESS.REQUEST_LOCAL;
    }
}