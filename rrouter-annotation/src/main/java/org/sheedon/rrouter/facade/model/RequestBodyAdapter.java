package org.sheedon.rrouter.facade.model;

/**
 * Request data adapter responsibilities
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/22 4:17 下午
 */
public interface RequestBodyAdapter<T> {

    /**
     * Get request data
     */
    T getRequestBody();

    /**
     * Request data factory, implement RequestBodyAdapter, hold requestBody
     *
     * @param <T> request data
     */
    abstract class Factory<T> implements RequestBodyAdapter<T> {

        private final T requestBody = createRequestBody();

        protected T createRequestBody() {
            return null;
        }

        public T getRequestBody() {
            return requestBody;
        }
    }
}
