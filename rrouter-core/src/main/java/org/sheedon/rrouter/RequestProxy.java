package org.sheedon.rrouter;

/**
 * 请求策略代理基本接口
 * 需要履行的职责
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 4:33 下午
 */
public interface RequestProxy {

    /**
     * 代理请求入口
     */
    void request();

    /**
     * 销毁
     */
    void onDestroy();

    /**
     * 请求策略代理工厂类
     * 代理请求Class，多策略配置的请求操作
     */
    abstract class RequestProxyFactory implements RequestProxy {


        /**
         * 绑定策略执行器
         * 执行请求组策略
         */
        protected abstract StrategyHandle.Responsibilities bindStrategyHandler();
    }
}
