/*
 * Copyright (C) 2022 Sheedon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * ????????????????????????
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/25 11:25 ??????
 */
public class RouterParseProcessor {

    private final Messager mMessager;
    private final Types mTypeUtils;
    private final Set<String> rRouterClassNames = new HashSet<>();
    // ????????????????????????Class?????????
    private final Map<String, RouterHoldClass> holderClasses = new HashMap<>();

    RouterParseProcessor(Messager mMessager, Types mTypeUtils) {
        this.mMessager = mMessager;
        this.mTypeUtils = mTypeUtils;
    }

    /**
     * ??????????????????????????????????????????????????????name?????????????????????????????????
     *
     * @param names Field ???????????????????????????
     */
    void run(RoundEnvironment environment, Set<String> names) {
        Set<? extends Element> mElements = environment.getElementsAnnotatedWith(RRouter.class);
        // ??????????????????
        if (mElements == null || mElements.isEmpty()) {
            return;
        }

        // ?????????????????? ????????????Map???
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
     * ???????????????Class
     *
     * @param element ????????????
     */
    private void insertHolderClass(TypeElement element) {
        // ???????????? @RRouter ??????
        if (checkRRouterClassError(element)) {
            return;
        }

        insertHolderClasses(element);

    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param element ?????????
     */
    private void insertHolderClasses(TypeElement element) {
        // ??????????????????????????????????????????
        // ???????????????????????????
        String qualifiedName = element.getQualifiedName().toString();
        if (holderClasses.containsKey(qualifiedName)) {
            return;
        }

        // ??????
        String simpleName = element.getSimpleName().toString();
        // ??????????????????
        TypeMirror superclass = element.getSuperclass();
        // ?????????
        String superclassName = null;
        // ????????????
        RetrievalClassModel retrievalClassModel = null;
        if (superclass != null) {
            // ????????????????????????
            TypeElement typeElement = (TypeElement) mTypeUtils.asElement(superclass);
            superclassName = typeElement.getQualifiedName().toString();
            // ?????? RequestRouter ???????????????
            retrievalClassModel = GenericsClassSearcher.getInstance().searchClassRequestRouterGenerics(element,
                    mTypeUtils, mMessager, ClassGenericsRetrieval.class);
        }

        // ?????????????????????
        RouterHoldClass holdClass = computeIfAbsent(qualifiedName, simpleName, superclassName);

        // ???????????????
        holdClass.addClassGenerics(retrievalClassModel);

        // ??????????????????
        RRouter router = element.getAnnotation(RRouter.class);
        holdClass.attachParameters(router, mTypeUtils);
        // ????????????????????????
        Set<Modifier> modifiers = element.getModifiers();
        holdClass.attachAbstract(modifiers.contains(Modifier.ABSTRACT));

        // ????????????????????????
        loadParentRouter(element);

        // ?????????????????????????????????????????????????????????????????? RouterHoldClass ???
        checkAndRecordInnerElement(element, holdClass);
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     * ?????????/????????????/????????????/???????????????/???????????????
     *
     * @param element ????????????
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
     * ??????????????????????????????
     *
     * @param key            ?????????
     * @param className      ??????
     * @param superclassName ????????????
     * @return RouterHoldClass ???????????????
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
     * ???????????? @RRouter ???????????????????????????????????????final?????????
     *
     * @param element ???????????????
     * @return ???????????????true:?????????false:??????
     */
    private boolean checkRRouterClassError(TypeElement element) {
        String qualifiedName = element.getQualifiedName().toString();
        // ?????? @RRouter ????????????????????????
        Element parentElement = element.getEnclosingElement();
        if (!(parentElement instanceof PackageElement)) {
            // ????????????????????????????????????????????????
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "The " + qualifiedName + " bound to @RRouter cannot be an inner class", element);
            return true;
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.FINAL)) {
            // ??????????????? final ?????????
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "The " + qualifiedName + " cannot be modified by final", element);
            return true;
        }

        return false;
    }

    /**
     * ???????????????????????????????????????
     * <p>
     * {@link Provider}
     * 0. ??????????????????
     * 1. ????????????
     * 2. ??????????????????????????????
     * 3. ????????????????????????-?????????
     * <p>
     * {@link StrategyType}
     * 0. ????????????
     * 1. ??????????????????????????????,???????????????,???????????????,????????????,????????????
     * <p>
     * {@link RequestStrategy}
     * 0. ????????????
     * 1. ??????????????????????????????,???????????????,???????????????,????????????,????????????
     * <p>
     * {@link RequestDataAdapter}
     * 0. ????????????
     * 1. ??????????????????????????????,???????????????,???????????????,????????????,????????????
     * <p>
     * {@link CallbackDataAdapter}
     * 0. ????????????
     * 1. ??????????????????????????????,???????????????,???????????????,????????????,????????????
     */
    private void checkAndRecordInnerElement(TypeElement element, RouterHoldClass holdClass) {
        if (holdClass.isCompleted()) {
            return;
        }

        List<? extends Element> enclosedElements = element.getEnclosedElements();

        // ????????????
        for (Element itemElement : enclosedElements) {
            // ????????????
            List<? extends AnnotationMirror> annotationMirrors = itemElement.getAnnotationMirrors();

            // ?????????????????????????????????
            if (annotationMirrors.isEmpty()) {
                continue;
            }

            RRouterConverter.getInstance().checkAnnotationMirror(itemElement, holdClass);
        }


        boolean isContain = rRouterClassNames.contains(element.getQualifiedName().toString());
        // ?????????????????????????????????
        if (RRouterConverter.getInstance().isHasNotProvider()
                && !findProviderByParents(holdClass.getSuperclassName(), holdClass)
                && isContain) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "please add @Provider in your construction method by "
                            + element.getQualifiedName().toString(),
                    element);
        }

        // ??????????????????????????????????????????
        if (holdClass.hasNotRemoteRequestStrategy()) {
            holdClass.addRemoteRequest(findRequestStrategy(element, true), mMessager, element, false);
        }

        // ??????????????????????????????????????????
        if (holdClass.hasNotLocalRequestStrategy()) {
            holdClass.addLocalRequest(findRequestStrategy(element, false), mMessager, element, false);
        }

        if (!isContain) {
            return;
        }

        // ?????????????????????????????????
        if (holdClass.isDeficiency()) {
            mMessager.printMessage(Diagnostic.Kind.ERROR,
                    "Class information is incomplete, "
                            + holdClass.getDeficiencyMessage(), element);
        }
    }

    /**
     * ?????????????????????????????? @Provider ?????????????????????
     *
     * @param superClassName ???????????????
     * @param holdClass      ???????????????
     * @return ??????????????????????????????
     */
    private boolean findProviderByParents(String superClassName, RouterHoldClass holdClass) {
        if (superClassName == null || superClassName.isEmpty()) {
            return false;
        }

        RouterHoldClass routerHoldClass = holderClasses.get(superClassName);
        if (routerHoldClass == null) {
            return false;
        }

        // ?????????????????????????????????
        ParameterSparseArray array = routerHoldClass.getParameterArray();
        if (array == null) {
            return false;
        }

        holdClass.setParameterArray(array);
        return true;
    }

    /**
     * ?????????????????? ???????????????????????? ?????????????????????
     *
     * @param element  ????????????
     * @param isRemote ???????????????
     * @return MethodInfoModel ??????????????????
     */
    private MethodInfoModel findRequestStrategy(TypeElement element, boolean isRemote) {
        // ?????????????????????????????????Object????????????????????????
        TypeMirror superclass = element.getSuperclass();
        TypeElement superElement = (TypeElement) mTypeUtils.asElement(superclass);
        if (superElement.getQualifiedName().toString().equals(Contract.OBJ_CLASS)) {
            return null;
        }

        // ????????????????????? holderClasses ?????????????????????????????????
        String className = superElement.getQualifiedName().toString();
        RouterHoldClass routerHoldClass = holderClasses.get(className);
        if (routerHoldClass == null) {
            return null;
        }

        // ??????????????????????????????
        MethodInfoModel model = isRemote ? routerHoldClass.getRemoteRequestStrategy()
                : routerHoldClass.getLocalRequestStrategy();

        // ?????????????????????????????????
        if (model != null) {
            return model;
        }
        // ??????????????????????????????
        return findRequestStrategy(superElement, isRemote);
    }

    /**
     * ??????????????????????????????????????????
     */
    Set<String> getRRouterClassNames() {
        return rRouterClassNames;
    }

    /**
     * ???????????????????????????????????????
     */
    RouterHoldClass getRouterHoldClass(String name) {
        return holderClasses.get(name);
    }

}
