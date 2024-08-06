/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOn;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Propagator;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Resource;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Selector;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryConfigurationFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(OpenTelemetryConfigurationFactoryTest.class.getClassLoader());

  @Test
  void create_Null() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk = OpenTelemetrySdk.builder().build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(null, spiHelper, closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }

  @Test
  void create_InvalidFileFormat() {
    List<OpenTelemetryConfiguration> testCases =
        Arrays.asList(
            new OpenTelemetryConfiguration(), new OpenTelemetryConfiguration().withFileFormat("1"));

    List<Closeable> closeables = new ArrayList<>();
    for (OpenTelemetryConfiguration testCase : testCases) {
      assertThatThrownBy(
              () ->
                  OpenTelemetryConfigurationFactory.getInstance()
                      .create(testCase, spiHelper, closeables))
          .isInstanceOf(ConfigurationException.class)
          .hasMessage("Unsupported file format. Supported formats include: 0.1");
      cleanup.addCloseables(closeables);
    }
  }

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdk.builder()
            .setPropagators(
                ContextPropagators.create(
                    TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance())))
            .build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance()
            .create(new OpenTelemetryConfiguration().withFileFormat("0.1"), spiHelper, closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }

  @Test
  void create_Disabled() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk = OpenTelemetrySdk.builder().build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance()
            .create(
                new OpenTelemetryConfiguration()
                    .withFileFormat("0.1")
                    .withDisabled(true)
                    // Logger provider configuration should be ignored since SDK is disabled
                    .withLoggerProvider(
                        new LoggerProvider()
                            .withProcessors(
                                Collections.singletonList(
                                    new LogRecordProcessor()
                                        .withSimple(
                                            new SimpleLogRecordProcessor()
                                                .withExporter(
                                                    new LogRecordExporter()
                                                        .withOtlp(new Otlp())))))),
                spiHelper,
                closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }

  @Test
  void create_Configured() {
    List<Closeable> closeables = new ArrayList<>();
    io.opentelemetry.sdk.resources.Resource expectedResource =
        io.opentelemetry.sdk.resources.Resource.getDefault().toBuilder()
            .put("service.name", "my-service")
            .put("key", "val")
            // resource attributes from resource ComponentProviders
            .put("color", "red")
            .put("shape", "square")
            .put("order", "second")
            .build();
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdk.builder()
            .setPropagators(
                ContextPropagators.create(
                    TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance(),
                        OtTracePropagator.getInstance(),
                        B3Propagator.injectingMultiHeaders(),
                        B3Propagator.injectingSingleHeader(),
                        JaegerPropagator.getInstance())))
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .setResource(expectedResource)
                    .setLogLimits(
                        () ->
                            LogLimits.builder()
                                .setMaxAttributeValueLength(1)
                                .setMaxNumberOfAttributes(2)
                                .build())
                    .addLogRecordProcessor(
                        io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor.builder(
                                OtlpGrpcLogRecordExporter.getDefault())
                            .build())
                    .build())
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(expectedResource)
                    .setSpanLimits(
                        SpanLimits.builder()
                            .setMaxNumberOfAttributes(1)
                            .setMaxAttributeValueLength(2)
                            .setMaxNumberOfEvents(3)
                            .setMaxNumberOfLinks(4)
                            .setMaxNumberOfAttributesPerEvent(5)
                            .setMaxNumberOfAttributesPerLink(6)
                            .build())
                    .setSampler(alwaysOn())
                    .addSpanProcessor(
                        io.opentelemetry.sdk.trace.export.BatchSpanProcessor.builder(
                                OtlpGrpcSpanExporter.getDefault())
                            .build())
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .setResource(expectedResource)
                    .registerMetricReader(
                        io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                                OtlpGrpcMetricExporter.getDefault())
                            .build())
                    .registerView(
                        InstrumentSelector.builder().setName("instrument-name").build(),
                        View.builder().setName("stream-name").build())
                    .build())
            .build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance()
            .create(
                new OpenTelemetryConfiguration()
                    .withFileFormat("0.1")
                    .withPropagator(
                        new Propagator()
                            .withComposite(
                                Arrays.asList(
                                    "tracecontext",
                                    "baggage",
                                    "ottrace",
                                    "b3multi",
                                    "b3",
                                    "jaeger")))
                    .withResource(
                        new Resource()
                            .withAttributes(
                                new Attributes()
                                    .withServiceName("my-service")
                                    .withAdditionalProperty("key", "val")))
                    .withLoggerProvider(
                        new LoggerProvider()
                            .withLimits(
                                new LogRecordLimits()
                                    .withAttributeValueLengthLimit(1)
                                    .withAttributeCountLimit(2))
                            .withProcessors(
                                Collections.singletonList(
                                    new LogRecordProcessor()
                                        .withBatch(
                                            new BatchLogRecordProcessor()
                                                .withExporter(
                                                    new LogRecordExporter()
                                                        .withOtlp(new Otlp()))))))
                    .withTracerProvider(
                        new TracerProvider()
                            .withLimits(
                                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal
                                        .model.SpanLimits()
                                    .withAttributeCountLimit(1)
                                    .withAttributeValueLengthLimit(2)
                                    .withEventCountLimit(3)
                                    .withLinkCountLimit(4)
                                    .withEventAttributeCountLimit(5)
                                    .withLinkAttributeCountLimit(6))
                            .withSampler(new Sampler().withAlwaysOn(new AlwaysOn()))
                            .withProcessors(
                                Collections.singletonList(
                                    new SpanProcessor()
                                        .withBatch(
                                            new BatchSpanProcessor()
                                                .withExporter(
                                                    new SpanExporter().withOtlp(new Otlp()))))))
                    .withMeterProvider(
                        new MeterProvider()
                            .withReaders(
                                Collections.singletonList(
                                    new MetricReader()
                                        .withPeriodic(
                                            new PeriodicMetricReader()
                                                .withExporter(
                                                    new MetricExporter()
                                                        .withOtlp(new OtlpMetric())))))
                            .withViews(
                                Collections.singletonList(
                                    new io.opentelemetry.sdk.extension.incubator.fileconfig.internal
                                            .model.View()
                                        .withSelector(
                                            new Selector().withInstrumentName("instrument-name"))
                                        .withStream(
                                            new Stream()
                                                .withName("stream-name")
                                                .withAttributeKeys(null))))),
                spiHelper,
                closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }
}
