/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.graal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import org.junit.jupiter.api.Test;

class IncubatingApiTests {
  @Test
  void incubatingApiIsLoadedViaReflection() {
    assertThat(LoggerProvider.noop().get("test")).isInstanceOf(ExtendedLogger.class);
    assertThat(TracerProvider.noop().get("test")).isInstanceOf(ExtendedTracer.class);
    assertThat(MeterProvider.noop().get("test").counterBuilder("test"))
        .isInstanceOf(ExtendedLongCounterBuilder.class);
  }
}
