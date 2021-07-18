package org.sheedon.repository;

import org.sheedon.repository.data.RspModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 网络请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:41 上午
 */
public abstract class AbstractNetRequestStrategy<RequestCard, ResponseModel>
        extends BaseRequestStrategy<RequestCard, ResponseModel> {

    private Disposable disposable;

    public AbstractNetRequestStrategy(StrategyHandle.StrategyCallback<ResponseModel> callback) {
        super(callback);
    }

    /**
     * 请求操作
     *
     * @param requestCard 请求卡片
     */
    @Override
    public void request(RequestCard requestCard) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = onLoadApiMethod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rspModel -> {
                    if (callback == null)
                        return;

                    if (rspModel.isSuccess()) {
                        callback.onDataLoaded(rspModel.getData(), DefaultStrategyHandler.PROGRESS.REQUEST_NETWORK);
                        return;
                    }

                    callback.onDataNotAvailable(rspModel.getMessage(), DefaultStrategyHandler.PROGRESS.REQUEST_NETWORK);
                }, throwable -> {
                    if (callback == null)
                        return;

                    callback.onDataNotAvailable(throwable.getMessage(), DefaultStrategyHandler.PROGRESS.REQUEST_NETWORK);
                });
    }

    /**
     * 加载API 方法
     */
    protected abstract Observable<RspModel<ResponseModel>> onLoadApiMethod();

    /**
     * 请求类型 - 网络请求
     */
    @Override
    public int onRequestType() {
        return DefaultStrategyHandler.REQUEST.TYPE_NETWORK_REQUEST;
    }

    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = null;
    }
}
