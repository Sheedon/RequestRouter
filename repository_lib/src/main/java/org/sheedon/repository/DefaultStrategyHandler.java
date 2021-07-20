package org.sheedon.repository;


import android.util.SparseArray;

import org.sheedon.repository.data.DataSource;
import org.sheedon.repository.strategy.StrategyConfig;
import org.sheedon.repository.strategy.StrategyHandlerFactory;

/**
 * 默认策略执行者
 * 采用适配器工厂，按类型获取策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 11:57 下午
 */
public final class DefaultStrategyHandler implements StrategyHandle.Responsibilities {

    // 单例
    public static DefaultStrategyHandler HANDLER = new DefaultStrategyHandler();

    // 策略执行工厂
    private final StrategyHandlerFactory handlerFactory;

    private DefaultStrategyHandler() {
        handlerFactory = new StrategyHandlerFactory();
    }

    /**
     * 请求策略执行
     *
     * @param requestStrategyType 策略类型
     * @param progress            请求进度
     * @param requestStrategies   请求策略集合
     * @param card                请求卡片
     * @param callback            反馈监听器
     * @param <RequestCard>       RequestCard
     * @return 执行是否成功
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(int requestStrategyType, int progress,
                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                       RequestCard card, StrategyHandle.ProgressCallback callback) {

        if (callback == null) {
            throw new NullPointerException("callback is null");
        }

        StrategyHandle handler = handlerFactory.loadStrategyHandler(requestStrategyType);
        if (handler == null) {
            callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
            return false;
        }

        return handler.handleRequestStrategy(progress, requestStrategies, card, callback);
    }

    /**
     * 反馈策略待执行方法
     *
     * @param requestStrategyType 策略类型
     * @param currentProgress     当前进度
     * @param callbackProgress    反馈进度
     * @param callback            反馈监听
     * @param model               反馈结果
     * @param message             描述信息
     * @param isSuccess           是否请求成功
     * @param <ResponseModel>     ResponseModel
     * @return 执行完成的进度
     */
    @Override
    public <ResponseModel> boolean handleCallbackStrategy(int requestStrategyType, int currentProgress,
                                                          int callbackProgress,
                                                          DataSource.Callback<ResponseModel> callback,
                                                          ResponseModel responseModel, String message,
                                                          boolean isSuccess,
                                                          StrategyHandle.ProgressCallback progressCallback) {

        if (callback == null) {
            throw new NullPointerException("callback is null");
        }

        if (progressCallback == null) {
            throw new NullPointerException("progressCallback is null");
        }

        StrategyHandle handler = handlerFactory.loadStrategyHandler(requestStrategyType);
        if (handler == null) {
            progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
            return false;
        }

        return handler.handleCallbackStrategy(currentProgress, callbackProgress, callback, responseModel,
                message, isSuccess, progressCallback);
    }
}
