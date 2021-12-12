package org.sheedon.rrouter;

import androidx.annotation.IntRange;

import java.util.Arrays;

/**
 * 流程链，请求策略执行器按照配置策略执行任务时，用该类记录流程状态。
 * 状态标志：0：未开始，1：请求中，2：请求完成
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 3:53 下午
 */
public class ProcessChain {

    public static final int STATUS_NORMAL = 0;// 默认状态，未开始
    public static final int STATUS_REQUESTING = 1;// 请求中
    public static final int STATUS_COMPLETED = 2;// 请求完成

    // 流程
    private final int[] process;
    // 状态
    private final int[] status;
    // 进度
    private int progress;

    public ProcessChain(int... process) {
        if (process == null || process.length == 0) {
            throw new NullPointerException("process cannot null,please add at least one!");
        }
        this.progress = 0;
        this.process = process;
        this.status = new int[process.length];
    }

    /**
     * 重置
     */
    public void reset() {
        Arrays.fill(status, STATUS_NORMAL);
        progress = 0;
    }

    /**
     * 获取当前流程key
     */
    public int getProcess() {
        return process[progress];
    }

    /**
     * 更新坐标为index的状态为 status
     *
     * @param index  坐标
     * @param status 状态 STATUS_NORMAL/STATUS_REQUESTING/STATUS_COMPLETED
     */
    public void updateOfIndex(int index,
                              @IntRange(from = STATUS_NORMAL, to = STATUS_COMPLETED) int status) {
        if (index < 0 || index >= this.status.length) {
            return;
        }

        this.status[index] = status;
    }

    /**
     * 更新当前状态，并且进度进1
     *
     * @param progress 进度
     */
    public void updateCurrentStatus(@IntRange(from = STATUS_NORMAL, to = STATUS_COMPLETED) int status) {
        updateOfIndex(progress, status);
        if (status == STATUS_COMPLETED) {
            progress += 1;
        }
    }

    public void updateAllStatusToCompleted() {
        int length = status.length;
        for (; progress < length; progress++) {
            updateOfIndex(progress, STATUS_COMPLETED);
        }
    }

    /**
     * 获取当前进度
     */
    public int getProgress() {
        return progress;
    }

    /**
     * 获取当前流程状态
     */
    public int getCurrentStatus() {
        if (status.length <= progress || progress < 0) {
            return STATUS_COMPLETED;
        }
        return status[progress];
    }

    /**
     * 根据进度获取状态
     *
     * @param progress 进度
     * @return 状态
     */
    public int getStatus(int progress) {
        if (progress < 0 || progress >= this.status.length) {
            return STATUS_COMPLETED;
        }

        return this.status[progress];
    }

    /**
     * 是否完成全部请求
     */
    public boolean isAllCompleted() {
        for (int positionStatus : status) {
            if (positionStatus != STATUS_COMPLETED) {
                return false;
            }
        }
        return true;
    }
}
