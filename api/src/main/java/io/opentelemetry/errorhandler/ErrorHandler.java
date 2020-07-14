package io.opentelemetry.errorhandler;

/** Handler handles irremediable events. */
public interface ErrorHandler {
  /**
   * Handle handles any error or exception deemed irremediable by an OpenTelemetry component.
   *
   * @param e The exception to be handled
   */
  void handle(OpenTelemetryException e);
}
