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
package org.sheedon.rrouter.core

import android.util.SparseArray

/**
 * 配置类，用于配置
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 9:42 下午
 */
class ConfigRepository private constructor(builder: Builder) {
    // 组策略执行者
    private var strategyHandler: StrategyHandle.Responsibilities
    private var rspConverter: Converter<*, IRspModel<*>?>
    private var errorMessage: String = "network data error"

    init {
        strategyHandler = builder.strategyHandler!!
        rspConverter = builder.rspConverter!!.createCheckConverter()!!
        errorMessage = builder.errorMessage
    }

    internal fun getStrategyHandler(): StrategyHandle.Responsibilities {
        return strategyHandler
    }

    fun getErrorMessage(): String = errorMessage

    fun getRspConverter(): Converter<*, IRspModel<*>?> {
        return rspConverter
    }

    class Builder {
        // 组策略执行者
        internal var strategyHandler: StrategyHandle.ResponsibilityFactory? = null

        // 请求策略工厂类
        private var factory: StrategyHandle.Factory? = null

        // 请求类型策略处理集合
        internal var strategyArray: SparseArray<StrategyHandle>? = null

        // 默认结果核实转换器
        internal var rspConverter: Converter.Factory? = null

        internal var errorMessage: String = "network data error"

        /**
         * 策略执行者
         *
         * @param strategyHandler 策略执行者
         * @return Builder
         */
        fun strategyHandler(strategyHandler: StrategyHandle.ResponsibilityFactory) = apply {
            this.strategyHandler = strategyHandler
        }

        /**
         * 请求策略工厂类
         *
         * @param factory 请求策略工厂类
         * @return Builder
         */
        fun factory(factory: StrategyHandle.Factory) = apply {
            this.factory = factory
        }

        /**
         * 请求类型策略处理集合
         *
         * @param strategyArray 请求类型策略处理集合
         * @return Builder
         */
        fun strategyArray(strategyArray: SparseArray<StrategyHandle>) = apply {
            if (strategyArray.size() == 0) {
                throw NullPointerException("strategyArray is empty")
            }
            this.strategyArray = strategyArray
            return this
        }

        /**
         * 请求类型策略处理集合
         *
         * @param rspConverter 请求类型策略处理集合
         * @return Builder
         */
        fun rspConverter(rspConverter: Converter.Factory) = apply {
            this.rspConverter = rspConverter
        }

        fun errorMessage(errorMessage: String) = apply {
            this.errorMessage = errorMessage
        }

        /**
         * 策略执行者/请求策略工厂类/请求类型策略处理集合 只要有一个不为空
         * 使用优先级：策略执行者 > 请求策略工厂类 > 请求类型策略处理集合
         */
        fun build(): ConfigRepository {
            if (rspConverter == null) {
                rspConverter = DefaultRspConverter()
            }
            // 不为空，则直接使用即可
            if (strategyHandler != null) {
                return ConfigRepository(this)
            }
            strategyHandler = defaultHandler
            if (factory != null) {
                strategyHandler!!.setHandlerFactory(factory)
                return ConfigRepository(this)
            }
            if (strategyArray != null) {
                val factory: StrategyHandle.Factory = object : StrategyHandle.Factory() {
                    override fun loadStrategyHandlerArray(): SparseArray<StrategyHandle> {
                        return strategyArray!!
                    }
                }
                strategyHandler!!.setHandlerFactory(factory)
                return ConfigRepository(this)
            }

            throw NullPointerException(
                "strategyHandler == null And factory == null " +
                        "And strategyArray == null"
            )
        }
    }

    companion object {
        /**
         * 构建者类中获取
         */
        private val defaultHandler: StrategyHandle.ResponsibilityFactory
            get() {
                if (RRouter.isInstalled()) {
                    throw RuntimeException("Please do not repeat settings")
                }
                return DefaultStrategyHandler.HANDLER
            }
    }
}