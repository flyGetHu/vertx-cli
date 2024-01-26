package com.vertx.example.web.service;

import java.util.List;

import com.vertx.example.web.mapper.UserMapper;
import com.vertx.example.web.model.User;

import cn.hutool.core.lang.Singleton;

public class UserService {
  private final UserMapper userMapper = Singleton.get(UserMapper.class);

  public List<User> selectAll() {
    return userMapper.selectAll();
  }
}
