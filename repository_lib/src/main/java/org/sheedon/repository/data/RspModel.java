package org.sheedon.repository.data;

/**
 * 网络数据结果
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/6/24 5:03 下午
 */
public class RspModel<T> {

    private String code;
    private String message;
    private T data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return code != null && code.equals("0000");
    }
}
