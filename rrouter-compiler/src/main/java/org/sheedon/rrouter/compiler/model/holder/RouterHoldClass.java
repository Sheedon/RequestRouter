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
package org.sheedon.rrouter.compiler.model.holder;

import org.sheedon.compilationtool.retrieval.core.RetrievalClassModel;
import org.sheedon.rrouter.compiler.handler.search.strategies.RRGenericsRecord;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.compiler.model.ParameterGroupModel;
import org.sheedon.rrouter.compiler.model.ParameterModel;
import org.sheedon.rrouter.compiler.model.ParameterSparseArray;
import org.sheedon.rrouter.facade.annotation.RRouter;
import org.sheedon.rrouter.strategy.support.AbstractRequestStrategy;

import java.util.Objects;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 路由只有类信息
 * 包含内容：类名称，父类全类名（RouterHoldClass的键），构造方法，
 * 请求策略类型，远程请求策略，本地请求策略
 * 请求数据适配器，反馈数据适配器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/26 9:38 上午
 */
public class RouterHoldClass {

    // 默认空请求方法名
    private static final String EMPTY_REQUEST_CLASSNAME
            = "org.sheedon.rrouter.strategy.support.NullRequestStrategy";

    // 类名
    private final String className;
    // 全类名
    private final String qualifiedName;
    // 父全类名
    private final String superclassName;
    // 包名
    private String packageName;
    // 类泛型信息
    private RRGenericsRecord classGenericsRecord;
    // 是否是抽象类
    private boolean isAbstract;
    // 请求策略
    private int requestStrategy;
    // 本地请求类 全类名
    private String localRequestClassName;
    // 远程请求类 全类名
    private String remoteRequestClassName;
    // 是否使用RRouter中注解
    private boolean usedRRouter;

    //--------------------------------- 内部元素的补充 ------------------------------------------
    // 构造方法参数集合 参数
    private ParameterSparseArray parameterArray;
    // 请求策略类型的方法
    private MethodInfoModel strategyTypeMethod;
    // 远程请求策略
    private MethodInfoModel remoteRequestStrategy;
    // 本地请求策略
    private MethodInfoModel localRequestStrategy;
    // 请求数据适配器类
    private MethodInfoModel requestAdapter;
    // 反馈数据转化适配器
    private MethodInfoModel callbackAdapter;
    // 是否添加完成
    private boolean isCompleted;

    /**
     * 构造方法，填充当前类的类名和父类类名
     *
     * @param simpleName     类名
     * @param superclassName 父类全类名
     */
    public RouterHoldClass(String simpleName, String qualifiedName, String superclassName) {
        this.className = simpleName;
        this.qualifiedName = qualifiedName;
        this.superclassName = superclassName;
        this.parameterArray = new ParameterSparseArray();
    }

    /**
     * 是否全部内容都填充完成
     * 防止重复填充，导致核实出错，也保障运行效率
     */
    public boolean isCompleted() {
        boolean currentCompleted = isCompleted;
        isCompleted = true;
        return currentCompleted;
    }

    /**
     * 是否缺失，数据不完整
     */
    public boolean isDeficiency() {
        return isAbstract
                || (localRequestClassName == null && localRequestStrategy == null
                && remoteRequestClassName == null && remoteRequestStrategy == null)
                || requestAdapter == null;
    }

    public String getDeficiencyMessage() {
        if (isAbstract) {
            return "the current class cannot be Abstract";
        }

        if(localRequestClassName == null && localRequestStrategy == null
                && remoteRequestClassName == null && remoteRequestStrategy == null){
            return "no request policy is configured for the current class";
        }

        if(requestAdapter == null){
            return "please add requestAdapter";
        }

        return "some information is missing";

    }

    /**
     * 附加 @RRouter 的参数信息
     * requestStrategy 请求策略类型
     * localRequestClass 本地请求策略类
     * remoteRequestClass 远程请求策略类
     *
     * @param router     @RRouter 注解
     * @param mTypeUtils 类型工具类
     */
    public void attachParameters(RRouter router, Types mTypeUtils) {
        if (router == null) {
            return;
        }

        requestStrategy = router.requestStrategy();

        // 本地请求类赋值
        try {
            Class<? extends AbstractRequestStrategy<?, ?>> clazz = router.localRequestClass();
        } catch (MirroredTypeException mte) {
            String className = getRequestClassName(mte, mTypeUtils);
            localRequestClassName = Objects.equals(className, EMPTY_REQUEST_CLASSNAME)
                    ? localRequestClassName : className;
        }

        // 远程请求类赋值
        try {
            Class<? extends AbstractRequestStrategy<?, ?>> clazz = router.remoteRequestClass();
        } catch (MirroredTypeException mte) {
            String className = getRequestClassName(mte, mTypeUtils);
            remoteRequestClassName = Objects.equals(className, EMPTY_REQUEST_CLASSNAME)
                    ? remoteRequestClassName : className;
        }

        usedRRouter = router.used();

    }

    /**
     * 附加是否是抽象类
     *
     * @param isAbstract 是否是抽象类
     */
    public void attachAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }


    /**
     * 获取请求类名
     *
     * @param mte        镜像类型异常
     * @param mTypeUtils 类型工具类
     * @return 全类名
     */
    private String getRequestClassName(MirroredTypeException mte, Types mTypeUtils) {
        TypeMirror typeMirror = mte.getTypeMirror();
        TypeElement element = (TypeElement) mTypeUtils.asElement(typeMirror);
        return element.getQualifiedName().toString();
    }


    /**
     * 添加构造方法的参数，根据字段长度添加到parameterArray 中
     *
     * @param group 参数信息
     */
    public void addConstructionParameter(ParameterModel[] group) {
        ParameterGroupModel model = ParameterGroupModel.build(group);
        parameterArray.put(group.length, model);
    }

    /**
     * 设置构造参数集合，一般都是当前类中不存在，于是从父类中提取
     *
     * @param parameterArray 参数集合
     */
    public void setParameterArray(ParameterSparseArray parameterArray) {
        this.parameterArray = parameterArray;
    }

    /**
     * 获取构造参数集合
     */
    public ParameterSparseArray getParameterArray() {
        return parameterArray;
    }

    /**
     * 传入请求策略类型方法 和 是否使用请求策略类型，进行请求策略类型的替换
     * 若非必须 并且 在 RRouter 中 usedRRouter设置为true
     * 则只取 RRouter 中的请求策略类型
     * 否则更换请求策略方法
     *
     * @param method                 请求策略类型方法
     * @param useRequestStrategyType 是否使用请求策略类型
     */
    public void addRequestStrategyType(MethodInfoModel method, boolean useRequestStrategyType) {
        if (!useRequestStrategyType && usedRRouter) {
            return;
        }

        strategyTypeMethod = method;
    }

    /**
     * 新增远程请求策略
     * 若之前一存在，则提示错误信息，远程请求策略只能添加一次
     *
     * @param requestStrategyMethod 请求策略方法
     * @param mMessager             描述工具
     * @param itemElement           元素
     * @param isContain             是否包含在RRouter中
     */
    public void addRemoteRequest(MethodInfoModel requestStrategyMethod, Messager mMessager, Element itemElement, boolean isContain) {
        if (remoteRequestStrategy != null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "remoteRequestStrategy method can only be one", itemElement);
            return;
        }

        if (isContain && requestStrategyMethod == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Please add remoteRequestStrategy method", itemElement);
            return;
        }


        remoteRequestStrategy = requestStrategyMethod;
    }

    /**
     * 新增本地请求策略
     * 若之前一存在，则提示错误信息，本地¬请求策略只能添加一次
     *
     * @param requestStrategyMethod 请求策略方法
     * @param mMessager             描述工具
     * @param itemElement           元素
     * @param isContain             是否包含在RRouter中
     */
    public void addLocalRequest(MethodInfoModel requestStrategyMethod, Messager mMessager, Element itemElement, boolean isContain) {
        if (localRequestStrategy != null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "localRequestStrategy method can only be one", itemElement);
            return;
        }

        if (isContain && requestStrategyMethod == null) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "Please add localRequestStrategy method", itemElement);
            return;
        }

        localRequestStrategy = requestStrategyMethod;
    }

    /**
     * 添加请求数据转化器方法
     * 若当前已存在，则添加失败
     *
     * @param method 请求
     * @return 是否添加成功，true：添加成功，false：添加失败
     */
    public boolean addRequestDataAdapter(MethodInfoModel method) {
        if (requestAdapter != null) {
            return false;
        }

        requestAdapter = method;
        return true;
    }

    /**
     * 添加反馈数据转化器方法
     * 若当前已存在，则添加失败
     *
     * @param method 请求
     * @return 是否添加成功，true：添加成功，false：添加失败
     */
    public boolean addCallbackDataAdapter(MethodInfoModel method) {
        if (callbackAdapter != null) {
            return false;
        }

        callbackAdapter = method;
        return true;
    }


    /**
     * 获取父类全类名
     */
    public String getSuperclassName() {
        return superclassName;
    }

    /**
     * 获取远程请求策略方法
     */
    public MethodInfoModel getRemoteRequestStrategy() {
        return remoteRequestStrategy;
    }

    /**
     * 获取本地请求策略方法
     */
    public MethodInfoModel getLocalRequestStrategy() {
        return localRequestStrategy;
    }

    /**
     * 是否不存在远程请求策略
     */
    public boolean hasNotRemoteRequestStrategy() {
        return remoteRequestStrategy == null && (remoteRequestClassName == null || remoteRequestClassName.isEmpty());
    }

    /**
     * 是否不存在本地请求策略
     */
    public boolean hasNotLocalRequestStrategy() {
        return localRequestStrategy == null && (localRequestClassName == null || localRequestClassName.isEmpty());
    }

    /**
     * 添加类上添加的泛型信息
     *
     * @param model 泛型Model
     */
    public void addClassGenerics(RetrievalClassModel model) {
        if (model == null) {
            return;
        }
        classGenericsRecord = (RRGenericsRecord) model.getRecord();
    }

    /**
     * 获取类添加的泛型记录信息
     */
    public RRGenericsRecord getClassGenericsRecord() {
        return classGenericsRecord;
    }

    /**
     * 获取泛型集合
     */
    public String[] getGenericsArray() {
        return classGenericsRecord.getGenericsArray();
    }

    /**
     * 获取当前路由类的类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取当前路由类的全类名
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * 是否是抽象类
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * 请求策略类型
     */
    public int getRequestStrategy() {
        return requestStrategy;
    }

    /**
     * 本地请求类名
     */
    public String getLocalRequestClassName() {
        return localRequestClassName;
    }

    /**
     * 远程请求类名
     */
    public String getRemoteRequestClassName() {
        return remoteRequestClassName;
    }

    /**
     * 请求策略类型方法
     */
    public MethodInfoModel getStrategyTypeMethod() {
        return strategyTypeMethod;
    }

    /**
     * 请求数据转化器方法
     */
    public MethodInfoModel getRequestAdapter() {
        return requestAdapter;
    }

    /**
     * 反馈数据转化器方法
     */
    public MethodInfoModel getCallbackAdapter() {
        return callbackAdapter;
    }

    /**
     * 获取包名
     */
    public String getPackageName() {
        if (packageName != null && !packageName.isEmpty()) {
            return packageName;
        }
        if (qualifiedName == null || className == null) {
            return null;
        }
        return packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf(className) - 1);
    }
}
