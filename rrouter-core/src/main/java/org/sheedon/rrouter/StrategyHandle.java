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

import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.rrouter.core.support.Request;

/**
 * 执行策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 9:18 下午
 */
public interface StrategyHandle {

    /**
     * 加载请求流程，用于在 ProcessChain类 中填充流程进度
     * 加载的流程ID数组为「策略执行方式」所对应的请求策略顺序
     * 例如A请求执行方式对应的策略依次为：1⃣ -> 2⃣ -> 3⃣ -> 2⃣ -> 3⃣
     * 那么返回数组为「1，2，3，2，3」，ProcessChain类 会将该流程的进度都设置为 ProcessChain.STATUS_NORMAL
     */
    int[] loadRequestProcess();

    /**
     * 请求策略执行
     *
     * @param processChain      流程链
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     RequestCard
     * @return 执行是否成功
     */
    <RequestCard> boolean handleRequestStrategy(ProcessChain processChain,
                                                SparseArray<Request<RequestCard>> requestStrategies,
                                                RequestCard card);

    /**
     * 反馈策略待执行方法
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param model           反馈结果
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> ResponseModel
     * @return 执行完成的进度
     */
    <ResponseModel> boolean handleCallbackStrategy(ProcessChain processChain,
                                                   DataSource.Callback<ResponseModel> callback,
                                                   ResponseModel model,
                                                   String message,
                                                   boolean isSuccess);


    /**
     * 策略执行器抽象工厂
     */
    abstract class Factory {

        /**
         * 加载策略处理集合
         */
        protected abstract SparseArray<StrategyHandle> loadStrategyHandlerArray();

        /**
         * 加载策略执行者
         *
         * @param requestStrategyType 请求策略类型
         * @return StrategyHandler
         */
        public StrategyHandle loadStrategyHandler(int requestStrategyType) {
            SparseArray<StrategyHandle> sparseArray = loadStrategyHandlerArray();
            return sparseArray.get(requestStrategyType);
        }
    }

    /**
     * 默认 组策略执行者 的职责
     */
    interface Responsibilities {

        /**
         * 加载请求流程，用于在 ProcessChain类 中填充流程进度
         * 加载的流程ID数组为「策略执行方式」所对应的请求策略顺序
         * 例如A请求执行方式对应的策略依次为：1⃣ -> 2⃣ -> 3⃣ -> 2⃣ -> 3⃣
         * 那么返回数组为「1，2，3，2，3」，ProcessChain类 会将该流程的进度都设置为 ProcessChain.STATUS_NORMAL
         *
         * @param strategyType 请求策略类型
         */
        int[] loadRequestProcess(int strategyType);

        // 执行请求策略
        <RequestCard> boolean handleRequestStrategy(int requestStrategyType, ProcessChain processChain,
                                                    SparseArray<Request<RequestCard>> requestStrategies,
                                                    RequestCard card);

        // 执行反馈处理策略
        <ResponseModel> boolean handleCallbackStrategy(int requestStrategyType, ProcessChain processChain,
                                                       DataSource.Callback<ResponseModel> callback,
                                                       ResponseModel responseModel, String message,
                                                       boolean isSuccess);
    }

    /**
     * 请求策略仓库工厂类
     */
    abstract class ResponsibilityFactory implements Responsibilities {

        abstract void setHandlerFactory(StrategyHandle.Factory handlerFactory);
    }
}
