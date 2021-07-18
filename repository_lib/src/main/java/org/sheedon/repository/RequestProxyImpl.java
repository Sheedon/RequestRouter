package org.sheedon.repository;

/**
 * 请求代理基本接口
 * 需要履行的职责：
 * -
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 11:16 下午
 */
public interface RequestProxyImpl {


    /**
     * 绑定请求调度策略执行器，所配置的请求策略皆由这个执行器来分配执行。
     *
     * @return StrategyHandle
     */
    StrategyHandle bindStrategyHandler();

    /**
     * 代理请求入口
     */
    void request();

    /**
     * 销毁
     */
    void onDestroy();

}
