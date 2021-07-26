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

    /**
     * 构建返回项
     *
     * @param code    返回状态编码
     * @param message 描述信息
     * @param data    数据
     * @param <T>     数据类型
     * @return RspModel<T>
     */
    public static <T> RspModel<T> build(String code, String message, T data) {
        RspModel<T> model = new RspModel();
        model.code = code;
        model.message = message;
        model.data = data;
        return model;
    }

    /**
     * 构建成功项
     */
    public static <T> RspModel<T> buildToSuccess(T data) {
        return build("0000", "", data);
    }

    /**
     * 构建失败项
     */
    public static <T> RspModel<T> buildToFailure(String message) {
        return build("-1", message, null);
    }
}