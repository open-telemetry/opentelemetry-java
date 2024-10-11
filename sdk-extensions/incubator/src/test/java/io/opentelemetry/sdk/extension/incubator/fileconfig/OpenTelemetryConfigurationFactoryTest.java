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
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOnModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetricModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.StreamModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
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
  void create_InvalidFileFormat() {
    List<OpenTelemetryConfigurationModel> testCases =
        Arrays.asList(
            new OpenTelemetryConfigurationModel(),
            new OpenTelemetryConfigurationModel().withFileFormat("1"));

    List<Closeable> closeables = new ArrayList<>();
    for (OpenTelemetryConfigurationModel testCase : testCases) {
      assertThatThrownBy(
              () ->
                  OpenTelemetryConfigurationFactory.getInstance()
                      .create(testCase, spiHelper, closeables))
          .isInstanceOf(ConfigurationException.class)
          .hasMessage("Unsupported file format. Supported formats include: 0.3");
      cleanup.addCloseables(closeables);
    }
  }

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk = OpenTelemetrySdk.builder().build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance()
            .create(
                new OpenTelemetryConfigurationModel().withFileFormat("0.3"), spiHelper, closeables);
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
                new OpenTelemetryConfigurationModel()
                    .withFileFormat("0.3")
                    .withDisabled(true)
                    // Logger provider configuration should be ignored since SDK is disabled
                    .withLoggerProvider(
                        new LoggerProviderModel()
                            .withProcessors(
                                Collections.singletonList(
                                    new LogRecordProcessorModel()
                                        .withSimple(
                                            new SimpleLogRecordProcessorModel()
                                                .withExporter(
                                                    new LogRecordExporterModel()
                                                        .withOtlp(new OtlpModel())))))),
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
                new OpenTelemetryConfigurationModel()
                    .withFileFormat("0.3")
                    .withPropagator(
                        new PropagatorModel()
                            .withComposite(
                                Arrays.asList(
                                    "tracecontext",
                                    "baggage",
                                    "ottrace",
                                    "b3multi",
                                    "b3",
                                    "jaeger")))
                    .withResource(
                        new ResourceModel()
                            .withAttributes(
                                Arrays.asList(
                                    new AttributeNameValueModel()
                                        .withName("service.name")
                                        .withValue("my-service"),
                                    new AttributeNameValueModel()
                                        .withName("key")
                                        .withValue("val"))))
                    .withLoggerProvider(
                        new LoggerProviderModel()
                            .withLimits(
                                new LogRecordLimitsModel()
                                    .withAttributeValueLengthLimit(1)
                                    .withAttributeCountLimit(2))
                            .withProcessors(
                                Collections.singletonList(
                                    new LogRecordProcessorModel()
                                        .withBatch(
                                            new BatchLogRecordProcessorModel()
                                                .withExporter(
                                                    new LogRecordExporterModel()
                                                        .withOtlp(new OtlpModel()))))))
                    .withTracerProvider(
                        new TracerProviderModel()
                            .withLimits(
                                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal
                                        .model.SpanLimitsModel()
                                    .withAttributeCountLimit(1)
                                    .withAttributeValueLengthLimit(2)
                                    .withEventCountLimit(3)
                                    .withLinkCountLimit(4)
                                    .withEventAttributeCountLimit(5)
                                    .withLinkAttributeCountLimit(6))
                            .withSampler(new SamplerModel().withAlwaysOn(new AlwaysOnModel()))
                            .withProcessors(
                                Collections.singletonList(
                                    new SpanProcessorModel()
                                        .withBatch(
                                            new BatchSpanProcessorModel()
                                                .withExporter(
                                                    new SpanExporterModel()
                                                        .withOtlp(new OtlpModel()))))))
                    .withMeterProvider(
                        new MeterProviderModel()
                            .withReaders(
                                Collections.singletonList(
                                    new MetricReaderModel()
                                        .withPeriodic(
                                            new PeriodicMetricReaderModel()
                                                .withExporter(
                                                    new PushMetricExporterModel()
                                                        .withOtlp(new OtlpMetricModel())))))
                            .withViews(
                                Collections.singletonList(
                                    new io.opentelemetry.sdk.extension.incubator.fileconfig.internal
                                            .model.ViewModel()
                                        .withSelector(
                                            new SelectorModel()
                                                .withInstrumentName("instrument-name"))
                                        .withStream(
                                            new StreamModel()
                                                .withName("stream-name")
                                                .withAttributeKeys(null))))),
                spiHelper,
                closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }
}
