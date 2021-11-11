package org.sheedon.rrouter;

import android.app.Application;

import java.util.Objects;

/**
 * 请求路由客户端
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 10:49 下午
 */
public class RRouter {

    // 单例
    private final static RRouter sInstance = new RRouter();
    private static boolean sInstalled = false;
    // 基础参数配置项
    private ConfigRepository configRepository;
    // 应用上下文
    private Application context;

    private RRouter() {

    }

    public static void setUp(Application application, ConfigRepository repository) {
        if (sInstalled) {
            return;
        }

        sInstance.context = Objects.requireNonNull(application, "application == null");
        sInstance.configRepository = Objects.requireNonNull(repository, "repository == null");
        sInstalled = true;
    }

    static RRouter getInstance() {
        return sInstance;
    }

    public Application getContext() {
        return context;
    }

    static boolean isInstalled() {
        return sInstalled;
    }

    ConfigRepository getConfigRepository() {
        return Objects.requireNonNull(configRepository, "please RRouter initialize first");
    }
}
