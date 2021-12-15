# RequestRouter
```tex
A framework for helping Android App to strategically transform request behavior.
```

[中文文档](https://github.com/Sheedon/RequestRouter/blob/master/README_CN.md)

With the request agent, the request action is configured according to the strategy and called in order as needed, simplifying the logic complexity caused by multiple request methods.

![请求路由](https://raw.githubusercontent.com/Sheedon/RequestRouter/82e73f4d32c3f820f8942f77c7fc6ad5feba2288/image/%E8%AF%B7%E6%B1%82%E8%B7%AF%E7%94%B1.svg)



### ONE、How to use

#### Step 1: Add the JitPack repository to your build file

```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```



#### Step 2: Add core dependencies

```groovy
dependencies {
    // apt has been used in rrouter-strategy, there is no need to add the introduction of rrouter-core
    // implementation 'com.github.Sheedon.RequestRouter:rrouter-core:lastVersion'
    implementation 'com.github.Sheedon.RequestRouter:rrouter-strategy:lastVersion'
}
```

`rrouter-core` , Request routing core package, mainly responsible for proxy execution of request strategy.

The request agent, through the request behavior provided by the client, forwards and requests the policy executor according to the configured scheduling policy, and executes the scheduling plan on its behalf, and after the scheduling result is obtained, the feedback listener provided by the client responds to the client.

`rrouter-strategy`, Local request + remote request strategy package.

The configured policies include: 
1. Single remote request 
2. Single local request 
3. Priority remote request, failed to take local request 
4. Priority local request, failed to take remote request 
5. Parallel remote local request



#### Step 3: Initialize SKD

```java
// Configure the default (remote and local) execution plan by configuring the warehouse
// 1⃣ 2⃣ 3⃣ You can choose any one
ConfigRepository repository = new ConfigRepository.Builder()
                .factory(new StrategyHandle.Factory())// 1⃣ Add strategy handle factory
                .strategyArray(StrategyConfig.strategyHandlerArray)// 2⃣ Populates the policy execution collection
                .strategyHandler(new StrategyHandle.ResponsibilityFactory())// 3⃣ Bind the policy executor
                .build();
// Initial setup scheme
RRouter.setUp(mApplication, repository);// As early as possible, it is recommended to initialize in Application
```



#### Step 4: Build the request

##### Business request

```java
public class LoginRequest  {

    // Callback listener
    protected DataSource.Callback<LoginModel> callback;
    // Request proxy
    protected AbstractRequestProxy<LoginCard, LoginModel> proxy;
    // request card
    private final LoginCard requestCard = new LoginCard();

    public LoginRequest(DataSource.Callback<LoginModel> callback) {
        this.callback = callback;
        proxy = new AbstractRequestProxy<LoginCard,
                LoginModel>(new LoginRequestStrategy(),// request strategy
                createProxyCallback()) {// proxy callback
            @Override
            protected LoginCard onCreateRequestCard() {
                return requestCard;
            }
        };
    }

    /**
     * login
     *
     * @param account  account
     * @param password password
     */
    public void login(String account, String password) {
        requestCard.update(account, password);
        proxy.request();
    }

    /**
     * Create proxy callback listener
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
     * destroy
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

##### Request strategy factory, configure the real scheduling request strategy, and request strategy type

```java
public class LoginRequestStrategy extends BaseRequestStrategyFactory<LoginCard, LoginModel> {

    /**
     * Real network request strategy
     */
    @Override
    protected Request<LoginCard> onCreateRealRemoteRequestStrategy(
            StrategyCallback<LoginModel> callback) {
        return new LoginNetWorkRequest(callback);
    }

    /**
     * real local request strategy
     */
    @Override
    protected Request<LoginCard> onCreateRealLocalRequestStrategy(
            StrategyCallback<LoginModel> callback) {
        return new LoginLocalRequest(callback);
    }

    /**
     * Priority network request login
     * Otherwise, log in according to the account record kept locally
     */
    @Override
    public int onLoadRequestStrategyType() {
        return StrategyConfig.STRATEGY.TYPE_NOT_DATA_TO_LOCATION;
    }
}
```

##### Remote request strategy

```java
public class LoginRemoteRequest extends AbstractRemoteRequestStrategy<LoginCard, LoginModel> {

    public LoginNetWorkRequest(StrategyCallback<LoginModel> callback) {
        super(callback);
    }

  	// Actually use rxjava + retrofit 
    @Override
    protected Observable<IRspModel<LoginModel>> onLoadMethod(LoginCard loginCard) {
        return Observable.just(RspModel.buildToSuccess(LoginModel.build()));
    }

}
```

##### Local request policy

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
            return Observable.just(RspModel.buildToFailure("Incorrect account password"));
        }
    }
}
```

#### Binding use

```java
public class MainViewModel{

    private LoginRequest loginRequest;

    /**
     * Login action
     */
    public void loginClick() {
        LoginRequest request = getLoginRequest();
        request.login(account, password);
    }

    /**
     * Login request, create request method creation
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
     * destroy
     */
    public void onDestroy() {
        loginRequest.destroy();
    }
}
```



### TWO、Advanced version

Simplified support for the provided `strategy library`. 
We use `notes` here to help users generate target template code during the compilation phase, 
so that users only need to consider core business writing.

#### Step 1: On the original basis, add a policy dependency

```groovy
dependencies {
    implementation 'com.github.Sheedon.RequestRouter:rrouter-annotation:lastVersion'
    annotationProcessor 'com.github.Sheedon.RequestRouter:rrouter-compiler:lastVersion'
}
```

#### Step 2: Add request routing

```java
// Request routing annotation
// Support four items
// 1. Request strategy type: requestStrategy
// 2. Whether to give priority to the annotation request strategy type：used
// 3. localRequestClass Local request strategy class-can be directly bound to the original implementation LoginLocalRequest.class
// 4. remoteRequestClass Remote request strategy class-can be directly bound to the original implementation LoginRemoteRequest.class
// Request routing class, currently only supports inheriting AbstractRequestRouter implementation
@RRouter(requestStrategy = DefaultStrategy.TYPE_NOT_DATA_TO_LOCATION)
public class LoginRouter extends AbstractRequestRouter<LoginCard, LoginModel> {

    /**
     * @Provider It is used to annotate the construction method. 
     * After annotation, the method implementation of the construction method will be created in the class generated at compile time.
     * Used to create objects with annotations
     */
    @Provider
    public LoginRouter() {
    }

    /**
     * Used for annotation to create objects with parameters
     */
    @Provider
    LoginRouter(String name, String password) {
        LoginRequestBodyAdapter adapter = requestAdapter();
        adapter.attach(name, password);
    }

    /**
     * @RequestStrategy After annotation, request methods are used on classes created at compile time.
     * A maximum of one can be used locally or remotely. The positions that can be added are as follows: onLoadRemoteMethod/remoteRequestClass
     * /onLoadLocalMethod/localRequestClass 
     * If you do not want to use the methods provided by default, 
     * you can also customize the implementation by adding a requestStrategy field in the annotation to indicate the invocation request type.
     *
     * Remote request method
     * 
     * @param loginCard Request card
     * @return Observable<IRspModel<LoginModel>>
     */
    @RequestStrategy
    @Override
    public Observable<IRspModel<LoginModel>> onLoadRemoteMethod(LoginCard loginCard) {
        return Observable.just(new Random().nextBoolean() ? RspModel.buildToSuccess(LoginModel.build())
                : RspModel.buildToFailure("Network request failed"));
    }
  
    /**
     * If the on Load Remote Method has been used and the @Request Strategy annotation has been marked, 
     * @Request Strategy cannot be added to the current method
     * Remote request class
     * @param callback bind callback
     * @return AbstractRequestStrategy<LoginCard, LoginModel>
     */
    @Override
    public AbstractRequestStrategy<LoginCard, LoginModel> remoteRequestClass(StrategyCallback<LoginModel> callback) {
        return new LoginRemoteRequest(callback);
    }


    /**
     * Complex logic can be added to a request policy class created "class that inherits AbstractRequestStrategy"
     * A simple build can be done directly using the onLoadLocalMethod method, {@link onLoadRemoteMethod()}
     */
    @Override
    @RequestStrategy
    public AbstractRequestStrategy<LoginCard, LoginModel> localRequestClass(StrategyCallback<LoginModel> callback) {
        return new LoginLocalRequest(callback);
    }

    /**
     * @RequestDataAdapter Request data converter annotation
     * Currently, annotations are only supported on requestAdapter methods and must be
     * The goal is to convert "request data" into "request cards"
     * 
     * Request the data conversion adapter
     */
    @RequestDataAdapter
    @Override
    public LoginRequestBodyAdapter requestAdapter() {
        return new LoginRequestBodyAdapter();
    }
  
  	/**
  	 * @CallbackDataAdapter Feedback data converter notes
  	 * Currently, only annotations on convertAdapter methods are supported
  	 * The goal is to convert the format of remote/local feedback into the desired format
     * Feedback results to the conversion adapter
     */
    @CallbackDataAdapter
    @Override
    protected Converter<LoginModel, ?> convertAdapter() {
        return super.convertAdapter();
    }

    /**
     * Request a data transformation policy
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

#### Step 3: Bind the request route

Add a binding to the class that needs to use request routing

```java
public class MainViewModel implements MainViewModelComponent.OnCallbackListener {

  	/**
     * @Request Annotate the request route to use
     * The request responsibility composition class MainViewModelComponent for MainViewModel is automatically created at compile time
     * The MainViewModelComponent performs the create and destroy actions for inversion of control.
     */
    @Request
    LoginRouter loginRouter;

    private IComponent component;

  	/**
  	 * Constructor that calls MainViewModelComponent in the initial method
  	 * Builder builder(AnnotationViewModel host, OnCallbackListener listener);
     * IComponent create(AnnotationViewModel host, OnCallbackListener listener);
     * new MainViewModelComponent.Builder()
     * Binds the current class to the feedback listener interface
  	 */
    public void initConfig() {
        component = MainViewModelComponent.create(this, this);
    }

  	/**
  	 * destroy
  	 */
    public void onDestroy(){
      	component.onDestroy();
    }
    
}
```



### THREE, custom request strategy scheme

#### Step 1: on the original basis, add a policy dependency

```groovy
dependencies {
    api 'com.github.Sheedon.RequestRouter:rrouter-core:lastVersion'
    api 'com.github.Sheedon.RequestRouter:rrouter-strategy-support:lastVersion'
}
```

#### Step 2: Create a request policy handler

Can inherit `BaseStrategyHandler` to construct the target strategy processing scheme.

```java
/**
 * Basic strategy executor
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/7/19 4:33 下午
 */
public abstract class BaseStrategyHandler implements StrategyHandle {


    /**
     * Processing request agent
     *
     * @param requestStrategies Request policy collection
     * @param card              request card
     * @param <RequestCard>     request card type
     * @return Whether the processing is successful
     */
    @Override
    public <RequestCard> boolean handleRequestStrategy(ProcessChain processChain,
                                                       SparseArray<Request<RequestCard>> requestStrategies,
                                                       RequestCard card) {
        return handleRealRequestStrategy(processChain, requestStrategies, card);
    }

    /**
     * The real processing request agent
     *
     * @param progress          Request progress
     * @param requestStrategies Request policy collection
     * @param card              Request card
     * @param callback          Callback listener
     * @param <RequestCard>     Request card type
     * @return Whether the call is successful
     */
    protected <RequestCard> boolean handleRealRequestStrategy(ProcessChain processChain,
                                                              SparseArray<Request<RequestCard>> requestStrategies,
                                                              RequestCard card) {
        // Get the request corresponding to the current progress
        Request<RequestCard> request = requestStrategies.get(processChain.getProcess());
        // The request does not exist, the request fails
        if (request == null) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        // If the status is not Unsent, the request fails
        if (processChain.getCurrentStatus() != ProcessChain.STATUS_NORMAL) {
            processChain.updateCurrentStatus(ProcessChain.STATUS_COMPLETED);
            return false;
        }

        // Request task
        processChain.updateCurrentStatus(ProcessChain.STATUS_REQUESTING);
        request.request(card);
        return true;
    }

    /**
     * Processing callback Agent
     *
     * @param callback        callback listener
     * @param message         message
     * @param isSuccess       callback successful
     * @param <ResponseModel> Response model type
     * @return Whether the processing is successful
     */
    @Override
    public <ResponseModel> boolean handleCallbackStrategy(ProcessChain processChain,
                                                          DataSource.Callback<ResponseModel> callback,
                                                          ResponseModel model,
                                                          String message,
                                                          boolean isSuccess) {

        // The current status has been completed, no additional callback processing
        if (processChain.getCurrentStatus() == ProcessChain.STATUS_COMPLETED) {
            return false;
        }

        // The current state is the default, which means that the process is wrong and will not be executed
        if (processChain.getCurrentStatus() == ProcessChain.STATUS_NORMAL) {
            return false;
        }

        // Real execution
        return handleRealCallbackStrategy(processChain, callback,
                model, message, isSuccess);
    }

    /**
     * Real handling of callback agents
     *
     * @param processChain    Process chain
     * @param callback        Feedback monitoring
     * @param model           model
     * @param message         Description
     * @param isSuccess       callback successful
     * @param <ResponseModel> Response model type
     * @return Whether the processing is successful
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
     * Processing callback results
     *
     * @param callback        callback listener
     * @param model           response model
     * @param message         message
     * @param isSuccess       callback successful
     * @param <ResponseModel> response model type
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

