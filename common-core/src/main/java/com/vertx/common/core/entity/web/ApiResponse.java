package com.vertx.common.core.entity.web;

import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vertx.common.core.enums.ApiResponseStatusEnum;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    @JsonProperty("status")
    private ApiResponseStatusEnum status = ApiResponseStatusEnum.OK;
    @JsonProperty("code")
    private int code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("data")
    private Object data;
    @JsonProperty("extra")
    private Object extra;

    public ApiResponse(ApiResponseStatusEnum status, int code, String msg, Object data, Object extra) {
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.extra = extra;
    }

    // Getters and Setters for all properties...

    public static Buffer successResponse(Object data) {
        return Json.encodeToBuffer(new ApiResponse(ApiResponseStatusEnum.OK, HttpStatus.HTTP_OK, "success", data, null));
    }

    public static Buffer successResponse(Object data, Object extra) {
        return Json.encodeToBuffer(new ApiResponse(ApiResponseStatusEnum.OK, HttpStatus.HTTP_OK, "success", data, extra));
    }

    public static Buffer errorResponse(String message) {
        return Json.encodeToBuffer(new ApiResponse(ApiResponseStatusEnum.ERROR, HttpStatus.HTTP_INTERNAL_ERROR, message, null, null));
    }

    public static Buffer errorResponse(int code, String message) {
        return Json.encodeToBuffer(new ApiResponse(ApiResponseStatusEnum.ERROR, code, message, null, null));
    }

    public static Buffer errorResponse(String message, int code, Object data, Object extra) {
        return Json.encodeToBuffer(new ApiResponse(ApiResponseStatusEnum.ERROR, code, message, data, extra));
    }

    @Override
    public String toString() {
        // Assuming you're using Jackson for JSON serialization
        return Json.encode(this);
    }
}