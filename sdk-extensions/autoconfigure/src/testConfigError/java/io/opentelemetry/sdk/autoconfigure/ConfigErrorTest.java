/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

// All tests fail due to config errors so never register a global. We can test everything here
// without separating test sets.
class ConfigErrorTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(GlobalOpenTelemetry.class);

  @Test
  @SetSystemProperty(key = "otel.propagators", value = "cat")
  void invalidPropagator() {
    assertThatThrownBy(AutoConfiguredOpenTelemetrySdk::initialize)
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "Unrecognized value for otel.propagators: cat. Make sure the artifact "
                + "including the propagator is on the classpath.");
  }

  @Test
  @SetSystemProperty(key = "otel.traces.sampler", value = "traceidratio")
  @SetSystemProperty(key = "otel.traces.sampler.arg", value = "bar")
  void invalidTraceIdRatio() {
    assertThatThrownBy(AutoConfiguredOpenTelemetrySdk::initialize)
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property otel.traces.sampler.arg=bar. Must be a double.");
  }

  @Test
  @SetSystemProperty(key = "otel.traces.sampler", value = "parentbased_traceidratio")
  @SetSystemProperty(key = "otel.traces.sampler.arg", value = "bar")
  void invalidTraceIdRatioWithParent() {
    assertThatThrownBy(AutoConfiguredOpenTelemetrySdk::initialize)
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property otel.traces.sampler.arg=bar. Must be a double.");
  }

  @Test
  @SetSystemProperty(key = "otel.traces.sampler", value = "cat")
  void invalidSampler() {
    assertThatThrownBy(AutoConfiguredOpenTelemetrySdk::initialize)
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unrecognized value for otel.traces.sampler: cat");
  }

  @Test
  @SetSystemProperty(key = "otel.traces.sampler", value = "traceidratio")
  @SetSystemProperty(key = "otel.traces.sampler.arg", value = "bar")
  void globalOpenTelemetryWhenError() {
    assertThat(GlobalOpenTelemetry.get())
        .isInstanceOf(OpenTelemetry.class)
        .extracting("propagators")
        // Failed to initialize so is no-op
        .isEqualTo(ContextPropagators.noop());

    LoggingEvent log =
        logs.assertContains(
            "Error automatically configuring OpenTelemetry SDK. "
                + "OpenTelemetry will not be enabled.");
    assertThat(log.getLevel()).isEqualTo(Level.ERROR);
    assertThat(log.getThrowable()).isInstanceOf(ConfigurationException.class);
  }
}
