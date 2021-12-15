# RequestRouter
```tex
一个用于帮助 Android App 进行请求行为策略化改造的框架。
```
[English](https://github.com/Sheedon/RequestRouter/blob/master/README.md)

借由请求代理，将请求动作按照策略配置，按需按序调用，简化多种请求方式造成的逻辑复杂度。

![请求路由](https://raw.githubusercontent.com/Sheedon/RequestRouter/82e73f4d32c3f820f8942f77c7fc6ad5feba2288/image/%E8%AF%B7%E6%B1%82%E8%B7%AF%E7%94%B1.svg)



### 一、使用方式

#### 第一步：将 JitPack 存储库添加到您的构建文件中

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```



#### 第二步：添加核心依赖

```groovy
dependencies {
    // rrouter-strategy 中已使用 apt ，可无需添加 rrouter-core 的引入
    // implementation 'com.github.Sheedon.RequestRouter:rrouter-core:lastVersion'
    implementation 'com.github.Sheedon.RequestRouter:rrouter-strategy:lastVersion'
}
```

`rrouter-core` ，请求路由核心包，主要负责代理执行请求策略。

请求代理，借由客户端提供的请求行为，按配置调度策略，转交与请求策略执行器，代为执行调度方案，并在得到调度结果后，由客户端提供的反馈监听器，响应与客户端。

`rrouter-strategy`，本地请求+远程请求 策略包。

已配置策略包括：

1. 单一远程请求
2. 单一本地请求
3. 优先远程请求，失败取本地请求
4. 优先本地请求，失败取远程请求
5. 并行远程本地请求



#### 第三步：初始化SKD

```java
// 通过配置仓库配置默认（远程/本地）执行方案
// 1⃣ 2⃣ 3⃣ 任取一种即可
ConfigRepository repository = new ConfigRepository.Builder()
                .factory(new StrategyHandle.Factory())// 1⃣ 添加策略仓库工厂
                .strategyArray(StrategyConfig.strategyHandlerArray)// 2⃣ 填充策略执行集合
                .strategyHandler(new StrategyHandle.ResponsibilityFactory())// 3⃣ 绑定策略执行器
                .build();
// 初始化设置方案
RRouter.setUp(mApplication, repository);// 尽可能早，推荐在Application中初始化
```



#### 第四步：构建请求

##### 业务请求项

```java
public class LoginRequest  {

    // 反馈监听器
    protected DataSource.Callback<LoginModel> callback;
    // 请求代理
    protected AbstractRequestProxy<LoginCard, LoginModel> proxy;
    // 请求卡片
    private final LoginCard requestCard = new LoginCard();

    public LoginRequest(DataSource.Callback<LoginModel> callback) {
        this.callback = callback;
        proxy = new AbstractRequestProxy<LoginCard,
                LoginModel>(new LoginRequestStrategy(),// 请求策略
                createProxyCallback()) {// 代理反馈监听器
            @Override
            protected LoginCard onCreateRequestCard() {
                return requestCard;
            }
        };
    }

    /**
     * 登陆
     *
     * @param account  账号
     * @param password 密码
     */
    public void login(String account, String password) {
        requestCard.update(account, password);
        proxy.request();
    }

    /**
     * 创建代理反馈监听器
     */
    protected DataSource.Callback<LoginModel> createProxyCallback() {
        return new DataSource.Callback<LoginModel>() {
            @Override
            public void onDataNotAvailable(String message) {
                if (callback != null)
                    callback.onDataNotAvailable(message);
            }

            @Override
            public void onDataLoaded(LoginModel responseModel) {
                if (callback != null)
                    callback.onDataLoaded(responseModel);
            }
        };
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (proxy != null) {
            proxy.onDestroy();
        }
        proxy = null;
        callback = null;
    }
}
```

##### 请求策略工厂，配置真实调度的请求策略，以及请求策略类型

```java
public class LoginRequestStrategy extends BaseRequestStrategyFactory<LoginCard, LoginModel> {

    /**
     * 真实网络请求策略
     */
    @Override
    protected Request<LoginCard> onCreateRealRemoteRequestStrategy(
            StrategyCallback<LoginModel> callback) {
        return new LoginNetWorkRequest(callback);
    }

    /**
     * 真实本地请求策略
     */
    @Override
    protected Request<LoginCard> onCreateRealLocalRequestStrategy(
            StrategyCallback<LoginModel> callback) {
        return new LoginLocalRequest(callback);
    }

    /**
     * 优先网络请求登陆
     * 否则按照本地保留的账号记录登陆操作
     */
    @Override
    public int onLoadRequestStrategyType() {
        return StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_LOCATION;
    }
}
```

##### 远程请求策略

```java
public class LoginRemoteRequest extends AbstractRemoteRequestStrategy<LoginCard, LoginModel> {

    public LoginNetWorkRequest(StrategyCallback<LoginModel> callback) {
        super(callback);
    }

  	// 实际可用 rxjava + retrofit 
    @Override
    protected Observable<IRspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        return Observable.just(RspModel.buildToSuccess(LoginModel.build()));
    }

}
```

##### 本地请求策略

```java
public class LoginLocalRequest extends AbstractLocalRequestStrategy<LoginCard, LoginModel> {

    public LoginLocalRequest(StrategyCallback<LoginModel> callback) {
        super(callback);
    }

    @Override
    protected Observable<IRspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        if (loginCard != null && Objects.equals(loginCard.getUserName(), "admin")
                && Objects.equals(loginCard.getPassword(), "root")) {
            callback.onDataLoaded(LoginModel.build());
            return Observable.just(RspModel.buildToSuccess(LoginModel.build()));
        } else {
            return Observable.just(RspModel.buildToFailure("账号密码错误"));
        }
    }
}
```

#### 绑定使用

```java
public class MainViewModel{

    private LoginRequest loginRequest;

    /**
     * 登陆动作
     */
    public void loginClick() {
        LoginRequest request = getLoginRequest();
        request.login(account, password);
    }

    /**
     * 登陆请求,创建请求方法创建
     */
    private LoginRequest getLoginRequest() {
        if (loginRequest == null) {
            loginRequest = new LoginRequest(new DataSource.Callback<LoginModel>() {
                @Override
                public void onDataNotAvailable(String message) {
                    Log.v(TAG,message);
                }

                @Override
                public void onDataLoaded(LoginModel loginModel) {
                    Log.v(TAG,"user: " + loginModel.getAccessToken());
                }
            });
        }
        return loginRequest;
    }

		/**
     * 销毁
     */
    public void onDestroy() {
        loginRequest.destroy();
    }
}
```



### 二、进阶版

对已提供的 `策略库` 的简化支持。我们这里使用 `注解` ，帮助使用者在编译阶段生成目标模版代码，让使用者只需考虑核心业务编写。

#### 第一步：在原有基础上，添加策略依赖

```groovy
dependencies {
    implementation 'com.github.Sheedon.RequestRouter:rrouter-annotation:lastVersion'
    annotationProcessor 'com.github.Sheedon.RequestRouter:rrouter-compiler:lastVersion'
}
```

#### 第二步：添加请求路由

```java
// 请求路由注解
// 支持四项内容
// 1. 请求策略类型：requestStrategy  
// 2. 是否优先使用注解请求策略类型：used
// 3. localRequestClass 本地请求策略类 - 可直接绑定原实现中的 LoginLocalRequest.class
// 4. remoteRequestClass 远程请求策略类 - 可直接绑定原实现中的 LoginRemoteRequest.class
// 请求路由类，当前只支持 继承 AbstractRequestRouter 实现
@RRouter(requestStrategy = DefaultStrategy.TYPE_NOT_DATA_TO_LOCATION)
public class LoginRouter extends AbstractRequestRouter<LoginCard, LoginModel> {

    /**
     * @Provider 用于标注构造方法，标注后，才会在编译时生成的类中创建 该构造方法 的方法实现。
     * 用于注解创建对象
     */
    @Provider
    public LoginRouter() {
    }

    /**
     * 用于注解创建带参对象
     */
    @Provider
    LoginRouter(String name, String password) {
        LoginRequestBodyAdapter adapter = requestAdapter();
        adapter.attach(name, password);
    }

    /**
     * @RequestStrategy 标注后，才会在编译时创建的类上去使用请求方法。
     * 本地/远程 各自最多使用一个，可添加的位置分别是：onLoadRemoteMethod/remoteRequestClass
     * /onLoadLocalMethod/localRequestClass 
     * 若不希望使用默认提供的方法，也可自定义实现，自定义实现，需在注解中添加requestStrategy字段，以表明调用请求类型。
     *
     * 远程请求方法
     * 
     * @param loginCard 请求卡片
     * @return Observable<IRspModel<LoginModel>>
     */
    @RequestStrategy
    @Override
    public Observable<IRspModel<LoginModel>> onLoadRemoteMethod(LoginCard loginCard) {
        return Observable.just(new Random().nextBoolean() ? RspModel.buildToSuccess(LoginModel.build())
                : RspModel.buildToFailure("网络请求失败"));
    }
  
    /**
     * 若已使用onLoadRemoteMethod，标注@RequestStrategy注解，当前方法便不可添加@RequestStrategy
     * 远程请求类
     * @param callback 回调绑定
     * @return AbstractRequestStrategy<LoginCard, LoginModel>
     */
    @Override
    public AbstractRequestStrategy<LoginCard, LoginModel> remoteRequestClass(StrategyCallback<LoginModel> callback) {
        return new LoginRemoteRequest(callback);
    }


    /**
     * 复杂逻辑可以添加到「继承AbstractRequestStrategy的类」创建的请求策略类中
     * 简单的可以直接使用onLoadLocalMethod方法构建，参考 {@link onLoadRemoteMethod()}
     */
    @Override
    @RequestStrategy
    public AbstractRequestStrategy<LoginCard, LoginModel> localRequestClass(StrategyCallback<LoginModel> callback) {
        return new LoginLocalRequest(callback);
    }

    /**
     * @RequestDataAdapter 请求数据转化器注解
     * 当前只支持在requestAdapter方法上标注，且必须标注
     * 目的是将「请求数据」转化为「请求卡片」
     * 请求数据转化适配器
     */
    @RequestDataAdapter
    @Override
    public LoginRequestBodyAdapter requestAdapter() {
        return new LoginRequestBodyAdapter();
    }
  
  	/**
  	 * @CallbackDataAdapter 反馈数据转化器注解
  	 * 当前只支持在convertAdapter方法上标注
  	 * 目的是将 远程/本地 反馈的格式 转换为所需的格式
     * 反馈结果转化适配器
     */
    @CallbackDataAdapter
    @Override
    protected Converter<LoginModel, ?> convertAdapter() {
        return super.convertAdapter();
    }

    /**
     * 请求数据转化策略
     */
    public static class LoginRequestBodyAdapter extends RequestBodyAdapter.Factory<LoginCard> {

        @Override
        protected LoginCard createRequestBody() {
            return new LoginCard();
        }

        public LoginRequestBodyAdapter attach(String name, String password) {
            getRequestBody().update(name, password);
            return this;
        }

    }
}
```

#### 第三步：绑定请求路由

在需要使用到请求路由的类中，添加绑定

```java
public class MainViewModel implements MainViewModelComponent.OnCallbackListener {

  	/**
     * @Request 标注需要使用的请求路由
     * 编译时会自动创建 MainViewModel 的 请求职责组合类 MainViewModelComponent
     * 由MainViewModelComponent 来代为执行 创建和销毁 动作，达到控制反转目的。
     */
    @Request
    LoginRouter loginRouter;

    private IComponent component;

  	/**
  	 * 初始方法中调用 MainViewModelComponent的构造方法
  	 * Builder builder(AnnotationViewModel host, OnCallbackListener listener);
		 * IComponent create(AnnotationViewModel host, OnCallbackListener listener);
		 * new MainViewModelComponent.Builder()
		 * 将当前类和反馈监听接口做绑定
  	 */
    public void initConfig() {
        component = MainViewModelComponent.create(this, this);
    }

  	/**
  	 * 销毁
  	 */
    public void onDestroy(){
      	component.onDestroy();
    }
    
}
```



### 三、自定义请求策略方案

#### 第一步：在原有基础上，添加策略依赖

```groovy
dependencies {
    api 'com.github.Sheedon.RequestRouter:rrouter-core:lastVersion'
    api 'com.github.Sheedon.RequestRouter:rrouter-strategy-support:lastVersion'
}
```

#### 第二步：创建请求策略处理器

可继承 `BaseStrategyHandler` 来构建 目标策略处理方案。

```java
/**
 * 基础策略执行者
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:33 下午
 */
public abstract class BaseStrategyHandler implements StrategyHandle {


    /**
     * 处理请求代理
     *
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param <RequestCard>     请求卡片
     * @return 是否处理成功
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(ProcessChain processChain,
                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                       RequestCard card) {
        return handleRealRequestStrategy(processChain, requestStrategies, card);
    }

    /**
     * 真实处理请求代理
     *
     * @param progress          请求进度
     * @param requestStrategies 请求策略集合
     * @param card              请求卡片
     * @param callback          反馈监听器
     * @param <RequestCard>     请求卡片类型
     * @return 是否调用成功
     */
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard card) {
        // 拿到当前进度对应的请求
        Request<RequestCard> request = requestStrategies.get(processChain.getProcess());
        // 请求不存在，则请求失败
        if (request == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        // 状态并非「未发送」，则请求失败
        if (processChain.getCurrentStatus() != ProcessChain.STATUS_NORMAL) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        // 请求任务
        processChain.updateCurrentStatus(ProcessChain.STATUS_REQUESTING);
        request.request(card);
        return true;
    }

    /**
     * 处理反馈代理
     *
     * @param callback        反馈监听
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 结果model类型
     * @return 是否处理成功
     */
    @Override
    public <ResponseModel> boolean handleCallbackStrategy(ProcessChain processChain,
                                                          DataSource.Callback<ResponseModel> callback,
                                                          ResponseModel model,
                                                          String message,
                                                          boolean isSuccess) {

        // 当前状态已完成，不做额外反馈处理
        if (processChain.getCurrentStatus() == ProcessChain.STATUS_COMPLETED) {
            return false;
        }

        // 当前状态是默认，意味着流程错误，不往下执行
        if (processChain.getCurrentStatus() == ProcessChain.STATUS_NORMAL) {
            return false;
        }

        // 真实执行
        return handleRealCallbackStrategy(processChain, callback,
                model, message, isSuccess);
    }

    /**
     * 真实处理反馈代理
     *
     * @param processChain    流程链
     * @param callback        反馈监听
     * @param model           数据
     * @param message         描述信息
     * @param isSuccess       是否请求成功
     * @param <ResponseModel> 反馈model类型
     * @return 是否处理成功
     */
    protected <ResponseModel>
    boolean handleRealCallbackStrategy(ProcessChain processChain,
                                       DataSource.Callback<ResponseModel> callback,
                                       ResponseModel model,
                                       String message,
                                       boolean isSuccess) {
        // 状态并非「发送中」，则反馈执行失败
        if (processChain.getCurrentStatus() != ProcessChain.STATUS_REQUESTING) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
        handleCallback(callback, model, message, isSuccess);
        return true;
    }

    /**
     * 处理反馈结果
     *
     * @param callback        反馈监听器
     * @param model           反馈Model
     * @param message         描述信息
     * @param isSuccess       是否为成功数据反馈
     * @param <ResponseModel> 反馈数据类型
     */
    protected <ResponseModel> void handleCallback(DataSource.Callback<ResponseModel> callback,
                                                  ResponseModel model, String message,
                                                  boolean isSuccess) {

        if (isSuccess) {
            callback.onDataLoaded(model);
            return;
        }

        callback.onDataNotAvailable(message);
    }
}
```

