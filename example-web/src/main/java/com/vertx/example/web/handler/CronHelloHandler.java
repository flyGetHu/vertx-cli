package com.vertx.example.web.handler;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.task.CronScheduler;
import com.vertx.common.core.task.CronSchedulerHandler;

public class CronHelloHandler implements CronSchedulerHandler {
    @Override
    public String getDescription() {
        return "测试定时任务";
    }

    @Override
    public CronScheduler getScheduler() {
        return new CronScheduler("0/5 * * * * ?");
    }

    @Override
    public String task() {
        StaticLog.info("测试定时任务");
        return null;
    }
}
