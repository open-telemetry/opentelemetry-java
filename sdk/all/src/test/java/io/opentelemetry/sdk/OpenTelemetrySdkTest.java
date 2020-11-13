/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk.ObfuscatedTracerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTelemetrySdkTest {

  @Mock private TracerSdkProvider tracerProvider;
  @Mock private MeterProvider meterProvider;
  @Mock private ContextPropagators propagators;
  @Mock private Clock clock;

  @Test
  void testGlobalDefault() {
    assertThat(((TracerSdkProvider) OpenTelemetrySdk.getGlobalTracerManagement()).get(""))
        .isSameAs(OpenTelemetry.getGlobalTracerProvider().get(""));
    assertThat(OpenTelemetrySdk.getGlobalMeterProvider())
        .isSameAs(OpenTelemetry.getGlobalMeterProvider());
  }

  @Test
  void testShortcutVersions() {
    assertThat(OpenTelemetry.getGlobalTracer("testTracer1"))
        .isEqualTo(OpenTelemetry.getGlobalTracerProvider().get("testTracer1"));
    assertThat(OpenTelemetry.getGlobalTracer("testTracer2", "testVersion"))
        .isEqualTo(OpenTelemetry.getGlobalTracerProvider().get("testTracer2", "testVersion"));
    assertThat(OpenTelemetry.getGlobalMeter("testMeter1"))
        .isEqualTo(OpenTelemetry.getGlobalMeterProvider().get("testMeter1"));
    assertThat(OpenTelemetry.getGlobalMeter("testMeter2", "testVersion"))
        .isEqualTo(OpenTelemetry.getGlobalMeterProvider().get("testMeter2", "testVersion"));
  }

  @Test
  void testBuilderDefaults() {
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
    assertThat(openTelemetry.getTracerProvider())
        .isInstanceOfSatisfying(
            ObfuscatedTracerProvider.class,
            obfuscatedTracerProvider ->
                assertThat(obfuscatedTracerProvider.unobfuscate())
                    .isInstanceOf(TracerSdkProvider.class));
    assertThat(openTelemetry.getMeterProvider()).isInstanceOf(MeterSdkProvider.class);
    assertThat(openTelemetry.getResource()).isEqualTo(Resource.getDefault());
    assertThat(openTelemetry.getClock()).isEqualTo(MillisClock.getInstance());
  }

  @Test
  void testReconfigure() {
    Resource resource = Resource.create(Attributes.builder().put("cat", "meow").build());
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setPropagators(propagators)
            .setClock(clock)
            .setResource(resource)
            .build();
    assertThat(((ObfuscatedTracerProvider) openTelemetry.getTracerProvider()).unobfuscate())
        .isEqualTo(tracerProvider);
    assertThat(openTelemetry.getMeterProvider()).isEqualTo(meterProvider);
    assertThat(openTelemetry.getPropagators()).isEqualTo(propagators);
    assertThat(openTelemetry.getResource()).isEqualTo(resource);
    assertThat(openTelemetry.getClock()).isEqualTo(clock);
  }

  @Test
  void testReconfigure_justClockAndResource() {
    Resource resource = Resource.create(Attributes.builder().put("cat", "meow").build());
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder().setClock(clock).setResource(resource).build();
    TracerProvider unobfuscatedTracerProvider =
        ((ObfuscatedTracerProvider) openTelemetry.getTracerProvider()).unobfuscate();

    assertThat(unobfuscatedTracerProvider).isInstanceOf(TracerSdkProvider.class);
    // this is not a great way to test this...
    assertThat(unobfuscatedTracerProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("clock", clock)
        .hasFieldOrPropertyWithValue("resource", resource);

    assertThat(openTelemetry.getMeterProvider()).isInstanceOf(MeterSdkProvider.class);
    // this is not awesome
    assertThat(openTelemetry.getMeterProvider())
        .extracting("registry")
        .extracting("meterProviderSharedState")
        .hasFieldOrPropertyWithValue("clock", clock)
        .hasFieldOrPropertyWithValue("resource", resource);

    assertThat(openTelemetry.getResource()).isSameAs(resource);
    assertThat(openTelemetry.getClock()).isSameAs(clock);
  }
}
