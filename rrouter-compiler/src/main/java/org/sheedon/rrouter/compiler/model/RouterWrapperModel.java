package org.sheedon.rrouter.compiler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * 请求路由包装类 基础信息
 * 包含内容
 * 1. 请求路由类 ClassName
 * 2. 回调的接口名+回调的接口实现方法（包括参数）
 * 3. 构造方法的参数集合
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/10 11:08 下午
 */
public class RouterWrapperModel {

    private final ClassName className;
    private String interfaceName;
    private String interfaceMethodName;
    private TypeName methodParameter;
    private final ParameterSparseArray parameterArray;


    // 构建类时创建出来的信息,在这里存储,减少重新创建时间
    private TypeSpec listenerTypeSpec;
    //


    public RouterWrapperModel(ClassName className, ParameterSparseArray parameterArray) {
        this.className = className;
        this.parameterArray = parameterArray;
    }

    public void attachInterfaceInfo(String interfaceName, String interfaceMethodName,
                                    TypeName methodParameter) {
        this.interfaceName = interfaceName;
        this.interfaceMethodName = interfaceMethodName;
        this.methodParameter = methodParameter;
    }

    public ClassName getClassName() {
        return className;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getInterfaceMethodName() {
        return interfaceMethodName;
    }

    public TypeName getMethodParameter() {
        return methodParameter;
    }

    public ParameterSparseArray getParameterArray() {
        return parameterArray;
    }

    public TypeSpec getListenerTypeSpec() {
        return listenerTypeSpec;
    }

    public void updateListener(TypeSpec typeSpec) {
        listenerTypeSpec = typeSpec;
    }
}
