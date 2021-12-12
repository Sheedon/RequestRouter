package org.sheedon.rrouter;

import org.sheedon.rrouter.core.support.StrategyCallback;

/**
 * 网络请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:41 上午
 */
public abstract class AbstractRemoteRequestStrategy<RequestCard, ResponseModel>
        extends BaseRequestStrategy<RequestCard, ResponseModel> {


    public AbstractRemoteRequestStrategy(StrategyCallback<ResponseModel> callback) {
        super(callback);
    }

    /**
     * 请求类型 - 网络请求
     */
    @Override
    public int onRequestType() {
        return StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST;
    }
}
