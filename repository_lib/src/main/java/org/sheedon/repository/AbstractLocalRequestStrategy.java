package org.sheedon.repository;

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
        return DefaultStrategyHandler.REQUEST.TYPE_LOCAL_REQUEST;
    }
}
