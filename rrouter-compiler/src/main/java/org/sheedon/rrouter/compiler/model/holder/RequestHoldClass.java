package org.sheedon.rrouter.compiler.model.holder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Request holding's Class information
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/25 3:35 下午
 */
public class RequestHoldClass {

    // 类名
    private final String className;
    // 一个类下面 路由字段 的 集合
    // 以该路由字段全类名为键，字段名为值
    private final Map<String, List<String>> routerFieldMap = new HashMap<>();

    public RequestHoldClass(String className) {
        this.className = className;
    }

    /**
     * 添加字段信息
     *
     * @param qualifiedName 字段全类名
     * @param fieldName     字段名称
     */
    public void addField(String qualifiedName, String fieldName) {
        routerFieldMap.computeIfAbsent(qualifiedName, value -> new LinkedList<>()).add(fieldName);
    }

    /**
     * 获取持有请求类的外部类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取字段信息
     * key 为字段全类名
     * value 为字段名称集合
     *
     * @return Map<String, List < String>>
     */
    public Map<String, List<String>> getRouterFieldMap() {
        return routerFieldMap;
    }
}
