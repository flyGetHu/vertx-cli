package com.vertx.example.web.mapper;

import com.vertx.example.web.model.User;
import com.vertx.mysql.helper.MysqlHelper;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import java.util.List;

public class UserMapper {

    public List<User> selectAll() {
        final Condition noCondition = DSL.noCondition();
        List<User> users = MysqlHelper.select(User.class, noCondition, List.of("name", "age"), "limit 10");
        return users;
    }

    public User selectById(int id) {
        final Condition condition = DSL.field("id").eq(id);
        final List<User> users = MysqlHelper.select(User.class, condition);
        if (users.isEmpty()) {
            return null;
        }
        return users.getFirst();
    }
}
