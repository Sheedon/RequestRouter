package org.sheedon.requestrepository;

import android.app.Application;

import org.sheedon.rrouter.ConfigRepository;
import org.sheedon.rrouter.RRouter;
import org.sheedon.rrouter.StrategyConfig;

/**
 *
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

        RRouter.setUp(this, repository);
    }
}
