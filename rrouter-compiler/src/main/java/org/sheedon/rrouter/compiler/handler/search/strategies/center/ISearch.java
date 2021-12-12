package org.sheedon.rrouter.compiler.handler.search.strategies.center;

import org.sheedon.rrouter.compiler.model.RetrievalClassModel;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

/**
 * 检索职责
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 8:25 下午
 */
public interface ISearch {

    /**
     * 搜索从当前类开始，层级向上，检索至目标
     * 将泛型所对应的「实体类全类名」绑定到其类型上，
     * 构建成RetrievalClassModel 存入classMap
     *
     * @param element  类型元素
     * @param types    类型工具类
     * @param messager 描述信息提示类
     */
    RetrievalClassModel searchClassGenerics(TypeElement element, Types types, Messager messager);
}
