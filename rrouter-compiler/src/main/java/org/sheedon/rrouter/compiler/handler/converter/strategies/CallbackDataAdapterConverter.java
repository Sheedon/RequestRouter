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
import org.sheedon.rrouter.compiler.handler.search.strategies.RRGenericsRecord;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.facade.annotation.CallbackDataAdapter;

import java.util.Objects;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 反馈数据适配转换器
 * 将 反馈数据转化适配器的方法元素（ExecutableElement）解析 转化为 方法信息模型（MethodInfoModel）
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 6:35 下午
 */
public class CallbackDataAdapterConverter extends AbstractConverter<ExecutableElement,
        MethodInfoModel, CallbackDataAdapter> {

    // 反馈数据适配器
    private static final String CONVERT_ADAPTER = "convertAdapter";

    public CallbackDataAdapterConverter(Messager messager, Types types) {
        super(messager, types);
    }

    /**
     * 反馈数据转化适配器的方法元素 构造出 方法信息模型
     * 1。必须采用 convertAdapter 方法名
     * 2。有且只能有一个参数，也就是被转化的信息
     * 3。不能是私有类
     *
     * @param element 反馈数据转化适配器的方法元素
     * @return 反馈数据转化适配器的方法模型
     */
    @Override
    public MethodInfoModel convert(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();

        if (!Objects.equals(methodName, CONVERT_ADAPTER)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "please use " +
                    CONVERT_ADAPTER + " method", element);
            return null;
        }

        if (element.getParameters().size() > 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + " has not parameter",
                    element);
            return null;
        }

        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + "'s modifier cannot private",
                    element);
            return null;
        }


        MethodInfoModel methodInfoModel = new MethodInfoModel();

        methodInfoModel.setMethodName(methodName);
        methodInfoModel.setReturnType(element.getReturnType());

        return methodInfoModel;
    }

    /**
     * 执行转化动作
     * 核实该方法除了当前注解外，是否还有其他「路由注解信息」
     *
     * @param annotation 注解
     * @param holdClass  路由持有类
     * @param element    元素
     * @return 是否执行完成
     */
    @Override
    protected boolean handleConvert(CallbackDataAdapter annotation, RouterHoldClass holdClass,
                                    ExecutableElement element) {
        if (!RRouterConverter.getInstance().isSingleElement()) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "There cannot be multiple types of annotations", element);
            return true;
        }

        RRouterConverter.getInstance().updateSingleElement(false);
        return super.handleConvert(annotation, holdClass, element);
    }

    /**
     * 处理注解解析，方法参数必须与类泛型一致，执行操作是否完成
     *
     * @param annotation CallbackDataAdapter 反馈数据适配器类
     * @param holdClass  路由持有类，填充源
     * @param model      方法信息模型
     * @param element    元素
     * @return 是否被处理
     */
    @Override
    protected boolean handleAnnotation(CallbackDataAdapter annotation, RouterHoldClass holdClass,
                                       MethodInfoModel model, ExecutableElement element) {

        if (model == null) {
            return true;
        }

        String parameterClassName = element.getEnclosingElement().asType().toString();
        RRGenericsRecord record = holdClass.getClassGenericsRecord();
        if (record == null
                || Objects.equals(record.get(RRGenericsRecord.RESPONSE_MODEL), parameterClassName)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "CallbackData and ResponseModel are mismatch", element);
            return true;
        }

        boolean isSuccess = holdClass.addCallbackDataAdapter(model);
        if (!isSuccess) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Only one @CallbackDataAdapter annotation can be added to a class", element);
        }
        return true;
    }

    /**
     * 加载 CallbackDataAdapter 类
     */
    @Override
    protected Class<CallbackDataAdapter> loadAnnotationClass() {
        return CallbackDataAdapter.class;
    }
}
