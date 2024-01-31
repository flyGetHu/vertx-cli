package com.vertx.example.web.entity.request;

import com.vertx.common.core.entity.db.QueryPageParam;
import com.vertx.example.web.entity.request.condition.UserPageCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageRequest extends QueryPageParam<UserPageCondition> {
}
