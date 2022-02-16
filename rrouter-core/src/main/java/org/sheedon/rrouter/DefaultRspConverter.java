package org.sheedon.rrouter;

import org.sheedon.rrouter.core.support.IRspModel;

/**
 * 默认响应结果转换器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2022/2/15 7:41 下午
 */
public class DefaultRspConverter extends Converter.Factory {

    @Override
    public Converter<IRspModel<?>, Boolean> createCheckConverter() {
        return DefaultConverter.INSTANCE;
    }

    static class DefaultConverter implements Converter<IRspModel<?>, Boolean> {

        private static final DefaultConverter INSTANCE = new DefaultConverter();

        @Override
        public Boolean convert(IRspModel<?> rspModel) {
            return rspModel != null && rspModel.isSuccess();
        }
    }
}
