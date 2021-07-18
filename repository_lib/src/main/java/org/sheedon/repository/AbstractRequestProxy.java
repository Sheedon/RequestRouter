package org.sheedon.repository;

import android.util.SparseArray;

import androidx.annotation.NonNull;

import org.sheedon.repository.data.DataSource;

/**
 * 抽象请求代理类,数据请求统一通过该类代为执行，请求模块解耦，
 * 业务模块只需要处理「请求参数」和「反馈结果」，无需考虑数据采用什么方式获取。
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 8:05 下午
 */
public abstract class AbstractRequestProxy<RequestCard, ResponseModel> implements RequestProxyImpl {

    /**
     * 请求项
     */
    private Request.Factory<RequestCard, ResponseModel> request;
    // 反馈监听器
    private DataSource.Callback<ResponseModel> callback;
    // 当前进度
    private int progress = DefaultStrategyHandler.PROGRESS.START;

    public AbstractRequestProxy(@NonNull Request.Factory<RequestCard, ResponseModel> request,
                                DataSource.Callback<ResponseModel> callback) {
        this.request = request;
        this.callback = callback;
    }

    /**
     * 绑定策略执行者
     */
    @Override
    public StrategyHandle bindStrategyHandler() {
        return DefaultStrategyHandler.HANDLER;
    }

    /**
     * 代理执行请求操作
     * <p>
     * 加载
     * 1. 加载已绑定的策略执行器，并核实判空处理
     * 2. 重置更新进度为初次请求状态
     * 3. 加载请求策略集合
     * 4. 加载请求策略类型 {@link DefaultStrategyHandler.STRATEGY}
     * 5. 加载请求参数卡片
     * <p>
     * 请求
     * 执行请求策略调度
     */
    @Override
    public void request() {
        this.progress = DefaultStrategyHandler.PROGRESS.START;
        requestDispatch();
    }

    /**
     * 请求调度
     */
    private void requestDispatch(){
        StrategyHandle handler = bindStrategyHandler();

        if (handler == null) {
            throw new NullPointerException("handler is null");
        }

        // 依次获取本地请求策略，网络请求策略，请求类型，请求卡片
        SparseArray<Request<RequestCard>> requestStrategies
                = request.createRequestStrategies(strategyCallback);

        if (requestStrategies == null || requestStrategies.size() == 0) {
            throw new RuntimeException("requestStrategies is null or empty!");
        }

        int strategyType = request.onLoadRequestStrategyType();
        RequestCard requestCard = onCreateRequestCard();

        // 获取当前状态
        boolean isSuccess = handler.handleRequestStrategy(strategyType, this.progress,
                requestStrategies, requestCard, progressCallback);

        if (!isSuccess) {
            strategyCallback.onDataNotAvailable("request failure", this.progress);
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
         * @param progress 进度
         */
        @Override
        public void onDataLoaded(ResponseModel responseModel, int progress) {
            handle(responseModel, "", true, progress);
        }

        /**
         * 数据加载失败
         * @param message 描述信息
         * @param progress 进度
         */
        @Override
        public void onDataNotAvailable(String message, int progress) {
            handle(null, message, false, progress);
        }

        /**
         * 反馈结果处理
         * @param responseModel 反馈数据Model
         * @param message 描述信息
         * @param isSuccess 是否成功
         * @param progress 反馈状态
         */
        private void handle(ResponseModel responseModel, String message, boolean isSuccess,
                            int progress) {
            if (callback == null)
                return;


            // 加载策略执行者，无执行者，则直接反馈给callback，否则提交给执行器去执行
            StrategyHandle handler = bindStrategyHandler();
            if (handler != null) {

                // 加载请求策略类型
                int type = request.onLoadRequestStrategyType();


                // 执行反馈处理
                AbstractRequestProxy.this.progress = handler.handleCallbackStrategy(type,
                        AbstractRequestProxy.this.progress, progress,
                        callback, responseModel, message, isSuccess);

                if(AbstractRequestProxy.this.progress != DefaultStrategyHandler.PROGRESS.COMPLETE){
                    requestDispatch();
                }

                return;
            }

            // 无执行器执行
            if (isSuccess) {
                callback.onDataLoaded(responseModel);
            } else {
                callback.onDataNotAvailable(message);
            }
        }
    };

    /**
     * 进度反馈监听器
     */
    private final StrategyHandle.ProgressCallback progressCallback
            = progress -> AbstractRequestProxy.this.progress = progress;


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
    }
}

