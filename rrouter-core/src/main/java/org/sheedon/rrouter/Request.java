package org.sheedon.rrouter;

import android.util.SparseArray;

import androidx.annotation.Nullable;

/**
 * 复杂请求中的具体请求类职责。 此类主要需要提供：「数据请求操作」、「该类请求类型」、「销毁」。
 * 请求操作：真实调度远程/本地等数据请求。
 * 该类请求类型：本请求类型，作为「请求策略」中的「键」关联。
 * 销毁：主动结束请求动作，关闭连接。
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

    // 取消请求
    void onCancel();

    // 销毁
    void onDestroy();

    /**
     * 请求行为的创建工厂类
     * 1. 创建获取实际请求集合
     * 2. 加载请求调度策略，后续按照请求调度策略方式，从请求集合中取请求，执行请求行为
     * 3. 提供销毁动作，依次执行请求销毁
     *
     * @param <RequestCard>
     * @param <ResponseModel>
     */
    abstract class Factory<RequestCard, ResponseModel> {

        /**
         * 创建请求策略集合，请求执行代理类需调用该方法，用于加载真实请求集合
         *
         * @param callback 反馈监听器
         * @return SparseArray<Request < RequestCard>>
         */
        public @Nullable
        SparseArray<Request<RequestCard>>
        createRequestStrategies(StrategyHandle.StrategyCallback<ResponseModel> callback) {

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
         * 请求取消操作
         */
        public abstract void onCancel();

        /**
         * 操作中止的销毁操作
         */
        public abstract void onDestroy();

    }


}
