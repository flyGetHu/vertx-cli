package com.vertx.common.core.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.task.TaskOptions;
import com.vertx.common.core.enums.EnvEnum;

import java.util.function.BiConsumer;

import static com.vertx.common.core.config.VertxLoadConfig.active;
import static com.vertx.common.core.config.VertxLoadConfig.vertx;

public interface CronSchedulerHandler {

    /**
     * 定时任务描述
     */
    String getDescription();

    /**
     * 获取定时任务调度器
     */
    CronScheduler getScheduler();

    /**
     * 定时任务执行体
     */
    String task();

    default void start(TaskOptions taskOptions) {
        if (taskOptions == null) {
            taskOptions = new TaskOptions();
        }
        final EnvEnum startEnv = taskOptions.getStartEnv();
        if (startEnv != null) {
            if (!active.equals(startEnv.getValue())) {
                StaticLog.info("当前环境为" + active + ",不启动定时任务:" + getDescription());
                return;
            }
        }
        if (taskOptions.isInitStart()) {
            try {
                String result = task();
                taskOptions.getTaskCallback().accept(result, getDescription());
            } catch (Exception e) {
                StaticLog.error(e, "定时任务执行异常:" + getDescription());
                taskOptions.getTaskCallback().accept(ExceptionUtil.stacktraceToString(e), getDescription());
            }
            taskOptions.setInitStart(false);
            start(taskOptions);
            return;
        }
        long timeUntilNextExecution = getScheduler().getTimeUntilNextExecution();
        StaticLog.info("定时任务:" + getDescription() + " 下次执行时间:" + DateUtil.offsetMillisecond(DateUtil.date(), (int) timeUntilNextExecution));
        TaskOptions finalTaskOptions = taskOptions;
        vertx.setTimer(timeUntilNextExecution, id -> {
            final BiConsumer<String, String> taskCallback = finalTaskOptions.getTaskCallback();
            try {
                String result = task();
                if (taskCallback != null) {
                    taskCallback.accept(result, getDescription());
                }
            } catch (Exception e) {
                StaticLog.error(e, "定时任务执行异常:" + getDescription());
                try {
                    if (taskCallback != null) {
                        taskCallback.accept(ExceptionUtil.stacktraceToString(e), getDescription());
                    }
                } catch (Exception ex) {
                    StaticLog.error(ex, "定时任务回调异常:" + getDescription());
                }
            }
            start(finalTaskOptions);
        });
    }
}