package org.sheedon.rrouter.compiler.model;

/**
 * 构造方法参数类
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/26 4:18 下午
 */
public class ParameterModel {

    // 类名-全类名
    private String className;
    // 字段名
    private String fieldName;

    private ParameterModel() {

    }

    /**
     * 构造参数信息Model
     *
     * @param className 字段的全类名
     * @param fieldName 字段名
     * @return ParameterModel
     */
    public static ParameterModel build(String className, String fieldName) {
        ParameterModel model = new ParameterModel();
        model.className = className;
        model.fieldName = fieldName;
        return model;
    }

    /**
     * 获取全类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取字段名称
     */
    public String getFieldName() {
        return fieldName;
    }
}
