package org.sheedon.rrouter.compiler.handler.converter.strategies;

import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.handler.converter.RRouterConverter;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.facade.annotation.StrategyType;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 策略类型转化器
 * 将 请求策略类型的方法元素（ExecutableElement）解析 转化为 方法信息模型（MethodInfoModel）
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 5:57 下午
 */
public class StrategyTypeConverter extends AbstractConverter<ExecutableElement,
        MethodInfoModel, StrategyType> {


    public StrategyTypeConverter(Messager messager, Types types) {
        super(messager, types);
    }

    /**
     * 请求策略类型的方法元素 构造出 方法信息模型
     * 1。不能包含参数
     * 2。反馈类型为int
     * 3。不能是私有类
     *
     * @param element 请求数据转化适配器的方法元素
     * @return 请求数据转化适配器的方法模型
     */
    @Override
    public MethodInfoModel convert(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();
        if (element.getParameters().size() > 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + " cannot has parameters",
                    element);
            return null;
        }

        if (!element.getReturnType().getKind().equals(TypeKind.INT)) {
            messager.printMessage(Diagnostic.Kind.ERROR, methodName + " return type need int",
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
     * 处理注解解析，执行操作是否完成
     *
     * @param annotation CallbackDataAdapter 反馈数据适配器类
     * @param holdClass  路由持有类，填充源
     * @param model      方法信息模型
     * @param element    元素
     * @return 是否被处理
     */
    @Override
    protected boolean handleAnnotation(StrategyType annotation, RouterHoldClass holdClass,
                                       MethodInfoModel strategyTypeMethod, ExecutableElement element) {
        RRouterConverter.getInstance().updateSingleElement(false);
        if (strategyTypeMethod == null) {
            return true;
        }

        holdClass.addRequestStrategyType(strategyTypeMethod, true);
        return true;
    }

    /**
     * 加载 @StrategyType 注解
     */
    @Override
    protected Class<StrategyType> loadAnnotationClass() {
        return StrategyType.class;
    }
}
