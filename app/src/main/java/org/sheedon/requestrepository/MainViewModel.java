package org.sheedon.requestrepository;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.sheedon.rrouter.DataSource;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.login.LoginRequest;

/**
 * 主页面-登陆操作
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 3:00 下午
 */
public class MainViewModel extends ViewModel {

    // 结果
    public MutableLiveData<String> result = new MutableLiveData<>();


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
            result.postValue("账号密码不能为空!");
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
                    result.postValue(message);
                }

                @Override
                public void onDataLoaded(LoginModel loginModel) {
                    result.postValue("user: " + loginModel.getUserId());
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
