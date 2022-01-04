package org.sheedon.rrouter.compiler.handler.search.strategies;

import org.sheedon.compilationtool.retrieval.core.IGenericsRecord;

import java.util.Objects;

/**
 * AbstractRequestRouter 泛型记录
 * 包含对应位置下的泛型内容
 * AbstractRequestRouter 中定义了两个泛型：「RequestCard」「ResponseModel」
 * 当前需要匹配同类型，并且设置到genericsArray制定的位置上
 * genericsArray[0] : RequestCard
 * genericsArray[1] : ResponseModel
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 2:15 下午
 */
public class RRGenericsRecord implements IGenericsRecord {

    // 请求卡片
    public static final String REQUEST_CARD = "RequestCard";
    // 反馈model
    public static final String RESPONSE_MODEL = "ResponseModel";

    // 泛型组
    private String[] genericsArray = new String[2];
    // 标志 0b00:两个都没有 0b01:有REQUEST_CARD 0b10:有ResponseModel 0b11:两者都有
    private int sign = 0B00;

    /**
     * AbstractRequestRouter 中定义了两个泛型：「RequestCard」「ResponseModel」
     * 当前需要匹配同类型，并且设置到genericsArray制定的位置上
     * genericsArray[0] : RequestCard
     * genericsArray[1] : ResponseModel
     *
     * @param typeName        泛型类型
     * @param entityClassName 实体类型
     */
    @Override
    public void put(String typeName, String entityClassName) {
        if (Objects.equals(typeName, REQUEST_CARD)) {
            genericsArray[0] = entityClassName;
            sign |= 0B01;
        } else if (Objects.equals(typeName, RESPONSE_MODEL)) {
            genericsArray[1] = entityClassName;
            sign |= 0B10;
        }
    }

    /**
     * 获取 AbstractRequestRouter中泛型所绑定的实体类全类名
     *
     * @param typeName 泛型类型
     * @return 实体类全类名
     */
    @Override
    public String get(String typeName) {
        if (Objects.equals(typeName, REQUEST_CARD)) {
            return genericsArray[0];
        } else if (Objects.equals(typeName, RESPONSE_MODEL)) {
            return genericsArray[1];
        }
        return null;
    }

    @Override
    public boolean isCompeted() {
        return (sign ^ 0B11) == 0;
    }

    /**
     * 复制IGenericsRecord
     */
    @Override
    public IGenericsRecord clone() {
        try {
            RRGenericsRecord record = (RRGenericsRecord) super.clone();
            record.genericsArray = new String[2];
            record.genericsArray[0] = genericsArray[0];
            record.genericsArray[1] = genericsArray[1];
            return record;
        } catch (CloneNotSupportedException e) {
            RRGenericsRecord record = new RRGenericsRecord();
            record.sign = sign;
            record.genericsArray[0] = genericsArray[0];
            record.genericsArray[1] = genericsArray[1];
            return record;
        }
    }

    /**
     * 获取泛型集合
     */
    public String[] getGenericsArray() {
        return genericsArray;
    }
}
