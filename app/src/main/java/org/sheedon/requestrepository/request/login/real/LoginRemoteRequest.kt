package org.sheedon.requestrepository.request.login.real

import io.reactivex.rxjava3.core.Observable
import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.requestrepository.request.config.rxjava.AbstractRemoteRequestStrategy

/**
 * 网络登陆
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:21 下午
 */
class LoginRemoteRequest(callback: StrategyCallback<RspModel<LoginModel>>) :
    AbstractRemoteRequestStrategy<LoginCard, RspModel<LoginModel>>(callback) {
    override fun onLoadMethod(loginCard: LoginCard): Observable<RspModel<LoginModel>> {
//        return Observable.just(RspModel.buildToSuccess(LoginModel.build()))
        return Observable.just(RspModel.buildToFailure("账号密码错误！！！！"))
    }
}