package org.sheedon.requestrepository;

import android.app.Application;


import org.sheedon.rrouter.core.ConfigRepository;
import org.sheedon.rrouter.core.RRouter;
import org.sheedon.rrouter.strategy.StrategyConfig;

/**
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/12/12 9:05 下午
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ConfigRepository repository = new ConfigRepository.Builder()
                .strategyArray(StrategyConfig.strategyHandlerArray)
                .build();

        RRouter.setUp(repository);
    }
}
