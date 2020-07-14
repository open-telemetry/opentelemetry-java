package io.opentelemetry.errorhandler;

/**
 * An {@code OpenTelemetryException} is an error or exception which is deemed irremediable by an
 * OpenTelemetry component. {@code OpenTelemetryExceptions} should be handled by the registered
 * OpenTelemetry {@link ErrorHandler} delegate. {@code OpenTelemetryExceptions} may be extended to
 * indicate that the exception comes from a specific OpenTelemetry component.
 */
public class OpenTelemetryException extends RuntimeException {
  private static final long serialVersionUID = 0L;

  public OpenTelemetryException(String message) {
    super(message);
  }

  public OpenTelemetryException(String message, Throwable cause) {
    super(message, cause);
  }
}
