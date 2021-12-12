package org.sheedon.rrouter.compiler.handler.converter;

import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.handler.converter.center.AnalysisCenter;
import org.sheedon.rrouter.compiler.handler.converter.strategies.CallbackDataAdapterConverter;
import org.sheedon.rrouter.compiler.handler.converter.strategies.ConstructionConverter;
import org.sheedon.rrouter.compiler.handler.converter.strategies.RequestDataAdapterConverter;
import org.sheedon.rrouter.compiler.handler.converter.strategies.RequestStrategyConverter;
import org.sheedon.rrouter.compiler.handler.converter.strategies.StrategyTypeConverter;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Types;

/**
 * RequestRouter转化器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/6 10:47 下午
 */
public class RRouterConverter {

    private final static RRouterConverter INSTANCE = new RRouterConverter();

    // 是否不存在 Provider
    private boolean hasNotProvider = true;
    private boolean isSingleElement;

    private ConstructionConverter constructionConverter;
    private final List<AnalysisCenter<ExecutableElement, MethodInfoModel>> centers = new ArrayList<>();

    private RRouterConverter() {

    }

    public static RRouterConverter getInstance() {
        return INSTANCE;
    }

    public void initConfig(Messager messager, Types types) {
        constructionConverter = new ConstructionConverter(messager, types);
        centers.add(new StrategyTypeConverter(messager, types));
        centers.add(new RequestStrategyConverter(messager, types));
        centers.add(new RequestDataAdapterConverter(messager, types));
        centers.add(new CallbackDataAdapterConverter(messager, types));
    }

    public void checkAnnotationMirror(Element itemElement, RouterHoldClass holdClass) {
        if (!(itemElement instanceof ExecutableElement)) {
            return;
        }

        if (constructionConverter.analysis(holdClass, (ExecutableElement) itemElement)) {
            return;
        }

        isSingleElement = true;

        try {
            for (AnalysisCenter<ExecutableElement, MethodInfoModel> center : centers) {
                if (center.analysis(holdClass, (ExecutableElement) itemElement)) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isHasNotProvider() {
        return hasNotProvider;
    }

    public void updateNotProvider() {
        this.hasNotProvider = false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSingleElement() {
        return isSingleElement;
    }

    public void updateSingleElement(boolean isSingleElement) {
        this.isSingleElement = isSingleElement;
    }
}
