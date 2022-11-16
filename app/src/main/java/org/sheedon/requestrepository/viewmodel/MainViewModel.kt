package org.sheedon.requestrepository.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import org.sheedon.requestrepository.request.login.LoginRequest
import org.sheedon.requestrepository.RspModel
import org.sheedon.requestrepository.data.model.LoginModel
import org.sheedon.rrouter.core.DataSource

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
        val request = getLoginRequest()
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
                    Log.v(TAG, "user: " + t?.getData()?.accessToken)
                }
            })
        }
        return loginRequest!!
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