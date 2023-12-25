package webserver.helper;

import cn.hutool.http.HttpStatus;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.app.AppConfig;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import webserver.entity.WebServiceOptions;

import static com.vertx.common.core.config.VertxLoadConfig.*;
import static com.vertx.common.core.entity.web.ApiResponse.errorResponse;

public class VertxWebHelper {
    public void startHttpServer(WebServiceOptions options) {
        if (!isInit) {
            StaticLog.error("VertxLoadConfig is not init");
            throw new webserver.exception.WebServerStartException("VertxLoadConfig is not init");
        }
        final HttpServerOptions httpServerOptions = new HttpServerOptions();
        final AppConfig.WebServer configWebServer = appConfig.getWebServer();
        if (configWebServer == null) {
            StaticLog.error("webServer is null");
            throw new webserver.exception.WebServerStartException("webServer is null");
        }
        httpServerOptions.setHost(configWebServer.getHost());
        httpServerOptions.setPort(configWebServer.getPort());
        httpServerOptions.setIdleTimeout(configWebServer.getTimeout());
        httpServerOptions.setAlpnVersions(configWebServer.getAlpnVersions());
        httpServerOptions.setRegisterWebSocketWriteHandlers(true);
        httpServerOptions.setCompressionSupported(configWebServer.isCompressionSupported());
        httpServerOptions.setCompressionLevel(configWebServer.getCompressionLevel());
        final HttpServer httpServer = vertx.createHttpServer(httpServerOptions);
        final Router mainRouter = Router.router(vertx);
        LoggerHandler loggerHandler = LoggerHandler.create(LoggerFormat.CUSTOM);
        loggerHandler.customFormatter(new CustomerLog());
        mainRouter.route("/*").handler(io.vertx.ext.web.handler.CorsHandler.create())
                .handler(context -> {
                    if (configWebServer.isLogEnabled()) {
                        loggerHandler.handle(context);
                    } else {
                        context.next();
                    }
                });
        final Router router = Router.router(vertx);
        options.initRouter.init(router);
        mainRouter.route(configWebServer.getPrefix()).subRouter(router);

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
