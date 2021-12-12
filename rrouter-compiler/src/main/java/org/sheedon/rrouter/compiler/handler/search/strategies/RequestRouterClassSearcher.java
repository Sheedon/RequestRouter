package org.sheedon.rrouter.compiler.handler.search.strategies;

import org.sheedon.rrouter.compiler.model.RetrievalClassModel;
import org.sheedon.rrouter.compiler.handler.search.strategies.center.IGenericsRecord;
import org.sheedon.rrouter.compiler.handler.search.strategies.center.ISearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * AbstractRequestRouter 类检索者
 * 搜索从当前类开始，层级向上，检索至{@link org.sheedon.rrouter.facade.router.AbstractRequestRouter}
 * 将RequestCard, ResponseModel所对应的「实体类全类名」绑定到泛型类型上，
 * 构建成RetrievalClassModel 存入classMap
 * 若存在，则直接从classMap中取得
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 8:24 下午
 */
public class RequestRouterClassSearcher implements ISearch {

    private final Map<String, RetrievalClassModel> classMap = new HashMap<>();
    // 目标全类名
    private final static String CLASSNAME = "org.sheedon.rrouter.facade.router.AbstractRequestRouter";
    // java包
    private final static String JAVA_PACKAGE = "java.";

    /**
     * 搜索从当前类开始，层级向上，检索至{@link org.sheedon.rrouter.facade.router.AbstractRequestRouter}
     * 将RequestCard, ResponseModel所对应的「实体类全类名」绑定到泛型类型上，
     * 构建成RetrievalClassModel 存入classMap
     *
     * @param element  类型元素
     * @param types    类型工具类
     * @param messager 描述信息提示类
     */
    @Override
    public RetrievalClassModel searchClassGenerics(TypeElement element, Types types, Messager messager) {
        TypeMirror superTypeMirror = element.getSuperclass();
        if (superTypeMirror == null) {
            return null;
        }

        String qualifiedName = element.getQualifiedName().toString();
        classMap.putIfAbsent(qualifiedName, new RetrievalClassModel());

        TypeElement superElement = (TypeElement) types.asElement(superTypeMirror);
        String superclassName = superElement.getQualifiedName().toString();
        RetrievalClassModel superClassModel = classMap.get(superclassName);
        if (superClassModel != null) {
            return superClassModel;
        }

        // 节点类型
        // 根节点
        if (superclassName.startsWith(JAVA_PACKAGE)) {
            return null;
        }

        // 目标节点
        if (Objects.equals(superclassName, CLASSNAME)) {
            // 目标节点
            RetrievalClassModel nodeClass = traverseTargetGenerics(superTypeMirror, qualifiedName);
            if (nodeClass == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "please add generics in " + qualifiedName, element);
                return null;
            }

            appendBindPosition(nodeClass, element.getTypeParameters());
            return nodeClass;
        }

        // 查询父类信息
        RetrievalClassModel classModel = searchClassGenerics(superElement, types, messager);
        if (classModel == null) {
            return null;
        }

        RetrievalClassModel currentModel = classMap.get(qualifiedName);
        if (classModel.isCompeted()) {
            currentModel.bindGenericsRecord(classModel.getRecord());
            return currentModel;
        }

        IGenericsRecord record = cloneBySuperRecord(classModel.getRecord());
        currentModel.bindGenericsRecord(record);
        RetrievalClassModel nodeClass = traverseNodeGenerics(superTypeMirror, qualifiedName, classModel);
        if (nodeClass == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "please add generics in " + qualifiedName, element);
            return null;
        }

        appendBindPosition(nodeClass, element.getTypeParameters());
        return nodeClass;

    }

    /**
     * 遍历目标泛型集合
     * 将当前制定的实体类型 填充到
     * {@link org.sheedon.rrouter.facade.router.AbstractRequestRouter}的RequestCard, ResponseModel 上
     * 流程
     * 1. 获取当前类的父类描述泛型
     * 2. 获取真实父类的泛型类型
     * 3. 匹配描述泛型kind == TypeKind.DECLARED，代表可以填充
     * 4. 不匹配的记录到对照表中
     */
    private RetrievalClassModel traverseTargetGenerics(TypeMirror superTypeMirror, String currentQualifiedName) {
        if (!(superTypeMirror instanceof DeclaredType)) {
            return null;
        }

        DeclaredType declaredType = (DeclaredType) superTypeMirror;

        TypeElement superElement = (TypeElement) declaredType.asElement();
        List<? extends TypeParameterElement> superParameters = superElement.getTypeParameters();
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        // 未设置泛型，或设置的泛型个数不一致
        if (typeArguments.isEmpty() || superParameters.size() != typeArguments.size()) {
            return null;
        }

        RetrievalClassModel classModel = classMap.get(currentQualifiedName);
        for (int index = 0; index < superParameters.size(); index++) {
            TypeParameterElement element = superParameters.get(index);
            String typeName = element.asType().toString();

            TypeMirror mirror = typeArguments.get(index);
            if (mirror.getKind() == TypeKind.DECLARED) {
                classModel.addGenericsRecord(typeName, mirror.toString());
            } else {
                classModel.recordType(mirror.toString(), typeName);
            }
        }
        return classModel;
    }


    /**
     * 追加绑定泛型类型的位置
     * AClass<T> extends BClass<T>{
     * <p>
     * }
     * 绑定T的位置为0
     *
     * @param nodeClass         节点Class信息
     * @param currentParameters 当前类的泛型参数 AClass<T> 中的T
     */
    private void appendBindPosition(RetrievalClassModel nodeClass, List<? extends TypeParameterElement> currentParameters) {
        int index = 0;
        for (TypeParameterElement parameter : currentParameters) {
            nodeClass.bindPosition(parameter.asType().toString(), index);
            index++;
        }
    }


    /**
     * 遍历目标泛型集合
     * 将
     * 1. 获取当前类的父类描述泛型
     * 2. 获取真实父类的泛型类型
     * 3. 匹配描述泛型kind == TypeKind.DECLARED，代表可以填充
     * 4. 不匹配的记录到对照表中
     */
    private RetrievalClassModel traverseNodeGenerics(TypeMirror superTypeMirror, String currentQualifiedName, RetrievalClassModel superClassModel) {
        if (!(superTypeMirror instanceof DeclaredType)) {
            return null;
        }

        DeclaredType declaredType = (DeclaredType) superTypeMirror;

        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

        if (typeArguments.isEmpty()) {
            return null;
        }

        RetrievalClassModel classModel = classMap.get(currentQualifiedName);

        Set<Integer> positions = superClassModel.getPositions();
        for (Integer position : positions) {
            String typeName = superClassModel.getTypeNameByPosition(position);
            TypeMirror mirror = typeArguments.get(position);
            if (mirror.getKind() == TypeKind.DECLARED) {
                classModel.addGenericsRecord(typeName, mirror.toString());
            } else {
                classModel.recordType(mirror.toString(), typeName);
            }
        }

        return classModel;
    }

    /**
     * 从父记录中拷贝泛型处理记录
     *
     * @param record 父泛型处理记录
     * @return 当前类的泛型处理记录
     */
    private IGenericsRecord cloneBySuperRecord(IGenericsRecord record) {
        if (record == null) {
            return new RRGenericsRecord();
        }

        try {
            return record.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new RRGenericsRecord();
    }
}
