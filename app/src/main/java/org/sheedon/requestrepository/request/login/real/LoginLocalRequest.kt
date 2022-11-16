package org.sheedon.requestrepository.request.login.real

import io.reactivex.rxjava3.core.Observable
import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.requestrepository.request.config.rxjava.AbstractLocalRequestStrategy

/**
 * 本地登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
class LoginLocalRequest(callback: StrategyCallback<RspModel<LoginModel>>) :
    AbstractLocalRequestStrategy<LoginCard, RspModel<LoginModel>>(callback) {
    protected override fun onLoadMethod(loginCard: LoginCard): Observable<RspModel<LoginModel>> {
        return if (loginCard.userName == "admin" && loginCard.password == "root"
        ) {
            Observable.just(
                RspModel.buildToSuccess(
                    LoginModel.build()
                )
            )
        } else {
            Observable.just(
                RspModel.buildToFailure(
                    "账号密码错误"
                )
            )
        }
    }
}