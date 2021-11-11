package org.sheedon.rrouter.strategy;

import android.util.SparseArray;

import org.sheedon.rrouter.DataSource;
import org.sheedon.rrouter.ProcessChain;
import org.sheedon.rrouter.Request;
import org.sheedon.rrouter.StrategyConfig;

/**
 * 同步请求网络和本地策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:48 下午
 */
public class SyncRemoteAndLocationStrategyHandler extends BaseStrategyHandler {

    private final Object lock = new Object();

    /**
     * 请求流程，本地/网络同时访问
     */
    @Override
    public int[] loadRequestProcess() {
        return new int[]{StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST,
                StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST};
    }

    /**
     * 类型为本地网络同步请求 {@link StrategyConfig.STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 则依次本地网络请求，设置进度
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param requestCard       请求卡片
     * @param <RequestCard>     RequestCard
     * @return 是否请求成功
     */
    @Override
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard requestCard) {
        // 拿到当前进度对应的请求
        Request<RequestCard> localRequest = requestStrategies.get(StrategyConfig.REQUEST.TYPE_LOCAL_REQUEST);
        Request<RequestCard> netRequest = requestStrategies.get(StrategyConfig.REQUEST.TYPE_REMOTE_REQUEST);
        // 请求不存在，则请求失败
        if (localRequest == null && netRequest == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        // 状态并非「未发送」，则请求失败
        if (processChain.getStatus(0) != ProcessChain.STATUS_NORMAL
                && processChain.getStatus(1) != ProcessChain.STATUS_NORMAL) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        request(localRequest, processChain, 0, requestCard);
        request(netRequest, processChain, 1, requestCard);

        return true;
    }

    /**
     * 执行请求操作
     *
     * @param request       请求项
     * @param processChain  流程链
     * @param index         任务坐标
     * @param requestCard   请求卡片
     * @param <RequestCard> 请求类型
     */
    private <RequestCard> void request(Request<RequestCard> request,
                                       ProcessChain processChain, int index,
                                       RequestCard requestCard) {
        if (request != null && processChain.getStatus(index) == ProcessChain.STATUS_NORMAL) {
            // 请求任务
            processChain.updateOfIndex(index, ProcessChain.STATUS_REQUESTING);
            request.request(requestCard);
        }
    }

    /**
     * 类型为并行请求 {@link StrategyConfig.STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION}，
     * 两者都是完成，则反馈失败，状态为提交中，则返回数据
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param responseModel   反馈model
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 反馈类型
     * @return 是否反馈成功
     */
    @Override
    protected <ResponseModel> boolean handleRealCallbackStrategy(ProcessChain processChain,
                                                                 DataSource.Callback<ResponseModel> callback,
                                                                 ResponseModel responseModel, String message,
                                                                 boolean isSuccess) {
        if (processChain == null) {
            return false;
        }

        synchronized (lock) {
            if (handleCallback(processChain, 0, callback,
                    responseModel, message, isSuccess)) {
                return true;
            }

            return handleCallback(processChain, 1, callback,
                    responseModel, message, isSuccess);

        }
    }

    /**
     * 反馈处理
     *
     * @param processChain    流程链
     * @param index           坐标
     * @param callback        反馈持有者
     * @param responseModel   反馈内容
     * @param message         消息
     * @param isSuccess       是否成功
     * @param <ResponseModel> 反馈类型
     * @return 是否反馈成功
     */
    private <ResponseModel> boolean handleCallback(ProcessChain processChain, int index,
                                                   DataSource.Callback<ResponseModel> callback,
                                                   ResponseModel responseModel, String message,
                                                   boolean isSuccess) {
        if (processChain.getStatus(index) == ProcessChain.STATUS_REQUESTING) {
            processChain.updateOfIndex(index, ProcessChain.STATUS_COMPLETED);
            handleCallback(callback, responseModel, message, isSuccess);
            return true;
        }
        return false;
    }


}
