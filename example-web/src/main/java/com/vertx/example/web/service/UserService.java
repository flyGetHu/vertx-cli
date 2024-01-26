package com.vertx.example.web.service;

import cn.hutool.core.lang.Singleton;
import com.vertx.example.web.mapper.UserMapper;
import com.vertx.example.web.model.User;

import java.util.List;

public class UserService {
    private final UserMapper userMapper = Singleton.get(UserMapper.class);

    public List<User> selectAll() {
        return userMapper.selectAll();
    }
}
