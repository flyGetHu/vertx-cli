package com.vertx.redis.helper;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class RedisHelperTest {
  // @BeforeEach
  // public void deployVerticle(Vertx vertx, VertxTestContext testContext) {
  // final VertxLoadConfig vertxLoadConfig = new VertxLoadConfig();
  // vertxLoadConfig.init("");
  // RedisClient.init(VertxLoadConfig.appConfig.getDatabase().getRedis(), true);
  // testContext.completeNow();
  // }

  /**
   * 测试Redis的hgetall命令
   *
   * @param testContext Vertx测试上下文
   * @throws InterruptedException 线程中断异常
   */
  @Test
  void testHashGetAll(VertxTestContext testContext) throws InterruptedException {
    final RedisOptions options = new RedisOptions();
    options.setConnectionString("redis://192.168.2.234:6379/1");
    options.setPassword("anjun2020");
    Redis.createClient(Vertx.vertx(), options).connect()
        .onSuccess(redisConnection -> {
          final RedisAPI api = RedisAPI.api(redisConnection);
          api.hgetall("track:emmis_order").onSuccess(res -> {
            System.out.println(res.toString());
            testContext.completeNow();
          });
        });
    testContext.awaitCompletion(10000, TimeUnit.SECONDS);
  }

  private static final String API_KEY = "YOUR_API_KEY";
  private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

  @Test
  void testConnect() {

  }

  /**
   * 获取起点和终点之间的路线信息。
   *
   * @param origin 起点地址
   * @param destination 终点地址
   * @return 返回路线信息，格式为JSON字符串
   * @throws Exception 如果出现异常，则抛出异常
   */
  private static String getDirections(String origin, String destination) throws Exception {
    String requestUrl = DIRECTIONS_API_URL + "?origin=" + origin + "&destination=" + destination + "&key=" + API_KEY;
    URI uri = new URI(requestUrl);
    URL url = uri.toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String output;
    StringBuilder jsonResponse = new StringBuilder();
    while ((output = br.readLine()) != null) {
      jsonResponse.append(output);
    }
    return jsonResponse.toString();
  }
}