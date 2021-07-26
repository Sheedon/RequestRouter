package org.sheedon.repository.strategy;

/**
 * 策略配置项
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:30 下午
 */
public interface StrategyConfig {

    /**
     * 策略
     */
    interface STRATEGY {
        int TYPE_ONLY_NETWORK = 0x1;// 单一网络请求
        int TYPE_NOT_DATA_TO_NET = 0x2;// 优先本地，无数据取网络
        int TYPE_SYNC_NETWORK_AND_LOCATION = 0x4;// 同步请求，本地和网络
        int TYPE_NOT_DATA_TO_LOCATION = 0x8;//优先网络请求，网络请求失败，搜索本地数据 「类似无网络登陆」
    }

    /**
     * 请求流程状态 整体分为 3 个状态，
     * 准备开始请求，请求中，请求完成
     * 请求中 分为两种状态：本地请求中 和 网络请求中。
     * <p>
     * 根据不同状态，需要执行不同的请求和反馈的策略
     */
    interface PROGRESS {
        // 准备开始请求
        int START = 0x1;
        // 本地请求中
        int REQUEST_LOCAL = 0x2;
        // 网络请求中
        int REQUEST_NETWORK = 0x4;
        // 请求完成
        int COMPLETE = 0x8;
        // 请求流程等错误
        int ERROR = 0x10;
    }

    /**
     * 请求类型
     */
    interface REQUEST {
        int TYPE_NETWORK_REQUEST = 1;// 网络请求
        int TYPE_LOCAL_REQUEST = 2;// 本地请求
    }
}
