package org.sheedon.requestrepository.request.login

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.data.card.LoginCard
import org.sheedon.requestrepository.request.login.real.LoginLocalCoroutineRequest
import org.sheedon.requestrepository.request.login.real.LoginRemoteCoroutineRequest
import org.sheedon.rrouter.strategy.BaseRequestStrategyFactory
import org.sheedon.rrouter.core.DataSource
import org.sheedon.rrouter.core.Request
import org.sheedon.rrouter.core.StrategyCallback
import org.sheedon.rrouter.strategy.StrategyConfig

/**
 * 登陆请求
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:08 下午
 */
class LoginRouter(
    callback: DataSource.Callback<RspModel<LoginModel>>,
    coroutineScope: CoroutineScope
) : BaseCoroutineRequest<LoginCard, RspModel<LoginModel>>(callback,coroutineScope) {
    // 请求卡片
    private val requestCard = LoginCard()
    override fun loadRequestCard(): LoginCard {
        return requestCard
    }

    init {
        Log.v("SXD", "coroutineScope:$coroutineScope")
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
    override fun createRequestStrategyFactory(coroutineScope: CoroutineScope): BaseRequestStrategyFactory<LoginCard, RspModel<LoginModel>> {
        return LoginRequestCoroutineStrategy(coroutineScope)
    }
}

class LoginRequestCoroutineStrategy(private val coroutineScope: CoroutineScope) :
    BaseRequestStrategyFactory<LoginCard, RspModel<LoginModel>>() {
    /**
     * 真实网络请求策略
     */
    override fun onCreateRealRemoteRequestStrategy(
        callback: StrategyCallback<RspModel<LoginModel>>?
    ): Request<LoginCard> {
        return LoginRemoteCoroutineRequest(coroutineScope, callback!!)
    }

    /**
     * 真实本地请求策略
     */
    override fun onCreateRealLocalRequestStrategy(
        callback: StrategyCallback<RspModel<LoginModel>>?
    ): Request<LoginCard> {
        return LoginLocalCoroutineRequest(coroutineScope, callback!!)
    }

    /**
     * 优先网络请求登陆
     * 否则按照本地保留的账号记录登陆操作
     */
    override fun onLoadRequestStrategyType(): Int {
        return StrategyConfig.STRATEGY.TYPE_SYNC_REMOTE_AND_LOCATION
    }
}