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
     * @param requestStrategyType 请求策略类型
     * @param progress            请求进度
     * @param requestStrategies   请求策略集合
     * @param card                请求卡片
     * @param callback            反馈监听器
     * @param <RequestCard>       RequestCard
     * @return 执行是否成功
     */
    <RequestCard> boolean handleRequestStrategy(int requestStrategyType, int progress,
                                                SparseArray<Request<RequestCard>> requestStrategies,
                                                RequestCard card, ProgressCallback callback);

    /**
     * 反馈策略待执行方法
     *
     * @param requestStrategyType 请求策略类型
     * @param currentProgress     当前进度
     * @param callbackProgress    反馈进度
     * @param callback            反馈监听
     * @param model               反馈结果
     * @param message             描述信息
     * @param isSuccess           是否请求成功
     * @param <ResponseModel>     ResponseModel
     * @return 执行完成的进度
     */
    <ResponseModel> int handleCallbackStrategy(int requestStrategyType, int currentProgress,
                                               int callbackProgress,
                                               DataSource.Callback<ResponseModel> callback,
                                               ResponseModel model,
                                               String message,
                                               boolean isSuccess);

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
