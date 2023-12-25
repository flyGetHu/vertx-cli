package com.vertx.common.core.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import cn.hutool.log.dialect.log4j2.Log4j2LogFactory;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.common.core.exception.UniqueAddressException;
import io.github.classgraph.*;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

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
     *
     * @param active 活跃配置名
     */
    public void init(String active) {
        checkUniqueAddress();
        vertx = Vertx.currentContext().owner();
        eventBus = vertx.eventBus();
        sharedData = vertx.sharedData();
        StaticLog.info("项目是否为集群环境:{}", vertx.isClustered());

        // 创建日志工厂对象
        final LogFactory logFactory = Log4j2LogFactory.create();
        LogFactory.setCurrentLogFactory(logFactory);
        StaticLog.info("初始化日志对象成功:{}", logFactory.getName());

        // 构建配置文件名
        String activeConfigName = "conf/config.";
        String env = active;
        if (StrUtil.isNotBlank(VertxLoadConfig.active)) {
            env = VertxLoadConfig.active;
        }
        if (StrUtil.isBlank(env)) {
            env = "dev";
        }
        activeConfigName += env + ".yaml";

        // 判断配置文件是否存在
        if (!await(vertx.fileSystem().exists(activeConfigName))) {
            StaticLog.warn("配置文件不存在:{}", activeConfigName);
            return;
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
                        .setConfig(new JsonObject().put("path", activeConfigName))
        );
        final JsonObject params = await(ConfigRetriever.create(vertx, configRetrieverOptions).getConfig());
        StaticLog.info("加载配置文件成功:{}", params.encodePrettily());

        // 将配置文件解析为Json对象和AppConfig对象
        appConfigJson = params;
        appConfig = params.mapTo(AppConfig.class);
        isInit = true;
    }


    // 检查唯一地址
    private void checkUniqueAddress() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo()
                .enableStaticFinalFieldConstantInitializerValues()
                .scan()) {
            // 检查事件总线接口是否有地址重复
            scanAndValidateHandlers(scanResult, "com.vertx.eventbus.handler.BusHandler");

            // 检查rabbitmq接口是否有地址重复
            scanAndValidateHandlers(scanResult, "com.vertx.rabbitmq.handler.RabbitMqHandler");

            // 检查数据库模型类是否有id字段
            ClassInfoList classesWithAnnotation = scanResult.getClassesWithAnnotation("com.vertx.common.annotations.TableName");
            classesWithAnnotation.forEach(classInfo -> {
                String idField = "id";
                Stream<String> fieldNames = scanResult.getClassInfo(classInfo.getName()).getFieldInfo().stream().map(FieldInfo::getName);
                if (fieldNames.noneMatch(fieldName -> fieldName.equals(idField))) {
                    System.out.println("警告: 数据库模型类" + classInfo.getName() + "没有名为\"" + idField + "\"的字段,请检查是否有误");
                }
            });
        } catch (Exception | UniqueAddressException e) {
            StaticLog.error(e, "扫描地址唯一性失败");
        }
    }


    // 扫描实现指定包名的类
    private void scanAndValidateHandlers(ScanResult scanResult, String packageName) throws UniqueAddressException {
        // 获取实现指定接口的类信息列表
        ClassInfoList busHandlerClasses = scanResult.getClassesImplementing(packageName);
        // 用于存储事件总线的唯一地址
        Set<String> eventBusUniqueAddress = new HashSet<>();

        // 遍历实现指定接口的类信息列表
        for (ClassInfo classInfo : busHandlerClasses) {
            // 获取类名
            String className = classInfo.getName();
            // 类名不以"Impl"结尾则继续遍历
            if (className.endsWith("Impl")) {
                continue;
            }

            // 获取类上的注解信息
            AnnotationInfoList annotationInfos = classInfo.getAnnotationInfo();
            // 标记是否具有唯一地址注解
            boolean hasUniqueAddress = false;
            // 遍历类上的注解信息
            for (AnnotationInfo annotationInfo : annotationInfos) {
                // 如果注解名为"com.vertx.common.annotations.UniqueAddress"
                if (annotationInfo.getName().equals("com.vertx.common.annotations.UniqueAddress")) {
                    // 获取唯一地址
                    String uniqueAddress = (String) annotationInfo.getParameterValues().get(0).getValue();
                    // 如果事件总书存在重复地址
                    if (eventBusUniqueAddress.contains(uniqueAddress)) {
                        // 打印警告信息
                        StaticLog.warn("注意EventBus接口和RabbitMq接口:");
                        StaticLog.warn("接口定义类命名不带impl,并且类上必须添加UniqueAddress注解,标明地址路径");
                        StaticLog.warn("项目启动会检查是否有重复的地址或者地址缺失");
                        StaticLog.warn("最终实现消费或者服务提供方命名必须添加impl,检查会过滤末尾Impl文件");
                        // 抛出唯一地址异常，提示类名和重复的地址
                        throw new UniqueAddressException(className + "\n地址重复:" + uniqueAddress);
                    }
                    // 将唯一地址添加到事件总书唯一地址集合中
                    eventBusUniqueAddress.add(uniqueAddress);
                    // 标记具有唯一地址注解
                    hasUniqueAddress = true;
                }
            }
            // 如果类上没有唯一地址注解
            if (!hasUniqueAddress) {
                // 打印警告信息
                StaticLog.warn("注意EventBus接口和RabbitMq接口:");
                StaticLog.warn("接口定义类命名不带impl,并且类上必须添加UniqueAddress注解,标明地址路径");
                StaticLog.warn("项目启动会检查是否有重复的地址或者地址缺失");
                StaticLog.warn("最终实现消费或者服务提供方命名必须添加impl,检查会过滤末尾Impl文件");
                // 抛出唯一地址异常，提示类名
                throw new UniqueAddressException(className + "\n事件总线地址未设置");
            }
        }
        // 清空事件总线唯一地址集合
        eventBusUniqueAddress.clear();
    }

}
