package org.sheedon.repository;

import org.sheedon.repository.data.DataSource;
import org.sheedon.repository.strategy.StrategyConfig;
import org.sheedon.repository.strategy.StrategyHandlerFactory;

import java.util.Queue;

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

    @SafeVarargs
    @Override
    public final <RequestCard> Queue<Request<RequestCard>> loadRequestQueue(int strategyType,
                                                                            Request<RequestCard>... requests) {
        if (requests == null || requests.length == 0)
            return null;

        StrategyHandle handler = handlerFactory.loadStrategyHandler(strategyType);

        return handler.loadRequestQueue(requests);
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
                                                       Queue<Request<RequestCard>> requestStrategies,
                                                       RequestCard card) {

        StrategyHandle handler = handlerFactory.loadStrategyHandler(requestStrategyType);
        if (handler == null) {
            return false;
        }

        return handler.handleRequestStrategy(requestStrategies, card);
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
    public <RequestCard, ResponseModel> int handleCallbackStrategy(int requestStrategyType,
                                                                   Queue<Request<RequestCard>> requestStrategies,
                                                                   DataSource.Callback<ResponseModel> callback,
                                                                   ResponseModel responseModel, String message,
                                                                   boolean isSuccess) {

        if (callback == null) {
            throw new NullPointerException("callback is null");
        }

        StrategyHandle handler = handlerFactory.loadStrategyHandler(requestStrategyType);
        if (handler == null) {
            return StrategyConfig.PROGRESS.ERROR;
        }

        return handler.handleCallbackStrategy(requestStrategies, callback, responseModel,
                message, isSuccess);
    }
}
