package org.sheedon.repository;

import androidx.annotation.Nullable;

import java.util.Queue;

/**
 * 请求处理
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 8:10 下午
 */
public interface Request<RequestCard> {
    // 请求数据
    void request(RequestCard requestCard);

    // 请求类型
    // 例如：网络请求，本地请求
    int onRequestType();

    // 是否调度完成
    boolean isComplete();

    // 销毁
    void onDestroy();

    abstract class Factory<RequestCard, ResponseModel> {

        /**
         * 创建请求策略集合
         *
         * @param callback     反馈监听器
         * @param handler      策略执行者
         * @param strategyType 策略类型
         * @return SparseArray<Request < RequestCard>>
         */
        public @Nullable
        Queue<Request<RequestCard>>
        createRequestStrategies(StrategyHandle.StrategyCallback<ResponseModel> callback,
                                StrategyHandle.Responsibilities handler, int strategyType) {

            return null;
        }


        /**
         * 用于处理后再次获取
         */
        public @Nullable
        Queue<Request<RequestCard>> onGetRequestStrategies() {
            return null;
        }

        /**
         * 加载请求策略类型
         * 由实际创建的请求策略提供策略类型
         * 例如 {@link DefaultStrategyHandler.STRATEGY}
         *
         * @return 策略类型
         */
        public abstract int onLoadRequestStrategyType();


        /**
         * 请求完成销毁操作
         */
        public abstract void onDestroy();

    }


}
