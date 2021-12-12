package org.sheedon.rrouter.compiler.handler.builder;

import org.sheedon.rrouter.compiler.Contract;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

/**
 * 远程请求策略构造器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/8 11:07 下午
 */
public class RemoteRequestBuilder extends AbstractRequestBuilder {


    // 抽象本地请求类名
    private final static String ABSTRACT_REMOTE_REQUEST_CLASS = "AbstractRemoteRequestStrategy";

    public RemoteRequestBuilder(Messager messager, Elements elements, Filer filer) {
        super(messager, elements, filer);
    }

    @Override
    protected String loadMethod() {
        return "onLoadRemoteMethod";
    }

    @Override
    protected String createRequestClassName(String routerClassName) {
        String className = routerClassName.replace(Contract.ROUTER, Contract.SEPARATOR);
        return className + Contract.REMOTE_REQUEST;
    }

    @Override
    protected String loadAbstractClassName() {
        return ABSTRACT_REMOTE_REQUEST_CLASS;
    }
}
