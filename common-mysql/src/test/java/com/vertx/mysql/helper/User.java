package com.vertx.mysql.helper;

import com.vertx.common.core.annotations.TableName;
import lombok.Data;

@Data
@TableName(name = "user")
public class User {
    private String name;
    private String age;
}