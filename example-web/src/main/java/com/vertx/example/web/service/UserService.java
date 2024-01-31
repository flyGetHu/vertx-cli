package com.vertx.example.web.service;

import cn.hutool.core.lang.Singleton;
import cn.hutool.log.StaticLog;
import com.vertx.example.web.mapper.UserMapper;
import com.vertx.example.web.model.User;

import java.util.List;

public class UserService {
    private final UserMapper userMapper = Singleton.get(UserMapper.class);

    public List<User> selectAll() {
        final User user = userMapper.selectById(1);
        StaticLog.info("user:{}", user);
        return userMapper.selectAll();
    }
}
