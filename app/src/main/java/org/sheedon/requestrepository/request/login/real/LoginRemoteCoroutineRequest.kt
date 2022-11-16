package org.sheedon.requestrepository.request.login.real

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.requestrepository.request.config.coroutine.AbstractRemoteRequestStrategy

/**
 * 网络登陆
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
class LoginRemoteCoroutineRequest(
    coroutineScope: CoroutineScope,
    callback: StrategyCallback<RspModel<LoginModel>>
) : AbstractRemoteRequestStrategy<LoginCard, RspModel<LoginModel>>(
    coroutineScope,
    callback
) {
    override suspend fun onLoadMethod(loginCard: LoginCard): RspModel<LoginModel> {
//        return Observable.just(RspModel.buildToSuccess(LoginModel.build()))
        return withContext(Dispatchers.IO) {
            RspModel.buildToFailure("账号密码错误！！！！")
        }
    }
}