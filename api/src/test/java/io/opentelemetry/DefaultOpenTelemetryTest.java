/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.trace.DefaultTracerProvider;
import io.opentelemetry.trace.TracerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultOpenTelemetryTest {

  @Mock private TracerProvider tracerProvider;
  @Mock private MeterProvider meterProvider;
  @Mock private BaggageManager baggageManager;
  @Mock private ContextPropagators propagators;

  @Test
  void defaultIsAllDefault() {
    OpenTelemetry openTelemetry = DefaultOpenTelemetry.builder().build();
    assertThat(openTelemetry.getTracerProvider()).isInstanceOf(DefaultTracerProvider.class);
    assertThat(openTelemetry.getMeterProvider()).isInstanceOf(DefaultMeterProvider.class);
    assertThat(openTelemetry.getBaggageManager()).isInstanceOf(BaggageManager.class);
  }

  @Test
  void canReconfigure() {
    OpenTelemetry openTelemetry =
        DefaultOpenTelemetry.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setBaggageManager(baggageManager)
            .setPropagators(propagators)
            .build();
    assertThat(openTelemetry.getTracerProvider()).isEqualTo(tracerProvider);
    assertThat(openTelemetry.getMeterProvider()).isEqualTo(meterProvider);
    assertThat(openTelemetry.getBaggageManager()).isEqualTo(baggageManager);
    assertThat(openTelemetry.getPropagators()).isEqualTo(propagators);
  }
}
