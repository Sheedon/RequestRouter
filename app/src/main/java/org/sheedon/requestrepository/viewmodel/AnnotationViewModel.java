package org.sheedon.requestrepository.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.annotation.LoginRouter;
import org.sheedon.rrouter.facade.annotation.Request;
import org.sheedon.rrouter.facade.router.IComponent;

/**
 * 通过请求路由注解的方式实现调度
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/12 7:36 下午
 */
public class AnnotationViewModel extends ViewModel
        implements AnnotationViewModelComponent.OnCallbackListener {

    private static final String TAG = AnnotationViewModel.class.getSimpleName();

    @Request
    LoginRouter loginRouter;

    private IComponent component;

    public void initConfig() {
        component = AnnotationViewModelComponent.create(this, this);
    }

    public void login(String name, String password) {
        // 后续这部分改成kotlin 调用方法 来实现
        LoginRouter.LoginRequestBodyAdapter requestAdapter = loginRouter.requestAdapter();
        loginRouter.request(requestAdapter.attach(name, password));
    }

    @Override
    public void onLoginRouterDataLoaded(LoginModel responseModel) {
        Log.v(TAG, "result:" + responseModel.getAccessToken());
    }

    @Override
    public void onDataNotAvailable(String type, String message) {
        Log.v(TAG, "type:" + type);
        Log.v(TAG, "message:" + message);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (component != null) {
            component.onDestroy();
        }
        component = null;
    }
}
