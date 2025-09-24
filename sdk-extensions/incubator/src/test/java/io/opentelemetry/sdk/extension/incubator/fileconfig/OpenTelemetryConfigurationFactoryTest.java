/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOnSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PropagatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PushMetricExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenTelemetryConfigurationFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          SpiHelper.create(OpenTelemetryConfigurationFactoryTest.class.getClassLoader()));

  @ParameterizedTest
  @MethodSource("fileFormatArgs")
  void create_FileFormat(String fileFormat, boolean isValid) {
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel().withFileFormat(fileFormat);

    if (isValid) {
      assertThatCode(() -> OpenTelemetryConfigurationFactory.getInstance().create(model, context))
          .doesNotThrowAnyException();
    } else {
      assertThatThrownBy(
              () -> OpenTelemetryConfigurationFactory.getInstance().create(model, context))
          .isInstanceOf(DeclarativeConfigException.class)
          .hasMessageMatching(
              "Unsupported file format '.+'\\. Supported formats include 0\\.4, 1\\.0\\*");
    }
  }

  private static Stream<Arguments> fileFormatArgs() {
    return Stream.of(
        // Invalid file formats
        Arguments.of(null, false),
        Arguments.of("0.3", false),
        Arguments.of("a0.4", false),
        Arguments.of("0.4a", false),
        Arguments.of("foo", false),
        Arguments.of("1.0-rc.a", false),
        Arguments.of("1.0.0", false),
        Arguments.of("1.0.3", false),
        // Valid file formats
        Arguments.of("0.4", true),
        Arguments.of("1.0-rc.1", true),
        Arguments.of("1.0-rc.2", true),
        Arguments.of("1.0", true));
  }

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel().withFileFormat("1.0-rc.1");
    ExtendedOpenTelemetrySdk expectedSdk =
        ExtendedOpenTelemetrySdk.create(
            OpenTelemetrySdk.builder().build(), SdkConfigProvider.create(model));
    cleanup.addCloseable(expectedSdk);

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk).hasToString(expectedSdk.toString());
  }

  @Test
  void create_Disabled() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel()
            .withFileFormat("1.0-rc.1")
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
                                                .withOtlpHttp(new OtlpHttpExporterModel()))))));
    ExtendedOpenTelemetrySdk expectedSdk =
        ExtendedOpenTelemetrySdk.create(
            OpenTelemetrySdk.builder().build(), SdkConfigProvider.create(model));
    cleanup.addCloseable(expectedSdk);

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk).hasToString(expectedSdk.toString());
  }

  @Test
  void create_Configured() throws NoSuchFieldException, IllegalAccessException {
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

    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel()
            .withFileFormat("1.0-rc.1")
            .withPropagator(
                new PropagatorModel()
                    .withCompositeList("tracecontext,baggage,ottrace,b3multi,b3,jaeger"))
            .withResource(
                new ResourceModel()
                    .withDetectionDevelopment(
                        new ExperimentalResourceDetectionModel()
                            .withDetectors(
                                Arrays.asList(
                                    new ExperimentalResourceDetectorModel()
                                        .withAdditionalProperty("order_first", null),
                                    new ExperimentalResourceDetectorModel()
                                        .withAdditionalProperty("order_second", null),
                                    new ExperimentalResourceDetectorModel()
                                        .withAdditionalProperty("shape_color", null))))
                    .withAttributes(
                        Arrays.asList(
                            new AttributeNameValueModel()
                                .withName("service.name")
                                .withValue("my-service"),
                            new AttributeNameValueModel().withName("key").withValue("val"))))
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
                                                .withOtlpHttp(new OtlpHttpExporterModel()))))))
            .withTracerProvider(
                new TracerProviderModel()
                    .withLimits(
                        new SpanLimitsModel()
                            .withAttributeCountLimit(1)
                            .withAttributeValueLengthLimit(2)
                            .withEventCountLimit(3)
                            .withLinkCountLimit(4)
                            .withEventAttributeCountLimit(5)
                            .withLinkAttributeCountLimit(6))
                    .withSampler(new SamplerModel().withAlwaysOn(new AlwaysOnSamplerModel()))
                    .withProcessors(
                        Collections.singletonList(
                            new SpanProcessorModel()
                                .withBatch(
                                    new BatchSpanProcessorModel()
                                        .withExporter(
                                            new SpanExporterModel()
                                                .withOtlpHttp(new OtlpHttpExporterModel()))))))
            .withMeterProvider(
                new MeterProviderModel()
                    .withReaders(
                        Collections.singletonList(
                            new MetricReaderModel()
                                .withPeriodic(
                                    new PeriodicMetricReaderModel()
                                        .withExporter(
                                            new PushMetricExporterModel()
                                                .withOtlpHttp(new OtlpHttpMetricExporterModel())))))
                    .withViews(
                        Collections.singletonList(
                            new ViewModel()
                                .withSelector(
                                    new ViewSelectorModel().withInstrumentName("instrument-name"))
                                .withStream(
                                    new ViewStreamModel()
                                        .withName("stream-name")
                                        .withAttributeKeys(null)))));

    ExtendedOpenTelemetrySdk expectedSdk =
        ExtendedOpenTelemetrySdk.create(
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
                                    OtlpHttpLogRecordExporter.getDefault())
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
                                    OtlpHttpSpanExporter.getDefault())
                                .build())
                        .build())
                .setMeterProvider(
                    SdkMeterProvider.builder()
                        .setResource(expectedResource)
                        .registerMetricReader(
                            io.opentelemetry.sdk.metrics.export.PeriodicMetricReader.builder(
                                    OtlpHttpMetricExporter.getDefault())
                                .build())
                        .registerView(
                            InstrumentSelector.builder().setName("instrument-name").build(),
                            View.builder().setName("stream-name").build())
                        .build())
                .build(),
            SdkConfigProvider.create(model));
    cleanup.addCloseable(expectedSdk);

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk).hasToString(expectedSdk.toString());

    // test that the meter provider is wired through to the tracer and logger providers
    Field field = SdkMeterProvider.class.getDeclaredField("sharedState");
    field.setAccessible(true);
    Object sharedState = field.get(sdk.getSdkMeterProvider());
    assertThat(sdk)
        .extracting("loggerProvider")
        .extracting("delegate")
        .extracting("sharedState")
        .extracting("logRecordProcessor")
        .extracting("worker")
        .extracting("processedLogsCounter")
        .extracting("sdkMeter")
        .extracting("meterProviderSharedState")
        .isEqualTo(sharedState);

    assertThat(sdk)
        .extracting("tracerProvider")
        .extracting("delegate")
        .extracting("sharedState")
        .extracting("activeSpanProcessor")
        .extracting("worker")
        .extracting("processedSpansCounter")
        .extracting("sdkMeter")
        .extracting("meterProviderSharedState")
        .isEqualTo(sharedState);
  }
}
