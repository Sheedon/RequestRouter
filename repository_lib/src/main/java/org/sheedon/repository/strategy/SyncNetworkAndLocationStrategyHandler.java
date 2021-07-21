package org.sheedon.repository.strategy;


import org.sheedon.repository.Request;
import org.sheedon.repository.data.DataSource;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 同步请求网络和本地策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:48 下午
 */
public class SyncNetworkAndLocationStrategyHandler extends BaseStrategyHandler {

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
     * 类型为本地网络同步请求 {@link STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 则依次本地网络请求，反馈进度网络请求
     * 按照次序
     *
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @return 是否调度成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(Queue<Request<RequestCard>> requestStrategies,
                                                              RequestCard card) {

        boolean isSuccess = false;
        for (Request<RequestCard> realRequest : requestStrategies) {
            if (realRequest != null) {
                isSuccess = true;
                realRequest.request(card);
            }
        }
        return isSuccess;
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
        handleCallback(callback, responseModel, message, isSuccess);
        return StrategyConfig.PROGRESS.COMPLETE;
    }

}
