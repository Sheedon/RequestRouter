package org.sheedon.repository.plugin;


import org.sheedon.repository.data.RspModel;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 观察者组合插件
 * 多重数据请求，统一反馈处理
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/25 3:29 下午
 */
public abstract class MergeObservablePlugin<RequestCard,ResponseModel> {

    protected final ResponseModel responseModel = createResponseModel();
    private Disposable disposable;


    /**
     * 创建反馈Model
     */
    protected abstract ResponseModel createResponseModel();

    /**
     * 加载API 方法
     */
    @SafeVarargs
    public final Observable<RspModel<ResponseModel>>
    onLoadMethod(Observable<RspModel<?>>... observables) {
        return Observable.create(emitter -> doMergeArrayObservable(emitter, observables));
    }

    /**
     * 执行组合注册
     *
     * @param emitter     发射器
     * @param observables 观察者队列
     */
    @SafeVarargs
    private final void doMergeArrayObservable(ObservableEmitter<RspModel<ResponseModel>> emitter,
                                              Observable<RspModel<?>>... observables) {
        Observable.mergeArrayDelayError(observables)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<RspModel<?>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull RspModel<?> rspModel) {
                        fillResponseModel(rspModel);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        emitter.onNext(RspModel.buildToSuccess(responseModel));
                        if (e instanceof CompositeException) {
                            CompositeException compositeException = (CompositeException) e;
                            List<Throwable> exceptions = compositeException.getExceptions();
                            StringBuilder error = new StringBuilder();
                            if (exceptions.size() > 0) {
                                for (Throwable exception : exceptions) {
                                    error.append(exception.getMessage()).append("|");
                                }
                            }
                            emitter.onError(new Throwable(error.toString()));
                        } else {
                            emitter.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        emitter.onNext(RspModel.buildToSuccess(responseModel));
                        emitter.onComplete();
                    }
                });
    }

    /**
     * 填充结果Model
     *
     * @param rspModel 反馈Model
     */
    protected abstract void fillResponseModel(RspModel<?> rspModel);

    /**
     * 销毁
     */
    public void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = null;
    }
}
