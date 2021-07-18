package org.sheedon.requestrepository.data.model;

import java.util.UUID;

/**
 * 登陆结果反馈
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:10 下午
 */
public class LoginModel {

    private String accessToken;
    private String userId;

    public static LoginModel build() {
        LoginModel model = new LoginModel();
        model.userId = UUID.randomUUID().toString();
        model.accessToken = UUID.randomUUID().toString().replace("-", "");
        return model;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
