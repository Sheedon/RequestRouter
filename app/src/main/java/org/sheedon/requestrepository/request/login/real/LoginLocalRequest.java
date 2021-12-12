package org.sheedon.requestrepository.request.login.real;


import org.sheedon.repository.AbstractLocalRequestStrategy;
import org.sheedon.repository.StrategyHandle;
import org.sheedon.repository.data.RspModel;
import org.sheedon.repository.strategy.StrategyConfig;
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
public class LoginLocalRequest extends AbstractLocalRequestStrategy<LoginCard, LoginModel> {

    public LoginLocalRequest(StrategyHandle.StrategyCallback<LoginModel> callback) {
        super(callback);
    }

    @Override
    public void request(LoginCard loginCard) {
        if (callback == null)
            return;

        if (loginCard != null && Objects.equals(loginCard.getUserName(), "admin")
                && Objects.equals(loginCard.getPassword(), "root")) {
            callback.onDataLoaded(LoginModel.build(), StrategyConfig.PROGRESS.REQUEST_LOCAL);
        } else {
            callback.onDataNotAvailable("账号密码错误!",StrategyConfig.PROGRESS.REQUEST_LOCAL);
        }
    }

    @Override
    protected Observable<RspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        return null;
    }
}
