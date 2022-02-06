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

import org.sheedon.rrouter.compiler.Contract;
import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.handler.converter.RRouterConverter;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.facade.annotation.RequestStrategy;
import org.sheedon.rrouter.strategy.parameter.DefaultRequestType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 请求策略转化器
 * 将 请求策略类的方法元素（ExecutableElement）解析 转化为 方法信息模型（MethodInfoModel）
 * 这里的请求策略包含了：本地请求策略和远程请求策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 6:04 下午
 */
public class RequestStrategyConverter extends AbstractConverter<ExecutableElement,
        MethodInfoModel, RequestStrategy> {

    // 远程请求类
    private static final String REMOTE_REQUEST_CLASS = "remoteRequestClass";
    // 远程请求方法
    private static final String ON_LOAD_REMOTE_METHOD = "onLoadRemoteMethod";
    // 本地请求类
    private static final String LOCAL_REQUEST_CLASS = "localRequestClass";
    // 本地请求方法
    private static final String ON_LOAD_LOCAL_METHOD = "onLoadLocalMethod";

    // 请求策略Class类型记录
    private static final Map<String, Boolean> requestStrategyClassMap = new HashMap<>();


    public RequestStrategyConverter(Messager messager, Types types) {
        super(messager, types);
    }

    /**
     * 请求策略类的方法元素 构造出 方法信息模型
     * 1。不能是私有类，否则这个类的包装类无法使用该方法
     * 2。反馈类型暂时只能为 AbstractRequestStrategy类或其子类 或者 Observable 类，其他类型不支持操作
     *
     * @param element 请求数据转化适配器的方法元素
     * @return 请求数据转化适配器的方法模型
     */
    @Override
    public MethodInfoModel convert(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();

        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    methodName + "'s modifier cannot private",
                    element);
            return null;
        }

        // 获取反馈类型，只有两种情况
        // 1: AbstractRequestStrategy类或其子类，代表请求Class
        // 2: Observable 类，代表请求方法
        // 3: 其他暂不处理，直接报错
        TypeElement returnElement = (TypeElement) types.asElement(element.getReturnType());
        String returnMethodName = returnElement.getQualifiedName().toString();
        // Observable 类
        if (returnMethodName.equals(Contract.OBSERVABLE_RXJAVA)) {
            return checkRequestMethod(element);
        }

        // AbstractRequestStrategy类或其子类，代表请求Class
        if (isRequestStrategyClass(returnElement)) {
            return checkRequestClass(element);
        }

        messager.printMessage(Diagnostic.Kind.ERROR,
                methodName + "() currently only supports Observable " +
                        "or inherits from AbstractRequestStrategy", element);
        return null;
    }

    /**
     * 处理注解解析，除了当前注解外，不能持有其他「请求路由注解」
     * 若核实为「远程请求策略」执行convert() 转化后，调度路由持有类 addRemoteRequest 方法
     * 否则，若核实为「本地请求策略」执行convert() 转化后，调度路由持有类 addLocalRequest 方法
     * 其他不做支持
     *
     * @param annotation CallbackDataAdapter 反馈数据适配器类
     * @param holdClass  路由持有类，填充源
     * @param model      方法信息模型
     * @param element    元素
     * @return 是否被处理
     */
    @Override
    protected boolean handleConvert(RequestStrategy annotation, RouterHoldClass holdClass,
                                    ExecutableElement element) {
        if (!RRouterConverter.getInstance().isSingleElement()) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "There cannot be multiple types of annotations", element);
            return true;
        }

        RRouterConverter.getInstance().updateSingleElement(false);
        MethodInfoModel requestStrategyMethod;
        if (isRemoteRequest(element, messager)) {
            // 执行远程请求设置
            requestStrategyMethod = convert(element);
            holdClass.addRemoteRequest(requestStrategyMethod, messager, element, false);
            return true;
        }

        if (isLocalRequest(element)) {
            // 执行本地请求设置
            requestStrategyMethod = convert(element);
            holdClass.addLocalRequest(requestStrategyMethod, messager, element, false);
            return true;
        }

        messager.printMessage(Diagnostic.Kind.ERROR,
                "Add requestStrategy() != 0 in the annotation " +
                        "or use the default request strategy method", element);
        return true;
    }

    @Override
    protected boolean handleAnnotation(RequestStrategy annotation, RouterHoldClass holdClass,
                                       MethodInfoModel convert, ExecutableElement element) {
        return true;
    }

    /**
     * 加载请求策略 @RequestStrategy 注解
     */
    @Override
    protected Class<RequestStrategy> loadAnnotationClass() {
        return RequestStrategy.class;
    }


    /**
     * 核实并且记录请求方法
     * 其中最多只能有一个请求参数，也就是请求card
     * 将方法名和参数 构造出 方法信息模型
     *
     * @param element  元素
     * @param messager 描述
     * @return MethodInfoModel 方法信息模型
     */
    private MethodInfoModel checkRequestMethod(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();

        // 将来优化，这个判断条件，最多一个请求参数
        if (element.getParameters().size() > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + " most one parameter",
                    element);
            return null;
        }

        MethodInfoModel methodInfoModel = new MethodInfoModel();
        methodInfoModel.setMethodName(methodName);

        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement parameter : parameters) {
            methodInfoModel.addParameterClass(parameter.asType());
        }
        methodInfoModel.setObj(true);

        return methodInfoModel;
    }

    /**
     * 核实并且记录请求类数据
     * 其中最多只能有一个请求参数，也就是请求card
     * 将方法名和参数 构造出 方法信息模型
     *
     * @param element  元素
     * @param messager 描述
     * @return MethodInfoModel
     */
    private MethodInfoModel checkRequestClass(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();

        if (element.getParameters().size() > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + " most one parameter",
                    element);
            return null;
        }

        MethodInfoModel methodInfoModel = new MethodInfoModel();
        methodInfoModel.setMethodName(methodName);

        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement parameter : parameters) {
            methodInfoModel.addParameterClass(parameter.asType());
        }
        methodInfoModel.setReturnType(element.getReturnType());
        methodInfoModel.setObj(false);

        return methodInfoModel;
    }

    /**
     * 是否是请求策略类
     * 是AbstractRequestStrategy类 或 AbstractRequestStrategy类的子类
     */
    private boolean isRequestStrategyClass(TypeElement typeElement) {
        String className = typeElement.getQualifiedName().toString();
        Boolean isStrategyClass = requestStrategyClassMap.get(className);
        // 之前已经查到，则直接返回
        if (isStrategyClass != null) {
            return isStrategyClass;
        }

        // 核实与 AbstractRequestStrategy 一致，则返回true
        if (Objects.equals(className, Contract.ARS_CLASS)) {
            requestStrategyClassMap.put(className, true);
            return true;
        }

        TypeElement superElement = (TypeElement) types.asElement(typeElement.getSuperclass());
        if (Objects.equals(superElement.getQualifiedName().toString(),
                Contract.OBJ_CLASS)) {
            requestStrategyClassMap.put(className, false);
            return false;
        }

        return isRequestStrategyClass(superElement);
    }

    /**
     * 是否是远程请求
     */
    private boolean isRemoteRequest(ExecutableElement element, Messager messager) {
        String methodName = element.getSimpleName().toString();
        if (isDefaultRemoteRequest(methodName)) return true;
        if (isDefaultLocalRequest(methodName)) return false;

        RequestStrategy requestStrategy = element.getAnnotation(RequestStrategy.class);
        if (requestStrategy.requestStrategy() == DefaultRequestType.TYPE_REMOTE_REQUEST)
            return true;
        if (requestStrategy.requestStrategy() == DefaultRequestType.TYPE_LOCAL_REQUEST)
            return false;

        messager.printMessage(Diagnostic.Kind.ERROR, "Please use the default method " +
                "or set to 「DefaultRequestType.TYPE_LOCAL_REQUEST" +
                " / DefaultRequestType.TYPE_REMOTE_REQUEST」 " +
                "in requestStrategy() in RequestStrategy", element);
        return false;

    }

    /**
     * 是否是本地请求
     *
     * @param element 元素
     * @return 是否是本地请求
     */
    private boolean isLocalRequest(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();
        if (isDefaultLocalRequest(methodName)) return true;
        if (isDefaultRemoteRequest(methodName)) return false;

        RequestStrategy requestStrategy = element.getAnnotation(RequestStrategy.class);
        if (requestStrategy.requestStrategy() == DefaultRequestType.TYPE_LOCAL_REQUEST)
            return true;
        if (requestStrategy.requestStrategy() == DefaultRequestType.TYPE_REMOTE_REQUEST)
            return false;

        messager.printMessage(Diagnostic.Kind.ERROR, "Please use the default method " +
                "or set to 「DefaultRequestType.TYPE_LOCAL_REQUEST" +
                " / DefaultRequestType.TYPE_REMOTE_REQUEST」 " +
                "in requestStrategy() in RequestStrategy", element);
        return false;

    }

    /**
     * 该方法是否为 默认远程请求方法名
     *
     * @param methodName 方法名
     * @return 是否为默认名称
     */
    private boolean isDefaultRemoteRequest(String methodName) {
        return Objects.equals(methodName, REMOTE_REQUEST_CLASS)
                || Objects.equals(methodName, ON_LOAD_REMOTE_METHOD);
    }

    /**
     * 该方法是否为 默认本地请求方法名
     *
     * @param methodName 方法名
     * @return 是否为默认名称
     */
    private boolean isDefaultLocalRequest(String methodName) {
        return Objects.equals(methodName, LOCAL_REQUEST_CLASS)
                || Objects.equals(methodName, ON_LOAD_LOCAL_METHOD);
    }
}
