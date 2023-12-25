package com.vertx.webclient.client;

import com.vertx.common.core.config.VertxLoadConfig;
import com.vertx.common.core.entity.app.AppConfig;
import io.vertx.ext.web.client.WebClientOptions;

public class WebClient {
    public static io.vertx.ext.web.client.WebClient webclient;


    /**
     * 初始化WebClient并返回
     * @param config WebClient的配置信息
     * @param isDefault 是否为默认的WebClient实例
     * @return 初始化后的WebClient实例
     */
    public static io.vertx.ext.web.client.WebClient init(AppConfig.WebClient config, Boolean isDefault) {
        final WebClientOptions options = new WebClientOptions();
        options.setKeepAlive(true);
        options.setConnectTimeout(config.getConnectTimeout());
        options.setReadIdleTimeout(config.getReadIdleTimeout());
        options.setWriteIdleTimeout(config.getWriteIdleTimeout());
        options.setIdleTimeout(config.getIdleTimeout());
        options.setTrustAll(true);
        options.setMaxPoolSize(config.getMaxPoolSize());
        options.setUserAgent("vertx-web-client");
        final io.vertx.ext.web.client.WebClient client = io.vertx.ext.web.client.WebClient.create(VertxLoadConfig.vertx, options);
        if(isDefault){
            webclient= client;
        }
        return webclient;
    }

}
