package org.sheedon.repository.strategy;

import android.util.SparseArray;

import org.sheedon.repository.Request;
import org.sheedon.repository.data.DataSource;

/**
 * 优先本地，无数据取网络
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:44 下午
 */
public class NotDataToNetworkStrategyHandler extends BaseStrategyHandler {

    /**
     * 类型为优先本地请求，无数据才网络请求 {@link STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 第一次请求，则做本地请求，反馈 {@link PROGRESS.REQUEST_LOCAL}
     * 当前进度为本地请求后，则获取网络请求代理并做请求，反馈 {@link PROGRESS.REQUEST_NETWORK}
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     请求卡片
     * @return 是否处理成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(int progress,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard card, ProgressCallback callback) {
        // 本地请求
        Request<RequestCard> localRequestStrategy =
                requestStrategies.get(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST);
        if (progress == StrategyConfig.PROGRESS.START && localRequestStrategy != null) {
            callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.REQUEST_LOCAL);
            localRequestStrategy.request(card);
            return true;
        }

        // 网络请求
        Request<RequestCard> netWorkRequestStrategy =
                requestStrategies.get(StrategyConfig.REQUEST.TYPE_NETWORK_REQUEST);
        if ((progress == StrategyConfig.PROGRESS.START || progress == StrategyConfig.PROGRESS.REQUEST_LOCAL)
                && netWorkRequestStrategy != null) {
            callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.REQUEST_NETWORK);
            netWorkRequestStrategy.request(card);
            return true;
        }

        callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
        return false;
    }


    /**
     * 类型为优先本地请求，无数据才网络请求 {@link STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 当前进度为本地请求 {@link PROGRESS.REQUEST_LOCAL}，反馈更改为 当前网络状态 {@link PROGRESS.REQUEST_LOCAL}
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，反馈完成进度状态 {@link PROGRESS.COMPLETE}
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
    protected <ResponseModel> boolean handleRealCallbackStrategy(int currentProgress, int callbackProgress,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel responseModel, String message,
                                                                 boolean isSuccess, ProgressCallback progressCallback) {

        if (callbackProgress == StrategyConfig.PROGRESS.REQUEST_LOCAL) {
            if (isSuccess) {
                progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.COMPLETE);
                handleCallback(callback, responseModel, message, isSuccess);
            } else {
                progressCallback.onCurrentProgressCallback(callbackProgress);
            }
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
