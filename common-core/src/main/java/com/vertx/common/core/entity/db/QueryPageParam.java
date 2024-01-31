package com.vertx.common.core.entity.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 这个类代表了分页查询的参数。
 * 它是一个通用类，可以与任何类型的条件一起使用。
 *
 * @param <T> 条件的类型
 */
@Data
public class QueryPageParam<T> {

    /**
     * 查询的页码。这是一个基于1的索引，即，第一页是1。
     */
    @JsonProperty("page")
    private Integer page;

    /**
     * 页面的大小，即，返回的最大结果数。
     */
    @JsonProperty("size")
    private Integer size;

    /**
     * 对结果进行排序的字段。
     */
    @JsonProperty("sort")
    private String sort;

    /**
     * 排序的顺序。这应该是"asc"表示升序或"desc"表示降序。
     */
    @JsonProperty("order")
    private String order;

    /**
     * 查询的条件。这可以是任何类型，取决于具体的使用场景。
     */
    @JsonProperty("condition")
    private T condition;
}