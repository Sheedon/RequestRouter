package org.sheedon.rrouter;

import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.Objects;


/**
 * 抽象请求代理类,数据请求统一通过该类代为执行，请求模块解耦，
 * 业务模块只需要处理「请求参数」和「反馈结果」，无需考虑数据采用什么方式获取。
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 8:05 下午
 */
public abstract class AbstractRequestProxy<RequestCard, ResponseModel>
        extends RequestProxy.RequestProxyFactory {

    // 请求项
    private Request.Factory<RequestCard, ResponseModel> request;
    // 反馈监听器
    private DataSource.Callback<ResponseModel> callback;
    // 真实调度的请求组
    private SparseArray<Request<RequestCard>> requestStrategies;
    // 进度
    private ProcessChain chain;
    // 请求卡片
    private RequestCard requestCard;

    public AbstractRequestProxy(@NonNull Request.Factory<RequestCard, ResponseModel> request,
                                DataSource.Callback<ResponseModel> callback) {
        // 填充默认请求工厂和反馈监听器
        this.request = request;
        this.callback = callback;

        // 通过请求工厂创建真实请求策略集合
        this.requestStrategies = request.createRequestStrategies(strategyCallback);
        if (requestStrategies == null || requestStrategies.size() == 0) {
            throw new NullPointerException("requestStrategies is null or empty!");
        }

        StrategyHandle.Responsibilities handler = bindStrategyHandler();
        if (handler == null) {
            throw new NullPointerException("handler is null");
        }

        // 加载请求策略,填充流程链，设置状态为未开始
        int[] strategyArray = handler.loadRequestProcess(request.onLoadRequestStrategyType());
        this.chain = new ProcessChain(strategyArray);

    }


    /**
     * 绑定策略执行者
     */
    protected StrategyHandle.Responsibilities bindStrategyHandler() {
        ConfigRepository repository = RRouter.getInstance().getConfigRepository();
        return repository.getStrategyHandler();
    }

    /**
     * 执行请求操作，需要任意满足以下两个条件之一
     * 1. 上一次请求调度完成
     * 2. 这次请求数据与上一次的请求数据不一致
     *
     * <p>
     * 需要请求的条件下
     * 1. 重置状态
     * 2. 核实并且填充请求类 RequestCard
     * 3. 执行调度
     * <p>
     */
    @Override
    public void request() {

        if (chain.getCurrentStatus() == ProcessChain.STATUS_REQUESTING) {
            // 当前进度请求中，则核实是否数据是否更改，更改才重新请求
            RequestCard requestCard = onCreateRequestCard();
            if (!Objects.equals(requestCard, this.requestCard) && request != null) {
                request.onCancel();
            } else {
                return;
            }
        }

        // 重置进度
        chain.reset();
        // 其他流程 - 未开始/已完成
        // 核实拿到 requestCard
        checkRequestCard();
        // 请求执行
        requestDispatch();
    }

    /**
     * 获取请求卡片，并且copy到当前requestCard
     */
    @SuppressWarnings("unchecked")
    private void checkRequestCard() {
        RequestCard requestCard = onCreateRequestCard();
        if (requestCard instanceof DataCloneable) {
            try {
                this.requestCard = (RequestCard) ((DataCloneable) requestCard).clone();
            } catch (CloneNotSupportedException e) {
                this.requestCard = requestCard;
            }
        } else {
            this.requestCard = requestCard;
        }
    }

    /**
     * 策略执行器 根据请求策略方式，执行请求操作
     */
    private void requestDispatch() {
        StrategyHandle.Responsibilities handler = bindStrategyHandler();

        if (handler == null) {
            throw new NullPointerException("handler is null");
        }

        int strategyType = request.onLoadRequestStrategyType();

        // 获取当前状态
        boolean isSuccess = handler.handleRequestStrategy(strategyType, chain,
                requestStrategies, requestCard);

        if (!isSuccess) {
            strategyCallback.onDataNotAvailable("request failure");
        }
    }

    /**
     * 创建请求Card
     * 由创建的请求类中动态添加
     */
    protected abstract RequestCard onCreateRequestCard();

    /**
     * 策略反馈监听器
     */
    private final StrategyHandle.StrategyCallback<ResponseModel> strategyCallback
            = new StrategyHandle.StrategyCallback<ResponseModel>() {
        /**
         * 数据加载成功
         * @param responseModel 反馈数据
         */
        @Override
        public void onDataLoaded(ResponseModel responseModel) {
            handle(responseModel, "", true, chain);
        }

        /**
         * 数据加载失败
         * @param message 描述信息
         * @param progress 进度
         */
        @Override
        public void onDataNotAvailable(String message) {
            handle(null, message, false, chain);
        }

        /**
         * 反馈结果处理
         * @param responseModel 反馈数据Model
         * @param message 描述信息
         * @param isSuccess 是否成功
         * @param progress 反馈状态
         */
        private void handle(ResponseModel responseModel, String message, boolean isSuccess,
                            ProcessChain chain) {
            if (callback == null)
                return;


            // 加载策略执行者，无执行者，则直接反馈给callback，否则提交给执行器去执行
            StrategyHandle.Responsibilities handler = bindStrategyHandler();
            if (handler != null) {

                // 加载请求策略类型
                int type = request.onLoadRequestStrategyType();


                // 执行反馈处理
                boolean handleSuccess = handler.handleCallbackStrategy(type, chain,
                        callback, responseModel, message, isSuccess);

                // 当前状态为完成，则代表执行完成
                if (chain.getCurrentStatus() == ProcessChain.STATUS_COMPLETED) {
                    notifyCallback(isSuccess, responseModel, message);
                    return;
                }

                // 执行未完成，执行下一个
                if (handleSuccess) {
                    requestDispatch();
                    return;
                }
            }

            // 无执行器执行
            notifyCallback(isSuccess, responseModel, message);
        }

        private void notifyCallback(boolean isSuccess, ResponseModel responseModel, String message) {
            // 无执行器执行
            if (isSuccess) {
                callback.onDataLoaded(responseModel);
            } else {
                callback.onDataNotAvailable(message);
            }
        }
    };


    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        if (request != null) {
            request.onDestroy();
        }
        request = null;
        callback = null;
        requestStrategies = null;
        chain = null;
    }
}

