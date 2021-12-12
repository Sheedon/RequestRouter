package org.sheedon.requestrepository.request.login;


import org.sheedon.repository.BaseRequestStrategyFactory;
import org.sheedon.repository.data.DataSource;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.BaseRequest;

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
        return new LoginRequestStrategy();
    }
}
