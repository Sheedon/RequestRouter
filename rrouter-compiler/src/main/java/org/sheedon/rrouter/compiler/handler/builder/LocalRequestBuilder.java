package org.sheedon.rrouter.compiler.handler.builder;

import org.sheedon.rrouter.compiler.Contract;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

/**
 * 本地请求策略构造器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/8 11:07 下午
 */
public class LocalRequestBuilder extends AbstractRequestBuilder {

    // 抽象本地请求类名
    private final static String ABSTRACT_LOCAL_REQUEST_CLASS = "AbstractLocalRequestStrategy";

    public LocalRequestBuilder(Messager messager, Elements elements, Filer filer) {
        super(messager, elements, filer);
    }

    @Override
    protected String loadMethod() {
        return "onLoadLocalMethod";
    }

    @Override
    protected String createRequestClassName(String routerClassName) {
        String className = routerClassName.replace(Contract.ROUTER, Contract.SEPARATOR);
        return className + Contract.LOCAL_REQUEST;
    }

    @Override
    protected String loadAbstractClassName() {
        return ABSTRACT_LOCAL_REQUEST_CLASS;
    }
}
