package org.sheedon.repository;

import org.sheedon.repository.data.RspModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 基础请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 11:33 上午
 */
public abstract class BaseRequestStrategy<RequestCard, ResponseModel>
        implements Request<RequestCard> {

    protected StrategyHandle.StrategyCallback<ResponseModel> callback;
    private Disposable disposable;
    private boolean complete = false;

    public BaseRequestStrategy(StrategyHandle.StrategyCallback<ResponseModel> callback) {
        this.callback = callback;
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

        disposable = onLoadMethod(requestCard)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rspModel -> {
                    if (callback == null) {
                        complete = true;
                        return;
                    }

                    if (rspModel == null) {
                        callback.onDataNotAvailable(RepositoryContract.NOT_BACK_DATA_ERROR,
                                onProgressType());
                        complete = true;
                        return;
                    }

                    if (rspModel.isSuccess()) {
                        callback.onDataLoaded(rspModel.getData(), onProgressType());
                        complete = true;
                        return;
                    }

                    callback.onDataNotAvailable(rspModel.getMessage(), onProgressType());
                    complete = true;
                }, throwable -> {
                    if (callback == null) {
                        complete = true;
                        return;
                    }

                    callback.onDataNotAvailable(throwable.getMessage(), onProgressType());
                    complete = true;
                });
    }

    /**
     * 加载API 方法
     */
    protected abstract Observable<RspModel<ResponseModel>> onLoadMethod(RequestCard requestCard);

    /**
     * 进度类型
     */
    protected abstract int onProgressType();

    @Override
    public boolean isComplete() {
        return complete;
    }

    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        callback = null;
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        disposable = null;
    }
}
