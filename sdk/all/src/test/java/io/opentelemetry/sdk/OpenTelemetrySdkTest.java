/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk.ObfuscatedTracerProvider;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.TracerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTelemetrySdkTest {

  @Mock private TracerProvider tracerProvider;
  @Mock private MeterProvider meterProvider;
  @Mock private BaggageManager baggageManager;
  @Mock private ContextPropagators propagators;
  @Mock private Clock clock;

  @Test
  void testGlobalDefault() {
    assertThat(((TracerSdkProvider) OpenTelemetrySdk.getGlobalTracerManagement()).get(""))
        .isSameAs(OpenTelemetry.get().getTracerProvider().get(""));
    assertThat(OpenTelemetrySdk.getGlobalBaggageManager())
        .isSameAs(OpenTelemetry.get().getBaggageManager());
    assertThat(OpenTelemetrySdk.getGlobalMeterProvider())
        .isSameAs(OpenTelemetry.get().getMeterProvider());
  }

  @Test
  void testShortcutVersions() {
    assertThat(OpenTelemetry.get().getTracer("testTracer1"))
        .isEqualTo(OpenTelemetry.get().getTracerProvider().get("testTracer1"));
    assertThat(OpenTelemetry.get().getTracer("testTracer2", "testVersion"))
        .isEqualTo(OpenTelemetry.get().getTracerProvider().get("testTracer2", "testVersion"));
    assertThat(OpenTelemetry.get().getMeter("testMeter1"))
        .isEqualTo(OpenTelemetry.get().getMeterProvider().get("testMeter1"));
    assertThat(OpenTelemetry.get().getMeter("testMeter2", "testVersion"))
        .isEqualTo(OpenTelemetry.get().getMeterProvider().get("testMeter2", "testVersion"));
  }

  @Test
  void testBuilderDefaults() {
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.newBuilder().build();
    assertThat(openTelemetry.getTracerProvider())
        .isInstanceOfSatisfying(
            ObfuscatedTracerProvider.class,
            obfuscatedTracerProvider ->
                assertThat(obfuscatedTracerProvider.unobfuscate())
                    .isInstanceOf(TracerSdkProvider.class));
    assertThat(openTelemetry.getMeterProvider()).isInstanceOf(MeterSdkProvider.class);
    assertThat(openTelemetry.getBaggageManager()).isInstanceOf(BaggageManagerSdk.class);
    assertThat(openTelemetry.getResource()).isEqualTo(Resource.getDefault());
    assertThat(openTelemetry.getClock()).isEqualTo(MillisClock.getInstance());
  }

  @Test
  void testReconfigure() {
    Resource resource =
        Resource.create(Attributes.newBuilder().setAttribute("cat", "meow").build());
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.newBuilder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setBaggageManager(baggageManager)
            .setPropagators(propagators)
            .setClock(clock)
            .setResource(resource)
            .build();
    assertThat(openTelemetry.getTracerProvider()).isEqualTo(tracerProvider);
    assertThat(openTelemetry.getMeterProvider()).isEqualTo(meterProvider);
    assertThat(openTelemetry.getBaggageManager()).isEqualTo(baggageManager);
    assertThat(openTelemetry.getPropagators()).isEqualTo(propagators);
    assertThat(openTelemetry.getResource()).isEqualTo(resource);
    assertThat(openTelemetry.getClock()).isEqualTo(clock);

    OpenTelemetrySdk previousOpenTelemetry = OpenTelemetrySdk.get();
    try {
      OpenTelemetry.set(previousOpenTelemetry.toBuilder().setResource(resource).build());

      assertThat(OpenTelemetry.get())
          .isInstanceOfSatisfying(
              OpenTelemetrySdk.class, sdk -> assertThat(sdk.getResource()).isEqualTo(resource));
    } finally {
      OpenTelemetry.set(previousOpenTelemetry);
    }
    assertThat(OpenTelemetry.get()).isEqualTo(previousOpenTelemetry);
  }
}
