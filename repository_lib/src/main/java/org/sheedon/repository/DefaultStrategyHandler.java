package org.sheedon.repository;

import android.util.SparseArray;

import org.sheedon.repository.data.DataSource;

/**
 * 默认策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 11:57 下午
 */
public final class DefaultStrategyHandler implements StrategyHandle {

    /**
     * 策略
     */
    public interface STRATEGY {
        int TYPE_ONLY_NETWORK = 0x1;// 单一网络请求
        int TYPE_NOT_DATA_TO_NET = 0x2;// 优先本地，无数据取网络
        int TYPE_SYNC_NETWORK_AND_LOCATION = 0x4;// 同步请求，本地和网络
        int TYPE_NOT_DATA_TO_LOCATION = 0x8;//优先网络请求，网络请求失败，搜索本地数据 「类似无网络登陆」
    }

    /**
     * 请求流程状态 整体分为 3 个状态，
     * 准备开始请求，请求中，请求完成
     * 请求中 分为两种状态：本地请求中 和 网络请求中。
     * <p>
     * 根据不同状态，需要执行不同的请求和反馈的策略
     */
    public interface PROGRESS {
        // 准备开始请求
        int START = 0x1;
        // 本地请求中
        int REQUEST_LOCAL = 0x2;
        // 网络请求中
        int REQUEST_NETWORK = 0x4;
        // 请求完成
        int COMPLETE = 0x8;
        // 请求流程等错误
        int ERROR = 0x10;
    }

    /**
     * 请求类型
     */
    public interface REQUEST {
        int TYPE_NETWORK_REQUEST = 1;// 网络请求
        int TYPE_LOCAL_REQUEST = 2;// 本地请求
    }

    /**
     * 单例执行者
     */
    public static DefaultStrategyHandler HANDLER = new DefaultStrategyHandler();

    private DefaultStrategyHandler() {

    }

    /**
     * 处理请求代理
     * 1. 类型为单一网络请求 {@link STRATEGY.TYPE_ONLY_NETWORK}，
     * 且第一次请求，则获取网络请求代理并做请求，反馈 {@link PROGRESS.REQUEST_NETWORK}
     * <p>
     * 2. 类型为优先本地请求，无数据才网络请求 {@link STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 第一次请求，则做本地请求，反馈 {@link PROGRESS.REQUEST_LOCAL}
     * 当前进度为本地请求后，则获取网络请求代理并做请求，反馈 {@link PROGRESS.REQUEST_NETWORK}
     * <p>
     * 3. 类型为本地网络同步请求 {@link STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 则依次本地网络请求，反馈进度网络请求 {@link PROGRESS.REQUEST_NETWORK}
     * <p>
     * 4. 类型为优先网络请求，请求错误，才本地请求 {@link STRATEGY.TYPE_NOT_DATA_TO_LOCATION}，
     * 第一次请求，则做网络请求，反馈 {@link PROGRESS.REQUEST_NETWORK}
     * 当前进度为本地请求后，则获取本地请求代理并做请求，反馈 {@link PROGRESS.REQUEST_LOCAL}
     *
     * @param requestStrategyType 请求策略类型
     * @param progress            请求进度
     * @param requestStrategies   请求策略集合
     * @param card                请求卡片
     * @param callback            反馈监听器
     * @param <RequestCard>       RequestCard
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(int requestStrategyType, int progress,
                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                       RequestCard card, ProgressCallback callback) {
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        switch (requestStrategyType) {
            case STRATEGY.TYPE_ONLY_NETWORK:
                // 上说明 处理 1
                Request<RequestCard> netWorkRequestStrategy =
                        requestStrategies.get(REQUEST.TYPE_NETWORK_REQUEST);
                if (progress == PROGRESS.START && netWorkRequestStrategy != null) {
                    callback.onCurrentProgressCallback(PROGRESS.REQUEST_NETWORK);
                    netWorkRequestStrategy.request(card);
                    return true;
                }
                callback.onCurrentProgressCallback(PROGRESS.ERROR);
                return false;
            case STRATEGY.TYPE_NOT_DATA_TO_NET:
                // 上说明 处理 2
                // 本地请求
                Request<RequestCard> localRequestStrategy =
                        requestStrategies.get(REQUEST.TYPE_LOCAL_REQUEST);
                if (progress == PROGRESS.START && localRequestStrategy != null) {
                    callback.onCurrentProgressCallback(PROGRESS.REQUEST_LOCAL);
                    localRequestStrategy.request(card);
                    return true;
                }

                // 网络请求
                netWorkRequestStrategy =
                        requestStrategies.get(REQUEST.TYPE_NETWORK_REQUEST);
                if ((progress == PROGRESS.START || progress == PROGRESS.REQUEST_LOCAL)
                        && netWorkRequestStrategy != null) {
                    callback.onCurrentProgressCallback(PROGRESS.REQUEST_NETWORK);
                    netWorkRequestStrategy.request(card);
                    return true;
                }

                callback.onCurrentProgressCallback(PROGRESS.ERROR);
                return false;
            case STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION:
                // 上说明 处理 3
                // 同时本地网络请求
                boolean isSuccess = false;
                if (progress == PROGRESS.START) {
                    callback.onCurrentProgressCallback(PROGRESS.REQUEST_NETWORK);
                    localRequestStrategy =
                            requestStrategies.get(REQUEST.TYPE_LOCAL_REQUEST);
                    if (localRequestStrategy != null) {
                        localRequestStrategy.request(card);
                        isSuccess = true;
                    }

                    netWorkRequestStrategy =
                            requestStrategies.get(REQUEST.TYPE_NETWORK_REQUEST);
                    if (netWorkRequestStrategy != null) {
                        netWorkRequestStrategy.request(card);
                        isSuccess = true;
                    }
                }

                if (isSuccess) {
                    return true;
                }

                callback.onCurrentProgressCallback(PROGRESS.ERROR);
                return false;
            case STRATEGY.TYPE_NOT_DATA_TO_LOCATION:
                // 上说明 处理 4
                netWorkRequestStrategy =
                        requestStrategies.get(REQUEST.TYPE_NETWORK_REQUEST);
                if (progress == PROGRESS.START && netWorkRequestStrategy != null) {
                    callback.onCurrentProgressCallback(PROGRESS.REQUEST_NETWORK);
                    netWorkRequestStrategy.request(card);
                    return true;
                }

                localRequestStrategy =
                        requestStrategies.get(REQUEST.TYPE_LOCAL_REQUEST);
                if ((progress == PROGRESS.START || progress == PROGRESS.REQUEST_NETWORK)
                        && localRequestStrategy != null) {
                    callback.onCurrentProgressCallback(PROGRESS.REQUEST_LOCAL);
                    localRequestStrategy.request(card);
                    return true;
                }

                callback.onCurrentProgressCallback(PROGRESS.ERROR);
                return false;
            default:
                callback.onCurrentProgressCallback(PROGRESS.ERROR);
                return false;
        }
    }

    /**
     * 处理反馈代理
     * 1. 类型为单一网络请求 {@link STRATEGY.TYPE_ONLY_NETWORK}，
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，则处理完成，反馈 {@link PROGRESS.COMPLETE}
     * 其他反馈 {@link PROGRESS.COMPLETE}
     * <p>
     * 2. 类型为优先本地请求，无数据才网络请求 {@link STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 当前进度为本地请求 {@link PROGRESS.REQUEST_LOCAL}，反馈更改为 当前网络状态 {@link PROGRESS.REQUEST_LOCAL}
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，反馈完成进度状态 {@link PROGRESS.COMPLETE}
     * <p>
     * 3. 类型为本地网络同步请求 {@link STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 当前进度为本地请求 {@link PROGRESS.REQUEST_LOCAL}，反馈更改为 {@link PROGRESS.REQUEST_NETWORK}
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，反馈完成进度状态 {@link PROGRESS.COMPLETE}
     * <p>
     * 4. 类型为优先网络请求，请求错误，才本地请求 {@link STRATEGY.TYPE_NOT_DATA_TO_LOCATION}，
     * 当前进度为网络请求 {@link PROGRESS.REQUEST_NETWORK}，反馈完成进度状态 {@link PROGRESS.REQUEST_LOCAL}
     * 当前进度为本地请求 {@link PROGRESS.REQUEST_LOCAL}，反馈完成进度状态 {@link PROGRESS.COMPLETE}
     *
     * @param requestStrategyType 请求策略类型
     * @param currentProgress     当前进度
     * @param callbackProgress    反馈进度
     * @param callback            反馈监听
     * @param responseModel       反馈结果
     * @param message             描述信息
     * @param isSuccess           是否请求成功
     * @param <ResponseModel>     ResponseModel
     * @return 执行完成的进度
     */
    @Override
    public <ResponseModel> int handleCallbackStrategy(int requestStrategyType, int currentProgress,
                                                      int callbackProgress,
                                                      DataSource.Callback<ResponseModel> callback,
                                                      ResponseModel responseModel,
                                                      String message,
                                                      boolean isSuccess) {

        // 当前状态已完成，或者 callback 为空，直接返回状态，不做额外反馈处理
        if (currentProgress == PROGRESS.COMPLETE || callback == null) {
            return PROGRESS.COMPLETE;
        }

        // 错误处理直接反馈错误信息，并返回 COMPLETE
        if (currentProgress == PROGRESS.ERROR) {
            callback.onDataNotAvailable(message);
            return PROGRESS.COMPLETE;
        }

        switch (requestStrategyType) {
            case STRATEGY.TYPE_ONLY_NETWORK:
                // 上说明 处理 1
                if (currentProgress == PROGRESS.REQUEST_NETWORK) {
                    handleCallback(callback, responseModel, message, isSuccess);
                }
                return PROGRESS.COMPLETE;
            case STRATEGY.TYPE_NOT_DATA_TO_NET:
                // 上说明 处理 2
                if (callbackProgress == PROGRESS.REQUEST_LOCAL) {
                    if (isSuccess) {
                        handleCallback(callback, responseModel, message, isSuccess);
                        return PROGRESS.COMPLETE;
                    } else {
                        return callbackProgress;
                    }
                }

                if (callbackProgress == PROGRESS.REQUEST_NETWORK) {
                    handleCallback(callback, responseModel, message, isSuccess);
                }
                return PROGRESS.COMPLETE;
            case STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION:
                // 上说明 处理 3
                if (callbackProgress == PROGRESS.REQUEST_LOCAL) {
                    handleCallback(callback, responseModel, message, isSuccess);
                    return currentProgress;
                }
                if (callbackProgress == PROGRESS.REQUEST_NETWORK) {
                    handleCallback(callback, responseModel, message, isSuccess);
                }
                return PROGRESS.COMPLETE;
            case STRATEGY.TYPE_NOT_DATA_TO_LOCATION:
                // 上说明 处理 4
                if (callbackProgress == PROGRESS.REQUEST_NETWORK) {
                    if (isSuccess) {
                        handleCallback(callback, responseModel, message, isSuccess);
                        return PROGRESS.COMPLETE;
                    } else {
                        return callbackProgress;
                    }
                }

                if (callbackProgress == PROGRESS.REQUEST_LOCAL) {
                    handleCallback(callback, responseModel, message, isSuccess);
                }
                return PROGRESS.COMPLETE;
        }
        return PROGRESS.COMPLETE;
    }

    /**
     * 处理反馈结果
     *
     * @param callback        反馈监听器
     * @param model           反馈Model
     * @param message         描述信息
     * @param isSuccess       是否为成功数据反馈
     * @param <ResponseModel> 反馈数据类型
     */
    private <ResponseModel> void handleCallback(DataSource.Callback<ResponseModel> callback,
                                                ResponseModel model, String message,
                                                boolean isSuccess) {

        if (isSuccess) {
            callback.onDataLoaded(model);
            return;
        }

        callback.onDataNotAvailable(message);
    }
}
