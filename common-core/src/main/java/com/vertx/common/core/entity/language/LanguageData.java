package com.vertx.common.core.entity.language;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LanguageData {
    @JsonProperty("name")
    private String name;
    @JsonProperty("zh")
    private String zh;
    @JsonProperty("en")
    private String en;
    @JsonProperty("pu")
    private String pu;
}