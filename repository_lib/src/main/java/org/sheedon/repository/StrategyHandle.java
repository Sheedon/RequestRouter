package org.sheedon.repository;

import android.util.SparseArray;

import org.sheedon.repository.data.DataSource;

/**
 * 执行策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 9:18 下午
 */
public interface StrategyHandle {

    /**
     * 请求策略执行
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     RequestCard
     * @return 执行是否成功
     */
    <RequestCard> boolean handleRequestStrategy(int progress,
                                                SparseArray<Request<RequestCard>> requestStrategies,
                                                RequestCard card, ProgressCallback callback);

    /**
     * 反馈策略待执行方法
     *
     * @param currentProgress  当前进度
     * @param callbackProgress 反馈进度
     * @param callback         反馈监听
     * @param model            反馈结果
     * @param message          描述信息
     * @param isSuccess        是否请求成功
     * @param <ResponseModel>  ResponseModel
     * @return 执行完成的进度
     */
    <ResponseModel> boolean handleCallbackStrategy(int currentProgress,
                                                   int callbackProgress,
                                                   DataSource.Callback<ResponseModel> callback,
                                                   ResponseModel model,
                                                   String message,
                                                   boolean isSuccess,
                                                   ProgressCallback progressCallback);


    /**
     * 策略执行器抽象工厂
     */
    abstract class Factory {
        public StrategyHandle loadStrategyHandler(int requestStrategyType) {
            return null;
        }
    }

    /**
     * 默认 组策略执行者 的职责
     */
    interface Responsibilities {
        // 执行请求策略
        <RequestCard> boolean handleRequestStrategy(int requestStrategyType, int progress,
                                                    SparseArray<Request<RequestCard>> requestStrategies,
                                                    RequestCard card, ProgressCallback callback);

        // 执行反馈处理策略
        <ResponseModel> boolean handleCallbackStrategy(int requestStrategyType, int currentProgress,
                                                       int callbackProgress,
                                                       DataSource.Callback<ResponseModel> callback,
                                                       ResponseModel responseModel, String message,
                                                       boolean isSuccess, ProgressCallback progressCallback);
    }

    /**
     * 进度反馈监听器
     */
    interface ProgressCallback {
        void onCurrentProgressCallback(int progress);
    }

    interface StrategyCallback<T> {
        // 数据加载成功, 请求成功
        void onDataLoaded(T t, int progress);

        // 数据加载失败, 请求失败
        void onDataNotAvailable(String message, int progress);

    }
}
