package com.vertx.rabbitmq.helper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.log.StaticLog;
import com.rabbitmq.client.MessageProperties;
import com.vertx.common.core.entity.mq.MqMessageData;
import com.vertx.common.core.enums.EnvEnum;
import com.vertx.common.core.utils.StrUtil;
import com.vertx.rabbitmq.client.RabbitMqClient;
import com.vertx.rabbitmq.enums.IRabbitMqExChangeEnum;
import com.vertx.rabbitmq.enums.RabbitMqExChangeTypeEnum;
import com.vertx.rabbitmq.exception.RabbitMQSendException;
import com.vertx.rabbitmq.handler.RabbitMqHandler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQConsumer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.vertx.common.core.config.VertxLoadConfig.*;
import static com.vertx.rabbitmq.client.RabbitMqClient.rabbitMqClient;
import static io.vertx.core.Future.await;

public class RabbitMqHelper {

    /**
     * 发送消息到交换机
     *
     * @param rabbitMqHandler 队列处理器
     * @param message         消息 与交换机类型匹配
     */
    public static <T> void sendMessageToExchange(RabbitMqHandler<T> rabbitMqHandler, T message) throws RabbitMQSendException {
        // Assemble exchange name
        IRabbitMqExChangeEnum rabbitMqExChangeEnum = rabbitMqHandler.getExchange();
        // Check if the exchange type supports sending messages to the exchange
        if (rabbitMqExChangeEnum.getType() == RabbitMqExChangeTypeEnum.DIRECT) {
            throw new RabbitMQSendException("Direct type exchange does not support sending messages to the exchange");
        }
        // Assemble message
        MqMessageData<T> mqMessageData = new MqMessageData<>(message);
        String exchanger = rabbitMqExChangeEnum.getExchanger();
        if (exchanger.isBlank()) {
            throw new RabbitMQSendException("The sending exchange cannot be empty");
        }
        // Check if the exchange message type matches
        if (rabbitMqExChangeEnum.getMessageType() != message.getClass()) {
            throw new RabbitMQSendException("Message type does not match");
        }
        // Persist message
        String persistence = rabbitMqHandler.persist(mqMessageData);
        if (!persistence.isBlank()) {
            StaticLog.warn("Message persistence failed: exchange name: " + exchanger + ", message: " + mqMessageData);
        }
        // Send message
        try {
            await(rabbitMqClient.basicPublish(
                    exchanger, "", MessageProperties.PERSISTENT_TEXT_PLAIN, Json.encodeToBuffer(mqMessageData)
            ));
        } catch (Exception e) {
            StaticLog.error(e, "Message serialization failed: exchange name: " + exchanger + ", message: " + mqMessageData);
            throw new RabbitMQSendException("Message serialization failed: exchange name: " + exchanger + ", message: " + mqMessageData);
        }
        if (appConfig.getMq().getRabbitmq().getSendConfirm()) {
            try {
                await(rabbitMqClient.waitForConfirms(TimeUnit.SECONDS.toMillis(5)));
            } catch (Exception e) {
                StaticLog.error(e, "Message sent failed: exchange name: " + exchanger + ", message: " + mqMessageData);
                throw new RabbitMQSendException("Message sent failed: exchange name: " + exchanger + ", message: " + mqMessageData);
            }
        }
        if (!active.equals(EnvEnum.PROD.getValue())) {
            StaticLog.debug("Message sent successfully: queue name: " + exchanger + ", message: " + message);
        }
    }


    public static <T> void sendMessageToQueue(RabbitMqHandler<T> rabbitMqHandler, T message) throws RabbitMQSendException {
        // Assemble queue name
        String queueName = assembleQueueName(rabbitMqHandler);
        if (queueName.isBlank()) {
            throw new RabbitMQSendException("队列名称不能为空");
        }
        // Assemble message
        MqMessageData<T> mqMessageData = new MqMessageData<>(message);
        // Persist message
        String persistence = rabbitMqHandler.persist(mqMessageData);
        if (!persistence.isBlank()) {
            StaticLog.warn("消息持久化失败:队列名称:" + queueName + ",消息:" + mqMessageData);
        }
        // Send message
        try {
            await(rabbitMqClient.basicPublish(
                    "", // Exchange name, send to the specified queue, no need to specify the exchange
                    queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    Json.encodeToBuffer(mqMessageData)
            ));
        } catch (Exception e) {
            StaticLog.error(e, "消息序列化失败:队列名称:" + queueName + ",消息:" + mqMessageData);
            throw new RabbitMQSendException("消息序列化失败:队列名称:" + queueName + ",消息:" + mqMessageData);
        }
        if (appConfig.getMq().getRabbitmq().getSendConfirm()) {
            try {
                await(rabbitMqClient.waitForConfirms(TimeUnit.SECONDS.toMillis(5)));
            } catch (Exception e) {
                StaticLog.error(e, "消息发送失败:队列名称:" + queueName + ",消息:" + mqMessageData);
                throw new RabbitMQSendException("消息发送失败:队列名称:" + queueName + ",消息:" + mqMessageData);
            }
        }
        if (!active.equals(EnvEnum.PROD.getValue())) {
            StaticLog.debug("Message sent successfully: queue name: " + queueName + ", message: " + message);
        }
    }

    private static final ConcurrentHashMap<String, Integer> retryMap = new ConcurrentHashMap<>();


    /**
     * 注册消费者
     *
     * @param rabbitMqHandler 队列处理器
     * @param <T>             消息类型
     */
    public static <T> void registerConsumer(RabbitMqHandler<T> rabbitMqHandler) {
        // Register queue
        registerQueue(rabbitMqHandler);
        // Assemble queue name
        String queueName = assembleQueueName(rabbitMqHandler);
        // Register consumer
        QueueOptions queueOptions = new QueueOptions();
        queueOptions.setMaxInternalQueueSize(rabbitMqHandler.getMaxInternalQueueSize());
        queueOptions.setKeepMostRecent(false);
        boolean autoAck = rabbitMqHandler.isAutoAck();
        queueOptions.setAutoAck(autoAck);
        RabbitMQConsumer rabbitMQConsumer = await(rabbitMqClient.basicConsumer(queueName, queueOptions));
        Class<T> requestClass = rabbitMqHandler.getRequestClass();
        // Consume message and execute business logic
        rabbitMQConsumer.handler(rabbitMQMessage -> {
            String msgId = "";
            // Message confirmation
            long deliveryTag = rabbitMQMessage.envelope().getDeliveryTag();
            try {
                // Message parsing
                JsonObject rabbitMqMsg = new JsonObject(rabbitMQMessage.body());
                if (!rabbitMqMsg.containsKey("id")) {
                    StaticLog.error("Message parsing failed, missing ID: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    ackMessage(autoAck, deliveryTag, "");
                    return;
                }
                msgId = rabbitMqMsg.getString("id");
                if (msgId == null || msgId.isBlank()) {
                    StaticLog.error("Message parsing failed, ID is null or blank: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    ackMessage(autoAck, deliveryTag, "");
                    return;
                }
                if (!rabbitMqMsg.containsKey("msg")) {
                    StaticLog.error("Message parsing failed, missing msg: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    ackMessage(autoAck, deliveryTag, msgId);
                    return;
                }
                JsonObject msgJson = rabbitMqMsg.getJsonObject("msg");
                if (msgJson == null) {
                    StaticLog.error("Message parsing failed, msg is null: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    ackMessage(autoAck, deliveryTag, msgId);
                    return;
                }
                T message;
                try {
                    message = msgJson.mapTo(requestClass);
                } catch (Exception e) {
                    StaticLog.error(e, "Message parsing failed: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    ackMessage(autoAck, deliveryTag, msgId);
                    return;
                }
                // Message parsing failed
                if (message == null) {
                    StaticLog.error("Message parsing failed: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    ackMessage(autoAck, deliveryTag, msgId);
                    return;
                }
                // Message persistence
                String res = rabbitMqHandler.handle(message);
                if (res != null && !res.isBlank()) {
                    rabbitMqHandler.callback(res, msgId);
                    StaticLog.error("Message processing failed: queue name: " + queueName + ", message: " + rabbitMqMsg);
                    // Check if need to retry
                    if (retryMap.getOrDefault(msgId, 0) >= rabbitMqHandler.getMaxRetry()) {
                        StaticLog.error("Message retry count exceeds maximum retry count: queue name: " + queueName + ", message: " + rabbitMqMsg);
                        ackMessage(autoAck, deliveryTag, msgId);
                        return;
                    }
                    // Retry
                    retryMap.put(msgId, retryMap.getOrDefault(msgId, 0) + 1);
                    // Return to queue
                    nackMessage(deliveryTag, rabbitMqHandler.getRetryInterval());
                    return;
                }
                // Callback
                rabbitMqHandler.callback("", msgId);
                ackMessage(autoAck, deliveryTag, msgId);
                if (!active.equals(EnvEnum.PROD.getValue())) {
                    StaticLog.debug("Message processing successful: queue name: " + queueName + ", message: " + rabbitMqMsg);
                }
            } catch (Throwable e) {
                // Callback
                if (msgId != null && !msgId.isBlank()) {
                    rabbitMqHandler.callback(ExceptionUtil.getSimpleMessage(e), msgId);
                }
                StaticLog.error(e, "Message processing failed: queue name: " + queueName + ", message: " + rabbitMQMessage.body());
                // If the exception is a parsing exception, then directly ack, because parsing exception re-entry queue is also a parsing exception
                if (e instanceof DecodeException || e instanceof IllegalArgumentException) {
                    ackMessage(autoAck, deliveryTag, "");
                    return;
                }
                // Check if need to retry
                if (!autoAck) {
                    nackMessage(deliveryTag, rabbitMqHandler.getRetryInterval());
                }
            }
            rabbitMQConsumer.exceptionHandler(e -> StaticLog.error(e, "Register consumer failed: queue name: " + queueName));
            rabbitMQConsumer.endHandler(v -> StaticLog.info("Consumer closed: queue name: " + queueName));
            StaticLog.info("Register consumer successful: queue name: " + queueName);
        });
    }


    // 组装队列名称:当前启动环境+模块名称+队列名称+业务开始日期+交换机名称+交换机类型
    private static String assembleQueueName(RabbitMqHandler<?> rabbitMqHandler) {
        RabbitMqExChangeTypeEnum exchangeType = rabbitMqHandler.getExchange().getType();
        String queueName = active + "." +
                rabbitMqHandler.getModuleName().getModelName().toLowerCase() + "." +
                rabbitMqHandler.getQueueName() + "." +
                exchangeType.name().toLowerCase() + "." +
                rabbitMqHandler.getDate();
        if (exchangeType != RabbitMqExChangeTypeEnum.DEFAULT) {
            queueName += "." + rabbitMqHandler.getExchange().getExchanger().toLowerCase();
        }
        return queueName;
    }

    private static <T> void registerQueue(RabbitMqHandler<T> rabbitMqHandler) {
        // Assemble queue name
        String queueName = assembleQueueName(rabbitMqHandler);
        // Register queue
        IRabbitMqExChangeEnum exchange = rabbitMqHandler.getExchange();
        // Check if the exchange is the default exchange, if not, register the exchange
        if (StrUtil.isBlank(exchange.getExchanger()) && exchange.getType() != RabbitMqExChangeTypeEnum.DEFAULT) {
            // Register exchange
            await(RabbitMqClient.rabbitMqClient.exchangeDeclare(
                    exchange.getExchanger(), exchange.getType().name().toLowerCase(), exchange.isDurable(), exchange.isAutoDelete()
            ));
        }
        // Check if the exchange message type matches
        if (exchange.getMessageType() != rabbitMqHandler.getRequestClass()) {
            StaticLog.error("The message type of the exchange: " + exchange.getExchanger() + " does not match, expected type: " + exchange.getMessageType() + ", actual type: " + rabbitMqHandler.getRequestClass());
            return;
        }
        StaticLog.info("Successfully registered exchange: exchange name: " + exchange.getExchanger() + ", exchange type: " + exchange.getType().name() + ", is it persistent: " + exchange.isDurable() + ", is it automatically deleted: " + exchange.isAutoDelete());
        // Register queue
        await(RabbitMqClient.rabbitMqClient.queueDeclare(
                queueName, rabbitMqHandler.isDurable(), rabbitMqHandler.isExclusive(), rabbitMqHandler.isAutoAck()
        ));
        await(RabbitMqClient.rabbitMqClient.queueBind(
                queueName, exchange.getExchanger(), ""
        ));
        StaticLog.info("Successfully registered queue: queue name: " + queueName + ", is it persistent: " + rabbitMqHandler.isDurable() + ", is it exclusive: " + rabbitMqHandler.isExclusive() + ", is it automatically deleted: " + rabbitMqHandler.isAutoAck());
    }


    // ack message
    private static void ackMessage(boolean autoAck, long deliveryTag, String msgId) {
        retryMap.remove(msgId);
        if (autoAck) {
            return;
        }
        // ack message
        try {
            await(rabbitMqClient.basicAck(deliveryTag, false));
        } catch (Exception e) {
            StaticLog.error(e, "ack message failed: deliveryTag: " + deliveryTag);
        }
    }

    // return to queue
    private static void nackMessage(long deliveryTag, long retryInterval) {
        // delay return to queue
        vertx.setTimer(retryInterval, id -> {
            try {
                await(rabbitMqClient.basicNack(deliveryTag, false, true));
            } catch (Exception e) {
                StaticLog.error(e, "return to queue failed: deliveryTag: " + deliveryTag);
            }
        });
    }
}
