package org.sheedon.requestrepository.request.login.real;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.sheedon.repository.AbstractNetRequestStrategy;
import org.sheedon.repository.StrategyHandle;
import org.sheedon.repository.data.RspModel;
import org.sheedon.repository.plugin.MergeObservablePlugin;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;

import java.lang.reflect.Type;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * 网络登陆
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
public class LoginNetWorkRequest extends AbstractNetRequestStrategy<LoginCard, LoginModel> {


    public LoginNetWorkRequest(StrategyHandle.StrategyCallback<LoginModel> callback) {
        super(callback);

    }

    /**
     * 加载登陆method
     */
    @Override
    protected Observable<RspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        LoginMergeObservable observable = new LoginMergeObservable();
        return observable.onLoadMethod(create(), create());
    }

    private Observable<RspModel<?>> create() {
        return Observable.create(emitter -> {
            TimeUnit.SECONDS.sleep(1);
            if (new Random().nextInt(10) > 5) {
                Type type = new TypeToken<RspModel<LoginModel>>() {
                }.getType();
                RspModel<LoginModel> loginModel = new Gson().fromJson("{\"code\":\"0000\",\"message\":\"请求成功\",\"data\":{\"accessToken\":\"83adf25d-718d-4d9b-93a3-614d03a2ed4d\",\"userId\":\"3be59c12b0c24f789e5bfcaa8da84aef\"}}", type);
                emitter.onNext(loginModel);
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable("网络错误"));
            }
        });
    }

    private class LoginMergeObservable extends MergeObservablePlugin<LoginCard, LoginModel> {

        @Override
        protected LoginModel createResponseModel() {
            return new LoginModel();
        }

        @Override
        protected void fillResponseModel(RspModel<?> rspModel) {
            LoginModel model = (LoginModel) rspModel.getData();
            responseModel.setUserId(model.getUserId());
            responseModel.setAccessToken(model.getAccessToken());
        }
    }
}
