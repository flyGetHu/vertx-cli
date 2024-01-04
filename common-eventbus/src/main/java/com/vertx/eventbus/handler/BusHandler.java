package com.vertx.eventbus.handler;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.enums.EnvEnum;
import com.vertx.common.core.exception.UniqueAddressException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;

import java.util.HashMap;
import java.util.Objects;

import static com.vertx.common.core.config.VertxLoadConfig.active;
import static com.vertx.common.core.config.VertxLoadConfig.eventBus;
import static io.vertx.core.Future.await;

public interface BusHandler<Request, Response> {

    /**
     * 获取请求类
     *
     * @return 返回请求类
     */
    Class<Request> getRequestClass();

    /**
     * 返回Response类型的Class对象
     *
     * @return 返回Response类型的Class对象
     */
    Class<Response> getResponseClass();

    /**
     * 获取地址
     *
     * @return 地址
     */
    String getAddress();

    /**
     * 处理请求并返回响应。
     *
     * @param request 包含请求信息的对象
     * @return 包含响应信息的对象
     */
    Response handle(Request request);

    default Response call(Request request) {
        final String encode;
        try {
            encode = Json.encode(request);
        } catch (Exception e) {
            StaticLog.error(e, "RPC服务序列化请求对象失败", getAddress());
            return null;
        }
        try {
            final Message<Object> message = await(eventBus.request(getAddress(), encode));
            final Object body = message.body();
            return Json.decodeValue(Json.encode(body), getResponseClass());
        } catch (DecodeException e) {
            StaticLog.error(e, "RPC服务反序列化响应对象失败", getAddress());
            return null;
        } catch (Exception e) {
            StaticLog.error(e, "RPC服务处理请求失败", getAddress());
            return null;
        }
    }

    HashMap<String, String> addressMap = new HashMap<String, String>();

    /**
     * 注册服务地址
     *
     * @throws UniqueAddressException 如果服务地址已存在，则抛出该异常
     */
    default void register() {
        final String address = this.getAddress();
        if (addressMap.containsKey(address)) {
            throw new UniqueAddressException("服务地址重复注册:" + address);
        }
        addressMap.put(address, address);
        StaticLog.info("注册服务地址:{}", address);
        final MessageConsumer<Object> consumer = eventBus.consumer(address, message -> {
            final Request request;
            try {
                final Object body = message.body();
                request = Json.decodeValue(body.toString(), getRequestClass());
            } catch (DecodeException e) {
                StaticLog.error(e, "RPC服务反序列化请求对象失败", address);
                message.fail(1, e.getMessage());
                return;
            }
            try {
                final Response response = this.handle(request);
                final String responseJson;
                if (response instanceof String) {
                    responseJson = (String) response;
                } else {
                    responseJson = Json.encode(response);
                }
                message.reply(responseJson);
                if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
                    final String requestJson = Json.encode(request);
                    StaticLog.info("RPC服务处理请求:{},请求参数,返回响应:{}", address, requestJson, responseJson);
                }
            } catch (Exception e) {
                StaticLog.error(e, "RPC服务处理请求失败", address);
                message.fail(1, e.getMessage());
            }
        });
        consumer.endHandler(v -> {
            StaticLog.info("服务地址:{}已关闭", address);
        });

        consumer.exceptionHandler(e -> {
            StaticLog.error(e, "服务地址:{}异常", address);
            consumer.unregister();
        });
    }
}
