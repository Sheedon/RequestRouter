package org.sheedon.rrouter.strategy;

import android.util.SparseArray;

import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.rrouter.ProcessChain;
import org.sheedon.rrouter.StrategyConfig;
import org.sheedon.rrouter.core.support.Request;

/**
 * 单一网络请求执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:15 下午
 */
public class OnlyRemoteStrategyHandler extends BaseStrategyHandler {


    /**
     * 请求流程，单一网络请求
     */
    @Override
    public int[] loadRequestProcess() {
        return new int[]{StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST};
    }


    /**
     * 执行真实请求行为
     * 类型为单一网络请求 {@link StrategyConfig.STRATEGY.TYPE_ONLY_NETWORK}，
     * 且当前状态为默认状态，则获取网络请求代理并做请求，否则请求调度失败
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param requestCard       请求卡片
     * @param <RequestCard>     RequestCard
     * @return 是否请求成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard requestCard) {
        return super.handleRealRequestStrategy(processChain, requestStrategies, requestCard);
    }

    /**
     * 类型为单一网络请求 {@link StrategyConfig.STRATEGY.TYPE_ONLY_NETWORK}，
     * 当前进度为网络请求 {@link ProcessChain.STATUS_REQUESTING}，则执行反馈操作
     * 设置状态 {@link ProcessChain.STATUS_COMPLETED}
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param responseModel   反馈model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 是否反馈成功
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(ProcessChain processChain,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel responseModel, String message,
                                                                 boolean isSuccess) {


        return super.handleRealCallbackStrategy(processChain, callback, responseModel, message, isSuccess);
    }
}