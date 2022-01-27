/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoConfiguredOpenTelemetrySdkTest {

  private AutoConfiguredOpenTelemetrySdkBuilder builder;

  @BeforeEach
  void resetGlobal() {
    GlobalOpenTelemetry.resetForTest();
    builder =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .addPropertiesSupplier(disableExportPropertySupplier());
  }

  @Test
  void builder_addTracerProviderCustomizer() {
    IdGenerator idGenerator = mock(IdGenerator.class);

    when(idGenerator.generateTraceId()).thenReturn(TraceId.fromLongs(9999999999L, 9999999999L));
    when(idGenerator.generateSpanId()).thenReturn(SpanId.fromLong(9999999999L));

    Span span =
        builder
            .addTracerProviderCustomizer(
                (tracerProviderBuilder, config) ->
                    tracerProviderBuilder.setIdGenerator(idGenerator))
            .build()
            .getOpenTelemetrySdk()
            .getTracer("test")
            .spanBuilder("name")
            .startSpan();

    assertThat(span.getSpanContext().getSpanId()).isEqualTo(SpanId.fromLong(9999999999L));
    assertThat(span.getSpanContext().getTraceId())
        .isEqualTo(TraceId.fromLongs(9999999999L, 9999999999L));
    span.end();
  }

  @Test
  @SuppressWarnings("unchecked")
  void builder_addPropagatorCustomizer() {
    Context extracted = Context.root().with(ContextKey.named("animal"), "bear");
    TextMapPropagator propagator1 = mock(TextMapPropagator.class);
    TextMapPropagator propagator2 = mock(TextMapPropagator.class);
    TextMapGetter<Map<String, String>> getter = mock(TextMapGetter.class);
    when(propagator2.extract(any(), any(), any())).thenReturn(extracted);

    OpenTelemetrySdk sdk =
        builder
            .addPropertiesSupplier(
                () -> Collections.singletonMap("otel.propagators", "tracecontext"))
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
            .build()
            .getOpenTelemetrySdk();

    assertThat(
            sdk.getPropagators()
                .getTextMapPropagator()
                .extract(Context.root(), Collections.emptyMap(), getter))
        .isEqualTo(extracted);
  }

  @Test
  void builder_addResourceCustomizer() {
    Resource autoConfiguredResource =
        builder
            .addResourceCustomizer(
                (resource, config) ->
                    resource.merge(Resource.builder().put("key2", "value2").build()))
            .build()
            .getResource();

    assertThat(autoConfiguredResource.getAttribute(stringKey("key2"))).isEqualTo("value2");
  }

  @Test
  void builder_addSamplerCustomizer() {
    Sampler sampler1 = mock(Sampler.class);
    Sampler sampler2 = mock(Sampler.class);

    SdkTracerProvider sdkTracerProvider =
        builder
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
            .build()
            .getOpenTelemetrySdk()
            .getSdkTracerProvider();

    assertThat(sdkTracerProvider)
        .extracting("sharedState")
        .extracting("sampler")
        .isEqualTo(sampler2);
  }

  @Test
  void builder_addSpanExporterCustomizer() {
    SpanExporter spanExporter1 = mock(SpanExporter.class);
    SpanExporter spanExporter2 = mock(SpanExporter.class);

    Mockito.lenient().when(spanExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    SdkTracerProvider sdkTracerProvider =
        builder
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
            .build()
            .getOpenTelemetrySdk()
            .getSdkTracerProvider();

    assertThat(sdkTracerProvider)
        .extracting("sharedState")
        .extracting("activeSpanProcessor")
        .extracting("worker")
        .extracting("spanExporter")
        .isEqualTo(spanExporter2);
  }

  @Test
  void builder_addPropertiesSupplier() {
    AutoConfiguredOpenTelemetrySdk autoConfigured =
        builder
            .addPropertiesSupplier(() -> Collections.singletonMap("key", "valueUnused"))
            .addPropertiesSupplier(() -> Collections.singletonMap("key", "value"))
            .addPropertiesSupplier(() -> Collections.singletonMap("otel-key", "otel-value"))
            .addPropertiesSupplier(
                () -> Collections.singletonMap("otel.service.name", "test-service"))
            .setResultAsGlobal(false)
            .build();

    assertThat(autoConfigured.getResource().getAttribute(ResourceAttributes.SERVICE_NAME))
        .isEqualTo("test-service");
    assertThat(autoConfigured.getConfig().getString("key")).isEqualTo("value");
    assertThat(autoConfigured.getConfig().getString("otel.key")).isEqualTo("otel-value");
  }

  @Test
  void builder_addMeterProviderCustomizer() {
    MetricReaderFactory metricReaderFactory = mock(MetricReaderFactory.class);
    MetricReader metricReader = mock(MetricReader.class);

    when(metricReaderFactory.apply(any())).thenReturn(metricReader);
    Mockito.lenient().when(metricReader.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(metricReader.flush()).thenReturn(CompletableResultCode.ofSuccess());

    SdkMeterProvider sdkMeterProvider =
        builder
            .addMeterProviderCustomizer(
                (meterProviderBuilder, configProperties) ->
                    meterProviderBuilder.registerMetricReader(metricReaderFactory))
            .build()
            .getOpenTelemetrySdk()
            .getSdkMeterProvider();
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);

    verify(metricReader).flush();
  }

  // TODO: add test for addMetricExporterCustomizer once OTLP export is enabled by default

  @Test
  void builder_setResultAsGlobalFalse() {
    GlobalOpenTelemetry.set(OpenTelemetry.noop());

    OpenTelemetry openTelemetry = builder.setResultAsGlobal(false).build().getOpenTelemetrySdk();

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isNotSameAs(openTelemetry);
  }

  @Test
  void builder_setResultAsGlobalTrue() {
    OpenTelemetrySdk openTelemetry = builder.setResultAsGlobal(true).build().getOpenTelemetrySdk();

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(openTelemetry);
  }

  private static Supplier<Map<String, String>> disableExportPropertySupplier() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.metrics.exporter", "none");
    props.put("otel.traces.exporter", "none");
    props.put("otel.logs.exporter", "none");
    return () -> props;
  }
}
