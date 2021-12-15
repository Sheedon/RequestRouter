package org.sheedon.requestrepository.request.login.real;

import org.sheedon.rrouter.AbstractRemoteRequestStrategy;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.strategy.model.IRspModel;
import org.sheedon.requestrepository.RspModel;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;

import java.util.Random;

import io.reactivex.rxjava3.core.Observable;

/**
 * 网络登陆
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
public class LoginRemoteRequest extends AbstractRemoteRequestStrategy<LoginCard, LoginModel> {


    public LoginRemoteRequest(StrategyCallback<LoginModel> callback) {
        super(callback);

    }

    @Override
    protected Observable<IRspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        return Observable.just(RspModel.buildToSuccess(LoginModel.build()));
    }


}
