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
        private int port;
        /**
         * 服务器主机名
         */
        private String host;
        /**
         * ALPN版本列表
         */
        private List<HttpVersion> alpnVersions = List.of(HttpVersion.HTTP_2, HttpVersion.HTTP_1_1);
        /**
         * 服务器路径前缀
         */
        private String prefix;
        /**
         * 超时时间
         */
        private int timeout;
        /**
         * 是否启用日志记录
         */
        private boolean logEnabled;
        /**
         * 是否支持压缩
         */
        private boolean compressionSupported;
        /**
         * 压缩级别
         */
        private int compressionLevel;
        /**
         * 忽略的路径列表
         */
        private String[] ignorePaths;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Database {
        private Mysql mysql;
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
        private String host;
        /**
         * 端口号
         */
        private int port;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
        /**
         * 数据库名
         */
        private String database;
        /**
         * 字符集
         */
        private String charset;
        /**
         * 时区
         */
        private String timezone;
        /**
         * 最大连接池大小
         */
        private int maxPoolSize;
        /**
         * 静默超时时间
         */
        private int idleTimeout;
        /**
         * 连接超时时间
         */
        private int connectionTimeout;
        /**
         * 连接最长存活时间
         */
        private int maxLifetime;
        /**
         * 最大等待队列大小
         */
        private int maxWaitQueueSize;
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
        private String host;
        /**
         * RabbitMQ的端口号
         */
        private int port;
        /**
         * RabbitMQ的用户名
         */
        private String username;
        /**
         * RabbitMQ的密码
         */
        private String password;
        /**
         * RabbitMQ的虚拟主机
         */
        private String virtualHost;
        /**
         * RabbitMQ的最大消息确认数量
         */
        private int maxQos;
        /**
         * 请求的通道最大数量
         */
        private int requestedChannelMax;
        /**
         * 是否自动恢复启用
         */
        private boolean automaticRecoveryEnabled;
        /**
         * 网络恢复间隔
         */
        private int networkRecoveryInterval;
        /**
         * 手动超时时间
         */
        private int handshakeTimeout;
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
        private int instances;

        /**
         * 是否开启故障转移。
         */
        private boolean ha;
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
        private int maxPoolSize;

        /**
         * connectTimeout，表示连接超时时间。
         */
        private int connectTimeout;

        /**
         * readIdleTimeout，表示读取空闲超时时间。
         */
        private int readIdleTimeout;

        /**
         * idleTimeout，表示连接空闲超时时间。
         */
        private int idleTimeout;

        /**
         * writeIdleTimeout，表示写入空闲超时时间。
         */
        private int writeIdleTimeout;
    }

}
