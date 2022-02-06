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
package org.sheedon.rrouter.compiler.model;

import org.sheedon.rrouter.compiler.utils.HashUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 构造方法 - 参数组Model
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/26 4:24 下午
 */
public class ParameterGroupModel {

    // 分割符
    private static final String REGEX = "&";

    // 参数集合
    private List<ParameterModel> group;
    // 参数组唯一值的key
    private final StringBuilder groupKeys = new StringBuilder();


    private ParameterGroupModel() {
    }


    /**
     * 将参数数组添加入ParameterGroupModel中，并且以参数类型为key，组合成groupKey，而构建参数组
     *
     * @param group 参数组 数组
     * @return ParameterGroupModel
     */
    public static ParameterGroupModel build(ParameterModel[] group) {
        ParameterGroupModel model = new ParameterGroupModel();
        if (group == null || group.length == 0) {
            model.group = new ArrayList<>(0);
            return model;
        }

        model.group = new ArrayList<>(group.length);
        for (ParameterModel parameterModel : group) {
            if (parameterModel.getClassName() == null) {
                continue;
            }

            model.group.add(parameterModel);
            int position = HashUtils.hashCode(parameterModel.getClassName());
            model.groupKeys.append(position).append(REGEX);
        }
        return model;
    }

    public List<ParameterModel> getGroup() {
        return group;
    }

    /**
     * 比较groupKey一致，则认为是同一个参数组¬
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterGroupModel that = (ParameterGroupModel) o;

        return Objects.equals(groupKeys.toString(), that.groupKeys.toString());
    }


    @Override
    public int hashCode() {
        return Objects.hash(group, groupKeys);
    }
}
