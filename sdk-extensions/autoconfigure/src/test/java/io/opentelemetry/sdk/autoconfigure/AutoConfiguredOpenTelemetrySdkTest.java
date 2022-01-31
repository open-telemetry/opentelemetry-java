/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoConfiguredOpenTelemetrySdkTest {

  private static final ContextKey<String> CONTEXT_KEY = ContextKey.named("animal");

  @Mock private IdGenerator idGenerator;
  @Mock private TextMapPropagator propagator1;
  @Mock private TextMapPropagator propagator2;
  @Mock private TextMapGetter<Map<String, String>> getter;
  @Mock private Sampler sampler1;
  @Mock private Sampler sampler2;
  @Mock private SpanExporter spanExporter1;
  @Mock private SpanExporter spanExporter2;

  @BeforeEach
  void resetGlobal() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void customize() {
    Context extracted = Context.root().with(CONTEXT_KEY, "bear");

    when(idGenerator.generateTraceId()).thenReturn(TraceId.fromLongs(9999999999L, 9999999999L));
    when(idGenerator.generateSpanId()).thenReturn(SpanId.fromLong(9999999999L));

    when(propagator2.extract(any(), any(), any())).thenReturn(extracted);

    when(sampler2.shouldSample(any(), any(), any(), any(), any(), any()))
        .thenReturn(SamplingResult.recordAndSample());

    when(spanExporter2.export(any())).thenReturn(CompletableResultCode.ofSuccess());

    when(spanExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    AutoConfiguredOpenTelemetrySdkBuilder autoConfiguration =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addTracerProviderCustomizer(
                (tracerProviderBuilder, config) ->
                    tracerProviderBuilder.setIdGenerator(idGenerator))
            .addPropagatorCustomizer(
                (previous, config) -> {
                  assertThat(previous).isSameAs(W3CTraceContextPropagator.getInstance());
                  return propagator1;
                })
            .addPropagatorCustomizer(
                (previous, config) -> {
                  assertThat(previous).isSameAs(propagator1);
                  return propagator2;
                })
            .addResourceCustomizer(
                (resource, config) ->
                    resource.merge(Resource.builder().put("key2", "value2").build()))
            .addSamplerCustomizer(
                (previous, config) -> {
                  assertThat(previous).isEqualTo(Sampler.parentBased(Sampler.alwaysOn()));
                  return sampler1;
                })
            .addSamplerCustomizer(
                (previous, config) -> {
                  assertThat(previous).isSameAs(sampler1);
                  return sampler2;
                })
            .addSpanExporterCustomizer(
                (previous, config) -> {
                  assertThat(previous).isSameAs(SpanExporter.composite());
                  return spanExporter1;
                })
            .addSpanExporterCustomizer(
                (previous, config) -> {
                  assertThat(previous).isSameAs(spanExporter1);
                  return spanExporter2;
                })
            .addPropertiesSupplier(() -> Collections.singletonMap("key", "valueUnused"))
            .addPropertiesSupplier(() -> Collections.singletonMap("key", "value"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel-key", "otel-value"))
            .addPropertiesSupplier(
                () -> Collections.singletonMap("otel.propagators", "tracecontext"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel.metrics.exporter", "none"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel.traces.exporter", "none"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel.logs.exporter", "none"))
            .addPropertiesSupplier(
                () -> Collections.singletonMap("otel.service.name", "test-service"))
            .setResultAsGlobal(false);

    GlobalOpenTelemetry.set(OpenTelemetry.noop());
    AutoConfiguredOpenTelemetrySdk autoConfigured = autoConfiguration.build();
    assertThat(autoConfigured.getResource().getAttribute(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("test-service");
    assertThat(autoConfigured.getResource().getAttribute(stringKey("key2"))).isEqualTo("value2");

    assertThat(autoConfigured.getConfig().getString("key")).isEqualTo("value");
    assertThat(autoConfigured.getConfig().getString("otel.key")).isEqualTo("otel-value");

    OpenTelemetrySdk sdk = autoConfigured.getOpenTelemetrySdk();
    // Verify setResultAsGlobal respected
    assertThat(sdk).isNotSameAs(GlobalOpenTelemetry.get());

    assertThat(
            sdk.getPropagators()
                .getTextMapPropagator()
                .extract(Context.root(), Collections.emptyMap(), getter))
        .isEqualTo(extracted);

    Span span = sdk.getTracerProvider().get("test").spanBuilder("test").startSpan();
    assertThat(span.getSpanContext().getSpanId()).isEqualTo(SpanId.fromLong(9999999999L));
    assertThat(span.getSpanContext().getTraceId())
        .isEqualTo(TraceId.fromLongs(9999999999L, 9999999999L));
    span.end();

    // Ensures the export happened.
    sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);
  }

  @Test
  void tracerProviderCustomizer() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    AutoConfiguredOpenTelemetrySdkBuilder autoConfiguration =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addTracerProviderCustomizer(
                (tracerProviderBuilder, config) -> {
                  tracerProviderBuilder.setResource(Resource.builder().put("cat", "meow").build());
                  return tracerProviderBuilder.addSpanProcessor(
                      SimpleSpanProcessor.create(spanExporter));
                })
            .addResourceCustomizer(
                (resource, config) -> resource.merge(Resource.builder().put("cow", "moo").build()))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel.metrics.exporter", "none"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel.traces.exporter", "none"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel.logs.exporter", "none"))
            .setResultAsGlobal(false);

    GlobalOpenTelemetry.set(OpenTelemetry.noop());
    AutoConfiguredOpenTelemetrySdk autoConfigured = autoConfiguration.build();
    assertThat(autoConfigured.getResource().getAttribute(stringKey("cow"))).isEqualTo("moo");

    OpenTelemetrySdk sdk = autoConfigured.getOpenTelemetrySdk();
    sdk.getTracerProvider().get("test").spanBuilder("test").startSpan().end();
    List<SpanData> spanItems = spanExporter.getFinishedSpanItems();
    assertThat(spanItems.size()).isEqualTo(1);
    SpanData spanData = spanItems.get(0);
    assertThat(spanData.getResource().getAttribute(stringKey("cat"))).isEqualTo("meow");

    // Ensures the export happened.
    sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);
  }
}
