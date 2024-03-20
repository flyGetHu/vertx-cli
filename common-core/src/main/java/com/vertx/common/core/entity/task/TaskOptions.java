package com.vertx.common.core.entity.task;


import com.vertx.common.core.enums.EnvEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiConsumer;

/**
 * Task configuration class
 */
@Setter
@Getter
public class TaskOptions {

    // Getters and Setters
    /**
     * Whether to initialize the task on startup
     */
    private boolean initStart = false;

    /**
     * Startup environment. If not null, the scheduled task will only start when active is startEnv
     */
    private EnvEnum startEnv = null;

    /**
     * Define the callback function when the task is completed
     * The first parameter: error message
     * The second parameter: task description
     */
    private BiConsumer<String, String> taskCallback = null;

}