package org.sheedon.rrouter.facade.router;

/**
 * 执行响应结果
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2022/1/19 4:10 下午
 */
public interface IResponseDispatcher<Response> {


    /**
     * 执行结果
     *
     * @param response 响应结果
     */
    void dispatchResponse(Response response);


}
