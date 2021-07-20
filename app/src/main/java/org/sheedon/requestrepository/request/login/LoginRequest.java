package org.sheedon.requestrepository.request.login;

import org.sheedon.repository.BaseRequestStrategyFactory;
import org.sheedon.repository.DefaultStrategyHandler;
import org.sheedon.repository.Request;
import org.sheedon.repository.StrategyHandle;
import org.sheedon.repository.data.DataSource;
import org.sheedon.repository.strategy.StrategyConfig;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.BaseRequest;
import org.sheedon.requestrepository.request.login.real.LoginLocalRequest;
import org.sheedon.requestrepository.request.login.real.LoginNetWorkRequest;

/**
 * 登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:08 下午
 */
public class LoginRequest extends BaseRequest<LoginCard, LoginModel> {

    // 请求卡片
    private final LoginCard requestCard = new LoginCard();

    public LoginRequest(DataSource.Callback<LoginModel> callback) {
        super(callback);
    }

    @Override
    protected LoginCard loadRequestCard() {
        return requestCard;
    }

    /**
     * 登陆
     *
     * @param account  账号
     * @param password 密码
     */
    public void login(String account, String password) {
        requestCard.update(account, password);
        proxy.request();
    }

    /**
     * 创建请求策略工厂
     */
    @Override
    protected BaseRequestStrategyFactory<LoginCard, LoginModel> createRequestStrategyFactory() {
        return new BaseRequestStrategyFactory<LoginCard, LoginModel>() {
            /**
             * 真实网络请求策略
             */
            @Override
            protected Request<LoginCard> onCreateRealNetworkRequestStrategy(
                    StrategyHandle.StrategyCallback<LoginModel> callback) {
                return new LoginNetWorkRequest(callback);
            }

            /**
             * 真实本地请求策略
             */
            @Override
            protected Request<LoginCard> onCreateRealLocalRequestStrategy(
                    StrategyHandle.StrategyCallback<LoginModel> callback) {
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
        };
    }
}
