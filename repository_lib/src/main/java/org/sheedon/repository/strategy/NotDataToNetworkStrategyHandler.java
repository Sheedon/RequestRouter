package org.sheedon.repository.strategy;

import org.sheedon.repository.Request;
import org.sheedon.repository.data.DataSource;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 优先本地，无数据取网络
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:44 下午
 */
public class NotDataToNetworkStrategyHandler extends BaseStrategyHandler {

    /**
     * 加载请求队列
     * 将所有网络请求添加到队列中，其他都省略
     *
     * @param requests      请求项
     * @param <RequestCard> 请求卡片
     * @return 请求队列
     */
    @SafeVarargs
    @Override
    public final <RequestCard> Queue<Request<RequestCard>> loadRequestQueue(Request<RequestCard>... requests) {
        ArrayDeque<Request<RequestCard>> queue = new ArrayDeque<>();
        for (Request<RequestCard> request : requests) {
            if (request == null)
                continue;

            if (request.onRequestType() == StrategyConfig.REQUEST.TYPE_NETWORK_REQUEST) {
                queue.offerLast(request);
            } else if (request.onRequestType() == StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST) {
                queue.offerFirst(request);
            }
        }
        return queue;
    }

    /**
     * 类型为优先本地请求，无数据才网络请求 {@link STRATEGY.TYPE_NOT_DATA_TO_NET}，
     * 按次序依次请求
     *
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     请求卡片
     * @return 是否调度成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(Queue<Request<RequestCard>> requestStrategies,
                                                              RequestCard card) {
        Request<RequestCard> request = requestStrategies.peek();
        if (request == null) {
            return false;
        }

        request.request(card);
        return true;
    }

    /**
     * 真实处理反馈调度
     *
     * @param requestStrategies 请求策略队列
     * @param callback          反馈监听
     * @param responseModel     反馈数据
     * @param message           描述信息
     * @param isSuccess         是否请求成功
     * @param <RequestCard>     请求卡片
     * @param <ResponseModel>   请求反馈结果
     */
    @Override
    protected <RequestCard, ResponseModel>
    int handleRealCallbackStrategy(Queue<Request<RequestCard>> requestStrategies,
                                    DataSource.Callback<ResponseModel> callback,
                                    ResponseModel responseModel, String message, boolean isSuccess) {

        if (requestStrategies != null && requestStrategies.size() != 0) {
            Request<RequestCard> request = requestStrategies.poll();
            if (request != null)
                request.onDestroy();
        }
        handleCallback(callback, responseModel, message, isSuccess);

        return requestStrategies != null && requestStrategies.size() != 0 ?
                StrategyConfig.PROGRESS.REQUEST_DOING : StrategyConfig.PROGRESS.COMPLETE;
    }
}
