package org.sheedon.rrouter.strategy.model;

/**
 * 基础数据结果反馈接收接口
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/3 12:18 上午
 */
public interface IRspModel<T> {

    T getData();

    boolean isSuccess();

    String getMessage();

}
