package org.sheedon.rrouter;

import android.util.SparseArray;

import androidx.annotation.Nullable;

import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;

/**
 * 请求行为的创建工厂类
 * 1. 创建获取实际请求集合
 * 2. 加载请求调度策略，后续按照请求调度策略方式，从请求集合中取请求，执行请求行为
 * 3. 提供销毁动作，依次执行请求销毁
 *
 * @param <RequestCard>
 * @param <ResponseModel>
 */
public abstract class RequestFactory<RequestCard, ResponseModel> {

    /**
     * 创建请求策略集合，请求执行代理类需调用该方法，用于加载真实请求集合
     *
     * @param callback 反馈监听器
     * @return SparseArray<Request < RequestCard>>
     */
    public @Nullable
    SparseArray<Request<RequestCard>>
    createRequestStrategies(StrategyCallback<ResponseModel> callback) {

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
