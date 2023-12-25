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

    public void init(String active) {
        checkUniqueAddress();
        vertx = Vertx.currentContext().owner();
        eventBus = vertx.eventBus();
        sharedData = vertx.sharedData();
        StaticLog.info("项目是否为集群环境:{}", vertx.isClustered());
        final LogFactory logFactory = Log4j2LogFactory.create();
        LogFactory.setCurrentLogFactory(logFactory);
        StaticLog.info("初始化日志对象成功:{}", logFactory.getName());
        String activeConfigName = "conf/config.";
        String env = active;
        if (StrUtil.isNotBlank(VertxLoadConfig.active)) {
            env = VertxLoadConfig.active;
        }
        if (StrUtil.isBlank(env)){
            env = "dev";
        }
        activeConfigName += env + ".yaml";
        if (!await(vertx.fileSystem().exists(activeConfigName))) {
            StaticLog.warn("配置文件不存在:{}", activeConfigName);
            return;
        }
        VertxLoadConfig.active = env;
        StaticLog.info("加载配置文件:{}", activeConfigName);
        final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions();
        configRetrieverOptions.addStore(
                new ConfigStoreOptions()
                        .setType("file")
                        .setFormat("yaml")
                        .setConfig(new JsonObject().put("path", activeConfigName))
        );
        final JsonObject params = await(ConfigRetriever.create(vertx, configRetrieverOptions).getConfig());
        StaticLog.info("加载配置文件成功:{}", params.encodePrettily());
        appConfigJson = params;
        appConfig = params.mapTo(AppConfig.class);
        isInit = true;
    }

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

    private void scanAndValidateHandlers(ScanResult scanResult, String packageName) throws UniqueAddressException {
        ClassInfoList busHandlerClasses = scanResult.getClassesImplementing(packageName);
        Set<String> eventBusUniqueAddress = new HashSet<>();

        for (ClassInfo classInfo : busHandlerClasses) {
            String className = classInfo.getName();
            if (className.endsWith("Impl")) {
                continue;
            }

            AnnotationInfoList annotationInfos = classInfo.getAnnotationInfo();
            boolean hasUniqueAddress = false;
            for (AnnotationInfo annotationInfo : annotationInfos) {
                if (annotationInfo.getName().equals("com.vertx.common.annotations.UniqueAddress")) {
                    String uniqueAddress = (String) annotationInfo.getParameterValues().get(0).getValue();
                    if (eventBusUniqueAddress.contains(uniqueAddress)) {
                        // Print warning messages
                        StaticLog.warn("注意EventBus接口和RabbitMq接口:");
                        StaticLog.warn("接口定义类命名不带impl,并且类上必须添加UniqueAddress注解,标明地址路径");
                        StaticLog.warn("项目启动会检查是否有重复的地址或者地址缺失");
                        StaticLog.warn("最终实现消费或者服务提供方命名必须添加impl,检查会过滤末尾Impl文件");
                        throw new UniqueAddressException(className + "\n地址重复:" + uniqueAddress);
                    }
                    eventBusUniqueAddress.add(uniqueAddress);
                    hasUniqueAddress = true;
                }
            }
            if (!hasUniqueAddress) {
                // Print warning messages
                StaticLog.warn("注意EventBus接口和RabbitMq接口:");
                StaticLog.warn("接口定义类命名不带impl,并且类上必须添加UniqueAddress注解,标明地址路径");
                StaticLog.warn("项目启动会检查是否有重复的地址或者地址缺失");
                StaticLog.warn("最终实现消费或者服务提供方命名必须添加impl,检查会过滤末尾Impl文件");
                throw new UniqueAddressException(className + "\n事件总线地址未设置");
            }
        }
        eventBusUniqueAddress.clear();
    }
}
