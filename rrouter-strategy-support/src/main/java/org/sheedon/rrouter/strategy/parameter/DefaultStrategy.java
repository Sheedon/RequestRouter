package org.sheedon.rrouter.strategy.parameter;

/**
 * 默认的请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/15 10:00 下午
 */
public interface DefaultStrategy {
    int TYPE_ONLY_REMOTE = 0;// 单一远程（网络）请求
    int TYPE_NOT_DATA_TO_REMOTE = 1;// 优先本地，无数据取远程（网络）
    int TYPE_SYNC_REMOTE_AND_LOCATION = 2;// 同步请求，本地和远程（网络）
    int TYPE_NOT_DATA_TO_LOCATION = 3;//优先远程（网络）请求，远程（网络）请求失败，搜索本地数据 「类似无网络登陆」
    int TYPE_ONLY_LOCAL = 4;// 单一本地请求
}
