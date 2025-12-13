/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

public class ErgonomicLogApiUsageTest {

  private static final AttributeKey<Double> PREVIOUS_TEMP =
      AttributeKey.doubleKey("com.acme.previous_temp");
  private static final AttributeKey<Double> NEW_TEMP = AttributeKey.doubleKey("com.acme.new_temp");

  @ParameterizedTest
  @MethodSource("loggers")
  void simple(
      org.apache.logging.log4j.Logger log4j2Logger,
      org.slf4j.Logger slf4jLogger,
      java.util.logging.Logger julLogger,
      io.opentelemetry.api.incubator.logs.ExtendedLogger otelLogger) {
    log4j2Logger.info("Hello world");
    log4j2Logger.log(org.apache.logging.log4j.Level.INFO, "Hello world");

    slf4jLogger.info("Hello world");
    slf4jLogger.atLevel(org.slf4j.event.Level.INFO).log("Hello world");

    julLogger.info("Hello world");
    julLogger.log(Level.INFO, "Hello world");

    // In opentelemetry, all log events have an event name. The notion of emitting a simple "Hello
    // world" string is a bit of a conceptual mismatch.
    // Instead, we emit an event with an event_name set to a value that would allow us to identify
    // hello world events
    otelLogger.info("com.acme.hello_world");
    otelLogger.log(Severity.INFO, "com.acem.hello_world");
    otelLogger.logBuilder(Severity.INFO, "com.acme.hello_world").emit();
  }

  @ParameterizedTest
  @MethodSource("loggers")
  void simpleWithException(
      org.apache.logging.log4j.Logger log4j2Logger,
      org.slf4j.Logger slf4jLogger,
      java.util.logging.Logger julLogger,
      io.opentelemetry.api.incubator.logs.ExtendedLogger otelLogger) {
    log4j2Logger.info("Hello world", new Exception("error!"));
    log4j2Logger.log(org.apache.logging.log4j.Level.INFO, "Hello world", new Exception("error!"));

    slf4jLogger.info("Hello world", new Exception("error!"));
    slf4jLogger
        .atLevel(org.slf4j.event.Level.INFO)
        .setCause(new Exception("error!"))
        .log("Hello world");

    // jul doesn't have severity overloads to record exception
    julLogger.log(Level.INFO, "Hello world", new Exception("error!"));

    otelLogger.info("com.acme.hello_world", new Exception("error!"));
    otelLogger.log(Severity.INFO, "com.acem.hello_world", new Exception("error!"));
    otelLogger
        .logBuilder(Severity.INFO, "com.acme.hello_world")
        .setException(new Exception("error!"))
        .emit();
  }

  @ParameterizedTest
  @MethodSource("loggers")
  void template(
      org.apache.logging.log4j.Logger log4j2Logger,
      org.slf4j.Logger slf4jLogger,
      java.util.logging.Logger julLogger,
      io.opentelemetry.api.incubator.logs.ExtendedLogger otelLogger) {
    log4j2Logger.info("Temperature changed from {} to {}", 72, 70);
    log4j2Logger.log(
        org.apache.logging.log4j.Level.INFO, "Temperature changed from {} to {}", 72, 70);

    slf4jLogger.info("Temperature changed from {} to {}", 72, 70);
    slf4jLogger
        .atLevel(org.slf4j.event.Level.INFO)
        .log("Temperature changed from {} to {}", 72, 70);

    // jul doesn't have severity overloads to record params
    julLogger.log(Level.INFO, "Temperature changed from {0} to {1}", new Object[] {72, 70});

    otelLogger.info(
        "com.acme.temperature_change", Attributes.of(PREVIOUS_TEMP, 72d, NEW_TEMP, 70d));
    otelLogger.log(
        Severity.INFO,
        "com.acme.temperature_change",
        Attributes.of(PREVIOUS_TEMP, 72d, NEW_TEMP, 70d));
    otelLogger
        .logBuilder(Severity.INFO, "com.acme.temperature_change")
        .setAttribute(PREVIOUS_TEMP, 72d)
        .setAttribute(NEW_TEMP, 70d)
        .emit();
  }

  private static Stream<Arguments> loggers() {
    return Stream.of(
        Arguments.of(
            LogManager.getLogger("log4j2-logger"),
            LoggerFactory.getLogger("slf4j-logger"),
            Logger.getLogger("jul-logger"),
            OpenTelemetry.noop().getLogsBridge().get("otel-logger")));
  }
}
