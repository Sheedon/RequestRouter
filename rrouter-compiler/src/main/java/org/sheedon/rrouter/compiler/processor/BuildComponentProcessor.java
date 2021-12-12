package org.sheedon.rrouter.compiler.processor;

import com.squareup.javapoet.ClassName;

import org.sheedon.rrouter.compiler.handler.builder.ComponentBuilder;
import org.sheedon.rrouter.compiler.handler.builder.LocalRequestBuilder;
import org.sheedon.rrouter.compiler.handler.builder.RemoteRequestBuilder;
import org.sheedon.rrouter.compiler.handler.builder.RouterWrapperBuilder;
import org.sheedon.rrouter.compiler.model.RouterWrapperModel;
import org.sheedon.rrouter.compiler.model.holder.RequestHoldClass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

/**
 * 构造 持有请求路由的包装组合类 XXComponent
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/11 10:54 上午
 */
public class BuildComponentProcessor {

    private final ComponentBuilder componentHandler;

    public BuildComponentProcessor(Filer filer) {
        componentHandler = new ComponentBuilder(filer);
    }

    /**
     * 通过创建请求类组合类，以实现控制反转，代为创建请求类的行为
     * 步骤
     * 1. 遍历holderClasses获取「请求路由」持有类的信息和其包含的「请求路由类」
     * 2. 根据「第一步」中获取的「请求路由类」，从wrapperMap中获取「路由包装类」
     * 3. 由此构建
     *
     * @param holderClasses 请求持有类
     * @param wrapperMap    路由包装类
     */
    void run(Map<String, RequestHoldClass> holderClasses, Map<String, RouterWrapperModel> wrapperMap) {
        for (Map.Entry<String, RequestHoldClass> classEntry : holderClasses.entrySet()) {
            // 请求类的持有类 类全名，例如A持有「B请求」，当前便是A的全类名
            String holderQualifiedName = classEntry.getKey();
            ClassName targetClassName = ClassName.bestGuess(holderQualifiedName);
            // 持有类信息
            RequestHoldClass holdClass = classEntry.getValue();

            Map<String, RouterWrapperModel> wrapper = fetchWrapper(holdClass, wrapperMap);
            componentHandler.buildComponentClass(targetClassName, wrapper);
        }
    }

    /**
     * 提取当前类需要的包装类信息
     *
     * @param value      持有路由类
     * @param wrapperMap 包装类Map
     */
    private Map<String, RouterWrapperModel> fetchWrapper(RequestHoldClass value, Map<String, RouterWrapperModel> wrapperMap) {
        Map<String, List<String>> routerFieldMap = value.getRouterFieldMap();

        Map<String, RouterWrapperModel> wrapperModels = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : routerFieldMap.entrySet()) {
            List<String> fieldNameList = entry.getValue();
            for (String fieldName : fieldNameList) {
                wrapperModels.put(fieldName, wrapperMap.get(entry.getKey()));
            }
        }
        return wrapperModels;

    }


}
