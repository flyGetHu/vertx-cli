package com.vertx.example.web.service;

import cn.hutool.core.lang.Singleton;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.db.QueryPageParam;
import com.vertx.common.core.entity.db.QueryPageResponse;
import com.vertx.eventbus.bus.DemoBus;
import com.vertx.example.web.entity.request.condition.UserPageCondition;
import com.vertx.example.web.mapper.UserMapper;
import com.vertx.example.web.model.User;

import java.util.List;

public class UserService {
    private final UserMapper userMapper = Singleton.get(UserMapper.class);
    public final DemoBus demoBus = Singleton.get(DemoBus.class);

    public List<User> selectAll() {
        final User user = userMapper.selectById(1);
        StaticLog.debug("user:{}", user);
        final String res = demoBus.call(user.getName());
        StaticLog.debug("res:{}", res);
        return userMapper.selectAll();
    }

    public QueryPageResponse<User> selectPage(QueryPageParam<UserPageCondition> param) {
        return userMapper.selectPage(param);
    }
}
