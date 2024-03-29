package org.sheedon.requestrepository.request.annotation;

import org.sheedon.requestrepository.RspModel;
import org.sheedon.requestrepository.data.card.LoginCard;
import org.sheedon.requestrepository.data.model.LoginModel;
import org.sheedon.rrouter.core.StrategyCallback;

import java.util.Random;

import io.reactivex.rxjava3.core.Observable;

/**
 * 登陆的请求路由
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/17 9:40 下午
 */
//@RRouter(requestStrategy = DefaultStrategy.TYPE_NOT_DATA_TO_LOCATION)
//public class LoginRouter extends AbstractRequestRouter<LoginCard, RspModel<LoginModel>> {
//
//    /**
//     * 用于注解创建对象
//     */
//    @Provider
//    public LoginRouter() {
//    }
//
//    /**
//     * 用于注解创建带参对象
//     */
//    @Provider
//    LoginRouter(String name, String password) {
//        LoginRequestBodyAdapter adapter = requestAdapter();
//        adapter.attach(name, password);
//    }
//
//    /**
//     * 远程请求方法
//     *
//     * @param loginCard 请求卡片
//     * @return Observable<IRspModel < LoginModel>>
//     */
//    @RequestStrategy
//    @Override
//    public Observable<RspModel<LoginModel>> onLoadRemoteMethod(LoginCard loginCard) {
//        return Observable.just(new Random().nextBoolean() ? RspModel.buildToSuccess(LoginModel.build())
//                : RspModel.buildToFailure("网络请求失败"));
//    }
//
//    /**
//     * 远程请求类
//     *
//     * @param callback 回调绑定
//     * @return AbstractRequestStrategy<LoginCard, LoginModel>
//     */
//    @Override
//    public AbstractRequestStrategy<LoginCard, RspModel<LoginModel>> remoteRequestClass(StrategyCallback<RspModel<LoginModel>> callback) {
//        return new LoginRemoteRequest(callback);
//    }
//
//    /**
//     * 复杂逻辑可以添加到「继承AbstractRequestStrategy的类」创建的请求策略类中
//     * 简单的可以直接使用onLoadLocalMethod方法构建，参考 {@link onLoadRemoteMethod()}
//     */
//    @Override
//    @RequestStrategy
//    public AbstractRequestStrategy<LoginCard, RspModel<LoginModel>> localRequestClass(StrategyCallback<RspModel<LoginModel>> callback) {
//        return new LoginLocalRequest(callback);
//    }
//
//    /**
//     * 请求数据转化适配器
//     */
//    @RequestDataAdapter
//    @Override
//    public LoginRequestBodyAdapter requestAdapter() {
//        return new LoginRequestBodyAdapter();
//    }
//
//    @Override
//    public IResponseDispatcher<RspModel<LoginModel>> dispatcher() {
//        return loginModel -> System.out.println(loginModel.toString());
//    }
//
//    /**
//     * 请求数据转化策略
//     */
//    public static class LoginRequestBodyAdapter extends RequestBodyAdapter.Factory<LoginCard> {
//
//        @Override
//        protected LoginCard createRequestBody() {
//            return new LoginCard();
//        }
//
//        public LoginRequestBodyAdapter attach(String name, String password) {
//            getRequestBody().update(name, password);
//            return this;
//        }
//
//    }
//}
