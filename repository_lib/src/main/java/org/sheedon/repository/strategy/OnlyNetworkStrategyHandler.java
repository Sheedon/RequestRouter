package org.sheedon.repository.strategy;

import android.util.SparseArray;

import org.sheedon.repository.Request;
import org.sheedon.repository.data.DataSource;


/**
 * 单一网络请求执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:15 下午
 */
public class OnlyNetworkStrategyHandler extends BaseStrategyHandler {

    /**
     * 类型为单一网络请求 {@link STRATEGY.TYPE_ONLY_NETWORK}，
     * 且第一次请求，则获取网络请求代理并做请求，反馈 {@link PROGRESS.REQUEST_NETWORK}
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>
     * @return
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(int progress, SparseArray<Request<RequestCard>> requestStrategies, RequestCard card, ProgressCallback callback) {
        Request<RequestCard> netWorkRequestStrategy =
                requestStrategies.get(StrategyConfig.REQUEST.TYPE_NETWORK_REQUEST);
        if (progress == StrategyConfig.PROGRESS.START && netWorkRequestStrategy != null) {
            callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.REQUEST_NETWORK);
            netWorkRequestStrategy.request(card);
            return true;
        }
        callback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
        return false;
    }


    /**
     * 类型为单一网络请求 {@link STRATEGY.TYPE_ONLY_NETWORK}，
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，则处理完成，反馈 {@link PROGRESS.COMPLETE}
     * 其他反馈 {@link PROGRESS.COMPLETE}
     *
     * @param currentProgress  当前进度
     * @param callbackProgress 反馈进度
     * @param callback         反馈监听
     * @param responseModel
     * @param message          描述信息
     * @param isSuccess        是否请求成功
     * @param progressCallback
     * @param <ResponseModel>
     * @return
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(int currentProgress, int callbackProgress, DataSource.Callback<ResponseModel> callback, ResponseModel responseModel, String message, boolean isSuccess, ProgressCallback progressCallback) {
        // 上说明 处理 1
        progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.COMPLETE);
        if (currentProgress == StrategyConfig.PROGRESS.REQUEST_NETWORK) {
            handleCallback(callback, responseModel, message, isSuccess);
            return true;
        }
        progressCallback.onCurrentProgressCallback(StrategyConfig.PROGRESS.ERROR);
        return false;
    }
}
