package org.sheedon.rrouter.compiler.processor;

import com.squareup.javapoet.ClassName;

import org.sheedon.rrouter.compiler.handler.builder.LocalRequestBuilder;
import org.sheedon.rrouter.compiler.handler.builder.RemoteRequestBuilder;
import org.sheedon.rrouter.compiler.handler.builder.RouterWrapperBuilder;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.compiler.model.RouterWrapperModel;
import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

/**
 * 构造RRouter的处理器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/7 10:35 下午
 */
public class BuildRRouterProcessor {

    private final RemoteRequestBuilder remoteHandler;
    private final LocalRequestBuilder localHandler;
    private final RouterWrapperBuilder wrapperHandler;

    public BuildRRouterProcessor(Messager messager, Elements elements, Filer filer) {
        remoteHandler = new RemoteRequestBuilder(messager, elements, filer);
        localHandler = new LocalRequestBuilder(messager, elements, filer);
        wrapperHandler = new RouterWrapperBuilder(elements, filer);
    }

    void run(RouterParseProcessor processor) {
        Set<String> classNames = processor.getRRouterClassNames();
        for (String className : classNames) {
            RouterHoldClass holdClass = processor.getRouterHoldClass(className);
            if (checkRouterNotComplete(holdClass)) {
                return;
            }
            ClassName remoteClassName = buildRemoteRequestStrategy(holdClass);
            ClassName localClassName = buildLocalRequestStrategy(holdClass);

            wrapperHandler.buildWrapperClass(holdClass, remoteClassName, localClassName);
        }
    }

    /**
     * 获取包装类集合
     */
    Map<String, RouterWrapperModel> getWrapperMap() {
        return wrapperHandler.getWrapperMap();
    }

    /**
     * 核实数据是否不齐全
     *
     * @param holdClass 路由持有类
     * @return 请求数据是否不齐全¬
     */
    private boolean checkRouterNotComplete(RouterHoldClass holdClass) {
        return false;
    }

    /**
     * 构建远程请求策略
     */
    private ClassName buildRemoteRequestStrategy(RouterHoldClass holdClass) {
        MethodInfoModel remoteRequestStrategy = holdClass.getRemoteRequestStrategy();
        if (remoteRequestStrategy == null || Objects.equals(remoteRequestStrategy.getObj(), false)) {
            return null;
        }
        return remoteHandler.buildRequestClass(holdClass);
    }

    /**
     * 构建本地请求策略
     */
    private ClassName buildLocalRequestStrategy(RouterHoldClass holdClass) {
        MethodInfoModel localRequestStrategy = holdClass.getLocalRequestStrategy();
        if (localRequestStrategy == null || Objects.equals(localRequestStrategy.getObj(), false)) {
            return null;
        }
        return localHandler.buildRequestClass(holdClass);
    }
}
