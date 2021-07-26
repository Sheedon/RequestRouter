package org.sheedon.repository.strategy;

import android.util.SparseArray;

import org.sheedon.repository.Request;
import org.sheedon.repository.data.DataSource;

/**
 * 同步请求网络和本地策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:48 下午
 */
public class SyncNetworkAndLocationStrategyHandler extends BaseStrategyHandler {


    /**
     * 类型为本地网络同步请求 {@link STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 则依次本地网络请求，反馈进度网络请求 {@link PROGRESS.REQUEST_NETWORK}
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     请求卡片类型
     * @return 是否处理成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(int progress,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard card, ProgressCallback callback) {
        // 同时本地网络请求
        if (progress == StrategyConfig.PROGRESS.START) {
            boolean isSuccess = false;
            callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.REQUEST_NETWORK);
            Request<RequestCard> localRequestStrategy =
                    requestStrategies.get(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST);
            if (localRequestStrategy != null) {
                localRequestStrategy.request(card);
                isSuccess = true;
            }

            Request<RequestCard> netWorkRequestStrategy =
                    requestStrategies.get(StrategyConfig.REQUEST.TYPE_NETWORK_REQUEST);
            if (netWorkRequestStrategy != null) {
                netWorkRequestStrategy.request(card);
                isSuccess = true;
            }
            if (isSuccess) {
                return true;
            }
        }

        if (progress == StrategyConfig.PROGRESS.ERROR || progress == StrategyConfig.PROGRESS.COMPLETE) {
            callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
            return false;
        }

        return true;
    }

    /**
     * 类型为本地网络同步请求 {@link STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 当前进度为本地请求 {@link PROGRESS.REQUEST_LOCAL}，反馈更改为 {@link PROGRESS.REQUEST_NETWORK}
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，反馈完成进度状态 {@link PROGRESS.COMPLETE}
     *
     * @param currentProgress  当前进度
     * @param callbackProgress 反馈进度
     * @param callback         反馈监听
     * @param responseModel    反馈model
     * @param message          描述信息
     * @param isSuccess        是否请求成功
     * @param progressCallback 进度反馈
     * @param <ResponseModel>  反馈model类型
     * @return 是否处理成功
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(int currentProgress, int callbackProgress,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel responseModel, String message,
                                                                 boolean isSuccess, ProgressCallback progressCallback) {
        if (callbackProgress == StrategyConfig.PROGRESS.REQUEST_LOCAL) {
            progressCallback.onCurrentProgressCallback(callbackProgress);
            handleCallback(callback, responseModel, message, isSuccess);
            return true;
        }
        if (callbackProgress == StrategyConfig.PROGRESS.REQUEST_NETWORK) {
            progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.COMPLETE);
            handleCallback(callback, responseModel, message, isSuccess);
            return true;
        }
        progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
        return false;
    }
}
