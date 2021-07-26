package org.sheedon.repository.strategy;

import android.util.SparseArray;

import org.sheedon.repository.Request;
import org.sheedon.repository.StrategyHandle;
import org.sheedon.repository.data.DataSource;

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
     * @param <RequestCard>     请求卡片
     * @return 是否处理成功
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(int progress,
                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                       RequestCard card, ProgressCallback callback) {
        return handleRealRequestStrategy(progress, requestStrategies, card, callback);
    }

    /**
     * 真实处理请求代理
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     请求卡片类型
     * @return 是否调用成功
     */
    protected abstract <RequestCard> boolean handleRealRequestStrategy(int progress,
                                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                                       RequestCard card, ProgressCallback callback);

    /**
     * 处理反馈代理
     *
     * @param currentProgress  当前进度
     * @param callbackProgress 反馈进度
     * @param callback         反馈监听
     * @param responseModel    结果model
     * @param message          描述信息
     * @param isSuccess        是否请求成功
     * @param progressCallback 进度监听器
     * @param <ResponseModel>  结果model类型
     * @return 是否处理成功
     */
    @Override
    public <ResponseModel> boolean handleCallbackStrategy(int currentProgress, int callbackProgress,
                                                          DataSource.Callback<ResponseModel> callback,
                                                          ResponseModel responseModel, String message,
                                                          boolean isSuccess, ProgressCallback progressCallback) {

        // 当前状态已完成，或者 callback 为空，直接返回状态，不做额外反馈处理
        if (currentProgress == StrategyConfig.PROGRESS.COMPLETE) {
            progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.COMPLETE);
            return false;
        }

        // 错误处理直接反馈错误信息，并返回 COMPLETE
        if (currentProgress == StrategyConfig.PROGRESS.ERROR) {
            progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.COMPLETE);
            callback.onDataNotAvailable(message);
            return false;
        }

        // 真实执行
        return handleRealCallbackStrategy(currentProgress, callbackProgress, callback,
                responseModel, message, isSuccess, progressCallback);
    }

    /**
     * 真实处理反馈代理
     *
     * @param currentProgress  当前进度
     * @param callbackProgress 反馈进度
     * @param callback         反馈监听
     * @param responseModel    反馈结果
     * @param message          描述信息
     * @param isSuccess        是否请求成功
     * @param progressCallback 进度反馈监听器
     * @param <ResponseModel>  反馈model类型
     * @return 是否处理成功
     */
    protected abstract <ResponseModel>
    boolean handleRealCallbackStrategy(int currentProgress, int callbackProgress,
                                       DataSource.Callback<ResponseModel> callback,
                                       ResponseModel responseModel, String message,
                                       boolean isSuccess, ProgressCallback progressCallback);

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
