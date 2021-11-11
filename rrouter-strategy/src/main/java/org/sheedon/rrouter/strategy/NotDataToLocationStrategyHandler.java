package org.sheedon.rrouter.strategy;

import android.util.SparseArray;

import org.sheedon.rrouter.DataSource;
import org.sheedon.rrouter.ProcessChain;
import org.sheedon.rrouter.Request;
import org.sheedon.rrouter.StrategyConfig;

/**
 * 优先网络请求，无数据本地数据请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:53 下午
 */
public class NotDataToLocationStrategyHandler extends BaseStrategyHandler {

    @Override
    public int[] loadRequestProcess() {
        return new int[]{StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST,
                StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST};
    }

    /**
     * 类型为优先网络请求，请求错误，才本地请求 {@link StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_LOCATION}，
     * 第一次请求，则做网络请求，第二次进来走本地请求
     * 且当前状态必须为默认状态，否则请求失败
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     请求卡片
     * @return 是否处理成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard requestCard) {
        return super.handleRealRequestStrategy(processChain, requestStrategies, requestCard);
    }


    /**
     * 类型为优先网络请求，请求错误，才本地请求 {@link StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_LOCATION}，
     * 当前进度为 {@link ProcessChain.STATUS_REQUESTING}，则执行反馈操作
     * 设置状态 {@link ProcessChain.STATUS_COMPLETED}
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param responseModel   结果model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 结果model类型
     * @return 是否处理成功
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(ProcessChain processChain,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel responseModel, String message,
                                                                 boolean isSuccess) {
        return super.handleRealCallbackStrategy(processChain, callback, responseModel, message, isSuccess);
    }
}
