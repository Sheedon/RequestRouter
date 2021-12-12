package org.sheedon.requestrepository.request.login;

import org.sheedon.rrouter.BaseRequestStrategyFactory;
import org.sheedon.rrouter.StrategyConfig;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.login.real.LoginLocalRequest;
import org.sheedon.requestrepository.request.login.real.LoginNetWorkRequest;
import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;

/**
 * 登陆请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/22 12:15 上午
 */
public class LoginRequestStrategy extends BaseRequestStrategyFactory<LoginCard, LoginModel> {

    /**
     * 真实网络请求策略
     */
    @Override
    protected Request<LoginCard> onCreateRealRemoteRequestStrategy(
            StrategyCallback<LoginModel> callback) {
        return new LoginNetWorkRequest(callback);
    }

    /**
     * 真实本地请求策略
     */
    @Override
    protected Request<LoginCard> onCreateRealLocalRequestStrategy(
            StrategyCallback<LoginModel> callback) {
        return new LoginLocalRequest(callback);
    }

    /**
     * 优先网络请求登陆
     * 否则按照本地保留的账号记录登陆操作
     */
    @Override
    public int onLoadRequestStrategyType() {
        return StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_LOCATION;
    }
}
