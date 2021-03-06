package org.sheedon.requestrepository.request.login.real;

import org.sheedon.requestrepository.RspModel;
import org.sheedon.rrouter.AbstractLocalRequestStrategy;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.core.support.IRspModel;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;

import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;

/**
 * 本地登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
public class LoginLocalRequest extends AbstractLocalRequestStrategy<LoginCard, RspModel<LoginModel>> {

    public LoginLocalRequest(StrategyCallback<RspModel<LoginModel>> callback) {
        super(callback);
    }

    @Override
    protected Observable<RspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        if (loginCard != null && Objects.equals(loginCard.getUserName(), "admin")
                && Objects.equals(loginCard.getPassword(), "root")) {
            return Observable.just(RspModel.buildToSuccess(LoginModel.build()));
        } else {
            return Observable.just(RspModel.buildToFailure("账号密码错误"));
        }
    }
}
