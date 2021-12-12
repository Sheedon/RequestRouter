package org.sheedon.rrouter.compiler.handler.converter.strategies;

import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.handler.converter.RRouterConverter;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.facade.annotation.RequestDataAdapter;

import java.util.Objects;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 请求数据适配器转换器
 * 将 请求数据适配器的方法元素（ExecutableElement）解析 转化为 方法信息模型（MethodInfoModel）
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 6:35 下午
 */
public class RequestDataAdapterConverter extends AbstractConverter<ExecutableElement,
        MethodInfoModel, RequestDataAdapter> {

    // 请求数据适配器
    private static final String REQUEST_ADAPTER = "requestAdapter";

    public RequestDataAdapterConverter(Messager messager, Types types) {
        super(messager, types);
    }

    /**
     * 请求数据转化适配器的方法元素 构造出 方法信息模型
     * 1。必须采用 requestAdapter 方法名
     * 2。不能包含参数
     * 3。不能是私有类
     *
     * @param element 请求数据转化适配器的方法元素
     * @return 请求数据转化适配器的方法模型
     */
    @Override
    public MethodInfoModel convert(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();

        if (!Objects.equals(methodName, REQUEST_ADAPTER)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "please use " + REQUEST_ADAPTER + "() method",
                    element);
            return null;
        }

        if (element.getParameters().size() > 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + " cannot has parameters",
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
     * @param element 元素
     * @return 是否被处理
     */
    @Override
    protected boolean handleConvert(RequestDataAdapter annotation, RouterHoldClass holdClass, ExecutableElement element) {
        if (!RRouterConverter.getInstance().isSingleElement()) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "There cannot be multiple types of annotations", element);
            return true;
        }

        RRouterConverter.getInstance().updateSingleElement(false);
        return super.handleConvert(annotation, holdClass, element);
    }

    /**
     * 处理注解解析，执行操作是否完成
     *
     * @param annotation CallbackDataAdapter 反馈数据适配器类
     * @param holdClass  路由持有类，填充源
     * @param model      方法信息模型
     * @param element    元素
     * @return 是否被处理
     */
    @Override
    protected boolean handleAnnotation(RequestDataAdapter annotation, RouterHoldClass holdClass,
                                       MethodInfoModel strategyTypeMethod, ExecutableElement element) {

        if (strategyTypeMethod == null) {
            return true;
        }

        boolean isSuccess = holdClass.addRequestDataAdapter(strategyTypeMethod);
        if (!isSuccess) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Only one @RequestDataAdapter annotation can be added to a class", element);
        }
        return true;
    }

    /**
     * 加载 @RequestDataAdapter 注解类
     */
    @Override
    protected Class<RequestDataAdapter> loadAnnotationClass() {
        return RequestDataAdapter.class;
    }
}
