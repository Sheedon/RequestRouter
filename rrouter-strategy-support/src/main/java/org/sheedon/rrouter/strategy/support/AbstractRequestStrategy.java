package org.sheedon.rrouter.strategy.support;

import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.strategy.model.IRspModel;

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
    protected abstract Observable<IRspModel<ResponseModel>> onLoadMethod(RequestCard requestCard);
}
