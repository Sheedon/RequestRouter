package org.sheedon.requestrepository.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import org.sheedon.requestrepository.request.login.LoginRequest
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.requestrepository.request.login.LoginRouter
import org.sheedon.rrouter.core.DataSource

import androidx.lifecycle.viewModelScope

/**
 * 基础模式下使用请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 3:00 下午
 */
class MainViewModel : ViewModel() {
    // 账户
    @JvmField
    var account: String? = null

    // 密码
    @JvmField
    var password: String? = null
    private var loginRequest: LoginRequest? = null
    private var loginRouter: LoginRouter? = null

    /**
     * 登陆动作
     */
    fun loginClick() {
        if (account == null || account!!.isEmpty()
            || password == null || password!!.isEmpty()
        ) {
            Log.v(TAG, "账号密码不能为空!")
            return
        }
//        val request = getLoginRequest()
        val request = getLoginRouter()
        request.login(account, password)
    }

    /**
     * 登陆请求
     */
    private fun getLoginRequest(): LoginRequest {
        if (loginRequest == null) {
            loginRequest = LoginRequest(object : DataSource.Callback<RspModel<LoginModel>> {
                override fun onDataNotAvailable(message: String?) {
                    Log.v(TAG, message!!)
                }

                override fun onDataLoaded(t: RspModel<LoginModel>?) {
                    Log.v(TAG, "user: " + t?.loadData()?.accessToken)
                }
            })
        }
        return loginRequest!!
    }

    private fun getLoginRouter(): LoginRouter {
        if (loginRouter == null) {
            loginRouter = LoginRouter(object : DataSource.Callback<RspModel<LoginModel>> {
                override fun onDataNotAvailable(message: String?) {
                    Log.v(TAG, message!!)
                }

                override fun onDataLoaded(t: RspModel<LoginModel>?) {
                    Log.v(TAG, "user: " + t?.loadData()?.accessToken)
                }
            },viewModelScope)
        }
        return loginRouter!!
    }

    override fun onCleared() {
        super.onCleared()
        if (loginRequest != null) {
            loginRequest!!.destroy()
        }
        loginRequest = null
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}