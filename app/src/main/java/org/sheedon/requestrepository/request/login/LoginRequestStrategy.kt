package org.sheedon.requestrepository.request.login

import org.sheedon.rrouter.strategy.BaseRequestStrategyFactory
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.requestrepository.request.login.real.LoginRemoteRequest
import org.sheedon.requestrepository.request.login.real.LoginLocalRequest
import org.sheedon.rrouter.core.Request
import org.sheedon.rrouter.strategy.StrategyConfig

/**
 * 登陆请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/22 12:15 上午
 */
class LoginRequestStrategy : BaseRequestStrategyFactory<LoginCard, RspModel<LoginModel>>() {
    /**
     * 真实网络请求策略
     */
    override fun onCreateRealRemoteRequestStrategy(
        callback: StrategyCallback<RspModel<LoginModel>>?
    ): Request<LoginCard> {
        return LoginRemoteRequest(callback!!)
    }

    /**
     * 真实本地请求策略
     */
    override fun onCreateRealLocalRequestStrategy(
        callback: StrategyCallback<RspModel<LoginModel>>?
    ): Request<LoginCard> {
        return LoginLocalRequest(callback!!)
    }

    /**
     * 优先网络请求登陆
     * 否则按照本地保留的账号记录登陆操作
     */
    override fun onLoadRequestStrategyType(): Int {
        return StrategyConfig.STRATEGY.TYPE_SYNC_REMOTE_AND_LOCATION
    }
}