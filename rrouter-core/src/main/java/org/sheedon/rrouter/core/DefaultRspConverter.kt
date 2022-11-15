package org.sheedon.rrouter.core

/**
 * 默认响应结果转换器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2022/2/15 7:41 下午
 */
class DefaultRspConverter : Converter.Factory() {

    override fun createCheckConverter(): Converter<*, IRspModel<*>?> {
        return DefaultConverter.INSTANCE
    }

    internal class DefaultConverter : Converter<Any?, IRspModel<*>?> {
        override fun convert(t: Any?): IRspModel<*>? {
            return if (t == null) {
                null
            } else t as IRspModel<*>
        }

        companion object {
            val INSTANCE = DefaultConverter()
        }
    }

}