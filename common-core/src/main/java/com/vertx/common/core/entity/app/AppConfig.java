package com.vertx.common.core.entity.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

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
        private int port;
        private String host;
        private String[] alpnVersions;
        private String prefix;
        private int timeout;
        private boolean logEnabled;
        private boolean compressionSupported;
        private int compressionLevel;
        private String[] ignorePaths;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Database {
        private Mysql mysql;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mysql {
        private String host;
        private int port;
        private String username;
        private String password;
        private String database;
        private String charset;
        private String timezone;
        private int maxPoolSize;
        private int idleTimeout;
        private int connectionTimeout;
        private int maxLifetime;
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
        private String host;
        private int port;
        private String username;
        private String password;
        private String virtualHost;
        private int maxQos;
        private int requestedChannelMax;
        private boolean automaticRecoveryEnabled;
        private int networkRecoveryInterval;
        private int handshakeTimeout;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vertx {
        private String verticle;
        private int instances;
        private boolean ha;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebClient {
        private int maxPoolSize;
        private int connectTimeout;
        private int readIdleTimeout;
        private int idleTimeout;
        private int writeIdleTimeout;
    }
}
