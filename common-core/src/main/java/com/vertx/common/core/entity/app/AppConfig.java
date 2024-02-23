package com.vertx.common.core.entity.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.http.HttpVersion;
import lombok.Data;

import java.util.List;

/**
 * app: # 应用配置
 * name: "example" # 应用名称
 * version: "1.0.0" # 应用版本
 * description: "示例项目" # 应用描述
 * webServer: # web服务配置
 * port: 3020 # 服务端口
 * host: "0.0.0.0" # 服务绑定的ip
 * alpnVersions: # 支持的协议版本
 * - "HTTP_2"
 * - "HTTP_1_1"
 * prefix: "/api/example/*" # 路由前缀
 * timeout: 30000 # web请求超时时间
 * logEnabled: true # 是否开启日志
 * compressionSupported: true # 是否支持压缩
 * compressionLevel: 6 # 压缩级别
 * ignorePaths: # 忽略的路径
 * - "/api/example/health"
 * <p>
 * database: # 数据库配置
 * mysql: # mysql数据库配置
 * host: "192.168.2.234" # 数据库地址
 * port: 3306 # 数据库端口
 * username: "anjun" # 数据库用户名
 * password: "anjun123" # 数据库密码
 * database: "vertx-example" # 数据库名称
 * charset: "utf8mb4" # 数据库编码
 * timezone: "Asia/Shanghai" # 数据库时区
 * maxPoolSize: 16 # 最大连接数 时间配置单位s
 * idleTimeout: 30 # 空闲连接超时时间
 * connectionTimeout: 2 # 连接超时时间
 * maxLifetime: 1800 # 连接最大生命周期
 * maxWaitQueueSize: 5000 #
 * mq:
 * rabbitmq: # rabbitmq配置 com.vertx.common.entity.Rabbitmq
 * host: "127.0.0.1" # rabbitmq地址
 * port: 5672 # rabbitmq端口
 * username: "guest" # rabbitmq用户名
 * password: "guest" # rabbitmq密码
 * virtualHost: "/" # rabbitmq虚拟主机
 * maxQos: 10 # 最大qos
 * requestedChannelMax: 4095 #
 * automaticRecoveryEnabled: true # 是否开启自动恢复
 * networkRecoveryInterval: 5000 # 网络恢复间隔
 * handshakeTimeout: 10000 # 握手超时时间
 * connectionTimeout: 10000 # 连接超时时间
 * reconnectAttempts: 100 # 重连次数
 * reconnectInterval: 500 # 重连间隔
 * requestedHeartbeat: 10 # 心跳间隔
 * <p>
 * <p>
 * <p>
 * vertx: # vertx配置
 * verticle: com.vertx.example.verticle.MainVerticle # verticle类
 * instances: 1 # verticle实例数
 * ha: true # 是否开启ha
 * <p>
 * webClient: # web客户端配置
 * maxPoolSize: 16 # 最大连接数
 * connectTimeout: 2000 # 连接超时时间
 * readIdleTimeout: 20000 # 读取超时时间
 * idleTimeout: 10000 # 空闲连接超时时间
 * writeIdleTimeout: 10000 # 写入超时时间
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    // 应用配置
    private App app;
    // web服务配置
    private WebServer webServer;
    // 数据库配置
    private Database database;
    // Rabbitmq配置
    private Mq mq;
    // vertx配置
    private Vertx vertx;
    // web客户端配置
    private WebClient webClient;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class App {
        private String name;
        private String version;
        private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebServer {
        /**
         * 服务器端口号
         */
        private int port = 8080;
        /**
         * 服务器主机名
         */
        private String host = "0.0.0.0";
        /**
         * ALPN版本列表
         */
        private List<HttpVersion> alpnVersions = List.of(HttpVersion.HTTP_2, HttpVersion.HTTP_1_1);
        /**
         * 服务器路径前缀
         */
        private String prefix = "/api/*";
        /**
         * 超时时间
         */
        private int timeout = 30000;
        /**
         * 是否启用日志记录
         */
        private boolean logEnabled = false;
        /**
         * 是否支持压缩
         */
        private boolean compressionSupported = false;
        /**
         * 压缩级别
         */
        private int compressionLevel = 6;
        /**
         * 忽略的路径列表
         */
        private String[] ignorePaths = new String[0];

        /**
         * 最大初始行长度
         */
        private int maxInitialLineLength;

        /**
         * 最大块大小
         */
        private int maxChunkSize;

        /**
         * 最大表头大小
         */
        private int maxHeaderSize;

        /**
         * 最大表单属性大小
         */
        private int maxFormAttributeSize;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Database {
        private Mysql mysql;

        private Redis redis;
    }

    /**
     * MySQL数据库连接配置
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mysql {
        /**
         * 主机地址
         */
        private String host = "127.0.0.1";
        /**
         * 端口号
         */
        private int port = 3306;
        /**
         * 用户名
         */
        private String username = "root";
        /**
         * 密码
         */
        private String password = "root";
        /**
         * 数据库名
         */
        private String database = "vertx";
        /**
         * 字符集
         */
        private String charset = "utf8mb4";
        /**
         * 时区
         */
        private String timezone = "Asia/Shanghai";
        /**
         * 最大连接池大小
         */
        private int maxPoolSize = 16;
        /**
         * 静默超时时间
         */
        private int idleTimeout = 30;
        /**
         * 连接超时时间
         */
        private int connectionTimeout = 2;
        /**
         * 连接最长存活时间
         */
        private int maxLifetime = 1800;
        /**
         * 最大等待队列大小
         */
        private int maxWaitQueueSize = 5000;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Redis {
        /**
         * Redis的主机地址
         */
        private String host = "127.0.0.1";
        /**
         * Redis的端口号
         */
        private int port = 6379;
        /**
         * Redis的密码
         */
        private String password = "";
        /**
         * Redis的数据库
         */
        private int database = 0;
        /**
         * Redis的最大连接数
         */
        private int maxPoolSize = 16;
        /**
         * 最大等待队列数
         */
        private int maxWaitQueueSize = 24;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mq {
        private Rabbitmq rabbitmq;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rabbitmq {
        /**
         * RabbitMQ的主机地址
         */
        private String host = "127.0.0.1";
        /**
         * RabbitMQ的端口号
         */
        private int port = 5672;
        /**
         * RabbitMQ的用户名
         */
        private String username = "guest";
        /**
         * RabbitMQ的密码
         */
        private String password = "guest";
        /**
         * RabbitMQ的虚拟主机
         */
        private String virtualHost = "/";
        /**
         * RabbitMQ的最大消息确认数量
         */
        private int maxQos = 1;

        /**
         * 是否发送确认
         */
        private Boolean sendConfirm = false;

        /**
         * 重连次数
         */
        private int reconnectAttempts = 100;

        /**
         * 链接超时时间
         */
        private int connectionTimeout = 10000;
        /**
         * 请求的通道最大数量
         */
        private int requestedChannelMax = 100;
        /**
         * 是否自动恢复启用
         */
        private Boolean automaticRecoveryEnabled = true;
        /**
         * 网络恢复间隔
         */
        private int networkRecoveryInterval = 5000;
        /**
         * 手动超时时间
         */
        private int handshakeTimeout = 10000;

        /**
         * 心跳间隔
         */
        private int requestedHeartbeat = 10;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vertx {
        /**
         * 服务端页控制器版本。
         */
        private String verticle;

        /**
         * 实例数量。
         */
        private int instances = 1;

        /**
         * 是否开启故障转移。
         */
        private boolean ha = false;
    }

    /**
     * WebClient类，表示一个用于发送HTTP请求的客户端。
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebClient {
        /**
         * maxPoolSize，表示最大连接池大小。
         */
        private int maxPoolSize = 10;

        /**
         * connectTimeout，表示连接超时时间。
         */
        private int connectTimeout = 10000;

        /**
         * readIdleTimeout，表示读取空闲超时时间。
         */
        private int readIdleTimeout = 10000;

        /**
         * idleTimeout，表示连接空闲超时时间。
         */
        private int idleTimeout = 10000;

        /**
         * writeIdleTimeout，表示写入空闲超时时间。
         */
        private int writeIdleTimeout = 10000;
    }

}
