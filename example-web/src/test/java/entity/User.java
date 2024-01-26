package entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vertx.common.core.annotations.TableName;
import lombok.Data;

@Data
@TableName(name = "user")
public class User {
    @JsonProperty("id")
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("age")
    private Integer age;
    @JsonProperty("create_time")
    private String createTime;
    @JsonProperty("update_time")
    private String updateTime;
}
