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
package org.sheedon.rrouter.facade.model;

/**
 * Request error feedback
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/23 5:01 下午
 */
public interface FailureCallback {

    /**
     * Return unavailable data
     *
     * @param type    Request routing type
     * @param message Description
     */
    void onDataNotAvailable(String type, String message);
}
