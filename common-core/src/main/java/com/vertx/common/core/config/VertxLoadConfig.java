package com.vertx.common.core.config;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import cn.hutool.log.dialect.log4j2.Log4j2LogFactory;
import com.vertx.common.core.annotations.TableName;
import com.vertx.common.core.annotations.UniqueAddress;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.common.core.exception.UniqueAddressException;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.Future.await;

public class VertxLoadConfig {

    // 是否已经初始化
    public static boolean isInit = false;

    // 环境变量 此环境变量不允许改动
    public static String active = "";

    // 配置文件
    public static AppConfig appConfig;

    // 配置文件json对象 此对象保存配置文件中所有信息,若appConfig对象中没有,则从此对象中获取
    public static JsonObject appConfigJson;

    // vertx全局对象
    public static Vertx vertx;

    // 事件总线
    public static EventBus eventBus;

    /**
     * vertx 共享数据容器
     * synchronous maps (local-only)
     * asynchronous maps
     * asynchronous locks
     * asynchronous counters
     */
    public static SharedData sharedData;

    /**
     * 初始化方法
     * 获取配置文件方式:
     * 1. 优先读取外部配置文件,文件位置为项目根目录下的config文件夹下
     * 2. 若外部配置文件不存在,则读取内部配置文件
     *
     * @param active 活跃配置名
     */
    public void init(String active) {
        localClassFilter();
        vertx = Vertx.currentContext().owner();
        eventBus = vertx.eventBus();
        sharedData = vertx.sharedData();
        StaticLog.info("项目是否为集群环境:{}", vertx.isClustered());

        // 创建日志工厂对象
        final LogFactory logFactory = Log4j2LogFactory.create();
        LogFactory.setCurrentLogFactory(logFactory);
        StaticLog.info("初始化日志对象成功:{}", logFactory.getName());
        // 获取当前项目的绝对路径
        final String path = System.getProperty("user.dir");
        // 构建配置文件名
        String activeConfigName = path + File.separator + "config.";
        String env = active;
        if (StrUtil.isNotBlank(VertxLoadConfig.active)) {
            env = VertxLoadConfig.active;
        }
        // 判断环境变量是否为空,为空则默认为dev
        if (StrUtil.isBlank(env)) {
            env = "dev";
        }
        // 拼接配置文件名
        activeConfigName += env + ".yaml";

        // 判断项目外部根目录是否存在
        if (!await(vertx.fileSystem().exists(activeConfigName))) {
            StaticLog.warn("项目外部配置文件不存在:{},尝试读取内部配置文件", activeConfigName);
            // 判断项目内部配置文件是否存在
            activeConfigName = "conf/config." + env + ".yaml";
            if (!await(vertx.fileSystem().exists(activeConfigName))) {
                StaticLog.error("项目内部配置文件不存在:{}", activeConfigName);
                throw new RuntimeException("项目内部配置文件不存在:" + activeConfigName);
            }
        }

        // 将环境变量设置为当前活跃配置
        VertxLoadConfig.active = env;
        StaticLog.info("加载配置文件:{}", activeConfigName);

        // 创建配置检索器并获取配置文件
        final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions();
        configRetrieverOptions.addStore(
                new ConfigStoreOptions()
                        .setType("file")
                        .setFormat("yaml")
                        .setConfig(new JsonObject().put("path", activeConfigName)));
        final JsonObject params = await(ConfigRetriever.create(vertx, configRetrieverOptions).getConfig());
        StaticLog.info("加载配置文件成功:{}", params.encodePrettily());

        // 将配置文件解析为Json对象和AppConfig对象
        appConfigJson = params;
        appConfig = params.mapTo(AppConfig.class);
        isInit = true;
    }

    private void localClassFilter() {
        final Set<Class<?>> classes = ClassUtil.scanPackage();
        // 用于存储事件总线的唯一地址
        final Set<String> eventBusUniqueAddress = new HashSet<>();
        for (Class<?> aClass : classes) {
            //判断类是否有指定注解,
            if (aClass.isAnnotationPresent(UniqueAddress.class)) {
                //获取注解对象
                UniqueAddress uniqueAddress = aClass.getAnnotation(UniqueAddress.class);
                //获取注解的值
                String uniqueAddressVal = uniqueAddress.value();
                if (eventBusUniqueAddress.contains(uniqueAddressVal)) {
                    // 打印警告信息
                    StaticLog.warn("注意EventBus接口和RabbitMq接口:");
                    StaticLog.warn("接口定义类命名不带impl,并且类上必须添加UniqueAddress注解,标明地址路径");
                    StaticLog.warn("项目启动会检查是否有重复的地址或者地址缺失");
                    StaticLog.warn("最终实现消费或者服务提供方命名必须添加impl,检查会过滤末尾Impl文件");
                    // 抛出唯一地址异常，提示类名和重复的地址
                    StaticLog.error(aClass.getName() + "\n地址重复:" + uniqueAddress);
                    throw new UniqueAddressException(aClass.getName() + "\n地址重复:" + uniqueAddress);
                }
                // 将唯一地址添加到事件总书唯一地址集合中
                eventBusUniqueAddress.add(uniqueAddressVal);
            }
            if (aClass.isAnnotationPresent(TableName.class)) {
                // 检查数据库模型类是否有id字段
                String idField = "id";
                final Field field = ClassUtil.getDeclaredField(aClass, idField);
                if (field == null) {
                    StaticLog.warn("警告: 数据库模型类" + aClass.getName() + "没有名为\"" + idField + "\"的字段,请检查是否有误");
                }
            }
        }
    }
}
