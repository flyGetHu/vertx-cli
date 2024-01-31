package webserver.helper;

import cn.hutool.http.HttpStatus;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.common.core.utils.StrUtil;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import webserver.entity.WebServiceOptions;

import static com.vertx.common.core.config.VertxLoadConfig.*;
import static com.vertx.common.core.entity.web.ApiResponse.errorResponse;

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
        httpServerOptions.setHost(configWebServer.getHost());
        httpServerOptions.setPort(configWebServer.getPort());
        // 设置服务器的超时时间
        httpServerOptions.setIdleTimeout(configWebServer.getTimeout());
        // 设置服务器的ALPN版本
        httpServerOptions.setAlpnVersions(configWebServer.getAlpnVersions());
        // 设置注册WebSocket写处理程序
        httpServerOptions.setRegisterWebSocketWriteHandlers(true);
        // 设置是否支持压缩
        httpServerOptions.setCompressionSupported(configWebServer.isCompressionSupported());
        // 设置压缩级别
        httpServerOptions.setCompressionLevel(configWebServer.getCompressionLevel());
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
        // 超时异常处理
        mainRouter.errorHandler(HttpStatus.HTTP_UNAVAILABLE, context -> {
            StaticLog.error(context.failure(), "接口超时:{}", context.request().path());
            context.end(errorResponse(HttpStatus.HTTP_INTERNAL_ERROR, "接口超时,请联系管理员!"));
        });
        // 404异常处理
        mainRouter.errorHandler(HttpStatus.HTTP_NOT_FOUND, context -> {
            StaticLog.error(context.failure(), "接口不存在:{}", context.request().path());
            context.end(errorResponse(HttpStatus.HTTP_NOT_FOUND, "接口不存在,请联系管理员!"));
        });
        // 405异常处理
        mainRouter.errorHandler(HttpStatus.HTTP_BAD_METHOD, context -> {
            StaticLog.error(context.failure(), "接口不支持:{}", context.request().path());
            context.end(errorResponse(HttpStatus.HTTP_BAD_METHOD, "接口不支持,请联系管理员!"));
        });
        // 统一处理异常
        mainRouter.errorHandler(HttpStatus.HTTP_INTERNAL_ERROR, context -> {
            StaticLog.error(context.failure(), "接口异常:{}", context.request().path());
            context.end(errorResponse(HttpStatus.HTTP_INTERNAL_ERROR, "接口异常,请联系管理员!"));
        });
        // 监听HTTP服务器
        httpServer.requestHandler(mainRouter).listen(configWebServer.getPort()).onComplete(res -> {
            if (res.succeeded()) {
                StaticLog.info("start http server:{}:{}", configWebServer.getHost(), configWebServer.getPort());
            } else {
                StaticLog.error(res.cause(), "http server start error");
                throw new webserver.exception.WebServerStartException(res.cause());
            }
        });
    }
}
