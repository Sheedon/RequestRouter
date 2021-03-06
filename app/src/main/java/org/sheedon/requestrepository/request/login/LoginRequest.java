package org.sheedon.requestrepository.request.login;


import org.sheedon.requestrepository.RspModel;
import org.sheedon.rrouter.BaseRequestStrategyFactory;
import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;

/**
 * 登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:08 下午
 */
public class LoginRequest extends BaseRequest<LoginCard, RspModel<LoginModel>> {

    // 请求卡片
    private final LoginCard requestCard = new LoginCard();

    public LoginRequest(DataSource.Callback<RspModel<LoginModel>> callback) {
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
    protected BaseRequestStrategyFactory<LoginCard, RspModel<LoginModel>> createRequestStrategyFactory() {
        return new LoginRequestStrategy();
    }
}
