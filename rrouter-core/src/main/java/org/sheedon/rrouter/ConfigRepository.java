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

import org.sheedon.rrouter.core.support.IRspModel;

import java.util.Objects;

/**
 * 配置类，用于配置
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 9:42 下午
 */
public class ConfigRepository {

    // 组策略执行者
    private final StrategyHandle.Responsibilities strategyHandler;
    private final Converter<?, IRspModel<?>> rspConverter;

    private ConfigRepository(Builder builder) {
        strategyHandler = builder.strategyHandler;
        rspConverter = builder.rspConverter.createCheckConverter();
    }

    StrategyHandle.Responsibilities getStrategyHandler() {
        return strategyHandler;
    }

    public Converter<?, IRspModel<?>> getRspConverter() {
        return rspConverter;
    }

    /**
     * 构建者类中获取
     */
    private static StrategyHandle.ResponsibilityFactory getDefaultHandler() {
        if (RRouter.isInstalled()) {
            throw new RuntimeException("Please do not repeat settings");
        }
        return DefaultStrategyHandler.HANDLER;
    }

    public static class Builder {

        // 组策略执行者
        private StrategyHandle.ResponsibilityFactory strategyHandler;
        // 请求策略工厂类
        private StrategyHandle.Factory factory;
        // 请求类型策略处理集合
        private SparseArray<StrategyHandle> strategyArray;
        // 默认结果核实转换器
        private Converter.Factory rspConverter;

        public Builder() {

        }

        /**
         * 策略执行者
         *
         * @param strategyHandler 策略执行者
         * @return Builder
         */
        public Builder strategyHandler(StrategyHandle.ResponsibilityFactory strategyHandler) {
            this.strategyHandler = Objects.requireNonNull(strategyHandler,
                    "strategyHandler == null");
            return this;
        }

        /**
         * 请求策略工厂类
         *
         * @param factory 请求策略工厂类
         * @return Builder
         */
        public Builder factory(StrategyHandle.Factory factory) {
            this.factory = Objects.requireNonNull(factory,
                    "factory == null");
            return this;
        }

        /**
         * 请求类型策略处理集合
         *
         * @param factory 请求类型策略处理集合
         * @return Builder
         */
        public Builder strategyArray(SparseArray<StrategyHandle> strategyArray) {
            if (strategyArray == null || strategyArray.size() == 0) {
                throw new NullPointerException("strategyArray == null");
            }
            this.strategyArray = strategyArray;
            return this;
        }

        /**
         * 请求类型策略处理集合
         *
         * @param factory 请求类型策略处理集合
         * @return Builder
         */
        public Builder rspConverter(Converter.Factory rspConverter) {
            this.rspConverter = Objects.requireNonNull(rspConverter, "rspConverter == null");
            return this;
        }


        /**
         * 策略执行者/请求策略工厂类/请求类型策略处理集合 只要有一个不为空
         * 使用优先级：策略执行者 > 请求策略工厂类 > 请求类型策略处理集合
         */
        public ConfigRepository build() {
            if (rspConverter == null) {
                rspConverter = new DefaultRspConverter();
            }
            // 不为空，则直接使用即可
            if (strategyHandler != null) {
                return new ConfigRepository(this);
            }
            strategyHandler = getDefaultHandler();

            if (factory != null) {
                strategyHandler.setHandlerFactory(factory);
                return new ConfigRepository(this);
            }

            if (strategyArray != null) {
                StrategyHandle.Factory factory = new StrategyHandle.Factory() {
                    @Override
                    protected SparseArray<StrategyHandle> loadStrategyHandlerArray() {
                        return strategyArray;
                    }
                };
                strategyHandler.setHandlerFactory(factory);
                return new ConfigRepository(this);
            }

            throw new NullPointerException("strategyHandler == null And factory == null " +
                    "And strategyArray == null");
        }
    }
}
