package com.vertx.mysql.helper;

import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MysqlHelperTest {

    @Test
    public void install() {
        Assertions.assertNotNull(MysqlHelper.install(null));
        Assertions.assertNotNull(MysqlHelper.install(""));
        Assertions.assertNotNull(MysqlHelper.install(" "));
        Assertions.assertNotNull(MysqlHelper.install("  "));
        User user = new User();
        user.setName("name");
        user.setAge("age");
        Assertions.assertNotNull(MysqlHelper.install(user));
    }

    @Test
    public void installBatch() {
        MysqlHelper.insertBatch(null);
        User user = new User();
        user.setName("name");
        user.setAge("age");
        MysqlHelper.insertBatch(List.of(user));
        MysqlHelper.insertBatch(List.of());
        MysqlHelper.insertBatch(List.of(null));
        MysqlHelper.insertBatch(List.of(null, user));
        MysqlHelper.insertBatch(List.of(user, null));
        MysqlHelper.insertBatch(List.of(user, null, user));
    }

    @Test
    public void update() {
        MysqlHelper.update(null, null);
        User user = new User();
        user.setName("name");
        user.setAge(null);
        Assertions.assertNotNull(MysqlHelper.update(user, DSL.and(DSL.field("id").eq(1)), true));
    }

    @Test
    public void delete() {
        User user = new User();
        user.setName("name");
        user.setAge("age");
        Assertions.assertNotNull(MysqlHelper.delete(User.class, null));
        Assertions.assertNotNull(MysqlHelper.delete(User.class, DSL.and(DSL.field("id").eq(1))));
    }

    @Test
    public void select() {
        User user = new User();
        user.setName("name");
        user.setAge("age");
        // Assertions.assertNotNull(MysqlHelper.select(User.class, null));
        // Assertions.assertNotNull(MysqlHelper.select(User.class,
        // DSL.and(DSL.field("id").eq(1))));
        Assertions.assertNotNull(MysqlHelper.select(User.class, DSL.and(DSL.field("id").eq(1)), List.of(
                "age"), "limit 1"));
    }
}
