/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTelemetrySdkTest {

  @RegisterExtension
  LogCapturer logCapturer = LogCapturer.create().captureForLogger(OpenTelemetrySdk.class.getName());

  @Mock private MetricExporter metricExporter;
  @Mock private SdkTracerProvider tracerProvider;
  @Mock private SdkMeterProvider meterProvider;
  @Mock private SdkLoggerProvider loggerProvider;
  @Mock private ContextPropagators propagators;

  @AfterEach
  void tearDown() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void buildAndRegisterGlobal() {
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder().setPropagators(propagators).buildAndRegisterGlobal();
    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(sdk);
    assertThat(sdk.getTracerProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.getTracerProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.get().getTracer(""));
    assertThat(sdk.getMeterProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.get().getMeterProvider().get(""))
        .isSameAs(GlobalOpenTelemetry.get().getMeter(""));

    assertThat(GlobalOpenTelemetry.getPropagators())
        .isSameAs(GlobalOpenTelemetry.get().getPropagators())
        .isSameAs(sdk.getPropagators())
        .isSameAs(propagators);
  }

  @Test
  void buildAndRegisterGlobal_castingGlobalToSdkFails() {
    OpenTelemetrySdk.builder().buildAndRegisterGlobal();

    assertThatThrownBy(
            () -> {
              @SuppressWarnings("unused")
              OpenTelemetrySdk shouldFail = (OpenTelemetrySdk) GlobalOpenTelemetry.get();
            })
        .isInstanceOf(ClassCastException.class);
  }

  @Test
  void builderDefaults() {
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
  void builder() {
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
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
    assertThat(openTelemetry.getSdkLoggerProvider()).isEqualTo(loggerProvider);
    assertThat(openTelemetry.getPropagators()).isEqualTo(propagators);
  }

  @Test
  void getTracer() {
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
  void tracerBuilder() {
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
    assertThat(openTelemetry.tracerBuilder("instr"))
        .isNotSameAs(OpenTelemetry.noop().tracerBuilder("instr"));
  }

  @Test
  void tracerBuilder_ViaProvider() {
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
    assertThat(openTelemetry.getTracerProvider().tracerBuilder("instr"))
        .isNotSameAs(OpenTelemetry.noop().tracerBuilder("instr"));
  }

  @Test
  void getTracerProvider() {
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
  void getMeter() {
    assertThat(GlobalOpenTelemetry.getMeter("testMeter1"))
        .isSameAs(GlobalOpenTelemetry.getMeterProvider().get("testMeter1"));
    assertThat(
            GlobalOpenTelemetry.meterBuilder("testMeter2")
                .setInstrumentationVersion("testVersion")
                .setSchemaUrl("https://example.invalid")
                .build())
        .isSameAs(
            GlobalOpenTelemetry.getMeterProvider()
                .meterBuilder("testMeter2")
                .setInstrumentationVersion("testVersion")
                .setSchemaUrl("https://example.invalid")
                .build());
  }

  @Test
  void meterBuilder() {
    when(metricExporter.getDefaultAggregation(any())).thenCallRealMethod();
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                    .build())
            .build();
    assertThat(openTelemetry.meterBuilder("instr"))
        .isNotSameAs(OpenTelemetry.noop().meterBuilder("instr"));
  }

  @Test
  void meterBuilder_ViaProvider() {
    when(metricExporter.getDefaultAggregation(any())).thenCallRealMethod();
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                    .build())
            .build();
    assertThat(openTelemetry.getMeterProvider().meterBuilder("instr"))
        .isNotSameAs(OpenTelemetry.noop().meterBuilder("instr"));
  }

  @Test
  void getMeterProvider() {
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
    when(metricExporter.getDefaultAggregation(any())).thenCallRealMethod();
    OpenTelemetrySdk.builder()
        .setMeterProvider(
            SdkMeterProvider.builder()
                .setResource(Resource.empty())
                .setClock(mock(Clock.class))
                .registerMetricReader(
                    PeriodicMetricReader.builder(metricExporter)
                        .setInterval(Duration.ofSeconds(10))
                        .build())
                .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                .registerView(
                    InstrumentSelector.builder().setName("name").build(),
                    View.builder().setName("new-name").build())
                .registerView(
                    InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                    View.builder().setAttributeFilter(key -> key.equals("foo")).build())
                .build())
        .setTracerProvider(
            SdkTracerProvider.builder()
                .setSampler(mock(Sampler.class))
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .setClock(mock(Clock.class))
                .setIdGenerator(mock(IdGenerator.class))
                .setResource(Resource.empty())
                .setSpanLimits(SpanLimits.builder().setMaxNumberOfAttributes(512).build())
                .build())
        .setPropagators(ContextPropagators.create(mock(TextMapPropagator.class)))
        .build();
  }

  // This is just a demonstration of the bare minimal required configuration in order to get useful
  // SDK.
  // Demonstrates how clear or confusing is SDK configuration
  @Test
  void trivialOpenTelemetrySdkConfigurationDemo() {
    when(metricExporter.getDefaultAggregation(any())).thenCallRealMethod();
    OpenTelemetrySdk.builder()
        .setMeterProvider(
            SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                .build())
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
    when(metricExporter.getDefaultAggregation(any())).thenCallRealMethod();
    OpenTelemetrySdk.builder()
        .setMeterProvider(
            SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                .build())
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .setSampler(mock(Sampler.class))
                .build())
        .setPropagators(ContextPropagators.create(mock(TextMapPropagator.class)))
        .build();

    OpenTelemetrySdk.builder()
        .setMeterProvider(
            SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                .registerView(
                    InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                    View.builder().setAggregation(Aggregation.explicitBucketHistogram()).build())
                .build())
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(mock(SpanExporter.class)))
                .setSampler(mock(Sampler.class))
                .setIdGenerator(mock(IdGenerator.class))
                .build())
        .setPropagators(ContextPropagators.create(mock(TextMapPropagator.class)))
        .build();
  }

  @Test
  void shutdown() {
    when(tracerProvider.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(meterProvider.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(loggerProvider.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build();

    // First call should call shutdown
    assertThat(sdk.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    verify(tracerProvider).shutdown();
    verify(meterProvider).shutdown();
    verify(loggerProvider).shutdown();
    assertThat(logCapturer.getEvents()).isEmpty();

    // Subsequent calls should log not call shutdown
    Mockito.reset(tracerProvider, meterProvider, loggerProvider);
    assertThat(sdk.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    sdk.close();

    verify(tracerProvider, never()).shutdown();
    verify(meterProvider, never()).shutdown();
    verify(loggerProvider, never()).shutdown();

    assertThat(logCapturer.getEvents())
        .hasSize(2)
        .allSatisfy(
            loggingEvent ->
                assertThat(loggingEvent.getMessage()).isEqualTo("Multiple shutdown calls"));
  }

  @Test
  void stringRepresentation() {
    SpanExporter spanExporter = mock(SpanExporter.class);
    when(spanExporter.toString()).thenReturn("MockSpanExporter{}");
    when(metricExporter.getDefaultAggregation(any())).thenCallRealMethod();
    when(metricExporter.toString()).thenReturn("MockMetricExporter{}");
    LogRecordExporter logRecordExporter = mock(LogRecordExporter.class);
    when(logRecordExporter.toString()).thenReturn("MockLogRecordExporter{}");
    TextMapPropagator propagator = mock(TextMapPropagator.class);
    when(propagator.toString()).thenReturn("MockTextMapPropagator{}");
    Resource resource =
        Resource.builder().put(AttributeKey.stringKey("service.name"), "otel-test").build();
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(resource)
                    .addSpanProcessor(
                        SimpleSpanProcessor.create(
                            SpanExporter.composite(spanExporter, spanExporter)))
                    .build())
            .setMeterProvider(
                SdkMeterProvider.builder()
                    .setResource(resource)
                    .registerMetricReader(PeriodicMetricReader.create(metricExporter))
                    .registerView(
                        InstrumentSelector.builder().setName("instrument").build(),
                        View.builder().setName("new-instrument").build())
                    .build())
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .setResource(resource)
                    .addLogRecordProcessor(
                        SimpleLogRecordProcessor.create(
                            LogRecordExporter.composite(logRecordExporter, logRecordExporter)))
                    .build())
            .setPropagators(ContextPropagators.create(propagator))
            .build();

    assertThat(sdk.toString())
        .isEqualTo(
            "OpenTelemetrySdk{"
                + "tracerProvider=SdkTracerProvider{"
                + "clock=SystemClock{}, "
                + "idGenerator=RandomIdGenerator{}, "
                + "resource=Resource{schemaUrl=null, attributes={service.name=\"otel-test\"}}, "
                + "spanLimitsSupplier=SpanLimitsValue{maxNumberOfAttributes=128, maxNumberOfEvents=128, maxNumberOfLinks=128, maxNumberOfAttributesPerEvent=128, maxNumberOfAttributesPerLink=128, maxAttributeValueLength=2147483647}, "
                + "sampler=ParentBased{root:AlwaysOnSampler,remoteParentSampled:AlwaysOnSampler,remoteParentNotSampled:AlwaysOffSampler,localParentSampled:AlwaysOnSampler,localParentNotSampled:AlwaysOffSampler}, "
                + "spanProcessor=SimpleSpanProcessor{spanExporter=MultiSpanExporter{spanExporters=[MockSpanExporter{}, MockSpanExporter{}]}}"
                + "}, "
                + "meterProvider=SdkMeterProvider{"
                + "clock=SystemClock{}, "
                + "resource=Resource{schemaUrl=null, attributes={service.name=\"otel-test\"}}, "
                + "metricReaders=[PeriodicMetricReader{exporter=MockMetricExporter{}, intervalNanos=60000000000}], "
                + "views=[RegisteredView{instrumentSelector=InstrumentSelector{instrumentName=instrument}, view=View{name=new-instrument, aggregation=DefaultAggregation, attributesProcessor=NoopAttributesProcessor{}}}]"
                + "}, "
                + "loggerProvider=SdkLoggerProvider{"
                + "clock=SystemClock{}, "
                + "resource=Resource{schemaUrl=null, attributes={service.name=\"otel-test\"}}, "
                + "logLimits=LogLimits{maxNumberOfAttributes=128, maxAttributeValueLength=2147483647}, "
                + "logRecordProcessor=SimpleLogRecordProcessor{logRecordExporter=MultiLogRecordExporter{logRecordExporters=[MockLogRecordExporter{}, MockLogRecordExporter{}]}}"
                + "}, "
                + "propagators=DefaultContextPropagators{textMapPropagator=MockTextMapPropagator{}}"
                + "}");
  }
}
