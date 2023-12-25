package webserver.exception;

/**
 * 当向RabbitMQ发送消息时发生错误时，抛出此异常。
 */
public class WebServerStartException extends RuntimeException {

    /**
     * 创建一个没有详细消息或原因的[WebServerStartException]新实例。
     */
    public WebServerStartException() {
        super();
    }

    /**
     * 使用指定的详细消息创建一个[WebServerStartException]新实例。
     *
     * @param message 详细消息。
     */
    public WebServerStartException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细消息和原因创建一个[WebServerStartException]新实例。
     *
     * @param message 详细消息。
     * @param cause   异常的原因。
     */
    public WebServerStartException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的原因创建一个[WebServerStartException]新实例。
     *
     * @param cause 异常的原因。
     */
    public WebServerStartException(Throwable cause) {
        super(cause);
    }
}