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
