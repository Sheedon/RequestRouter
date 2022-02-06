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
package org.sheedon.rrouter.compiler.handler.converter.strategies;

import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.handler.converter.RRouterConverter;
import org.sheedon.rrouter.compiler.model.ParameterModel;
import org.sheedon.rrouter.facade.annotation.Provider;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;

/**
 * 构造方法转化器
 * 将 构造的方法元素（ExecutableElement）解析 转化为 参数模式数组（ParameterModel[]）
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/3 5:31 下午
 */
public class ConstructionConverter extends AbstractConverter<ExecutableElement,
        ParameterModel[], Provider> {

    public ConstructionConverter(Messager messager, Types types) {
        super(messager, types);
    }

    /**
     * 处理注解解析，更新处理构造方法添加状态，代表已存在构造方法，并且将构造参数填入到「路由持有类」中
     *
     * @param annotation CallbackDataAdapter 反馈数据适配器类
     * @param holdClass  路由持有类，填充源
     * @param model      方法信息模型
     * @param element    元素
     * @return 是否被处理
     */
    @Override
    protected boolean handleAnnotation(Provider annotation, RouterHoldClass holdClass,
                                       ParameterModel[] models, ExecutableElement element) {
        RRouterConverter.getInstance().updateNotProvider();
        holdClass.addConstructionParameter(models);
        return true;
    }

    /**
     * 请求构造方法的方法元素 构造出 参数信息模型
     * 参数类型 + 字段名
     *
     * @param element 请求数据转化适配器的方法元素
     * @return 请求数据转化适配器的方法模型
     */
    @Override
    public ParameterModel[] convert(ExecutableElement element) {
        if (element == null) {
            return new ParameterModel[0];
        }

        List<? extends VariableElement> parameters = element.getParameters();
        ParameterModel[] parameterModels = new ParameterModel[parameters.size()];
        int index = 0;
        for (VariableElement parameter : parameters) {
            String className = parameter.asType().toString();
            String fieldName = parameter.getSimpleName().toString();
            parameterModels[index] = ParameterModel.build(className, fieldName);
            index++;
        }
        return parameterModels;
    }

    /**
     * 加载 @Provider 注解
     */
    @Override
    protected Class<Provider> loadAnnotationClass() {
        return Provider.class;
    }
}
