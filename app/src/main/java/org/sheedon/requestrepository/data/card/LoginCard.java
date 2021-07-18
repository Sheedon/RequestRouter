package org.sheedon.requestrepository.data.card;

/**
 * 登陆卡片
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/18 2:09 下午
 */
public class LoginCard {

    private String userName;
    private String password;


    public void update(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
