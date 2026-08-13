/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;

/** Verifies autoconfigure works in OSGi. */
@ExtendWith(BundleContextExtension.class)
public class AutoconfigureTest {

  @Test
  void autoConfiguredSdkInitializes() {
    AutoConfiguredOpenTelemetrySdk autoConfigured =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(AutoconfigureTest::config)
            .build();

    // The component loader autoconfigure uses: autoconfigure bundle's classloader.
    ComponentLoader autoConfigureLoader =
        ComponentLoader.forClassLoader(AutoConfiguredOpenTelemetrySdk.class.getClassLoader());

    Resource resource =
        Resource.getDefault()
            .merge(
                Resource.create(
                    Attributes.of(
                        AttributeKey.stringKey("test.customizer"), "test-osgi-customizer")));
    OpenTelemetrySdk expected =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(resource)
                    .setSampler(new TestSamplerProvider.NoopSampler())
                    .addSpanProcessor(
                        BatchSpanProcessor.builder(
                                OtlpHttpSpanExporter.builder()
                                    .setComponentLoader(autoConfigureLoader)
                                    .build())
                            .build())
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .setResource(resource)
                    .registerMetricReader(new TestMetricReaderProvider.NoopMetricReader())
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .setResource(resource)
                    .addLogRecordProcessor(
                        BatchLogRecordProcessor.builder(
                                OtlpHttpLogRecordExporter.builder()
                                    .setComponentLoader(autoConfigureLoader)
                                    .build())
                            .build())
                    .build())
            .setPropagators(
                ContextPropagators.create(
                    TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance(),
                        B3Propagator.injectingSingleHeader())))
            .build();
    try {
      assertThat(autoConfigured.getOpenTelemetrySdk().toString()).isEqualTo(expected.toString());
    } finally {
      autoConfigured.getOpenTelemetrySdk().close();
      expected.close();
    }
  }

  private static Map<String, String> config() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.traces.exporter", "otlp");
    props.put("otel.metrics.exporter", "test-noop-reader");
    props.put("otel.logs.exporter", "otlp");
    props.put("otel.exporter.otlp.protocol", "http/protobuf");
    props.put("otel.propagators", "tracecontext,baggage,b3");
    props.put("otel.traces.sampler", "test-noop");
    return props;
  }
}
