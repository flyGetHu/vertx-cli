package com.vertx.eventbus.handler;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.enums.EnvEnum;
import com.vertx.common.core.exception.UniqueAddressException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;

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
        // 定义一个字符串变量encode，用于存储序列化后的请求对象
        final String encode;
        try {
            // 调用Json类的encode方法将request对象序列化为JSON字符串，并将结果赋值给encode变量
            encode = Json.encode(request);
        } catch (Exception e) {
            // 如果序列化过程中发生异常，则记录错误日志，并返回null
            StaticLog.error(e, "RPC服务序列化请求对象失败", getAddress());
            return null;
        }
        try {
            // 调用eventBus的request方法发送请求，并等待响应
            final Message<Object> message = await(eventBus.request(getAddress(), encode));
            // 获取响应消息的body部分
            final Object body = message.body();
            // 将body部分反序列化为指定类型的对象，并返回结果
            return Json.decodeValue(Json.encode(body), getResponseClass());
        } catch (DecodeException e) {
            // 如果反序列化过程中发生异常，则记录错误日志，并返回null
            StaticLog.error(e, "RPC服务反序列化响应对象失败", getAddress());
            return null;
        } catch (Exception e) {
            // 如果处理请求过程中发生异常，则记录错误日志，并返回null
            StaticLog.error(e, "RPC服务处理请求失败", getAddress());
            return null;
        }
    }

    /**
     * 注册服务地址
     *
     * @throws UniqueAddressException 如果服务地址已存在，则抛出该异常
     */
    default void register() {
        // 获取服务地址
        final String address = this.getAddress();
        // 记录日志：注册服务地址
        StaticLog.info("注册服务地址:{}", address);
        // 创建消息消费者
        final MessageConsumer<Object> consumer = eventBus.consumer(address, message -> {
            // 声明请求对象
            final Request request;
            try {
                // 获取消息体并解码为请求对象
                final Object body = message.body();
                request = Json.decodeValue(body.toString(), getRequestClass());
            } catch (DecodeException e) {
                // 记录日志：RPC服务反序列化请求对象失败
                StaticLog.error(e, "RPC服务反序列化请求对象失败", address);
                // 消息失败，返回错误信息
                message.fail(1, e.getMessage());
                return;
            }
            try {
                // 处理请求并返回响应
                final Response response = this.handle(request);
                // 根据响应类型转换为JSON字符串或保持原样
                final String responseJson;
                if (response instanceof String) {
                    responseJson = (String) response;
                } else {
                    responseJson = Json.encode(response);
                }
                // 回复消息，并设置响应内容为JSON字符串或保持原样
                message.reply(responseJson);
                // 判断环境是否为非生产环境，并记录日志：RPC服务处理请求及返回响应
                if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
                    final String requestJson = Json.encode(request);
                    StaticLog.debug("RPC服务处理请求:{},请求参数,返回响应:{}", address, requestJson, responseJson);
                }
            } catch (Exception e) {
                // 记录日志：RPC服务处理请求失败，并返回错误信息
                StaticLog.error(e, "RPC服务处理请求失败", address);
                // 消息失败，返回错误信息
                message.fail(1, e.getMessage());
            }
        });
        // 设置消费者结束处理程序，记录日志：服务地址已关闭
        consumer.endHandler(v -> {
            StaticLog.info("服务地址:{}已关闭", address);
        });
        // 设置消费者异常处理程序，记录日志：服务地址异常，并注销消费者
        consumer.exceptionHandler(e -> {
            StaticLog.error(e, "服务地址:{}异常", address);
            consumer.unregister();
        });
    }

}
