package org.sheedon.repository.strategy;

import android.util.SparseArray;

import org.sheedon.repository.StrategyHandle;

/**
 * 策略执行者工厂实现
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 5:26 下午
 */
public final class StrategyHandlerFactory extends StrategyHandle.Factory {

    private final SparseArray<StrategyHandle> strategyHandlerArray = new SparseArray<>();

    public StrategyHandlerFactory() {
        createStrategyHandlerArray();
    }

    /**
     * 创建加载策略执行集合
     */
    private void createStrategyHandlerArray() {
        strategyHandlerArray.put(StrategyConfig.STRATEGY.TYPE_ONLY_NETWORK,
                new OnlyNetworkStrategyHandler());
        strategyHandlerArray.put(StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_NET,
                new NotDataToNetworkStrategyHandler());
        strategyHandlerArray.put(StrategyConfig.STRATEGY.TYPE_SYNC_NETWORK_AND_LOCATION,
                new SyncNetworkAndLocationStrategyHandler());
        strategyHandlerArray.put(StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_LOCATION,
                new NotDataToLocationStrategyHandler());
    }

    /**
     * 根据请求策略类型加载策略执行者
     *
     * @param requestStrategyType 请求策略类型
     * @return StrategyHandle
     */
    @Override
    public StrategyHandle loadStrategyHandler(int requestStrategyType) {
        StrategyHandle handle = strategyHandlerArray.get(requestStrategyType);
        if (handle != null) {
            return handle;
        } else {
            return super.loadStrategyHandler(requestStrategyType);
        }
    }
}
