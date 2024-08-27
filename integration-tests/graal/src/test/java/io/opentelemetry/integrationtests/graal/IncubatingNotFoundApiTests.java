/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtests.graal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import org.junit.jupiter.api.Test;

public class IncubatingNotFoundApiTests {
  @Test
  void incubatingApiIsNotFoundViaReflection() {
    assertThat(LoggerProvider.noop().get("test")).isInstanceOf(Logger.class);
    assertThat(TracerProvider.noop().get("test")).isInstanceOf(Tracer.class);
    assertThat(MeterProvider.noop().get("test").counterBuilder("test"))
        .isInstanceOf(LongCounterBuilder.class);
  }
}
