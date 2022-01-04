package org.sheedon.rrouter.compiler.handler.search.strategies;

import org.sheedon.compilationtool.retrieval.core.IGenericsRecord;
import org.sheedon.compilationtool.retrieval.core.IRetrieval;

import java.util.HashSet;
import java.util.Set;

/**
 * 请求路由泛型检索策略
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2022/1/4 9:31 上午
 */
public class RRGenericsRetrievalStrategy extends IRetrieval.AbstractRetrieval{

    private final Set<String> packages = new HashSet<String>(){
        {
            add("java.");
        }
    };

    @Override
    public String canonicalName() {
        return "org.sheedon.rrouter.facade.router.AbstractRequestRouter";
    }

    @Override
    public Set<String> filterablePackages() {
        return packages;
    }

    @Override
    public IGenericsRecord genericsRecord() {
        return new RRGenericsRecord();
    }
}
