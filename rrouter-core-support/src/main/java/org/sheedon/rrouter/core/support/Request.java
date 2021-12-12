package org.sheedon.rrouter.core.support;

/**
 * 复杂请求中的具体请求类职责。 此类主要需要提供：「数据请求操作」、「该类请求类型」、「销毁」。
 * 请求操作：真实调度远程/本地等数据请求。
 * 该类请求类型：本请求类型，作为「请求策略」中的「键」关联。
 * 销毁：主动结束请求动作，关闭连接。
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/17 8:10 下午
 */
public interface Request<RequestCard> {
    // 请求数据
    void request(RequestCard requestCard);

    // 请求类型
    // 例如：网络请求，本地请求
    int onRequestType();

    // 取消请求
    void onCancel();

    // 销毁
    void onDestroy();
}
