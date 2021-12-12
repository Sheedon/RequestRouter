package org.sheedon.rrouter.compiler.model;

import org.sheedon.rrouter.facade.annotation.IntRange;

/**
 * 泛型类型信息
 * class TestClass<String,T>{}
 * T -> name: T  type: TYPE_GENERICS
 * String -> name: String  type: TYPE_ENTITY
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 3:36 下午
 */
public class GenericsTypeModel {

    public static final int TYPE_GENERICS = 0;// 泛型类型
    public static final int TYPE_ENTITY = 1;// 实体类型

    // 泛型类型名称/实体类型全类名
    private String name;
    // 名称类型：泛型类型/实体类型
    private int type;

    public static GenericsTypeModel build(String name,
                                          @IntRange(from = TYPE_GENERICS, to = TYPE_ENTITY) int type) {
        GenericsTypeModel model = new GenericsTypeModel();
        model.name = name;
        model.type = type;
        return model;
    }

    /**
     * 更新泛型类型的名称
     *
     * @param name 泛型类型名称
     */
    public void updateGenericsName(String name) {
        this.name = name;
        this.type = TYPE_GENERICS;
    }

    /**
     * 泛型类型名称 转化为实体类型
     *
     * @param name 实体类型
     */
    public void convertToEntity(String name) {
        this.name = name;
        this.type = TYPE_ENTITY;
    }

    /**
     * 获取类名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取类信息
     */
    public int getType() {
        return type;
    }
}
