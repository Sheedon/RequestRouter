package org.sheedon.repository;

/**
 * 基础请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:33 上午
 */
public abstract class BaseRequestStrategy<RequestCard, ResponseModel>
        implements Request<RequestCard> {

    protected StrategyHandle.StrategyCallback<ResponseModel> callback;

    public BaseRequestStrategy(StrategyHandle.StrategyCallback<ResponseModel> callback) {
        this.callback = callback;
    }

    @Override
    public void onDestroy() {
        callback = null;
    }
}
