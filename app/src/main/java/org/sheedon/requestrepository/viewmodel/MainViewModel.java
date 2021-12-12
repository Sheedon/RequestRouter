package org.sheedon.requestrepository.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.login.LoginRequest;

/**
 * 基础模式下使用请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 3:00 下午
 */
public class MainViewModel extends ViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    // 账户
    public String account;
    // 密码
    public String password;

    private LoginRequest loginRequest;

    /**
     * 登陆动作
     */
    public void loginClick() {
        if (account == null || account.isEmpty()
                || password == null || password.isEmpty()) {
            Log.v(TAG,"账号密码不能为空!");
            return;
        }

        LoginRequest request = getLoginRequest();
        request.login(account, password);
    }

    /**
     * 登陆请求
     */
    private LoginRequest getLoginRequest() {
        if (loginRequest == null) {
            loginRequest = new LoginRequest(new DataSource.Callback<LoginModel>() {
                @Override
                public void onDataNotAvailable(String message) {
                    Log.v(TAG,message);
                }

                @Override
                public void onDataLoaded(LoginModel loginModel) {
                    Log.v(TAG,"user: " + loginModel.getAccessToken());
                }
            });
        }
        return loginRequest;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (loginRequest != null) {
            loginRequest.destroy();
        }
        loginRequest = null;
    }
}
