/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTelemetrySdkTest {

  @Mock private SdkTracerProvider tracerProvider;
  @Mock private SdkMeterProvider meterProvider;
  @Mock private SdkLogEmitterProvider logEmitterProvider;
  @Mock private ContextPropagators propagators;
  @Mock private Clock clock;

  @AfterEach
  void tearDown() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void testRegisterGlobal() {
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder().setPropagators(propagators).buildAndRegisterGlobal();
    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(sdk);
    assertThat(sdk.getTracerProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.getTracerProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.get().getTracer(""));
    assertThat(sdk.getMeterProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.get().getMeterProvider().get(""));

    assertThat(GlobalOpenTelemetry.getPropagators())
        .isSameAs(GlobalOpenTelemetry.get().getPropagators())
        .isSameAs(sdk.getPropagators())
        .isSameAs(propagators);
  }

  @Test
  void castingGlobalToSdkFails() {
    OpenTelemetrySdk.builder().buildAndRegisterGlobal();

    assertThatThrownBy(
            () -> {
              @SuppressWarnings("unused")
              OpenTelemetrySdk shouldFail = (OpenTelemetrySdk) GlobalOpenTelemetry.get();
            })
        .isInstanceOf(ClassCastException.class);
  }

  @Test
  void testShortcutVersions() {
    assertThat(GlobalOpenTelemetry.getTracer("testTracer1"))
        .isSameAs(GlobalOpenTelemetry.getTracerProvider().get("testTracer1"));
    assertThat(GlobalOpenTelemetry.getTracer("testTracer2", "testVersion"))
        .isSameAs(GlobalOpenTelemetry.getTracerProvider().get("testTracer2", "testVersion"));
    assertThat(
            GlobalOpenTelemetry.tracerBuilder("testTracer2")
                .setInstrumentationVersion("testVersion")
                .setSchemaUrl("https://example.invalid")
                .build())
        .isSameAs(
            GlobalOpenTelemetry.getTracerProvider()
                .tracerBuilder("testTracer2")
                .setInstrumentationVersion("testVersion")
                .setSchemaUrl("https://example.invalid")
                .build());
  }

  @Test
  void testBuilderDefaults() {
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
    assertThat(openTelemetry.getTracerProvider())
        .isInstanceOfSatisfying(
            OpenTelemetrySdk.ObfuscatedTracerProvider.class,
            obfuscatedTracerProvider ->
                assertThat(obfuscatedTracerProvider.unobfuscate())
                    .isInstanceOf(SdkTracerProvider.class));
    assertThat(openTelemetry.getMeterProvider())
        .isInstanceOfSatisfying(
            OpenTelemetrySdk.ObfuscatedMeterProvider.class,
            obfuscatedMeterProvider ->
                assertThat(obfuscatedMeterProvider.unobfuscate())
                    .isInstanceOf(SdkMeterProvider.class));
  }

  @Test
  void building() {
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLogEmitterProvider(logEmitterProvider)
            .setPropagators(propagators)
            .build();
    assertThat(
            ((OpenTelemetrySdk.ObfuscatedTracerProvider) openTelemetry.getTracerProvider())
                .unobfuscate())
        .isEqualTo(tracerProvider);
    assertThat(openTelemetry.getSdkTracerProvider()).isEqualTo(tracerProvider);
    assertThat(
            ((OpenTelemetrySdk.ObfuscatedMeterProvider) openTelemetry.getMeterProvider())
                .unobfuscate())
        .isEqualTo(meterProvider);
    assertThat(openTelemetry.getSdkLogEmitterProvider()).isEqualTo(logEmitterProvider);
    assertThat(openTelemetry.getPropagators()).isEqualTo(propagators);
  }

  @Test
  void testConfiguration_tracerSettings() {
    Resource resource = Resource.create(Attributes.builder().put("cat", "meow").build());
    IdGenerator idGenerator = mock(IdGenerator.class);
    SpanLimits spanLimits = SpanLimits.getDefault();
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setClock(clock)
                    .setResource(resource)
                    .setIdGenerator(idGenerator)
                    .setSpanLimits(spanLimits)
                    .build())
            .build();
    TracerProvider unobfuscatedTracerProvider =
        ((OpenTelemetrySdk.ObfuscatedTracerProvider) openTelemetry.getTracerProvider())
            .unobfuscate();

    assertThat(unobfuscatedTracerProvider)
        .isInstanceOfSatisfying(
            SdkTracerProvider.class,
            sdkTracerProvider ->
                assertThat(sdkTracerProvider.getSpanLimits()).isEqualTo(spanLimits));
    // Since TracerProvider is in a different package, the only alternative to this reflective
    // approach would be to make the fields public for testing which is worse than this.
    assertThat(unobfuscatedTracerProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("clock", clock)
        .hasFieldOrPropertyWithValue("resource", resource)
        .hasFieldOrPropertyWithValue("idGenerator", idGenerator);
  }

  @Test
  void testTracerBuilder() {
    final OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
    assertThat(openTelemetry.tracerBuilder("instr"))
        .isNotSameAs(OpenTelemetry.noop().tracerBuilder("instr"));
  }

  @Test
  void testTracerBuilderViaProvider() {
    final OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
    assertThat(openTelemetry.getTracerProvider().tracerBuilder("instr"))
        .isNotSameAs(OpenTelemetry.noop().tracerBuilder("instr"));
  }

  @Test
  void testTracerProviderAccess() {
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    assertThat(openTelemetry.getTracerProvider())
        .asInstanceOf(type(OpenTelemetrySdk.ObfuscatedTracerProvider.class))
        .isNotNull()
        .extracting(OpenTelemetrySdk.ObfuscatedTracerProvider::unobfuscate)
        .isSameAs(tracerProvider);
    assertThat(openTelemetry.getSdkTracerProvider()).isNotNull();
  }

  @Test
  void testMeterProviderAccess() {
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build();
    assertThat(openTelemetry.getMeterProvider())
        .asInstanceOf(type(OpenTelemetrySdk.ObfuscatedMeterProvider.class))
        .isNotNull()
        .extracting(OpenTelemetrySdk.ObfuscatedMeterProvider::unobfuscate)
        .isSameAs(meterProvider);
    assertThat(openTelemetry.getSdkMeterProvider()).isNotNull();
  }

  // This is just a demonstration of maximum that one can do with OpenTelemetry configuration.
  // Demonstrates how clear or confusing is SDK configuration
  @Test
  void fullOpenTelemetrySdkConfigurationDemo() {
    SpanLimits newConfig = SpanLimits.builder().setMaxNumberOfAttributes(512).build();

    OpenTelemetrySdkBuilder sdkBuilder =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setSampler(mock(Sampler.class))
                    .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                    .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                    .setClock(mock(Clock.class))
                    .setIdGenerator(mock(IdGenerator.class))
                    .setResource(Resource.empty())
                    .setSpanLimits(newConfig)
                    .build());

    sdkBuilder.build();
  }

  // This is just a demonstration of the bare minimal required configuration in order to get useful
  // SDK.
  // Demonstrates how clear or confusing is SDK configuration
  @Test
  void trivialOpenTelemetrySdkConfigurationDemo() {
    OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .build())
        .setPropagators(ContextPropagators.create(mock(TextMapPropagator.class)))
        .build();
  }

  // This is just a demonstration of two small but not trivial configurations.
  // Demonstrates how clear or confusing is SDK configuration
  @Test
  void minimalOpenTelemetrySdkConfigurationDemo() {
    OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .setSampler(mock(Sampler.class))
                .build())
        .setPropagators(ContextPropagators.create(mock(TextMapPropagator.class)))
        .build();

    OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .setSampler(mock(Sampler.class))
                .setIdGenerator(mock(IdGenerator.class))
                .build())
        .setPropagators(ContextPropagators.create(mock(TextMapPropagator.class)))
        .build();
  }
}
