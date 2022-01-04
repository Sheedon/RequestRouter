package org.sheedon.rrouter.compiler.processor;

import org.sheedon.compilationtool.retrieval.ClassGenericsRetrieval;
import org.sheedon.compilationtool.retrieval.core.RetrievalClassModel;
import org.sheedon.rrouter.compiler.Contract;
import org.sheedon.rrouter.compiler.handler.converter.RRouterConverter;
import org.sheedon.rrouter.compiler.model.MethodInfoModel;
import org.sheedon.rrouter.compiler.handler.search.GenericsClassSearcher;
import org.sheedon.rrouter.compiler.model.ParameterSparseArray;
import org.sheedon.rrouter.compiler.model.holder.RouterHoldClass;
import org.sheedon.rrouter.facade.annotation.CallbackDataAdapter;
import org.sheedon.rrouter.facade.annotation.Provider;
import org.sheedon.rrouter.facade.annotation.RRouter;
import org.sheedon.rrouter.facade.annotation.RequestDataAdapter;
import org.sheedon.rrouter.facade.annotation.RequestStrategy;
import org.sheedon.rrouter.facade.annotation.StrategyType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 路由类解析处理器
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/25 11:25 下午
 */
public class RouterParseProcessor {

    private final Messager mMessager;
    private final Types mTypeUtils;
    private final Set<String> rRouterClassNames = new HashSet<>();
    // 持有请求路由类的Class类信息
    private final Map<String, RouterHoldClass> holderClasses = new HashMap<>();

    RouterParseProcessor(Messager mMessager, Types mTypeUtils) {
        this.mMessager = mMessager;
        this.mTypeUtils = mTypeUtils;
    }

    /**
     * 注解执行，处理类前，先核实是否包含在name中，不存在，则无需创建
     *
     * @param names Field 使用到的请求路由类
     */
    void run(RoundEnvironment environment, Set<String> names) {
        Set<? extends Element> mElements = environment.getElementsAnnotatedWith(RRouter.class);
        // 无数据则结束
        if (mElements == null || mElements.isEmpty()) {
            return;
        }

        // 遍历，插入到 请求类的Map中
        for (Element element : mElements) {
            if (element instanceof TypeElement) {
                String name = ((TypeElement) element).getQualifiedName().toString();
                if (names.contains(name)) {
                    rRouterClassNames.add(name);
                    insertHolderClass((TypeElement) element);
                }
            }
        }
    }

    /**
     * 插入处理的Class
     *
     * @param element 类型元素
     */
    private void insertHolderClass(TypeElement element) {
        // 核实持有 @RRouter 的类
        if (checkRRouterClassError(element)) {
            return;
        }

        insertHolderClasses(element);

    }

    /**
     * 执行真实的创建插入类操作，当前类和父类
     *
     * @param element 类元素
     */
    private void insertHolderClasses(TypeElement element) {
        // 记录当前类的全类名和类名信息
        // 存在则无需再次操作
        String qualifiedName = element.getQualifiedName().toString();
        if (holderClasses.containsKey(qualifiedName)) {
            return;
        }

        // 类名
        String simpleName = element.getSimpleName().toString();
        // 父类描述信息
        TypeMirror superclass = element.getSuperclass();
        // 父类名
        String superclassName = null;
        // 泛型信息
        RetrievalClassModel retrievalClassModel = null;
        if (superclass != null) {
            // 获取父类描述信息
            TypeElement typeElement = (TypeElement) mTypeUtils.asElement(superclass);
            superclassName = typeElement.getQualifiedName().toString();
            // 检索 RequestRouter 的路由信息
            retrievalClassModel = GenericsClassSearcher.getInstance().searchClassRequestRouterGenerics(element,
                    mTypeUtils, mMessager, ClassGenericsRetrieval.class);
        }

        // 构造路由持有类
        RouterHoldClass holdClass = computeIfAbsent(qualifiedName, simpleName, superclassName);

        // 插入类注解
        holdClass.addClassGenerics(retrievalClassModel);

        // 路由注解解析
        RRouter router = element.getAnnotation(RRouter.class);
        holdClass.attachParameters(router, mTypeUtils);
        // 设置是否为抽象类
        Set<Modifier> modifiers = element.getModifiers();
        holdClass.attachAbstract(modifiers.contains(Modifier.ABSTRACT));

        // 加载父类路由信息
        loadParentRouter(element);

        // 核实类内部元素是否有误，无误则记录到记录当前 RouterHoldClass 中
        checkAndRecordInnerElement(element, holdClass);
    }

    /**
     * 加载父路由，从当前路由向上检索，获取路由类的所有数据
     * 类信息/请求策略/请求行为/请求转化器/反馈转化器
     *
     * @param element 类型元素
     */
    private void loadParentRouter(TypeElement element) {
        TypeMirror superclass = element.getSuperclass();
        TypeElement superElement = (TypeElement) mTypeUtils.asElement(superclass);
        if (superElement.getQualifiedName().toString().equals(Contract.OBJ_CLASS)) {
            return;
        }

        insertHolderClasses(element);

    }

    /**
     * 核实如果不存在则替换
     *
     * @param key            核实键
     * @param className      类名
     * @param superclassName 父全类名
     * @return RouterHoldClass 路由持有类
     */
    private RouterHoldClass computeIfAbsent(String key, String className, String superclassName) {
        RouterHoldClass routerHoldClass = holderClasses.get(key);
        if (routerHoldClass != null) {
            return routerHoldClass;
        }

        routerHoldClass = new RouterHoldClass(className, key, superclassName);
        holderClasses.put(key, routerHoldClass);
        return routerHoldClass;
    }

    /**
     * 核实持有 @RRouter 的类不能是内部类，抽象类，final装饰类
     *
     * @param element 类所在元素
     * @return 是否有误，true:有误，false:无误
     */
    private boolean checkRRouterClassError(TypeElement element) {
        String qualifiedName = element.getQualifiedName().toString();
        // 绑定 @RRouter 的类不能是内部类
        Element parentElement = element.getEnclosingElement();
        if (!(parentElement instanceof PackageElement)) {
            // 父级不是包元素，则代表这是内部类
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "The " + qualifiedName + " bound to @RRouter cannot be an inner class", element);
            return true;
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.FINAL)) {
            // 当前类不能 final 来修饰
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "The " + qualifiedName + " cannot be modified by final", element);
            return true;
        }

        return false;
    }

    /**
     * 核实内部元素是否不符合条件
     * <p>
     * {@link Provider}
     * 0. 至少需要一个
     * 1. 可以重复
     * 2. 获取构造方法参数集合
     * 3. 加载父级构造方法-去重复
     * <p>
     * {@link StrategyType}
     * 0. 不可重复
     * 1. 当前存在则取当前的值,当前不存在,取父类信息,都不存在,则不填充
     * <p>
     * {@link RequestStrategy}
     * 0. 不可重复
     * 1. 当前存在则取当前的值,当前不存在,取父类信息,都不存在,则不填充
     * <p>
     * {@link RequestDataAdapter}
     * 0. 不可重复
     * 1. 当前存在则取当前的值,当前不存在,取父类信息,都不存在,则不填充
     * <p>
     * {@link CallbackDataAdapter}
     * 0. 不可重复
     * 1. 当前存在则取当前的值,当前不存在,取父类信息,都不存在,则不填充
     */
    private void checkAndRecordInnerElement(TypeElement element, RouterHoldClass holdClass) {
        if (holdClass.isCompleted()) {
            return;
        }

        List<? extends Element> enclosedElements = element.getEnclosedElements();

        // 当前级别
        for (Element itemElement : enclosedElements) {
            // 获取注解
            List<? extends AnnotationMirror> annotationMirrors = itemElement.getAnnotationMirrors();

            // 注解为空，则代表无数据
            if (annotationMirrors.isEmpty()) {
                continue;
            }

            RRouterConverter.getInstance().checkAnnotationMirror(itemElement, holdClass);
        }


        boolean isContain = rRouterClassNames.contains(element.getQualifiedName().toString());
        // 子类父类都不存在则报错
        if (RRouterConverter.getInstance().isHasNotProvider()
                && !findProviderByParents(holdClass.getSuperclassName(), holdClass)
                && isContain) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "please add @Provider in your construction method by "
                            + element.getQualifiedName().toString(),
                    element);
        }

        // 当前类没有设置远程请求类策略
        if (holdClass.hasNotRemoteRequestStrategy()) {
            holdClass.addRemoteRequest(findRequestStrategy(element, true), mMessager, element, isContain);
        }

        // 当前类没有设置本地请求类策略
        if (holdClass.hasNotLocalRequestStrategy()) {
            holdClass.addLocalRequest(findRequestStrategy(element, false), mMessager, element, isContain);
        }

        if (!isContain) {
            return;
        }

        // 类的信息不完全，则报错
        if (holdClass.isDeficiency()) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "Class information is incomplete, "
                            + holdClass.getDeficiencyMessage(), element);
        }
    }

    /**
     * 是否从父类中拿到持有 @Provider 注解的构造方法
     *
     * @param superClassName 父类全类名
     * @param holdClass      路由持有类
     * @return 是否从父类中提取成功
     */
    private boolean findProviderByParents(String superClassName, RouterHoldClass holdClass) {
        if (superClassName == null || superClassName.isEmpty()) {
            return false;
        }

        RouterHoldClass routerHoldClass = holderClasses.get(superClassName);
        if (routerHoldClass == null) {
            return false;
        }

        // 从父类获取构造方法参数
        ParameterSparseArray array = routerHoldClass.getParameterArray();
        if (array == null) {
            return false;
        }

        holdClass.setParameterArray(array);
        return true;
    }

    /**
     * 通过类型元素 依次查找父类信息 以获取请求策略
     *
     * @param element  类型元素
     * @param isRemote 是否为远程
     * @return MethodInfoModel 方法信息模型
     */
    private MethodInfoModel findRequestStrategy(TypeElement element, boolean isRemote) {
        // 获取父类信息，若类名为Object的类，代表不存在
        TypeMirror superclass = element.getSuperclass();
        TypeElement superElement = (TypeElement) mTypeUtils.asElement(superclass);
        if (superElement.getQualifiedName().toString().equals(Contract.OBJ_CLASS)) {
            return null;
        }

        // 通过父类类名在 holderClasses 中搜索不到，说明不存在
        String className = superElement.getQualifiedName().toString();
        RouterHoldClass routerHoldClass = holderClasses.get(className);
        if (routerHoldClass == null) {
            return null;
        }

        // 有该类，获取请求策略
        MethodInfoModel model = isRemote ? routerHoldClass.getRemoteRequestStrategy()
                : routerHoldClass.getLocalRequestStrategy();

        // 策略不为空，则直接返回
        if (model != null) {
            return model;
        }
        // 策略不存在，再找上级
        return findRequestStrategy(superElement, isRemote);
    }

    /**
     * 获取需要创建的路由全类名集合
     */
    Set<String> getRRouterClassNames() {
        return rRouterClassNames;
    }

    /**
     * 根据路由类名获取路由持有类
     */
    RouterHoldClass getRouterHoldClass(String name) {
        return holderClasses.get(name);
    }

}
