package org.sheedon.rrouter.compiler.handler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import org.sheedon.rrouter.compiler.Contract;
import org.sheedon.rrouter.compiler.model.ParameterGroupModel;
import org.sheedon.rrouter.compiler.model.ParameterModel;
import org.sheedon.rrouter.compiler.model.ParameterSparseArray;
import org.sheedon.rrouter.compiler.model.RouterWrapperModel;
import org.sheedon.rrouter.compiler.utils.ClassUtils;
import org.sheedon.rrouter.facade.model.FailureCallback;
import org.sheedon.rrouter.facade.router.IComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * 请求路由组合类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/10 12:20 上午
 */
public class ComponentBuilder {

    // 回调监听器名
    private static final String ON_CALLBACK_LISTENER = "OnCallbackListener";
    // 构造类
    private static final String BUILDER = "Builder";
    // 宿主类
    private static final String HOST = "host";

    private final Filer filer;

    public ComponentBuilder(Filer filer) {
        this.filer = filer;
    }


    /**
     * 构建组合类
     *
     * @param holdClassName 持有类信息
     * @param wrapperMap    包装类信息 key：字段名，value：包装类信息
     */
    public void buildComponentClass(ClassName holdClassName, Map<String, RouterWrapperModel> wrapperMap) {
        try {
            //类名和包名
            String className = holdClassName.simpleName() + Contract.COMPONENT_SUFFIX;
            String packageName = holdClassName.packageName();

            ClassName interfaceClassName = ClassName.get(packageName + Contract.POINT + className,
                    ON_CALLBACK_LISTENER);

            // 构造字段
            List<FieldSpec> fieldSpecs = buildFields(packageName, wrapperMap, interfaceClassName);

            // 构造内部接口
            TypeSpec internalInterface = buildListenerInterface(wrapperMap);

            // 内部类 Builder
            String builderPackageName = holdClassName.packageName()
                    + Contract.POINT + className;
            ClassName builderClassName = ClassName.get(builderPackageName
                    , BUILDER);

            List<MethodSpec> specs = new ArrayList<>();


            // 私有构造方法
            MethodSpec constructionMethods = buildConstructionMethods(builderClassName,
                    holdClassName, wrapperMap.keySet());
            specs.add(constructionMethods);

            // 构建反馈监听类
            List<MethodSpec> attachListeners = buildAttachListener(wrapperMap);
            specs.addAll(attachListeners);

            // 构建builder
            MethodSpec builderMethod = buildToBuilder(builderClassName, holdClassName, interfaceClassName);
            specs.add(builderMethod);

            // 构建当前类的快速构建静态方法
            MethodSpec createMethod = buildToCreate(holdClassName, interfaceClassName);
            specs.add(createMethod);

            // 构建实现销毁方法
            MethodSpec destroyMethod = buildDestroy(wrapperMap.keySet());
            specs.add(destroyMethod);

            // 构建 内部构造类
            TypeSpec builderFieldSpec = buildBuilder(builderClassName, holdClassName,
                    wrapperMap, className, interfaceClassName);

            // 类创建
            TypeSpec requestTypeSpec = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(IComponent.class)
                    .addFields(fieldSpecs)
                    .addMethods(specs)
                    .addType(builderFieldSpec)
                    .addType(internalInterface)
                    .addJavadoc(Contract.DOC)
                    .build();


            JavaFile javaFile = JavaFile.builder(packageName, requestTypeSpec)
                    .build();

            javaFile.writeTo(filer);

//            javaFile.writeTo(System.out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建字段
     * 1. 通过包装类map 创建字段
     * 2. 添加反馈监听器
     *
     * @param packageName        组合类的包名
     * @param wrapperMap         请求路由包装类
     * @param interfaceClassName 接口类名
     * @return List<FieldSpec>
     */
    private List<FieldSpec> buildFields(String packageName, Map<String, RouterWrapperModel> wrapperMap, ClassName interfaceClassName) {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        for (Map.Entry<String, RouterWrapperModel> entry : wrapperMap.entrySet()) {
            String fieldName = entry.getKey();
            RouterWrapperModel routerWrapper = entry.getValue();

            // 路由字段创建
            FieldSpec fieldSpec = FieldSpec.builder(routerWrapper.getClassName(),
                    fieldName,
                    Modifier.PRIVATE).build();
            fieldSpecs.add(fieldSpec);
        }

        // 反馈监听器
        FieldSpec interfaceFieldSpec = FieldSpec.builder(interfaceClassName,
                Contract.LISTENER, Modifier.PRIVATE).build();
        fieldSpecs.add(interfaceFieldSpec);

        return fieldSpecs;
    }


    /**
     * 构建内部监听器接口
     *
     * @param wrapperMap 包装类的map
     * @return TypeSpec
     */
    private TypeSpec buildListenerInterface(Map<String, RouterWrapperModel> wrapperMap) {

        Collection<RouterWrapperModel> wrapperModels = wrapperMap.values();

        // 添加路由装饰类内的接口，用于后续继承
        List<ClassName> interfaceClassNames = new ArrayList<>();
        for (RouterWrapperModel wrapperModel : wrapperModels) {
            String canonicalName = wrapperModel.getClassName().canonicalName();
            interfaceClassNames.add(ClassName.get(canonicalName,
                    wrapperModel.getInterfaceName()));
        }
        interfaceClassNames.add(ClassName.get(FailureCallback.class));

        return TypeSpec.interfaceBuilder(ON_CALLBACK_LISTENER)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterfaces(interfaceClassNames)
                .build();
    }


    /**
     * 构建 私有构造方法，拿到构造者方法，填充请求类
     *
     * @param builderClassName builder的className
     * @param holdClassName    宿主类信息
     * @param keySet           字段内容
     * @return MethodSpec
     */
    private MethodSpec buildConstructionMethods(ClassName builderClassName, ClassName holdClassName,
                                                Set<String> fieldNameArray) {

        // 构建参数 构建者类
        String fieldName = ClassUtils.convertLittleCamelCase(BUILDER);
        ParameterSpec spec = ParameterSpec.builder(builderClassName
                , fieldName)
                .build();

        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(spec);

        // 获取宿主类
        builder.addStatement("$T host = $N.host", holdClassName, fieldName);

        // 绑定路由字段内容
        for (String routerField : fieldNameArray) {
            builder.addStatement("host.$N = this.$N = $N.$N",
                    routerField, routerField, fieldName, routerField);
        }

        builder.addStatement("this.$N = $N.$N",Contract.LISTENER,
                fieldName,Contract.LISTENER);

        builder.addStatement("attachListener()");


        return builder.build();
    }

    /**
     * 添加私有的附加监听器
     *
     * @param wrapperMap 装饰类Map
     * @return List<MethodSpec>
     */
    private List<MethodSpec> buildAttachListener(Map<String, RouterWrapperModel> wrapperMap) {

        List<MethodSpec> specs = new ArrayList<>();

        MethodSpec.Builder attachListener = MethodSpec.methodBuilder("attachListener")
                .addModifiers(Modifier.PRIVATE);

        for (Map.Entry<String, RouterWrapperModel> entry : wrapperMap.entrySet()) {

            String methodName = "attach"
                    + ClassUtils.convertBigCamelCase(entry.getKey())
                    + Contract.LISTENER;

            attachListener.addStatement("$N()", methodName);
            MethodSpec methodSpec = buildRouterListener(methodName, entry.getKey(), entry.getValue());
            specs.add(methodSpec);

        }

        specs.add(0, attachListener.build());

        return specs;
    }

    /**
     * 创建路由监听器
     *
     * @param methodName      方法名
     * @param routerFieldName 路由字段名
     * @param wrapperModel    装饰类
     * @return MethodSpec
     */
    private MethodSpec buildRouterListener(String methodName, String routerFieldName,
                                           RouterWrapperModel wrapperModel) {
        TypeSpec listenerTypeSpec = wrapperModel.getListenerTypeSpec();
        if (listenerTypeSpec == null) {
            listenerTypeSpec = buildRealListener(wrapperModel);
        }

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("$N.attachListener($L)",
                        routerFieldName,
                        listenerTypeSpec)
                .build();

    }


    /**
     * 创建真实的监听器
     *
     * @param wrapperModel 包装类
     * @return TypeSpec
     */
    private TypeSpec buildRealListener(RouterWrapperModel wrapperModel) {
        String listenerCanonicalName
                = wrapperModel.getClassName().canonicalName()
                + Contract.POINT
                + wrapperModel.getInterfaceName();
        ClassName listenerTypeName = ClassName.bestGuess(listenerCanonicalName);


        // 用于传递参数
        CodeBlock pramBlock = CodeBlock.builder()
                .build();

        List<MethodSpec> innerMethods = new ArrayList<>();


        // onDataLoaded 方法
        MethodSpec.Builder dataLoadedBuilder = MethodSpec.methodBuilder(wrapperModel.getInterfaceMethodName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                // 字段
                .addParameter(ParameterSpec.builder(wrapperModel.getMethodParameter()
                        , "result")
                        .build())
                .addStatement("if(listener == null) return")
                .addStatement("listener.$N(result)", wrapperModel.getInterfaceMethodName());

        innerMethods.add(dataLoadedBuilder.build());

        // onDataNotAvailable 方法
        MethodSpec.Builder notAvailableBuilder = MethodSpec.methodBuilder("onDataNotAvailable")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                // 字段
                .addParameter(ParameterSpec.builder(String.class, "type").build())
                .addParameter(ParameterSpec.builder(String.class, "message").build())
                .addStatement("if(listener == null) return")
                .addStatement("listener.onDataNotAvailable(type, message)");

        innerMethods.add(notAvailableBuilder.build());

        TypeSpec typeSpec = TypeSpec.anonymousClassBuilder(pramBlock)
                .superclass(listenerTypeName)
                .addMethods(innerMethods)
                .build();

        wrapperModel.updateListener(typeSpec);

        // 创建匿名内部类
        return typeSpec;
    }


    /**
     * 创建 builder 方法
     *
     * @param holdClassName 请求持有类
     * @return MethodSpec
     */
    private MethodSpec buildToBuilder(ClassName builderClassName,
                                      ClassName holdClassName,
                                      ClassName interfaceClassName) {

        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(holdClassName, "host").build())
                .addParameter(ParameterSpec.builder(interfaceClassName, "listener").build())
                .addStatement("return new $T(host, listener)", builderClassName)
                .returns(builderClassName)
                .build();
    }

    /**
     * 构建快速创建当前类的静态方法
     *
     * @param holdClassName 持有类名
     * @return MethodSpec
     */
    private MethodSpec buildToCreate(ClassName holdClassName, ClassName interfaceClassName) {

        return MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(holdClassName, "host").build())
                .addParameter(ParameterSpec.builder(interfaceClassName, "listener").build())
                .addStatement("return builder(host, listener).build()")
                .returns(IComponent.class)
                .build();

    }

    /**
     * 构建销毁方法
     *
     * @param fieldNames 字段名
     * @return MethodSpec
     */
    private MethodSpec buildDestroy(Set<String> fieldNames) {

        MethodSpec.Builder destroyBuilder = MethodSpec.methodBuilder("onDestroy")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class);

        for (String fieldName : fieldNames) {
            destroyBuilder.beginControlFlow("if(this.$N != null)", fieldName)
                    .addStatement("this.$N.onDestroy()", fieldName)
                    .endControlFlow()
                    .addStatement("this.$N = null", fieldName);
        }

        destroyBuilder.addStatement("this.listener = null");


        return destroyBuilder.build();
    }


    /**
     * 内部构建类
     *
     * @param builderClassName 构建类信息
     * @param holdClassName    持有类信息
     * @param wrapperMap       请求装饰类
     * @param className        构建的Component类名
     * @return FieldSpec
     */
    private TypeSpec buildBuilder(ClassName builderClassName, ClassName holdClassName,
                                  Map<String, RouterWrapperModel> wrapperMap,
                                  String componentClassName,
                                  ClassName interfaceClassName) {

        // 字段
        List<FieldSpec> fields = createBuilderFields(holdClassName, wrapperMap, interfaceClassName);

        List<MethodSpec> methodSpecs = new ArrayList<>();

        // 构造方法
        MethodSpec constructionMethodSpec = createBuilderConstruction(holdClassName, interfaceClassName);
        methodSpecs.add(constructionMethodSpec);

        // 请求路由的创建方法
        for (Map.Entry<String, RouterWrapperModel> entry : wrapperMap.entrySet()) {
            methodSpecs.addAll(routerMethods(builderClassName,
                    entry.getKey(), entry.getValue()));
        }

        // build方法
        MethodSpec buildMethod = createBuildMethod(wrapperMap.entrySet(),
                componentClassName);
        methodSpecs.add(buildMethod);


        return TypeSpec.classBuilder(BUILDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addFields(fields)
                .addMethods(methodSpecs)
                .build();
    }


    /**
     * 创建 Builder 中的字段信息
     *
     * @param holdClassName 路由持有类/宿主类
     * @param wrapperMap    路由信息
     * @return List<FieldSpec>
     */
    private List<FieldSpec> createBuilderFields(ClassName holdClassName,
                                                Map<String, RouterWrapperModel> wrapperMap,
                                                ClassName interfaceClassName) {

        List<FieldSpec> specs = new ArrayList<>();
        specs.add(FieldSpec.builder(holdClassName, "host")
                .addModifiers(Modifier.PRIVATE)
                .build());

        // 反馈监听器
        specs.add(FieldSpec.builder(interfaceClassName, Contract.LISTENER)
                .addModifiers(Modifier.PRIVATE)
                .build());

        for (Map.Entry<String, RouterWrapperModel> entry : wrapperMap.entrySet()) {
            specs.add(FieldSpec.builder(entry.getValue().getClassName(), entry.getKey())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
        }

        return specs;
    }

    /**
     * 构造Builder 的构造方法
     */
    private MethodSpec createBuilderConstruction(ClassName holdClassName,
                                                 ClassName interfaceClassName) {

        // 构建参数 构建者类
        List<ParameterSpec> specs = new ArrayList<>();
        specs.add(ParameterSpec.builder(holdClassName, "host").build());
        specs.add(ParameterSpec.builder(interfaceClassName, Contract.LISTENER)
                .build());

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameters(specs)
                .addStatement("this.host = $T.requireNonNull(host, \"host == null\")", Objects.class)
                .addStatement("this.listener = $T.requireNonNull(listener, \"listener == null\")", Objects.class)
                .build();
    }


    /**
     * 路由方法创建
     *
     * @param builderClassName 当前Builder类名
     * @param fieldName        字段名 作为 方法名
     * @param wrapperModel     包装类方法
     * @return List<MethodSpec>
     */
    private List<MethodSpec> routerMethods(ClassName builderClassName,
                                           String fieldName,
                                           RouterWrapperModel wrapperModel) {

        List<MethodSpec> specs = new ArrayList<>();

        ParameterSparseArray array = wrapperModel.getParameterArray();
        for (List<ParameterGroupModel> value : array.values()) {
            for (ParameterGroupModel model : value) {
                MethodSpec methodSpec = routerMethod(builderClassName, wrapperModel.getClassName(),
                        fieldName, model.getGroup());
                specs.add(methodSpec);
            }
        }

        return specs;
    }

    /**
     * 以路由字段名作为方法名，以路由装饰构建方法提供的参数作为方法参数，来创建请求路由类
     *
     * @param builderClassName 当前Builder类名
     * @param wrapperClassName 路由装饰类名
     * @param fieldName        字段名
     * @param parameterModels  路由构建方法参数
     * @return MethodSpec
     */
    private MethodSpec routerMethod(ClassName builderClassName,
                                    ClassName wrapperClassName,
                                    String fieldName,
                                    List<ParameterModel> parameterModels) {

        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        StringBuilder fieldNames = new StringBuilder();
        for (ParameterModel model : parameterModels) {
            parameterSpecs.add(ParameterSpec.builder(
                    ClassName.bestGuess(model.getClassName()),
                    model.getFieldName())
                    .build());
            fieldNames.append(model.getFieldName()).append(", ");
        }

        int length = fieldNames.length();
        if (length > 0) {
            fieldNames.delete(length - 2, length);
        }

        return MethodSpec.methodBuilder(fieldName)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecs)
                .addStatement("$N = new $T($N)", fieldName, wrapperClassName, fieldNames.toString())
                .addStatement("return this")
                .returns(builderClassName)
                .build();
    }


    /**
     * 创建 build方法
     * 若路由类中不存在无参数构造方法，那么进行非空核实
     * 否则，若用户没有主动调度方法创建，则调用无参数方法构建
     *
     * @param values             路由集合
     * @param componentClassName 当前组合类类名
     * @return MethodSpec
     */
    private MethodSpec createBuildMethod(Set<Map.Entry<String, RouterWrapperModel>> values,
                                         String componentClassName) {

        MethodSpec.Builder build = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(IComponent.class);

        for (Map.Entry<String, RouterWrapperModel> value : values) {
            String filedName = value.getKey();
            RouterWrapperModel wrapperModel = value.getValue();
            List<ParameterGroupModel> models = wrapperModel.getParameterArray().get(0);
            if (models != null) {
                build.beginControlFlow("if($N == null)", filedName)
                        .addStatement("$N = new $T()", filedName, wrapperModel.getClassName())
                        .endControlFlow();
            } else {
                build.addStatement("$N = $T.requireNonNull($N, \"please use $N method\")", filedName, Objects.class,
                        filedName, filedName);

            }
        }

        build.addStatement("return new $N(this)", componentClassName);

        return build.build();

    }
}
