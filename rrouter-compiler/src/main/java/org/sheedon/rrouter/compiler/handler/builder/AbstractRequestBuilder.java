package org.sheedon.rrouter.compiler.handler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.sheedon.rrouter.compiler.Contract;
import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.utils.ClassUtils;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.strategy.model.IRspModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import io.reactivex.rxjava3.core.Observable;

/**
 * 抽象请求策略构造器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/8 11:08 下午
 */
public abstract class AbstractRequestBuilder {

    // 抽象请求策略
    private final static String WRAPPER = "wrapper";
    private final static String CALLBACK = "callback";
    private final static String ON_LOAD_METHOD = "onLoadMethod";
    private final static String ON_DESTROY = "onDestroy";

    protected Messager messager;
    protected Elements elements;
    protected Filer filer;

    public AbstractRequestBuilder(Messager messager, Elements elements, Filer filer) {
        this.messager = messager;
        this.elements = elements;
        this.filer = filer;
    }

    /**
     * 构建请求策略Class
     */
    public ClassName buildRequestClass(RouterHoldClass holdClass) {

        // 类名和包名
        String className = createRequestClassName(holdClass.getClassName());
        String packageName = holdClass.getPackageName() + Contract.POINT + Contract.REAL;

        try {

            // 泛型集合 转化为 ClassName集合
            String[] genericsArray = holdClass.getGenericsArray();
            ClassName[] genericsClass = new ClassName[genericsArray.length];
            for (int index = 0; index < genericsArray.length; index++) {
                genericsClass[index] = ClassUtils.convertByQualifiedName(genericsArray[index]);
            }

            // 获取父类
            ParameterizedTypeName superClassName = createSuperClass(genericsClass);

            // 装饰类字段
            ClassName wrapperClassName = ClassName.get(holdClass.getPackageName(),
                    holdClass.getClassName() + Contract.WRAPPER_SUFFIX);
            FieldSpec fieldSpec = FieldSpec.builder(wrapperClassName, WRAPPER,
                    Modifier.PRIVATE).build();

            // 方法集合
            List<MethodSpec> specs = new ArrayList<>(3);

            // 构造方法
            MethodSpec constructorMethod = buildConstructionMethod(className,
                    wrapperClassName, genericsClass[1]);
            specs.add(constructorMethod);

            // Observable方法集合
            MethodSpec observableMethod = buildLoadMethod(genericsClass);
            specs.add(observableMethod);

            MethodSpec destroyMethod = buildDestroyMethod();
            specs.add(destroyMethod);

            // 类创建
            TypeSpec requestTypeSpec = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .superclass(superClassName)
                    .addField(fieldSpec)
                    .addMethods(specs)
                    .addJavadoc(Contract.DOC)
                    .build();


            JavaFile javaFile = JavaFile.builder(packageName, requestTypeSpec)
                    .build();

            javaFile.writeTo(filer);

//            javaFile.writeTo(System.out);
            return ClassName.get(packageName, className);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * 创建父类 继承自抽象请求策略类，需要实现泛型
     *
     * @param ClassName[] 泛型集合类
     * @return ParameterizedTypeName
     */
    private ParameterizedTypeName createSuperClass(ClassName[] genericsClass) {

        // 获取泛型
        // 父类ClassName
        ClassName superClassName = ClassName.get(Contract.CORE_PACKAGE,
                loadAbstractClassName());

        // 得到带实现泛型的父类
        return ParameterizedTypeName.get(superClassName, genericsClass);
    }

    /**
     * 创建构造方法
     */
    private MethodSpec buildConstructionMethod(String className,
                                               ClassName wrapperClassName,
                                               ClassName responseClassName) {

        // StrategyCallback<Model> callback
        ParameterizedTypeName callbackTypeName = ParameterizedTypeName.get(ClassName.get(StrategyCallback.class),
                responseClassName);


        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(wrapperClassName, WRAPPER)
                .addParameter(callbackTypeName, CALLBACK)
                .addStatement("super($N)", CALLBACK)
                .addStatement("this.$N = $N", WRAPPER, WRAPPER)
                .build();
    }

    /**
     * 构建 需要加载的请求调度方法
     */
    private MethodSpec buildLoadMethod(ClassName[] genericsClass) {

        // 反馈结果
        ParameterizedTypeName rsp = ParameterizedTypeName.get(ClassName.get(IRspModel.class),
                genericsClass[1]);
        ParameterizedTypeName observable = ParameterizedTypeName.get(ClassName.get(Observable.class), rsp);

        // 请求字段
        ClassName requestCard = genericsClass[0];
        String cardName = ClassUtils.convertLittleCamelCase(requestCard.simpleName());


        return MethodSpec.methodBuilder(ON_LOAD_METHOD)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addParameter(requestCard, cardName)
                .addStatement("return $N.$N($N)", WRAPPER, loadMethod(),cardName)
                .returns(observable)
                .build();
    }

    /**
     * 加载方法
     */
    protected abstract String loadMethod();

    /**
     * 构建 销毁方法
     */
    private MethodSpec buildDestroyMethod() {
        return MethodSpec.methodBuilder(ON_DESTROY)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("super.onDestroy()")
                .addStatement("$N = null", WRAPPER)
                .build();
    }

    /**
     * 创建请求策略类名
     *
     * @param routerClassName 路由类名
     * @return 请求策略类名
     */
    protected abstract String createRequestClassName(String routerClassName);

    /**
     * 加载抽象请求策略类名
     */
    protected abstract String loadAbstractClassName();
}
