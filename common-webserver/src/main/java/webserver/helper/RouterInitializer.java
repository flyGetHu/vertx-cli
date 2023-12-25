package webserver.helper;

import io.vertx.ext.web.Router;

public interface RouterInitializer {
    void init(Router router);
}