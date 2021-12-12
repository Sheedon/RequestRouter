package org.sheedon.rrouter.compiler.handler.search;

import org.sheedon.rrouter.compiler.model.RetrievalClassModel;
import org.sheedon.rrouter.compiler.handler.search.strategies.RequestRouterClassSearcher;
import org.sheedon.rrouter.compiler.handler.search.strategies.center.ISearch;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

/**
 * 类包含的泛型检索者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/5 1:58 下午
 */
public class GenericsClassSearcher {

    // 单例
    private final static GenericsClassSearcher INSTANCE = new GenericsClassSearcher();

    // 搜索map
    private final Map<String, ISearch> searchMap = new HashMap<String, ISearch>() {
        {
            put(RequestRouterClassSearcher.class.getCanonicalName(), new RequestRouterClassSearcher());
        }
    };

    private GenericsClassSearcher() {
    }

    public static GenericsClassSearcher getInstance() {
        return INSTANCE;
    }

    /**
     * 搜索从当前类开始，层级向上，检索至{@link org.sheedon.rrouter.facade.router.AbstractRequestRouter}
     * 将RequestCard, ResponseModel所对应的「实体类全类名」绑定到泛型类型上，
     * 构建成RetrievalClassModel 存入classMap
     *
     * @param element  类型元素
     * @param types    类型工具类
     * @param messager 描述信息提示类
     */
    public RetrievalClassModel searchClassRequestRouterGenerics(TypeElement element, Types types, Messager messager, Class<? extends ISearch> cls) {
        if (cls == null) {
            throw new RuntimeException("please add class by search");
        }

        ISearch search = searchMap.get(cls.getCanonicalName());
        return search.searchClassGenerics(element, types, messager);
    }

}
