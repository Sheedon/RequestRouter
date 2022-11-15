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

import androidx.annotation.IntRange
import java.util.*

/**
 * 流程链，请求策略执行器按照配置策略执行任务时，用该类记录流程状态。
 * 状态标志：0：未开始，1：请求中，2：请求完成
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 3:53 下午
 */
class ProcessChain(vararg process: Int) {
    // 流程
    private val process: IntArray

    // 状态
    private val status: IntArray

    /**
     * 获取当前进度
     */
    // 进度
    private var progress: Int


    init {
        if (process.isEmpty()) {
            throw NullPointerException("process cannot null,please add at least one!")
        }
        progress = 0
        this.process = process
        status = IntArray(process.size)
    }

    /**
     * 重置
     */
    fun reset() {
        Arrays.fill(status, STATUS_NORMAL)
        progress = 0
    }

    /**
     * 获取当前流程key
     */
    fun getProcess(): Int {
        return process[progress]
    }

    /**
     * 更新坐标为index的状态为 status
     *
     * @param index  坐标
     * @param status 状态 STATUS_NORMAL/STATUS_REQUESTING/STATUS_COMPLETED
     */
    fun updateOfIndex(
        index: Int,
        @IntRange(from = STATUS_NORMAL.toLong(), to = STATUS_COMPLETED.toLong()) status: Int
    ) {
        if (index < 0 || index >= this.status.size) {
            return
        }
        this.status[index] = status
    }

    /**
     * 更新当前状态，并且进度进1
     *
     * @param status 进度状态
     */
    fun updateCurrentStatus(
        @IntRange(
            from = STATUS_NORMAL.toLong(),
            to = STATUS_COMPLETED.toLong()
        ) status: Int
    ) {
        updateOfIndex(progress, status)
        if (status == STATUS_COMPLETED) {
            progress += 1
        }
    }

    fun updateAllStatusToCompleted() {
        val length = status.size
        while (progress < length) {
            updateOfIndex(progress, STATUS_COMPLETED)
            progress++
        }
    }

    /**
     * 获取当前进度
     */
    fun getProgress(): Int {
        return progress
    }

    /**
     * 获取当前流程状态
     */
    fun getCurrentStatus(): Int{
        return if (status.size <= progress || progress < 0) {
            STATUS_COMPLETED
        } else status[progress]
    }

    /**
     * 根据进度获取状态
     *
     * @param progress 进度
     * @return 状态
     */
    fun getStatus(progress: Int): Int {
        return if (progress < 0 || progress >= status.size) {
            STATUS_COMPLETED
        } else status[progress]
    }

    /**
     * 是否完成全部请求
     */
    fun isAllCompleted(): Boolean {
        for (positionStatus in status) {
            if (positionStatus != STATUS_COMPLETED) {
                return false
            }
        }
        return true
    }

    companion object {
        const val STATUS_NORMAL = 0 // 默认状态，未开始
        const val STATUS_REQUESTING = 1 // 请求中
        const val STATUS_COMPLETED = 2 // 请求完成
    }
}