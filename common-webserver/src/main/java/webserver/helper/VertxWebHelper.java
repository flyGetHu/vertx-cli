package webserver.helper;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.common.core.utils.StrUtil;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import webserver.entity.WebServiceOptions;
import webserver.exception.WebServerStartException;

import java.util.List;

import static com.vertx.common.core.config.VertxLoadConfig.*;
import static com.vertx.common.core.entity.web.ApiResponse.errorResponse;
import static io.vertx.core.Future.await;

public class VertxWebHelper {
  public void startHttpServer(WebServiceOptions options) {
    // 判断是否已经初始化
    if (!isInit) {
      StaticLog.error("VertxLoadConfig is not init");
      throw new webserver.exception.WebServerStartException("VertxLoadConfig is not init");
    }
    // 创建HTTP服务器选项
    final HttpServerOptions httpServerOptions = new HttpServerOptions();
    // 获取应用程序配置中的Web服务器选项
    final AppConfig.WebServer configWebServer = appConfig.getWebServer();
    // 如果webServer为空，则抛出异常
    if (configWebServer == null) {
      StaticLog.error("webServer is null");
      throw new webserver.exception.WebServerStartException("webServer is null");
    }
    // 设置服务器的主机和端口
    final String host = configWebServer.getHost();
    if (StrUtil.isBlank(host) || host.split("\\.").length != 4) {
      StaticLog.error("webServer host is null or error");
      throw new webserver.exception.WebServerStartException("webServer host is null or error");
    }
    httpServerOptions.setHost(host);
    final int port = configWebServer.getPort();
    if (port <= 0 || port > 65535) {
      StaticLog.error("webServer port is error, port:{}", port);
      throw new webserver.exception.WebServerStartException("webServer port is error,range:1-65535");
    }
    httpServerOptions.setPort(port);
    // 设置服务器的超时时间
    final int timeout = configWebServer.getTimeout();
    if (timeout <= 0) {
      StaticLog.error("webServer timeout is error, timeout:{}", timeout);
      throw new webserver.exception.WebServerStartException("webServer timeout is error,timeout>0");
    }
    httpServerOptions.setIdleTimeout(timeout);
    // 设置服务器的ALPN版本
    final List<HttpVersion> alpnVersions = configWebServer.getAlpnVersions();
    if (alpnVersions == null || alpnVersions.isEmpty()) {
      StaticLog.error("webServer alpnVersions is null or empty");
      throw new webserver.exception.WebServerStartException("webServer alpnVersions is null or empty");
    }
    httpServerOptions.setAlpnVersions(alpnVersions);
    // 设置注册WebSocket写处理程序
    httpServerOptions.setRegisterWebSocketWriteHandlers(configWebServer.isRegisterWebSocketWriteHandlers());
    // 设置是否支持压缩
    httpServerOptions.setCompressionSupported(configWebServer.isCompressionSupported());
    // 设置压缩级别
    final int compressionLevel = configWebServer.getCompressionLevel();
    if (compressionLevel < 0 || compressionLevel > 9) {
      StaticLog.error("webServer compressionLevel is error, compressionLevel:{}", compressionLevel);
      throw new webserver.exception.WebServerStartException("webServer compressionLevel is error,range:0-9");
    }
    httpServerOptions.setCompressionLevel(compressionLevel);
    // 设置最大分片大小
    final int maxChunkSize = configWebServer.getMaxChunkSize();
    if (maxChunkSize <= 0) {
      StaticLog.error("webServer maxChunkSize is error, maxChunkSize:{}", maxChunkSize);
      throw new webserver.exception.WebServerStartException("webServer maxChunkSize is error,maxChunkSize>0");
    }
    httpServerOptions.setMaxChunkSize(maxChunkSize);
    // 设置最大头大小
    final int maxInitialLineLength = configWebServer.getMaxInitialLineLength();
    if (maxInitialLineLength <= 0) {
      StaticLog.error("webServer maxInitialLineLength is error, maxInitialLineLength:{}", maxInitialLineLength);
      throw new webserver.exception.WebServerStartException(
          "webServer maxInitialLineLength is error,maxInitialLineLength>0");
    }
    httpServerOptions.setMaxInitialLineLength(maxInitialLineLength);
    // 设置最大表单属性大小
    final int maxHeaderSize = configWebServer.getMaxHeaderSize();
    if (maxHeaderSize <= 0) {
      StaticLog.error("webServer maxHeaderSize is error, maxHeaderSize:{}", maxHeaderSize);
      throw new webserver.exception.WebServerStartException("webServer maxHeaderSize is error,maxHeaderSize>0");
    }
    httpServerOptions.setMaxHeaderSize(maxHeaderSize);
    // 设置最大表单属性大小
    final int maxFormAttributeSize = configWebServer.getMaxFormAttributeSize();
    if (maxFormAttributeSize <= 0) {
      StaticLog.error("webServer maxFormAttributeSize is error, maxFormAttributeSize:{}", maxFormAttributeSize);
      throw new webserver.exception.WebServerStartException(
          "webServer maxFormAttributeSize is error,maxFormAttributeSize>0");
    }
    httpServerOptions.setMaxFormAttributeSize(maxFormAttributeSize);
    // 创建HTTP服务器
    final HttpServer httpServer = vertx.createHttpServer(httpServerOptions);
    // 创建路由器
    final Router mainRouter = Router.router(vertx);
    // 创建日志处理器
    LoggerHandler loggerHandler = LoggerHandler.create(LoggerFormat.CUSTOM);
    // 设置自定义的日志格式
    loggerHandler.customFormatter(new CustomerLog());
    final CorsHandler corsHandler = CorsHandler.create();
    mainRouter.route("/*")
        // 配置跨域处理
        .handler(corsHandler)
        // content-type和编码格式
        .handler(context -> {
          context.response().putHeader("content-type", "application/json; charset=utf-8");
          // 配置日志处理中间件
          if (configWebServer.isLogEnabled()) {
            loggerHandler.handle(context);
          } else {
            context.next();
          }
        });
    // 创建另一个路由器
    final Router router = Router.router(vertx);
    // 初始化路由器
    options.initRouter.init(router);
    // 将路由器添加到主路由器
    final String webServerPrefix = configWebServer.getPrefix();
    // 判断前缀是否为空,且是否以"/"开头,末尾以/*结尾
    if (StrUtil.isBlank(webServerPrefix) || !webServerPrefix.startsWith("/") || !webServerPrefix.endsWith("/*")) {
      StaticLog.error("webServerPrefix is error");
      throw new webserver.exception.WebServerStartException("webServerPrefix is error");
    }
    StaticLog.info("Web服务请求前缀:{}", webServerPrefix);
    mainRouter.route(webServerPrefix).subRouter(router);
    // 400异常处理
    mainRouter.errorHandler(400, context -> {
      StaticLog.error(context.failure(), "接口请求错误:{}", context.request().path());
      context.end(errorResponse(400, "接口请求错误,请联系管理员!"));
    });
    // 401 Unauthorized：当请求需要用户验证时返回。如果请求已包含验证凭据，则表示认证失败。
    mainRouter.errorHandler(401, context -> {
      StaticLog.error(context.failure(), "接口未授权:{}", context.request().path());
      context.end(errorResponse(401, "接口未授权,请联系管理员!"));
    });
    // 403 Forbidden：服务器已经理解请求，但是拒绝执行它
    mainRouter.errorHandler(403, context -> {
      StaticLog.error(context.failure(), "接口禁止访问:{}", context.request().path());
      context.end(errorResponse(403, "接口禁止访问,请联系管理员!"));
    });
    // 404异常处理
    mainRouter.errorHandler(404, context -> {
      StaticLog.error(context.failure(), "接口不存在:{}", context.request().path());
      context.end(errorResponse(404, "接口不存在,请联系管理员!"));
    });
    // 405异常处理
    mainRouter.errorHandler(405, context -> {
      StaticLog.error(context.failure(), "接口不允许:{}", context.request().path());
      context.end(errorResponse(405, "接口不允许,请联系管理员!"));
    });
    // 406 Not Acceptable：服务器无法根据客户端请求的内容特性完成请求。
    mainRouter.errorHandler(406, context -> {
      StaticLog.error(context.failure(), "接口不可接受:{}", context.request().path());
      context.end(errorResponse(406, "接口不可接受,请联系管理员!"));
    });
    // 413异常处理
    mainRouter.errorHandler(413, context -> {
      StaticLog.error(context.failure(), "接口请求过大:{}", context.request().path());
      context.end(errorResponse(413, "接口请求过大,请联系管理员!"));
    });
    // 415 Unsupported Media Type：请求的格式不受请求页面的支持。
    mainRouter.errorHandler(415, context -> {
      StaticLog.error(context.failure(), "不支持的媒体类型:{}", context.request().path());
      context.end(errorResponse(415, "不支持的媒体类型,请联系管理员!"));
    });
    // 429异常处理
    mainRouter.errorHandler(429, context -> {
      StaticLog.error(context.failure(), "接口请求过多:{}", context.request().path());
      context.end(errorResponse(429, "接口请求过多,请联系管理员!"));
    });
    // 500统一处理异常
    mainRouter.errorHandler(500, context -> {
      StaticLog.error(context.failure(), "接口异常:{}", context.request().path());
      context.end(errorResponse(500, "接口异常,请联系管理员!"));
    });
    // 502 Bad Gateway：服务器作为网关或代理，从上游服务器收到无效响应。
    mainRouter.errorHandler(502, context -> {
      StaticLog.error(context.failure(), "错误的网关:{}", context.request().path());
      context.end(errorResponse(502, "错误的网关,请联系管理员!"));
    });
    // 504超时异常处理
    mainRouter.errorHandler(504, context -> {
      StaticLog.error(context.failure(), "接口超时:{}", context.request().path());
      context.end(errorResponse(504, "接口超时,请联系管理员!"));
    });
    // 监听HTTP服务器
    try {
      await(httpServer.requestHandler(mainRouter).listen(port));
    } catch (Exception e) {
      StaticLog.error(e, "启动http服务器失败");
      throw new WebServerStartException("启动http服务器失败", e);
    }
    StaticLog.info("启动http服务器:{}:{}", host, port);
  }
}
