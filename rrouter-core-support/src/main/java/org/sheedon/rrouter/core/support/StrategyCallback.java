package org.sheedon.rrouter.core.support;

/**
 * 策略回调监听器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 10:26 下午
 */
public interface StrategyCallback<T> {
    // 数据加载成功, 请求成功
    void onDataLoaded(T t);

    // 数据加载失败, 请求失败
    void onDataNotAvailable(String message);
}
