package webserver.helper;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerFormatter;

public class CustomerLog implements LoggerFormatter {
  @Override
  public String format(RoutingContext routingContext, long ms) {
    // 获取请求和响应对象
    HttpServerRequest request = routingContext.request();
    HttpServerResponse response = routingContext.response();

    // 获取响应状态码
    int statusCode = response.getStatusCode();

    // 获取远程地址、请求版本、请求方法和请求路径
    String remoteAddress = request.remoteAddress().host();
    String version = request.version().name();
    String method = request.method().name();
    String path = request.path();

    // 获取已写入的响应字节数
    long bytesWritten = response.bytesWritten();

    // 获取用户代理信息
    String userAgent = request.getHeader("user-agent");

    // 格式化返回数据
    return String.format(
        "{\"remoteAddress\":\"%s\",\"version\":\"%s\",\"method\":\"%s\",\"path\":\"%s\",\"statusCode\":%d,\"bytesWritten\":%d,\"userAgent\":\"%s\",\"ms\":%d}",
        remoteAddress, version, method, path, statusCode, bytesWritten, userAgent, ms);
  }

}
