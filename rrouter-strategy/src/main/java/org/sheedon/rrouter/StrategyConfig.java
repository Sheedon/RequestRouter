/*
 * Copyright (C) 2022 Sheedon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sheedon.rrouter;

import android.util.SparseArray;

import org.sheedon.rrouter.strategy.NotDataToLocationStrategyHandler;
import org.sheedon.rrouter.strategy.NotDataToRemoteStrategyHandler;
import org.sheedon.rrouter.strategy.OnlyLocalStrategyHandler;
import org.sheedon.rrouter.strategy.OnlyRemoteStrategyHandler;
import org.sheedon.rrouter.strategy.SyncRemoteAndLocationStrategyHandler;
import org.sheedon.rrouter.strategy.parameter.DefaultRequestType;
import org.sheedon.rrouter.strategy.parameter.DefaultStrategy;

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
    interface STRATEGY extends DefaultStrategy {
    }

    /**
     * 请求类型
     */
    interface REQUEST extends DefaultRequestType {
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
