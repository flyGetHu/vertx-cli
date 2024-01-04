package com.vertx.mysql.utils;

import cn.hutool.core.util.StrUtil;
import com.vertx.common.core.annotations.TableName;

import java.util.Optional;

/**
 * 表名工具类
 */
public class TableUtil {


    /**
     * 获取表名
     *
     * @param c 实体类
     * @return 返回表名
     */
    public static String getTableName(Class<?> c) {
        return Optional.ofNullable(c.getAnnotation(TableName.class))
                .map(TableName::name)
                .filter(name -> !StrUtil.isBlank(name))
                .orElseGet(c::getSimpleName);
    }

}
