package com.vertx.mysql.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.mysql.exception.MysqlClientInitException;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import static com.vertx.common.core.config.VertxLoadConfig.appConfig;
import static com.vertx.common.core.config.VertxLoadConfig.isInit;
import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static io.vertx.core.Future.await;

public class MysqlClient {

  public static Pool mysqlClient;

  /**
   * 初始化连接池
   *
   * @param config MySQL连接配置
   * @param isDef  是否为默认连接池
   * @return 返回初始化后的连接池
   * @throws MysqlClientInitException 如果全局初始化未完成，则抛出该异常
   */
  public Pool init(AppConfig.Mysql config, Boolean isDef) {
    if (!isInit) {
      StaticLog.error("全局初始化未完成,请先调用:VertxLoadConfig.init()");
      throw new MysqlClientInitException("全局初始化未完成,请先调用:VertxLoadConfig.init()");
    }
    final MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions();
    final String host = config.getHost();
    if (StrUtil.isBlank(host)) {
      StaticLog.error("mysql host is null");
      throw new MysqlClientInitException("mysql host is null");
    }
    mySQLConnectOptions.setHost(host);
    final int port = config.getPort();
    mySQLConnectOptions.setPort(port);
    final String username = config.getUsername();
    if (StrUtil.isBlank(username)) {
      StaticLog.error("mysql username is null");
      throw new MysqlClientInitException("mysql username is null");
    }
    mySQLConnectOptions.setUser(username);
    final String password = config.getPassword();
    if (StrUtil.isBlank(password)) {
      StaticLog.error("mysql password is null");
      throw new MysqlClientInitException("mysql password is null");
    }
    mySQLConnectOptions.setPassword(password);
    final String database = config.getDatabase();
    if (StrUtil.isBlank(database)) {
      StaticLog.error("mysql database is null");
      throw new MysqlClientInitException("mysql database is null");
    }
    mySQLConnectOptions.setDatabase(database);
    String charset = config.getCharset();
    if (StrUtil.isBlank(charset)) {
      charset = "utf8mb4";
    }
    mySQLConnectOptions.setCharset(charset);
    String timezone = config.getTimezone();
    if (StrUtil.isBlank(timezone)) {
      timezone = "Asia/Shanghai";
    }
    mySQLConnectOptions.getProperties().put("serverTimezone", timezone);
    final AppConfig.App app = appConfig.getApp();
    if (app == null) {
      StaticLog.error("app config is null");
      throw new MysqlClientInitException("app config is null");
    }
    final String poolName = app.getName();
    final String version = app.getVersion();
    final PoolOptions poolOptions = new PoolOptions();
    poolOptions.setName(String.format("mysql-pool-%s-%s", poolName, version));

    int maxSize = config.getMaxPoolSize();
    if (maxSize <= 0) {
      maxSize = 16;
    }
    poolOptions.setMaxSize(maxSize);
    int maxWaitQueueSize = config.getMaxWaitQueueSize();
    if (maxWaitQueueSize <= 0) {
      maxWaitQueueSize = 1024;
    }
    poolOptions.setMaxWaitQueueSize(maxWaitQueueSize);
    int idleTimeout = config.getIdleTimeout();
    if (idleTimeout <= 0) {
      idleTimeout = 30;
    }
    poolOptions.setIdleTimeout(idleTimeout);
    int connectionTimeout = config.getConnectionTimeout();
    if (connectionTimeout <= 0) {
      connectionTimeout = 2;
    }
    poolOptions.setConnectionTimeout(connectionTimeout);
    int maxLifetime = config.getMaxLifetime();
    if (maxLifetime <= 0) {
      maxLifetime = 1800;
    }
    poolOptions.setMaxLifetime(maxLifetime);
    final Pool pool = Pool.pool(vertx, mySQLConnectOptions, poolOptions);
    await(pool.query("select  1").execute());
    StaticLog.info("mysql连接成功");
    if (isDef) {
      mysqlClient = pool;
    }
    return pool;
  }
}
