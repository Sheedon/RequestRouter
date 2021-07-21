package org.sheedon.repository.strategy;


import org.sheedon.repository.Request;
import org.sheedon.repository.StrategyHandle;
import org.sheedon.repository.data.DataSource;

import java.util.Queue;


/**
 * 基础策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:33 下午
 */
public abstract class BaseStrategyHandler implements StrategyHandle {


    /**
     * 处理请求代理
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @return 是否调用成功
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(Queue<Request<RequestCard>> requestStrategies,
                                                       RequestCard card) {
        return handleRealRequestStrategy(requestStrategies, card);
    }

    /**
     * 真实处理请求代理
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @return 是否调用成功
     */
    protected abstract <RequestCard>
    boolean handleRealRequestStrategy(Queue<Request<RequestCard>> requestStrategies,
                                      RequestCard card);

    /**
     * 处理反馈代理
     *
     * @param callback      反馈监听
     * @param responseModel 反馈数据
     * @param message       描述信息
     * @param isSuccess     是否请求成功
     */
    @Override
    public <RequestCard, ResponseModel>
    int handleCallbackStrategy(Queue<Request<RequestCard>> requestStrategies,
                               DataSource.Callback<ResponseModel> callback,
                               ResponseModel responseModel, String message,
                               boolean isSuccess) {

        return handleRealCallbackStrategy(requestStrategies, callback, responseModel, message, isSuccess);
    }

    /**
     * 真实处理反馈代理
     *
     * @param callback      反馈监听
     * @param responseModel 反馈数据
     * @param message       描述信息
     * @param isSuccess     是否请求成功
     */
    protected abstract <RequestCard, ResponseModel>
    int handleRealCallbackStrategy(Queue<Request<RequestCard>> requestStrategies,
                                    DataSource.Callback<ResponseModel> callback,
                                    ResponseModel responseModel, String message,
                                    boolean isSuccess);

    /**
     * 处理反馈结果
     *
     * @param callback        反馈监听器
     * @param model           反馈Model
     * @param message         描述信息
     * @param isSuccess       是否为成功数据反馈
     * @param <ResponseModel> 反馈数据类型
     */
    protected <ResponseModel> void handleCallback(DataSource.Callback<ResponseModel> callback,
                                                  ResponseModel model, String message,
                                                  boolean isSuccess) {

        if (isSuccess) {
            callback.onDataLoaded(model);
            return;
        }

        callback.onDataNotAvailable(message);
    }
}
