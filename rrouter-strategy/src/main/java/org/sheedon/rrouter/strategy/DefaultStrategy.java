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
package org.sheedon.rrouter.strategy;

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
