package org.sheedon.rrouter.compiler.model;

import org.sheedon.rrouter.compiler.handler.search.strategies.RRGenericsRecord;
import org.sheedon.rrouter.compiler.handler.search.strategies.center.IGenericsRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 检索类Model，目的是将祖父类的泛型类型传递到当前类中
 * 例如祖父类 GrandParentsClass，父类 ParentsClass，当前类 CurrentClass。
 * 我们要把 将 CurrentClass 中的 String 和 Integer 与 GrandParentsClass 中的 泛型F，T 关联
 * <p>
 * 第一步 将 GrandParentsClass中的F,T 和 ParentsClass 中GrandParentsClass的A,C关联
 * 这就是 recordType 方法，存入到 compareTable
 * <p>
 * 第二步 将 ParentsClass<A,B,C> 和 GrandParentsClass<A,C>关联
 * 这就是通过位置匹配，将同种泛型类型名称的记录坐标，存入bindPositions中，存入的是compareTable关联的父级泛型
 * <p>
 * 第三步 在 CurrentClass 中的 ParentsClass 进行位置匹配，就能拿到对应位置的祖父级泛型类型，完成 泛型——实体类型 关联
 * <p>
 * 如果层级不止这些，那也是循环执行当前流程
 * <code>
 * class GrandParentsClass<F,T>{
 * }
 * <p>
 * class ParentsClass<A,B,C> extends GrandParentsClass<A,C>{
 * }
 * <p>
 * class CurrentClass extends ParentsClass<String,Double,Integer>{
 * }
 *
 *
 * </code>
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 4:25 下午
 */
public class RetrievalClassModel {

    // 请求路由泛型记录
    private IGenericsRecord record;
    // 泛型对照类，当前类与父类
    private final Map<String, String> compareTable = new HashMap<>();
    // 泛型对照表的key 关联当前泛型集合的位置
    private final Map<Integer, String> bindPositions = new HashMap<>();

    public RetrievalClassModel() {
    }

    /**
     * 添加泛型记录
     *
     * @param typeName        参数类型名称
     * @param entityClassName 实际参数类型名称
     */
    public void addGenericsRecord(String typeName, String entityClassName) {
        IGenericsRecord record = getRecord();
        record.put(typeName, entityClassName);
    }

    /**
     * 获取泛型记录
     */
    public IGenericsRecord getRecord() {
        if (record == null) {
            record = new RRGenericsRecord();
        }
        return record;
    }

    /**
     * 泛型对照表，
     * class CurrentClass<T> extends SuperClass<T>{
     * <p>
     * }
     * <p>
     * class SuperClass<K>{
     * <p>
     * }
     * <p>
     * 匹配 T 和 K
     *
     * @param currentTypeName 当前泛型类型名称
     * @param superTypeName   父类泛型名称
     */
    public void recordType(String currentTypeName, String superTypeName) {
        compareTable.put(currentTypeName, superTypeName);
    }


    /**
     * 泛型类型绑定坐标
     *
     * @param typeName 泛型类型
     * @param index    坐标
     */
    public void bindPosition(String typeName, int index) {
        String superTypeName = compareTable.get(typeName);
        if (superTypeName == null) {
            return;
        }
        bindPositions.put(index, superTypeName);
    }

    /**
     * 是否补充完全
     */
    public boolean isCompeted() {
        IGenericsRecord record = getRecord();
        return record.isCompeted();
    }

    /**
     * 绑定泛型记录
     *
     * @param record 泛型记录
     */
    public void bindGenericsRecord(IGenericsRecord record) {
        this.record = record;
    }

    /**
     * 获取坐标集合
     */
    public Set<Integer> getPositions() {
        return bindPositions.keySet();
    }

    /**
     * 根据坐标获取类型名称
     *
     * @param position 坐标
     * @return 类型名称
     */
    public String getTypeNameByPosition(int position) {
        return bindPositions.get(position);
    }
}
