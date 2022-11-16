package org.sheedon.requestrepository.request.login

import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.rrouter.strategy.BaseRequestStrategyFactory
import org.sheedon.rrouter.core.DataSource

/**
 * 登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:08 下午
 */
class LoginRequest(callback: DataSource.Callback<RspModel<LoginModel>>) :
    BaseRequest<LoginCard, RspModel<LoginModel>>(callback) {
    // 请求卡片
    private val requestCard = LoginCard()
    override fun loadRequestCard(): LoginCard {
        return requestCard
    }

    /**
     * 登陆
     *
     * @param account  账号
     * @param password 密码
     */
    fun login(account: String?, password: String?) {
        requestCard.update(account, password)
        proxy.request()
    }

    /**
     * 创建请求策略工厂
     */
    override fun createRequestStrategyFactory(): BaseRequestStrategyFactory<LoginCard, RspModel<LoginModel>> {
        return LoginRequestStrategy()
    }
}