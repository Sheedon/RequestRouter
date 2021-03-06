package org.sheedon.requestrepository.request.login.real;

import org.sheedon.rrouter.AbstractRemoteRequestStrategy;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.requestrepository.RspModel;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;

import io.reactivex.rxjava3.core.Observable;

/**
 * 网络登陆
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
public class LoginRemoteRequest extends AbstractRemoteRequestStrategy<LoginCard, RspModel<LoginModel>> {


    public LoginRemoteRequest(StrategyCallback<RspModel<LoginModel>> callback) {
        super(callback);

    }

    @Override
    protected Observable<RspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        return Observable.just(RspModel.buildToSuccess(LoginModel.build()));
    }


}
