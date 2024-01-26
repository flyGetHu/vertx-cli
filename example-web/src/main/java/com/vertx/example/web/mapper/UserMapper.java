package com.vertx.example.web.mapper;

import java.util.List;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import com.vertx.example.web.model.User;
import com.vertx.mysql.helper.MysqlHelper;

public class UserMapper {

  public List<User> selectAll() {
    final Condition noCondition = DSL.noCondition();
    List<User> users = MysqlHelper.select(User.class, noCondition);
    return users;
  }
}
