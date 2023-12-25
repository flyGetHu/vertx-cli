package webserver.helper;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerFormatter;

public class CustomerLog implements LoggerFormatter {
    @Override
    public String format(RoutingContext routingContext, long ms) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        int statusCode = response.getStatusCode();
        String remoteAddress = request.remoteAddress().host();
        String version = request.version().name();
        String method = request.method().name();
        String path = request.path();
        long bytesWritten = response.bytesWritten();
        String userAgent = request.getHeader("user-agent");

        return String.format(
                "{\"remoteAddress\":\"%s\",\"version\":\"%s\",\"method\":\"%s\",\"path\":\"%s\",\"statusCode\":%d,\"bytesWritten\":%d,\"userAgent\":\"%s\",\"ms\":%d}",
                remoteAddress, version, method, path, statusCode, bytesWritten, userAgent, ms);
    }
}
