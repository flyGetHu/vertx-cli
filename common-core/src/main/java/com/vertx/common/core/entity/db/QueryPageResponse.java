package com.vertx.common.core.entity.db;

import lombok.Data;

import java.util.List;

@Data
public class QueryPageResponse<T> {
    private Integer page;
    private Integer size;
    private Integer total;
    private List<T> data;
}
