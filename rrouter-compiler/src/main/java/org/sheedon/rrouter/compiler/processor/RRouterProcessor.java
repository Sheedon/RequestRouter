package org.sheedon.rrouter.compiler.processor;

import com.google.auto.service.AutoService;

import org.sheedon.rrouter.compiler.handler.converter.RRouterConverter;
import org.sheedon.rrouter.facade.annotation.CallbackDataAdapter;
import org.sheedon.rrouter.facade.annotation.Provider;
import org.sheedon.rrouter.facade.annotation.RRouter;
import org.sheedon.rrouter.facade.annotation.Request;
import org.sheedon.rrouter.facade.annotation.RequestDataAdapter;
import org.sheedon.rrouter.facade.annotation.RequestStrategy;
import org.sheedon.rrouter.facade.annotation.StrategyType;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * A request routing annotation processor,designed to obtain all the required
 * "request routing classes" according to the request strategy routing class holding class,
 * and generate the "request routing class" into "request routing packaging class"
 * and "request strategy class" , "Local Request Strategy Class",
 * "Remote Request Strategy Class".
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/24 6:33 下午
 */
@AutoService(Processor.class)
public class RRouterProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;
    private Elements mElementUtils;
    private Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mTypeUtils = processingEnv.getTypeUtils();

        RRouterConverter.getInstance().initConfig(mMessager, mTypeUtils);
    }

    /**
     * 获取需要处理的注解内容
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(Request.class.getCanonicalName());
        supportTypes.add(RRouter.class.getCanonicalName());
        supportTypes.add(Provider.class.getCanonicalName());
        supportTypes.add(StrategyType.class.getCanonicalName());
        supportTypes.add(RequestStrategy.class.getCanonicalName());
        supportTypes.add(RequestDataAdapter.class.getCanonicalName());
        supportTypes.add(CallbackDataAdapter.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        // 采集 @Request
        RequestFieldProcessor requestProcessor = new RequestFieldProcessor(mMessager, mTypeUtils);
        requestProcessor.run(roundEnv);

        // 核实 @RRouter 是否需要创建，在RequestFieldProcessor中查找不到则无需创建
        Set<String> names = requestProcessor.getFieldQualifiedNames();
        if (names.isEmpty()) {
            return true;
        }

        // 采集 @RRouter
        RouterParseProcessor routerParseProcessor = new RouterParseProcessor(mMessager, mTypeUtils);
        routerParseProcessor.run(roundEnv, names);

        // RRouter相关类 RRouterWrapper / LocalRequest + RemoteRequest 的生成
        BuildRRouterProcessor buildRRouterProcessor = new BuildRRouterProcessor(mMessager, mElementUtils, mFiler);
        buildRRouterProcessor.run(routerParseProcessor);

        // 持有请求路由的包装类构建 ViewModelComponent的生成
        BuildComponentProcessor buildComponentProcessor = new BuildComponentProcessor(mFiler);
        buildComponentProcessor.run(requestProcessor.getHolderClasses(),
                buildRRouterProcessor.getWrapperMap());

        return true;
    }
}
