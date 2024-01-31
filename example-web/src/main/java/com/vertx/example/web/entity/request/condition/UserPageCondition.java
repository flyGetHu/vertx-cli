package com.vertx.example.web.entity.request.condition;

import lombok.Data;

@Data
public class UserPageCondition {

    /**
     * 查询用户的名字。这是一个模糊查询，即，查询以这个名字开头的用户。
     */
    private String likeRightName;
}
