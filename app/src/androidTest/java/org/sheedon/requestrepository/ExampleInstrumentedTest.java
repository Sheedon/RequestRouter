package org.sheedon.requestrepository;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sheedon.repository.data.DataSource;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.requestrepository.request.login.LoginRequest;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        LoginRequest request = new LoginRequest(new DataSource.Callback<LoginModel>() {
            @Override
            public void onDataNotAvailable(String message) {
                System.out.println("message:" + message);
            }

            @Override
            public void onDataLoaded(LoginModel loginModel) {
                System.out.println("loginModel:" + loginModel.getUserId());
            }
        });
        request.login("zhangsan","123456");
    }
}