package org.sheedon.rrouter;

import android.util.SparseArray;

import org.sheedon.rrouter.strategy.NotDataToLocationStrategyHandler;
import org.sheedon.rrouter.strategy.NotDataToRemoteStrategyHandler;
import org.sheedon.rrouter.strategy.OnlyLocalStrategyHandler;
import org.sheedon.rrouter.strategy.OnlyRemoteStrategyHandler;
import org.sheedon.rrouter.strategy.SyncRemoteAndLocationStrategyHandler;

/**
 * 策略配置项
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/3 12:24 上午
 */
public interface StrategyConfig {

    /**
     * 策略
     */
    interface STRATEGY {
        int TYPE_ONLY_REMOTE = 0;// 单一远程（网络）请求
        int TYPE_NOT_DATA_TO_REMOTE = 1;// 优先本地，无数据取远程（网络）
        int TYPE_SYNC_REMOTE_AND_LOCATION = 2;// 同步请求，本地和远程（网络）
        int TYPE_NOT_DATA_TO_LOCATION = 3;//优先远程（网络）请求，远程（网络）请求失败，搜索本地数据 「类似无网络登陆」
        int TYPE_ONLY_LOCAL = 4;// 单一本地请求
    }

    /**
     * 请求类型
     */
    interface REQUEST {
        int TYPE_REMOTE_REQUEST = 1;// 远程请求
        int TYPE_LOCAL_REQUEST = 2;// 本地请求
    }


    /**
     * 策略执行集合
     */
    SparseArray<StrategyHandle> strategyHandlerArray = new SparseArray<StrategyHandle>() {
        {
            put(STRATEGY.TYPE_ONLY_REMOTE, new OnlyRemoteStrategyHandler());
            put(STRATEGY.TYPE_NOT_DATA_TO_REMOTE, new NotDataToRemoteStrategyHandler());
            put(STRATEGY.TYPE_SYNC_REMOTE_AND_LOCATION, new SyncRemoteAndLocationStrategyHandler());
            put(STRATEGY.TYPE_NOT_DATA_TO_LOCATION, new NotDataToLocationStrategyHandler());
            put(STRATEGY.TYPE_ONLY_LOCAL, new OnlyLocalStrategyHandler());
        }
    };
}
