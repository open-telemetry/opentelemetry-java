/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.extension.trace.jaeger.sampler.internal.JaegerRemoteSamplerComponentProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

// Connecting to the (unavailable) endpoint during the initial poll logs a warning; suppress it.
@SuppressLogger(JaegerRemoteSampler.class)
class JaegerRemoteSamplerComponentProviderTest {

  private static final int DEFAULT_POLLING_INTERVAL_MILLIS = 60000;

  @Test
  void create_appliesConfiguredInterval() {
    // Regression test for reading the wrong declarative config key: the property is "interval", so
    // reading "internal" silently dropped the operator's value and fell back to the default.
    Sampler sampler =
        create(
            "endpoint: http://localhost:14250\n"
                + "interval: 10000\n"
                + "initial_sampler:\n"
                + "  always_off: {}\n");
    try {
      assertThat(((JaegerRemoteSampler) sampler).getPollingIntervalMs()).isEqualTo(10000);
    } finally {
      ((JaegerRemoteSampler) sampler).shutdown();
    }
  }

  @Test
  void create_defaultsIntervalWhenOmitted() {
    Sampler sampler =
        create("endpoint: http://localhost:14250\n" + "initial_sampler:\n" + "  always_off: {}\n");
    try {
      assertThat(((JaegerRemoteSampler) sampler).getPollingIntervalMs())
          .isEqualTo(DEFAULT_POLLING_INTERVAL_MILLIS);
    } finally {
      ((JaegerRemoteSampler) sampler).shutdown();
    }
  }

  private static Sampler create(String yaml) {
    DeclarativeConfigProperties config =
        DeclarativeConfiguration.toConfigProperties(
            new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    return new JaegerRemoteSamplerComponentProvider().create(config);
  }
}
