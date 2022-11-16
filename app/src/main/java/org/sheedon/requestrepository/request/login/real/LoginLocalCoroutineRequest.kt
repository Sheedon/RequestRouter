package org.sheedon.requestrepository.request.login.real

import kotlinx.coroutines.CoroutineScope
import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.requestrepository.request.config.coroutine.AbstractLocalRequestStrategy

/**
 * 本地登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
class LoginLocalCoroutineRequest(
    coroutineScope: CoroutineScope,
    callback: StrategyCallback<RspModel<LoginModel>>
) : AbstractLocalRequestStrategy<LoginCard, RspModel<LoginModel>>(coroutineScope, callback) {
    override suspend fun onLoadMethod(requestCard: LoginCard): RspModel<LoginModel> {
        return if (requestCard.userName == "admin" && requestCard.password == "root"
        ) {
            RspModel.buildToSuccess(LoginModel.build())
        } else {
            RspModel.buildToFailure("账号密码错误")
        }
    }
}