package org.sheedon.rrouter.compiler.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 参数稀疏数组
 * 包含一个类的构造方法的参数组成
 * 例如：
 * <code>
 * class TestClass{
 * // 1⃣
 * public TestClass(){
 * <p>
 * }
 * // 2⃣
 * public TestClass(String name){
 * <p>
 * }
 * // 3⃣
 * public TestClass(String name,int age){
 * }
 * <p>
 * // 4⃣
 * public TestClass(int age,String name){
 * <p>
 * }
 * }
 * </code>
 * 1⃣ 0个参数
 * 2⃣ 1个参数
 * 3⃣ 2个参数 String + int
 * 4⃣ 2个参数 int + String
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/29 3:40 下午
 */
public class ParameterSparseArray extends HashMap<Integer, List<ParameterGroupModel>> {

    // 如上例子所示 Map 中的 key代表参数个数，value代表参数组model集合
    private final Map<Integer, List<ParameterGroupModel>> array = new LinkedHashMap<>();


    /**
     * 将指定值与此映射中的指定键相关联。 如果映射先前包含键的映射，查找匹配是否一致，一致则不做处理，否则添加。
     *
     * @param key   当前参数数量
     * @param value 参数组
     * @return List<ParameterGroupModel>
     */
    @SuppressWarnings("UnusedReturnValue")
    public List<ParameterGroupModel> put(Integer key, ParameterGroupModel value) {
        List<ParameterGroupModel> models = array.get(key);
        if (models == null) {
            models = new ArrayList<>();
        }

        if (models.contains(value)) {
            return models;
        }

        models.add(value);

        array.put(key, models);
        return models;
    }

    /**
     * 将指定值与此映射中的指定键相关联。 如果映射先前包含键的映射，查找匹配是否一致，一致则不做处理，否则添加。
     *
     * @param key   当前参数数量
     * @param value 参数组集合
     * @return List<ParameterGroupModel>
     */
    @Override
    public List<ParameterGroupModel> put(Integer key, List<ParameterGroupModel> value) {
        for (ParameterGroupModel model : value) {
            put(key, model);
        }
        return get(key);
    }

    /**
     * 通过个数获取参数组集合信息
     *
     * @param key 个数
     * @return 参数组
     */
    @Override
    public List<ParameterGroupModel> get(Object key) {
        return array.get(key);
    }

    /**
     * 参数集合个数
     */
    @Override
    public int size() {
        return array.size();
    }

    /**
     * 参数集合是否为空
     */
    @Override
    public boolean isEmpty() {
        return array.isEmpty();
    }

    /**
     * 指定个数的数据是否存在
     * @param key 指定个数
     * @return 是否存在参数组
     */
    @Override
    public boolean containsKey(Object key) {
        return array.containsKey(key);
    }

    /**
     * 指定参数组是否存在
     * @param value 参数组
     * @return 是否存在参数组
     */
    @Override
    public boolean containsValue(Object value) {
        return array.containsValue(value);
    }


    /**
     * 清空
     */
    @Override
    public void clear() {
        super.clear();
        array.clear();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Entry<Integer, List<ParameterGroupModel>>> entrySet() {
        return array.entrySet();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Integer> keySet() {
        return array.keySet();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Collection<List<ParameterGroupModel>> values() {
        return array.values();
    }
}
