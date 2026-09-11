/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AlwaysOnSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchLogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PropagatorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SimpleLogRecordProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanLimitsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ResourceModelAccessor;
import io.opentelemetry.sdk.internal.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.internal.OpenTelemetrySdkBuilderUtil;
import io.opentelemetry.sdk.internal.SdkConfigProvider;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;

class OpenTelemetryConfigurationFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create()
          .captureForLogger(OpenTelemetryConfigurationFactory.class.getName(), Level.WARN);

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(ComponentLoader.forClassLoader(getClass().getClassLoader()));

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("fileFormatArgs")
  @SuppressLogger(OpenTelemetryConfigurationFactory.class)
  void create_FileFormat(String fileFormat, boolean isValid) {
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel().setFileFormat(fileFormat);

    if (isValid) {
      assertThatCode(() -> OpenTelemetryConfigurationFactory.getInstance().create(model, context))
          .doesNotThrowAnyException();
    } else {
      assertThatThrownBy(
              () -> OpenTelemetryConfigurationFactory.getInstance().create(model, context))
          .isInstanceOf(DeclarativeConfigException.class)
          .hasMessageMatching(
              "Unsupported file format '.+'\\. Supported formats include 0\\.4, 1\\.\\*");
    }
  }

  private static Stream<Arguments> fileFormatArgs() {
    return Stream.of(
        Arguments.argumentSet("null invalid", null, false),
        Arguments.argumentSet("0.3 invalid", "0.3", false),
        Arguments.argumentSet("a0.4 invalid", "a0.4", false),
        Arguments.argumentSet("0.4a invalid", "0.4a", false),
        Arguments.argumentSet("foo invalid", "foo", false),
        Arguments.argumentSet("1.0-rc.a invalid", "1.0-rc.a", false),
        Arguments.argumentSet("1.0.0 invalid", "1.0.0", false),
        Arguments.argumentSet("1.0.3 invalid", "1.0.3", false),
        Arguments.argumentSet("1.0.0-rc.3 invalid", "1.0.0-rc.3", false),
        Arguments.argumentSet("1.1.0 invalid", "1.1.0", false),
        Arguments.argumentSet("1.a invalid", "1.a", false),
        Arguments.argumentSet("0.4 valid", "0.4", true),
        Arguments.argumentSet("1.0-rc.1 valid", "1.0-rc.1", true),
        Arguments.argumentSet("1.0-rc.2 valid", "1.0-rc.2", true),
        Arguments.argumentSet("1.0-rc.3 valid", "1.0-rc.3", true),
        Arguments.argumentSet("1.0 valid", "1.0", true),
        Arguments.argumentSet("1.2 valid", "1.2", true),
        Arguments.argumentSet("1.12 valid", "1.12", true),
        Arguments.argumentSet("1.1 valid", "1.1", true));
  }

  @Test
  @SuppressLogger(OpenTelemetryConfigurationFactory.class)
  void create_FileFormatVersionMismatch_LogsWarning() {
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel().setFileFormat("1.0-rc.3");

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context).getSdk();
    cleanup.addCloseable(sdk);

    logCapturer.assertContains(
        "Configuration file_format '1.0-rc.3' does not exactly match expected version '1.1'");
  }

  @Test
  void create_FileFormatExactMatch_NoWarning() {
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel().setFileFormat("1.1");

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context).getSdk();
    cleanup.addCloseable(sdk);

    assertThat(logCapturer.size()).isEqualTo(0);
  }

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel().setFileFormat("1.1");
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdkBuilderUtil.setConfigProvider(
                OpenTelemetrySdk.builder(),
                SdkConfigProvider.create(DeclarativeConfiguration.toConfigProperties(model)))
            .build();
    cleanup.addCloseable(expectedSdk);

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context).getSdk();
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk).hasToString(expectedSdk.toString());
  }

  @Test
  void create_Disabled() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel()
            .setFileFormat("1.1")
            .setDisabled(true)
            // Logger provider configuration should be ignored since SDK is disabled
            .setLoggerProvider(
                new LoggerProviderModel()
                    .setProcessors(
                        Collections.singletonList(
                            new LogRecordProcessorModel()
                                .setSimple(
                                    new SimpleLogRecordProcessorModel()
                                        .setExporter(
                                            new LogRecordExporterModel()
                                                .setOtlpHttp(new OtlpHttpExporterModel()))))));
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdkBuilderUtil.setConfigProvider(
                OpenTelemetrySdk.builder(),
                SdkConfigProvider.create(DeclarativeConfiguration.toConfigProperties(model)))
            .build();
    cleanup.addCloseable(expectedSdk);

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context).getSdk();
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk).hasToString(expectedSdk.toString());
  }

  @Test
  void create_Configured() throws NoSuchFieldException {
    List<Closeable> closeables = new ArrayList<>();
    Resource expectedResource =
        Resource.getDefault().toBuilder()
            .put("service.name", "my-service")
            .put("key", "val")
            // resource attributes from resource ComponentProviders
            .put("color", "red")
            .put("shape", "square")
            .put("order", "second")
            .build();

    OpenTelemetryConfigurationModel model =
        new OpenTelemetryConfigurationModel()
            .setFileFormat("1.1")
            .setPropagator(
                new PropagatorModel().setCompositeList("tracecontext,baggage,b3multi,b3"))
            .setResource(
                ResourceModelAccessor.setDetection(
                        new ResourceModel(),
                        new ExperimentalResourceDetectionModel()
                            .setDetectors(
                                Arrays.asList(
                                    new ExperimentalResourceDetectorModel()
                                        .setAdditionalProperty("order_first", null),
                                    new ExperimentalResourceDetectorModel()
                                        .setAdditionalProperty("order_second", null),
                                    new ExperimentalResourceDetectorModel()
                                        .setAdditionalProperty("shape_color", null))))
                    .setAttributes(
                        Arrays.asList(
                            new AttributeNameValueModel()
                                .setName("service.name")
                                .setValue("my-service"),
                            new AttributeNameValueModel().setName("key").setValue("val"))))
            .setLoggerProvider(
                new LoggerProviderModel()
                    .setLimits(
                        new LogRecordLimitsModel()
                            .setAttributeValueLengthLimit(1)
                            .setAttributeCountLimit(2))
                    .setProcessors(
                        Collections.singletonList(
                            new LogRecordProcessorModel()
                                .setBatch(
                                    new BatchLogRecordProcessorModel()
                                        .setExporter(
                                            new LogRecordExporterModel()
                                                .setOtlpHttp(new OtlpHttpExporterModel()))))))
            .setTracerProvider(
                new TracerProviderModel()
                    .setLimits(
                        new SpanLimitsModel()
                            .setAttributeCountLimit(1)
                            .setAttributeValueLengthLimit(2)
                            .setEventCountLimit(3)
                            .setLinkCountLimit(4)
                            .setEventAttributeCountLimit(5)
                            .setLinkAttributeCountLimit(6))
                    .setSampler(new SamplerModel().setAlwaysOn(new AlwaysOnSamplerModel()))
                    .setProcessors(
                        Collections.singletonList(
                            new SpanProcessorModel()
                                .setBatch(
                                    new BatchSpanProcessorModel()
                                        .setExporter(
                                            new SpanExporterModel()
                                                .setOtlpHttp(new OtlpHttpExporterModel()))))))
            .setMeterProvider(
                new MeterProviderModel()
                    .setReaders(
                        Collections.singletonList(
                            new MetricReaderModel()
                                .setPeriodic(
                                    new PeriodicMetricReaderModel()
                                        .setExporter(
                                            new PushMetricExporterModel()
                                                .setOtlpHttp(new OtlpHttpMetricExporterModel())))))
                    .setViews(
                        Collections.singletonList(
                            new ViewModel()
                                .setSelector(
                                    new ViewSelectorModel().setInstrumentName("instrument-name"))
                                .setStream(
                                    new ViewStreamModel()
                                        .setName("stream-name")
                                        .setAttributeKeys(null)))));

    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdkBuilderUtil.setConfigProvider(
                OpenTelemetrySdk.builder()
                    .setPropagators(
                        ContextPropagators.create(
                            TextMapPropagator.composite(
                                W3CTraceContextPropagator.getInstance(),
                                W3CBaggagePropagator.getInstance(),
                                B3Propagator.injectingMultiHeaders(),
                                B3Propagator.injectingSingleHeader())))
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
                                BatchLogRecordProcessor.builder(
                                        OtlpHttpLogRecordExporter.builder()
                                            .setComponentLoader(context)
                                            .build())
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
                                BatchSpanProcessor.builder(
                                        OtlpHttpSpanExporter.builder()
                                            .setComponentLoader(context)
                                            .build())
                                    .build())
                            .build())
                    .setMeterProvider(
                        SdkMeterProvider.builder()
                            .setResource(expectedResource)
                            .registerMetricReader(
                                PeriodicMetricReader.builder(
                                        OtlpHttpMetricExporter.builder()
                                            .setComponentLoader(context)
                                            .build())
                                    .build())
                            .registerView(
                                InstrumentSelector.builder().setName("instrument-name").build(),
                                View.builder().setName("stream-name").build())
                            .build()),
                SdkConfigProvider.create(DeclarativeConfiguration.toConfigProperties(model)))
            .build();

    cleanup.addCloseable(expectedSdk);

    ExtendedOpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(model, context).getSdk();
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk).hasToString(expectedSdk.toString());

    // test that the meter provider is wired through to the tracer and logger providers
    Field field = SdkMeterProvider.class.getDeclaredField("sharedState");
    field.setAccessible(true);

    // Lazily initialized
    assertThat(sdk)
        .extracting("loggerProvider")
        .extracting("delegate")
        .extracting("sharedState")
        .extracting("logRecordProcessor")
        .extracting("worker")
        .extracting("logProcessorInstrumentation")
        .extracting("processedLogs")
        .isNull();

    // Lazily initialized
    assertThat(sdk)
        .extracting("tracerProvider")
        .extracting("delegate")
        .extracting("sharedState")
        .extracting("activeSpanProcessor")
        .extracting("worker")
        .extracting("spanProcessorInstrumentation")
        .extracting("processedSpans")
        .isNull();
  }
}
