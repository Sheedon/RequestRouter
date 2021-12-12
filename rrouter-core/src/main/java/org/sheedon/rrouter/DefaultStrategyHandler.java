package org.sheedon.rrouter;

import android.util.SparseArray;

import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.rrouter.core.support.Request;

/**
 * 默认策略执行者
 * 采用适配器工厂，按类型获取策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 6:56 下午
 */
final class DefaultStrategyHandler extends StrategyHandle.ResponsibilityFactory {

    // 单例
    public static DefaultStrategyHandler HANDLER = new DefaultStrategyHandler();

    // 策略执行工厂
    private StrategyHandle.Factory handlerFactory;

    private DefaultStrategyHandler() {

    }

    @Override
    public void setHandlerFactory(StrategyHandle.Factory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    @Override
    public int[] loadRequestProcess(int strategyType) {
        StrategyHandle handler = handlerFactory.loadStrategyHandler(strategyType);
        return handler.loadRequestProcess();
    }

    /**
     * 请求策略执行
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     RequestCard
     * @return 执行是否成功
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(int requestStrategyType,
                                                       ProcessChain processChain,
                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                       RequestCard card) {


        StrategyHandle handler = handlerFactory.loadStrategyHandler(requestStrategyType);
        if (handler == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        return handler.handleRequestStrategy(processChain, requestStrategies, card);
    }

    /**
     * 反馈策略待执行方法
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 执行完成的进度
     */
    @Override
    public <ResponseModel> boolean handleCallbackStrategy(int requestStrategyType,
                                                          ProcessChain processChain,
                                                          DataSource.Callback<ResponseModel> callback,
                                                          ResponseModel responseModel, String message,
                                                          boolean isSuccess) {
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }

        StrategyHandle handler = handlerFactory.loadStrategyHandler(requestStrategyType);
        if (handler == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        return handler.handleCallbackStrategy(processChain, callback, responseModel,
                message, isSuccess);
    }
}
