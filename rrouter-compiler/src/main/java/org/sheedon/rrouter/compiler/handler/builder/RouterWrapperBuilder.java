package org.sheedon.rrouter.compiler.handler.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.sheedon.rrouter.compiler.Contract;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.compiler.model.ParameterGroupModel;
import org.sheedon.rrouter.compiler.model.ParameterModel;
import org.sheedon.rrouter.compiler.model.ParameterSparseArray;
import org.sheedon.rrouter.compiler.model.RouterWrapperModel;
import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.compiler.utils.ClassUtils;
import org.sheedon.rrouter.core.support.DataSource;
import org.sheedon.rrouter.core.support.Request;
import org.sheedon.rrouter.core.support.StrategyCallback;
import org.sheedon.rrouter.facade.model.FailureCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * 路由装饰类 构建
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/8 11:53 下午
 */
public class RouterWrapperBuilder {

    // 请求数据适配器
    private static final String REQUEST_BODY_ADAPTER = "requestBodyAdapter";
    // 反馈数据适配器
    private static final String CALLBACK_BODY_ADAPTER = "callbackBodyAdapter";
    // 抽象请求代理
    private static final String ABSTRACT_REQUEST_PROXY = "AbstractRequestProxy";
    // 代理字段
    private static final String PROXY = "proxy";
    // 请求方法名
    private static final String REQUEST_METHOD = "request";
    // 响应model
    private static final String RESPONSE_MODEL = "responseModel";
    // 创建代理方法
    private static final String CREATE_PROXY = "createProxy";
    // 附加监听器方法
    private static final String ATTACH_LISTENER = "attachListener";
    // 创建请求策略工厂
    private static final String STRATEGY_FACTORY = "createRequestStrategyFactory";
    // 创建请求卡片
    private static final String CREATE_REQUEST_CARD = "onCreateRequestCard";
    // 请求适配器
    private static final String REQUEST_ADAPTER = "requestAdapter";
    // 包路径
    private static final String CORE_REQUEST = Contract.CORE_PACKAGE + ".Request";

    private final Elements elements;
    private final Filer filer;
    // 包装类集合，key：路由类全类名， value：包装类信息
    private final Map<String, RouterWrapperModel> wrapperMap = new HashMap<>();

    public RouterWrapperBuilder(Elements elements, Filer filer) {
        this.elements = elements;
        this.filer = filer;
    }

    /**
     * 构建路由装饰类
     *
     * @param holdClass       路由持有类
     * @param remoteClassName 远程请求类
     * @param localClassName  本地请求类
     */
    public void buildWrapperClass(RouterHoldClass holdClass,
                                  ClassName remoteClassName, ClassName localClassName) {
        try {
            //类名和包名
            String className = holdClass.getClassName() + Contract.WRAPPER_SUFFIX;
            String packageName = holdClass.getPackageName();

            // 包装类 数据填充到map中，用于后续创建包装类组
            ClassName wrapperClassName = ClassName.get(packageName, className);
            RouterWrapperModel routerWrapperModel = wrapperMap.computeIfAbsent(
                    holdClass.getQualifiedName(),
                    value -> new RouterWrapperModel(wrapperClassName,
                            holdClass.getParameterArray()));

            // 泛型集合 转化为 ClassName集合
            String[] genericsArray = holdClass.getGenericsArray();
            ClassName[] genericsClass = new ClassName[genericsArray.length];
            for (int index = 0; index < genericsArray.length; index++) {
                genericsClass[index] = ClassUtils.convertByQualifiedName(genericsArray[index]);
            }

            TypeName superInterface = buildSuperInterface(genericsClass[1]);

            // 反馈接口名
            String listenerInterfaceName = Contract.INTERFACE_PREFIX
                    + holdClass.getClassName()
                    + Contract.INTERFACE_SUFFIX;
            ClassName interfaceClassName = ClassName.get(holdClass.getPackageName() + Contract.POINT + className,
                    listenerInterfaceName);

            // 构造字段
            List<FieldSpec> fieldSpecs = buildFields(holdClass, genericsClass, interfaceClassName);


            // 构造内部接口
            String interfaceMethodName = Contract.INTERFACE_PREFIX
                    + holdClass.getClassName() + Contract.DATA_LOADED;
            TypeSpec internalInterface = buildListenerInterface(interfaceMethodName, listenerInterfaceName,
                    holdClass, genericsClass[1], routerWrapperModel);


            // 创建构造方法
            List<MethodSpec> constructionMethods = buildConstructionMethods(holdClass);
            List<MethodSpec> specs = new ArrayList<>(constructionMethods);

            // 附加监听器
            MethodSpec attachMethod = buildAttachListener(holdClass, interfaceClassName);
            specs.add(attachMethod);

            // 创建代理方法 createProxy()
            MethodSpec proxyMethod = buildProxy(genericsClass, holdClass);
            specs.add(proxyMethod);

            // 创建createRequestStrategyFactory
            MethodSpec factoryMethod = buildRequestStrategyFactory(genericsClass, holdClass,
                    remoteClassName, localClassName, className);
            specs.add(factoryMethod);

            // 创建 requestAdapter
            MethodSpec requestAdapterMethod = buildLazyRequestAdapter(holdClass.getRequestAdapter());
            specs.add(requestAdapterMethod);

            // 请求方法
            MethodSpec requestMethod = buildRequest(genericsClass[0]);
            specs.add(requestMethod);

            // 成功回调方法
            MethodSpec dataLoadedMethod = buildDataLoaded(genericsClass[1],
                    holdClass.getCallbackAdapter(), interfaceMethodName);
            specs.add(dataLoadedMethod);

            // 数据不可用方法
            MethodSpec notAvailableMethod = buildDataNotAvailable(holdClass.getQualifiedName());
            specs.add(notAvailableMethod);

            // 销毁方法
            MethodSpec destroyMethod = buildDestroy();
            specs.add(destroyMethod);

            // 类创建
            TypeSpec requestTypeSpec = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .superclass(ClassName.get(holdClass.getPackageName(), holdClass.getClassName()))
                    .addSuperinterface(superInterface)
                    .addFields(fieldSpecs)
                    .addMethods(specs)
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
     * 获取包装类集合
     */
    public Map<String, RouterWrapperModel> getWrapperMap() {
        return wrapperMap;
    }

    /**
     * 实现接口
     */
    private TypeName buildSuperInterface(ClassName responseClassName) {
        return ParameterizedTypeName.get(ClassName.get(DataSource.Callback.class),
                responseClassName);
    }

    /**
     * 创建字段
     * 1.请求数据转化适配器
     * 2.请求代理工厂
     * 3.反馈监听者
     */
    private List<FieldSpec> buildFields(RouterHoldClass holdClass,
                                        ClassName[] genericsClass,
                                        ClassName interfaceClassName) {

        List<FieldSpec> fieldSpecList = new ArrayList<>(3);
        MethodInfoModel requestAdapterMethod = holdClass.getRequestAdapter();
        MethodInfoModel callbackAdapterMethod = holdClass.getCallbackAdapter();

        // 请求数据适配器
        FieldSpec requestAdapterField = FieldSpec.builder(requestAdapterMethod.getReturnType(),
                REQUEST_BODY_ADAPTER,
                Modifier.PRIVATE).build();
        fieldSpecList.add(requestAdapterField);

        if (callbackAdapterMethod != null) {
            // 反馈数据适配器
            FieldSpec callbackAdapterField = FieldSpec.builder(callbackAdapterMethod.getReturnType(),
                    CALLBACK_BODY_ADAPTER, Modifier.PRIVATE).build();
            fieldSpecList.add(callbackAdapterField);
        }

        // 请求代理
        ParameterizedTypeName proxyTypeName =
                ParameterizedTypeName.get(ClassName.get(Contract.CORE_PACKAGE, ABSTRACT_REQUEST_PROXY),
                        genericsClass);
        FieldSpec proxyField = FieldSpec.builder(proxyTypeName,
                PROXY,
                Modifier.PRIVATE).build();
        fieldSpecList.add(proxyField);

        // 信息反馈监听器
        FieldSpec callbackListener = FieldSpec.builder(
                interfaceClassName,
                Contract.LISTENER,
                Modifier.PRIVATE).build();
        fieldSpecList.add(callbackListener);

        return fieldSpecList;
    }

    /**
     * 创建自定义的反馈监听器接口
     * 不直接采用 DataSource.Callback，是为了指明 1.错误是从哪个请求中反馈 2. 防止成功反馈方法名重复
     * 职责：
     * 1。继承FailureCallback ，用于错误反馈，包含内容：路由代理类+错误描述
     * 2。onXXDataLoaded：反馈的结果是目标类型，也是为了防止方法名重复
     */
    private TypeSpec buildListenerInterface(String interfaceMethodName,
                                            String listenerInterfaceName,
                                            RouterHoldClass holdClass,
                                            ClassName callbackClassName,
                                            RouterWrapperModel routerWrapperModel) {

        // 若路由类中设置了 反馈路由方法，那么需要做类型转化操作
        // 目标返回类型
        TypeName targetTypeName;
        if (holdClass.getCallbackAdapter() != null) {
            // 转化类型
            MethodInfoModel callbackAdapter = holdClass.getCallbackAdapter();

            ParameterizedTypeName returnType = (ParameterizedTypeName) callbackAdapter.getReturnType();
            List<TypeName> typeNames = returnType.typeArguments;
            targetTypeName = typeNames.get(1);
        } else {
            // 原来类型
            targetTypeName = callbackClassName;
        }

        interfaceMethodName = ClassUtils.convertLittleCamelCase(interfaceMethodName);

        // 填充接口信息
        routerWrapperModel.attachInterfaceInfo(listenerInterfaceName, interfaceMethodName, targetTypeName);

        return TypeSpec.interfaceBuilder(listenerInterfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(FailureCallback.class))
                .addMethod(MethodSpec.methodBuilder(interfaceMethodName)
                        .addParameter(targetTypeName, RESPONSE_MODEL)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .build();
    }

    /**
     * 创建构建方法组
     */
    private List<MethodSpec> buildConstructionMethods(RouterHoldClass holdClass) {

        //构造方法集合
        List<MethodSpec> constructionMethods = new ArrayList<>();
        // 获取构造方法参数组
        ParameterSparseArray parameterArray = holdClass.getParameterArray();
        Set<Map.Entry<Integer, List<ParameterGroupModel>>> entries = parameterArray.entrySet();

        // 遍历构造单个构造方法
        for (Map.Entry<Integer, List<ParameterGroupModel>> entry : entries) {
            List<ParameterGroupModel> value = entry.getValue();
            for (ParameterGroupModel model : value) {
                constructionMethods.add(buildConstructionMethod(model));
            }
        }

        return constructionMethods;
    }

    /**
     * 通过参数组构建一个构造方法
     *
     * @param model 参数组
     * @return MethodSpec
     */
    private MethodSpec buildConstructionMethod(ParameterGroupModel model) {

        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<ParameterModel> group = model.getGroup();

        // 字段名 用于放入 super(name, password)中
        StringBuilder fieldNames = new StringBuilder();
        for (ParameterModel parameterModel : group) {
            TypeElement element = elements.getTypeElement(parameterModel.getClassName());

            // 构建参数
            ParameterSpec spec = ParameterSpec.builder(ClassName.get(element)
                    , parameterModel.getFieldName())
                    .build();
            parameterSpecs.add(spec);
            // 添加字段名
            fieldNames.append(parameterModel.getFieldName()).append(",");
        }

        if (fieldNames.length() > 0) {
            fieldNames.deleteCharAt(fieldNames.length() - 1);
        }

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(parameterSpecs)
                .addStatement("super($N)", fieldNames.toString())
                .addStatement("createProxy()")
                .build();
    }

    /**
     * 创建附加监听器方法
     * 用于请求组合类对该类进行监听
     */
    private MethodSpec buildAttachListener(RouterHoldClass holdClass,
                                           ClassName interfaceClassName) {
        return MethodSpec.methodBuilder(ATTACH_LISTENER)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(interfaceClassName, Contract.LISTENER)
                .addStatement("this.$N = $N", Contract.LISTENER, Contract.LISTENER)
                .build();
    }

    /**
     * 创建 请求代理类
     */
    private MethodSpec buildProxy(ClassName[] genericsClass, RouterHoldClass holdClass) {

        MethodInfoModel requestAdapter = holdClass.getRequestAdapter();
        TypeName requestReturnType = requestAdapter.getReturnType();
        TypeSpec proxyTypeSpec = buildProxyInnerClass(genericsClass,
                requestReturnType);

        return MethodSpec.methodBuilder(CREATE_PROXY)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("$N = $L", PROXY, proxyTypeSpec)
                .build();
    }

    /**
     * 构建代理内部类
     *
     * @param genericsClass   泛型数组
     * @param requestTypeName 请求类型名
     */
    private TypeSpec buildProxyInnerClass(ClassName[] genericsClass, TypeName requestTypeName) {

        ParameterizedTypeName proxyTypeName =
                ParameterizedTypeName.get(ClassName.get(Contract.CORE_PACKAGE, ABSTRACT_REQUEST_PROXY),
                        genericsClass);

        // 用于传递参数
        CodeBlock pramBlock = CodeBlock.builder()
                .add("$N(), this", STRATEGY_FACTORY)
                .build();

        // REQUEST_ADAPTER
        MethodSpec printDesc = MethodSpec.methodBuilder(CREATE_REQUEST_CARD)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addStatement("$T adapter = $N()", requestTypeName, REQUEST_ADAPTER)
                .addStatement("return adapter.getRequestBody()")
                .returns(genericsClass[0])
                .build();

        // 创建匿名内部类
        return TypeSpec.anonymousClassBuilder(pramBlock)
                .superclass(proxyTypeName)
                .addMethod(printDesc)
                .build();
    }

    /**
     * 创建 请求策略工厂类
     *
     * @param genericsClass    泛型数组
     * @param holdClass        路由持有类
     * @param remoteClassName  远程类名
     * @param localClassName   本地类名
     * @param currentClassName 当前类名
     */
    private MethodSpec buildRequestStrategyFactory(ClassName[] genericsClass,
                                                   RouterHoldClass holdClass,
                                                   ClassName remoteClassName,
                                                   ClassName localClassName,
                                                   String currentClassName) {
        ParameterizedTypeName proxyTypeName =
                ParameterizedTypeName.get(ClassName.get(Contract.CORE_PACKAGE,
                        "RequestFactory"),
                        genericsClass);

        TypeSpec factoryTypeSpec = buildFactoryInnerClass(genericsClass, holdClass,
                remoteClassName, localClassName, currentClassName);

        return MethodSpec.methodBuilder(STRATEGY_FACTORY)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("return $L", factoryTypeSpec)
                .returns(proxyTypeName)
                .build();
    }

    /**
     * 创建工厂的内部类方法实现
     *
     * @param genericsClass    泛型数组
     * @param holdClass        路由持有类
     * @param remoteClassName  远程类名
     * @param localClassName   本地类名
     * @param currentClassName 当前类
     */
    private TypeSpec buildFactoryInnerClass(ClassName[] genericsClass,
                                            RouterHoldClass holdClass,
                                            ClassName remoteClassName,
                                            ClassName localClassName,
                                            String currentClassName) {
        ParameterizedTypeName factoryTypeName =
                ParameterizedTypeName.get(ClassName.get(Contract.CORE_PACKAGE,
                        "BaseRequestStrategyFactory"),
                        genericsClass);

        // 用于传递参数
        CodeBlock pramBlock = CodeBlock.builder()
                .build();

        List<MethodSpec> innerMethods = new ArrayList<>();

        // 远程方法构建
        MethodSpec remoteRequestStrategy = createRealRequestStrategy(holdClass.getRemoteRequestStrategy(),
                holdClass.getRemoteRequestClassName(),
                genericsClass, remoteClassName,
                "onCreateRealRemoteRequestStrategy",
                currentClassName);
        if (remoteRequestStrategy != null) {
            innerMethods.add(remoteRequestStrategy);
        }

        // 本地方法构建
        MethodSpec localRequestStrategy = createRealRequestStrategy(holdClass.getLocalRequestStrategy(),
                holdClass.getLocalRequestClassName(),
                genericsClass, localClassName,
                "onCreateRealLocalRequestStrategy",
                currentClassName);
        if (localRequestStrategy != null) {
            innerMethods.add(localRequestStrategy);
        }

        // 请求策略类型构建
        MethodSpec strategyType = createRequestStrategyType(holdClass);
        innerMethods.add(strategyType);

        // 创建匿名内部类
        return TypeSpec.anonymousClassBuilder(pramBlock)
                .superclass(factoryTypeName)
                .addMethods(innerMethods)
                .build();
    }

    /**
     * 创建真实的请求策略
     * 1。Request + genericsClass[0]「Card」
     * 2。onCreateRealRemoteRequestStrategy/onCreateRealLocalRequestStrategy
     * 3。parameter：StrategyCallback + genericsClass[1]「Model」+ callback
     * 4。CodeBlock：
     * 4.1 请求方法 真实请求类 + 当前类名 + callback ，以提供方法
     * 4.2 请求类 remoteRequestClass/localRequestClass(callback)
     * 4.3 注解中的类名 构造请求类
     *
     * @param requestStrategy  请求方法
     * @param requestClassName 请求类名
     * @param genericsClass    注解集合
     * @param realClassName    真实请求类名
     * @param methodName       方法名
     * @param currentClassName 当前类的类名
     */
    private MethodSpec createRealRequestStrategy(MethodInfoModel requestStrategy,
                                                 String requestClassName,
                                                 ClassName[] genericsClass,
                                                 ClassName realClassName,
                                                 String methodName,
                                                 String currentClassName) {

        // 请求方法 + 请求类名 不存在，则无需创建当前类
        if (requestStrategy == null && (requestClassName == null || requestClassName.isEmpty())) {
            return null;
        }

        // Request<Card>
        ParameterizedTypeName requestTypeName =
                ParameterizedTypeName.get(ClassName.get(Request.class),
                        genericsClass[0]);

        // StrategyCallback<Model> callback
        ParameterizedTypeName callbackTypeName = ParameterizedTypeName.get(ClassName.get(StrategyCallback.class),
                genericsClass[1]);
        ParameterSpec callbackSpec = ParameterSpec.builder(callbackTypeName, "callback").build();

        // 代码块
        CodeBlock.Builder requestCodeBlock = CodeBlock.builder();

        if (requestStrategy != null) {
            boolean isMethod = (boolean) requestStrategy.getObj();

            if (isMethod) {
                requestCodeBlock.add("return new $T($N.this, callback)",
                        realClassName, currentClassName);
            } else {
                requestCodeBlock.add("return $N(callback)", requestStrategy.getMethodName());
            }
        } else {
            ClassName className = ClassUtils.convertByQualifiedName(requestClassName);
            requestCodeBlock.add("return new $T(callback)", className);
        }

        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addParameter(callbackSpec)
                .addStatement(requestCodeBlock.build())
                .returns(requestTypeName)
                .build();
    }

    /**
     * 创建请求策略类型
     * 有类型方法，优先使用类型方法来获取策略类型
     * 否则，采用注解定义的类型
     *
     * @param holdClass 路由持有类
     */
    private MethodSpec createRequestStrategyType(RouterHoldClass holdClass) {
        MethodInfoModel strategyTypeMethod = holdClass.getStrategyTypeMethod();

        // 代码块
        CodeBlock.Builder typeCodeBlock = CodeBlock.builder();

        if (strategyTypeMethod == null) {
            typeCodeBlock.add("return $L", holdClass.getRequestStrategy());
        } else {
            typeCodeBlock.add("return $N()", strategyTypeMethod.getMethodName());
        }

        return MethodSpec.methodBuilder("onLoadRequestStrategyType")
                .addModifiers(Modifier.PUBLIC)
                .addStatement(typeCodeBlock.build())
                .returns(TypeName.INT)
                .build();
    }

    /**
     * 创建懒加载的 请求数据适配器类
     */
    private MethodSpec buildLazyRequestAdapter(MethodInfoModel adapterMethod) {
        return MethodSpec.methodBuilder(REQUEST_ADAPTER)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .beginControlFlow("if ($N == null)", REQUEST_BODY_ADAPTER)
                .addStatement("$N = super.$N()", REQUEST_BODY_ADAPTER, REQUEST_ADAPTER)
                .endControlFlow()
                .addStatement("return $N", REQUEST_BODY_ADAPTER)
                .returns(adapterMethod.getReturnType())
                .build();
    }

    /**
     * 重写请求方法
     * void request(Card card)
     */
    private MethodSpec buildRequest(ClassName cardClassName) {
        String fieldName = ClassUtils.convertLittleCamelCase(cardClassName.simpleName());
        return MethodSpec.methodBuilder(REQUEST_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(cardClassName, fieldName)
                .addStatement("super.$N($N)", REQUEST_METHOD, fieldName)
                .addStatement("$N.$N()", PROXY, REQUEST_METHOD)
                .build();
    }

    /**
     * 创建请求数据反馈
     *
     * @param modelClassName      反馈类型名
     * @param adapterMethod       反馈适配器方法
     * @param interfaceMethodName 接口方法名
     */
    private MethodSpec buildDataLoaded(ClassName modelClassName,
                                       MethodInfoModel adapterMethod,
                                       String interfaceMethodName) {
        String fieldName = ClassUtils.convertLittleCamelCase(modelClassName.simpleName());

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onDataLoaded")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(modelClassName, fieldName)
                .addStatement("if ($N == null) return", Contract.LISTENER);

        // 若有适配器方法，则创建类型转化
        if (adapterMethod != null) {
            ParameterizedTypeName typeName = (ParameterizedTypeName) adapterMethod.getReturnType();
            builder.beginControlFlow("if($N == null)", CALLBACK_BODY_ADAPTER)
                    .addStatement("$N = $N()", CALLBACK_BODY_ADAPTER, adapterMethod.getMethodName())
                    .endControlFlow()
                    .addStatement("$T result = $N.convert($N)", typeName.typeArguments.get(1),
                            CALLBACK_BODY_ADAPTER, fieldName)
                    .addStatement("$N.$N(result)", Contract.LISTENER, ClassUtils.convertLittleCamelCase(interfaceMethodName));
        } else {
            builder.addStatement("$N.$N($N)", Contract.LISTENER, ClassUtils.convertLittleCamelCase(interfaceMethodName), fieldName);
        }
        return builder.build();
    }

    /**
     * 创建请求数据错误
     */
    private MethodSpec buildDataNotAvailable(String qualifiedName) {
        return MethodSpec.methodBuilder("onDataNotAvailable")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(String.class, "message")
                .addStatement("if ($N == null) return", Contract.LISTENER)
                .addStatement("$N.onDataNotAvailable(\"$N\", message)", Contract.LISTENER, qualifiedName)
                .build();
    }

    /**
     * 创建销毁方法
     */
    private MethodSpec buildDestroy() {
        return MethodSpec.methodBuilder("onDestroy")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("super.onDestroy()")
                .beginControlFlow("if ($N != null)", PROXY)
                .addStatement("$N.onDestroy()", PROXY)
                .endControlFlow()
                .addStatement("$N = null", PROXY)
                .build();
    }


}
