package com.vertx.common.core.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.StaticLog;
import org.apache.logging.log4j.core.util.CronExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

public class CronScheduler {

    private final CronExpression cronExpression;
    private LocalDateTime nextExecutionTime;

    public CronScheduler(String cronExpressionString) {
        try {
            this.cronExpression = new CronExpression(cronExpressionString);
            StaticLog.info("cron表达式" + cronExpressionString + "初始化成功");
        } catch (Throwable e) {
            StaticLog.error(e, "cron表达式格式错误:{}", cronExpressionString);
            throw new RuntimeException(e);
        }
        calculateNextExecutionTime();
    }

    public long getTimeUntilNextExecution() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(nextExecutionTime)) {
            calculateNextExecutionTime();
        }
        Duration duration = Duration.between(now, nextExecutionTime);
        if (duration.toMillis() < 1) {
            StaticLog.warn("cron表达式{}下次执行时间小于1ms,重新计算", cronExpression.getCronExpression());
            return getTimeUntilNextExecution();
        }
        return duration.toMillis();
    }

    private void calculateNextExecutionTime() {
        Date now = new Date();
        Date date = cronExpression.getNextValidTimeAfter(now);
        nextExecutionTime = DateUtil.toLocalDateTime(date);
    }
}