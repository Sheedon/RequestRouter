package org.sheedon.rrouter.compiler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * 方法信息Model
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/29 11:41 下午
 */
public class MethodInfoModel {

    // 方法名
    private String methodName;
    // 反馈类型
    private TypeName returnType;
    // 方法参数
    private List<TypeName> parameterClass;
    // 其他属性
    private Object obj;

    public MethodInfoModel() {
        parameterClass = new LinkedList<>();
    }

    /**
     * 设置方法名
     *
     * @param methodName 方法名
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 设置返回类型
     *
     * @param returnType 返回类型
     */
    public void setReturnType(TypeMirror returnType) {
        this.returnType = ClassName.get(returnType);
    }

    /**
     * 添加参数类
     *
     * @param parameterType 参数类型
     */
    public void addParameterClass(TypeMirror parameterType) {
        parameterClass.add(ClassName.get(parameterType));
    }

    public void setObj(Object obj){
        this.obj = obj;
    }

    public Object getObj() {
        return obj;
    }

    public String getMethodName() {
        return methodName;
    }

    public TypeName getReturnType() {
        return returnType;
    }

    public List<TypeName> getParameterClass() {
        return parameterClass;
    }
}
