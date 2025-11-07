/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutoConfiguredOpenTelemetrySdkTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForType(AutoConfiguredOpenTelemetrySdkBuilder.class);

  @Mock private IdGenerator idGenerator;
  @Mock private TextMapPropagator propagator1;
  @Mock private TextMapPropagator propagator2;
  @Mock private TextMapGetter<Map<String, String>> getter;
  @Mock private Sampler sampler1;
  @Mock private Sampler sampler2;
  @Mock private MetricReader metricReader;
  @Mock private LogRecordProcessor logRecordProcessor;

  private AutoConfiguredOpenTelemetrySdkBuilder builder;

  private static BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
      getTracerProviderBuilderSpy() {
    BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
        traceCustomizer =
            spy(
                new BiFunction<
                    SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>() {
                  @Override
                  public SdkTracerProviderBuilder apply(
                      SdkTracerProviderBuilder builder, ConfigProperties config) {
                    return builder;
                  }
                });
    return traceCustomizer;
  }

  private static BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
      getMeterProviderBuilderSpy() {
    BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
        metricCustomizer =
            spy(
                new BiFunction<
                    SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>() {
                  @Override
                  public SdkMeterProviderBuilder apply(
                      SdkMeterProviderBuilder builder, ConfigProperties config) {
                    return builder;
                  }
                });
    return metricCustomizer;
  }

  private static BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder>
      getLoggerProviderBuilderSpy() {
    BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder> logCustomizer =
        spy(
            new BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder>() {
              @Override
              public SdkLoggerProviderBuilder apply(
                  SdkLoggerProviderBuilder builder, ConfigProperties config) {
                return builder;
              }
            });
    return logCustomizer;
  }

  @BeforeEach
  void resetGlobal() {
    GlobalOpenTelemetry.resetForTest();
    builder =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(disableExportPropertySupplier());
  }

  @Test
  void builder_addTracerProviderCustomizer() {
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
  void builder_addPropagatorCustomizer() {
    Context extracted = Context.root().with(ContextKey.named("animal"), "bear");

    when(propagator2.extract(any(), any(), any())).thenReturn(extracted);

    OpenTelemetrySdk sdk =
        builder
            .addPropertiesSupplier(() -> singletonMap("otel.propagators", "tracecontext"))
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
  void builder_addAutoConfigurationCustomizerProviderUsingComponentLoader() {
    AutoConfigurationCustomizerProvider customizerProvider =
        mock(AutoConfigurationCustomizerProvider.class);

    SpiHelper spiHelper =
        SpiHelper.create(AutoConfiguredOpenTelemetrySdkBuilder.class.getClassLoader());

    ComponentLoader componentLoader =
        new ComponentLoader() {
          @SuppressWarnings("unchecked")
          @Override
          public <T> Iterable<T> load(Class<T> spiClass) {
            if (spiClass.equals(AutoConfigurationCustomizerProvider.class)) {
              return Collections.singletonList((T) customizerProvider);
            }
            return spiHelper.load(spiClass);
          }
        };

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        builder.setComponentLoader(componentLoader).build();

    assertThat(
            Objects.requireNonNull(autoConfiguredOpenTelemetrySdk.getConfig()).getComponentLoader())
        .isSameAs(componentLoader);
    verify(customizerProvider).customize(any());
    verifyNoMoreInteractions(customizerProvider);

    autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().shutdown().join(10, TimeUnit.SECONDS);
  }

  @Test
  void builder_addPropertiesSupplier() {
    AutoConfiguredOpenTelemetrySdk autoConfigured =
        builder
            .addPropertiesSupplier(() -> singletonMap("key", "valueUnused"))
            .addPropertiesSupplier(() -> singletonMap("key", "value"))
            .addPropertiesSupplier(() -> singletonMap("otel-key", "otel-value"))
            .addPropertiesSupplier(() -> singletonMap("otel.service.name", "test-service"))
            .build();

    assertThat(autoConfigured.getResource().getAttribute(stringKey("service.name")))
        .isEqualTo("test-service");
    assertThat(autoConfigured.getConfig().getString("key")).isEqualTo("value");
    assertThat(autoConfigured.getConfig().getString("otel.key")).isEqualTo("otel-value");
  }

  @Test
  void builder_addPropertiesCustomizer() {
    AutoConfiguredOpenTelemetrySdk autoConfigured =
        builder
            .addPropertiesSupplier(() -> singletonMap("some-key", "defaultValue"))
            .addPropertiesSupplier(() -> singletonMap("otel.service.name", "default-service-name"))
            .addPropertiesCustomizer(
                config -> {
                  Map<String, String> overrides = new HashMap<>();
                  overrides.put("some-key", "override");
                  overrides.put(
                      "otel.service.name",
                      config.getString("otel.service.name", "").replace("default", "overridden"));
                  return overrides;
                })
            .addPropertiesCustomizer(
                config -> singletonMap("some-key", config.getString("some-key", "") + "-2"))
            .build();

    assertThat(autoConfigured.getResource().getAttribute(stringKey("service.name")))
        .isEqualTo("overridden-service-name");
    assertThat(autoConfigured.getConfig().getString("some-key")).isEqualTo("override-2");
    assertThat(autoConfigured.getConfig().getString("some.key")).isEqualTo("override-2");
  }

  @Test
  void builder_setConfigPropertiesCustomizer() {
    AutoConfiguredOpenTelemetrySdk autoConfigured =
        AutoConfigureUtil.setConfigPropertiesCustomizer(
                builder.addPropertiesCustomizer(config -> singletonMap("some-key", "defaultValue")),
                config -> {
                  assertThat(config.getString("some-key")).isEqualTo("defaultValue");

                  Map<String, String> map = new HashMap<>(singletonMap("some-key", "override"));
                  map.putAll(disableExportPropertySupplier().get());
                  return DefaultConfigProperties.createFromMap(map);
                })
            .build();

    assertThat(autoConfigured.getConfig().getString("some.key")).isEqualTo("override");
  }

  @Test
  void builder_addMeterProviderCustomizer() {
    Mockito.lenient().when(metricReader.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(metricReader.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(metricReader.getDefaultAggregation(any())).thenCallRealMethod();

    SdkMeterProvider sdkMeterProvider =
        builder
            .addMeterProviderCustomizer(
                (meterProviderBuilder, configProperties) ->
                    meterProviderBuilder.registerMetricReader(metricReader))
            .build()
            .getOpenTelemetrySdk()
            .getSdkMeterProvider();
    sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);

    verify(metricReader).forceFlush();
  }

  @Test
  void builder_addLoggerProviderCustomizer() {
    Mockito.lenient()
        .when(logRecordProcessor.shutdown())
        .thenReturn(CompletableResultCode.ofSuccess());
    when(logRecordProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());

    SdkLoggerProvider sdkLoggerProvider =
        builder
            .addLoggerProviderCustomizer(
                (loggerProviderBuilder, configProperties) ->
                    loggerProviderBuilder.addLogRecordProcessor(logRecordProcessor))
            .build()
            .getOpenTelemetrySdk()
            .getSdkLoggerProvider();
    sdkLoggerProvider.forceFlush().join(10, TimeUnit.SECONDS);

    verify(logRecordProcessor).forceFlush();
  }

  @Test
  void builder_setResultAsGlobalFalse() {
    GlobalOpenTelemetry.set(OpenTelemetry.noop());

    OpenTelemetrySdk openTelemetry = builder.build().getOpenTelemetrySdk();

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isNotSameAs(openTelemetry);
  }

  @Test
  void builder_setResultAsGlobalTrue() {
    OpenTelemetrySdk openTelemetry = builder.setResultAsGlobal().build().getOpenTelemetrySdk();

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(openTelemetry);
  }

  @Test
  void builder_registersShutdownHook() {
    builder = spy(builder);
    Thread thread = new Thread();
    doReturn(thread).when(builder).shutdownHook(any());

    OpenTelemetrySdk sdk = builder.build().getOpenTelemetrySdk();

    verify(builder, times(1)).shutdownHook(sdk);
    assertThat(Runtime.getRuntime().removeShutdownHook(thread)).isTrue();
  }

  @Test
  void builder_customizes() {
    builder = spy(builder);
    OpenTelemetrySdk sdk = builder.build().getOpenTelemetrySdk();
    assertThat(sdk).isNotNull();
    verify(builder, times(1)).mergeSdkTracerProviderConfigurer();
  }

  @Test
  void builder_shutdownHook() throws InterruptedException {
    OpenTelemetrySdk sdk = mock(OpenTelemetrySdk.class);

    Thread thread = builder.shutdownHook(sdk);
    thread.start();
    thread.join();

    verify(sdk).close();
  }

  @Test
  void builder_CallAutoConfigureListeners() {
    builder = spy(builder);

    assertThatCode(() -> builder.build()).doesNotThrowAnyException();

    verify(builder, times(1)).callAutoConfigureListeners(any(), any());
  }

  @Test
  void builder_callAutoConfigureListeners() {
    AutoConfigureListener listener = mock(AutoConfigureListener.class);
    SpiHelper spiHelper = mock(SpiHelper.class);
    when(spiHelper.getListeners()).thenReturn(Collections.singleton(listener));
    OpenTelemetrySdk sdk = mock(OpenTelemetrySdk.class);

    builder.callAutoConfigureListeners(spiHelper, sdk);

    verify(listener).afterAutoConfigure(sdk);
  }

  @Test
  void builder_catchesException() throws InterruptedException {
    OpenTelemetrySdk sdk = mock(OpenTelemetrySdk.class);
    doThrow(NoClassDefFoundError.class).when(sdk).close();

    try {
      Thread thread = builder.shutdownHook(sdk);
      thread.start();
      thread.join();
    } catch (NoClassDefFoundError e) {
      fail("shutdownHook threw unexpected NoClassDefFoundError", e);
    }

    verify(sdk).close();
  }

  private static Supplier<Map<String, String>> disableExportPropertySupplier() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.metrics.exporter", "none");
    props.put("otel.traces.exporter", "none");
    props.put("otel.logs.exporter", "none");
    return () -> props;
  }

  @Test
  void disableSdk() {
    BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
        traceCustomizer = getTracerProviderBuilderSpy();
    BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
        metricCustomizer = getMeterProviderBuilderSpy();
    BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder> logCustomizer =
        getLoggerProviderBuilderSpy();

    AutoConfiguredOpenTelemetrySdk autoConfiguredSdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "true"))
            .addTracerProviderCustomizer(traceCustomizer)
            .addMeterProviderCustomizer(metricCustomizer)
            .addLoggerProviderCustomizer(logCustomizer)
            .build();

    assertThat(autoConfiguredSdk.getOpenTelemetrySdk()).isInstanceOf(OpenTelemetrySdk.class);

    // When the SDK is disabled, configuration is skipped and none of the customizers are called
    verify(traceCustomizer, never()).apply(any(), any());
    verify(metricCustomizer, never()).apply(any(), any());
    verify(logCustomizer, never()).apply(any(), any());
  }

  @Test
  void disableSdk_PropagatorCustomizer() {
    Context extracted = Context.root().with(ContextKey.named("animal"), "bear");

    when(propagator2.extract(any(), any(), any())).thenReturn(extracted);

    AutoConfiguredOpenTelemetrySdk autoConfiguredSdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "true"))
            .addPropertiesSupplier(() -> singletonMap("otel.propagators", "tracecontext"))
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
            .build();

    // When the SDK is disabled, propagators are still configured
    assertThat(autoConfiguredSdk.getOpenTelemetrySdk().getPropagators()).isNotNull();
    Consumer<TextMapPropagator> propagatorConsumer =
        propagator -> {
          assertThat(propagator.extract(Context.root(), Collections.emptyMap(), getter))
              .isEqualTo(extracted);
        };
    assertThat(autoConfiguredSdk.getOpenTelemetrySdk().getPropagators().getTextMapPropagator())
        .isInstanceOfSatisfying(TextMapPropagator.class, propagatorConsumer);
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
            .addPropertiesSupplier(() -> singletonMap("otel.metrics.exporter", "none"))
            .addPropertiesSupplier(() -> singletonMap("otel.traces.exporter", "none"))
            .addPropertiesSupplier(() -> singletonMap("otel.logs.exporter", "none"));

    AutoConfiguredOpenTelemetrySdk autoConfigured = autoConfiguration.build();
    assertThat(autoConfigured.getResource().getAttribute(stringKey("cow"))).isEqualTo("moo");

    OpenTelemetrySdk sdk = autoConfigured.getOpenTelemetrySdk();
    sdk.getTracerProvider().get("test").spanBuilder("test").startSpan().end();
    List<SpanData> spanItems = spanExporter.getFinishedSpanItems();
    assertThat(spanItems.size()).isEqualTo(1);
    SpanData spanData = spanItems.get(0);
    assertThat(spanData.getResource().getAttribute(stringKey("cat"))).isEqualTo("meow");

    // Ensures the export happened.
    sdk.shutdown().join(10, TimeUnit.SECONDS);
  }

  @Test
  void testNonStringProperties() {
    Properties properties = System.getProperties();

    properties.putIfAbsent("my-key", 7);
    properties.putIfAbsent(7.39, "my-value");
    properties.putIfAbsent(new BigDecimal("7.397"), new BigInteger("7"));

    AutoConfiguredOpenTelemetrySdk autoConfigured = builder.build();

    assertThat(autoConfigured)
        .extracting("config")
        .isInstanceOfSatisfying(
            ConfigProperties.class,
            config -> {
              String value1 = config.getString("my.key");
              assertThat(value1).isEqualTo("7");
              String value2 = config.getString("7.39");
              assertThat(value2).isEqualTo("my-value");
              String value3 = config.getString("7.397");
              assertThat(value3).isEqualTo("7");
            });
  }

  @Test
  @SuppressLogger(AutoConfiguredOpenTelemetrySdkBuilder.class)
  void configurationError_propagators() {
    BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
        traceCustomizer = getTracerProviderBuilderSpy();
    BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
        metricCustomizer = getMeterProviderBuilderSpy();
    BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder> logCustomizer =
        getLoggerProviderBuilderSpy();

    assertThatThrownBy(
            () ->
                // Override the provider builders with mocks which we can verify are closed
                AutoConfiguredOpenTelemetrySdk.builder()
                    .addTracerProviderCustomizer(traceCustomizer)
                    .addMeterProviderCustomizer(metricCustomizer)
                    .addLoggerProviderCustomizer(logCustomizer)
                    .addPropertiesSupplier(() -> singletonMap("otel.metrics.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.traces.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.logs.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.propagators", "foo"))
                    .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "true"))
                    .build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.propagators");

    // When the SDK is disabled and propagators are mis-configured, none of the customizers are
    // called
    verify(traceCustomizer, never()).apply(any(), any());
    verify(metricCustomizer, never()).apply(any(), any());
    verify(logCustomizer, never()).apply(any(), any());

    assertThatThrownBy(
            () ->
                // Override the provider builders with mocks which we can verify are closed
                AutoConfiguredOpenTelemetrySdk.builder()
                    .addTracerProviderCustomizer(traceCustomizer)
                    .addMeterProviderCustomizer(metricCustomizer)
                    .addLoggerProviderCustomizer(logCustomizer)
                    .addPropertiesSupplier(() -> singletonMap("otel.metrics.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.traces.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.logs.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.propagators", "foo"))
                    .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "false"))
                    .build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.propagators");

    // When the SDK is enabled and propagators are mis-configured, none of the customizers are
    // called
    verify(traceCustomizer, never()).apply(any(), any());
    verify(metricCustomizer, never()).apply(any(), any());
    verify(logCustomizer, never()).apply(any(), any());
  }

  @Test
  @SuppressLogger(AutoConfiguredOpenTelemetrySdkBuilder.class)
  void configurationError_runtime() {
    BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
        traceCustomizer = getTracerProviderBuilderSpy();
    BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
        metricCustomizer = getMeterProviderBuilderSpy();
    BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder> logCustomizer =
        getLoggerProviderBuilderSpy();

    doThrow(new RuntimeException()).when(traceCustomizer).apply(any(), any());

    assertThatThrownBy(
            () ->
                AutoConfiguredOpenTelemetrySdk.builder()
                    .addTracerProviderCustomizer(traceCustomizer)
                    .addMeterProviderCustomizer(metricCustomizer)
                    .addLoggerProviderCustomizer(logCustomizer)
                    .addPropertiesSupplier(() -> singletonMap("otel.metrics.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.traces.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.logs.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "false"))
                    .build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unexpected configuration error");
  }

  @Test
  @SuppressLogger(AutoConfiguredOpenTelemetrySdkBuilder.class)
  void configurationError_ClosesResources() {
    // AutoConfiguredOpenTelemetrySdk should close partially configured resources if an exception
    // short circuits configuration. Verify that SdkTracerProvider, SdkMeterProvider, and
    // SdkLoggerProvider
    // are closed when configuration fails late due to an invalid propagator.
    SdkTracerProvider tracerProvider = mock(SdkTracerProvider.class);
    SdkMeterProvider meterProvider = mock(SdkMeterProvider.class);
    SdkLoggerProvider loggerProvider = mock(SdkLoggerProvider.class);
    SdkTracerProviderBuilder tracerProviderBuilder = mock(SdkTracerProviderBuilder.class);
    SdkMeterProviderBuilder meterProviderBuilder = mock(SdkMeterProviderBuilder.class);
    SdkLoggerProviderBuilder loggerProviderBuilder = mock(SdkLoggerProviderBuilder.class);
    when(tracerProviderBuilder.build()).thenReturn(tracerProvider);
    when(meterProviderBuilder.build()).thenReturn(meterProvider);
    when(loggerProviderBuilder.build()).thenReturn(loggerProvider);

    // Throw an error when closing and verify other resources are still closed
    doThrow(new IOException("Error!")).when(tracerProvider).close();

    assertThatThrownBy(
            () ->
                // Override the provider builders with mocks which we can verify are closed
                AutoConfiguredOpenTelemetrySdk.builder()
                    .addTracerProviderCustomizer((u1, u2) -> tracerProviderBuilder)
                    .addMeterProviderCustomizer((u1, u2) -> meterProviderBuilder)
                    .addLoggerProviderCustomizer((u1, u2) -> loggerProviderBuilder)
                    .addPropertiesSupplier(() -> singletonMap("otel.metrics.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.traces.exporter", "none"))
                    .addPropertiesSupplier(() -> singletonMap("otel.logs.exporter", "foo"))
                    .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "false"))
                    .build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized value for otel.logs.exporter: foo");

    verify(tracerProvider).close();
    verify(meterProvider).close();

    logs.assertContains("Error closing io.opentelemetry.sdk.trace.SdkTracerProvider: Error!");
  }

  @Test
  void globalOpenTelemetryLock() throws InterruptedException, ExecutionException, TimeoutException {
    CountDownLatch autoconfigStarted = new CountDownLatch(1);
    CountDownLatch completeAutoconfig = new CountDownLatch(1);
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    // Submit a future to autoconfigure the SDK and set the result as global. Add a customization
    // hook which blocks until we say so.
    CompletableFuture<OpenTelemetrySdk> autoConfiguredOpenTelemetryFuture =
        CompletableFuture.supplyAsync(
            () ->
                builder
                    .addLoggerProviderCustomizer(
                        (sdkLoggerProviderBuilder, configProperties) -> {
                          autoconfigStarted.countDown();
                          try {
                            completeAutoconfig.await();
                          } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                          }
                          return sdkLoggerProviderBuilder;
                        })
                    .setResultAsGlobal()
                    .build()
                    .getOpenTelemetrySdk(),
            executorService);

    // Wait for autoconfiguration to enter our callback, then try to get an instance of
    // GlobalOpenTelemetry. GlobalOpenTelemetry.get() should block until we release the
    // completeAutoconfig latch and allow autoconfiguration to complete.
    autoconfigStarted.await();
    CompletableFuture<OpenTelemetry> globalOpenTelemetryFuture =
        CompletableFuture.supplyAsync(GlobalOpenTelemetry::get, executorService);
    Thread.sleep(10);
    assertThat(globalOpenTelemetryFuture.isDone()).isFalse();
    assertThat(autoConfiguredOpenTelemetryFuture.isDone()).isFalse();

    // Release the latch, allowing autoconfiguration to complete. Confirm that our
    // GlobalOpenTelemetry.get() future resolved to the same instance as autoconfiguration.
    completeAutoconfig.countDown();
    assertThat(unobfuscate(globalOpenTelemetryFuture.get(10, TimeUnit.SECONDS)))
        .isSameAs(autoConfiguredOpenTelemetryFuture.get(10, TimeUnit.SECONDS));

    // Cleanup
    executorService.shutdown();
    autoConfiguredOpenTelemetryFuture.get().shutdown().join(10, TimeUnit.SECONDS);
    GlobalOpenTelemetry.resetForTest();
  }

  private static OpenTelemetry unobfuscate(OpenTelemetry openTelemetry) {
    try {
      Field delegateField =
          Class.forName("io.opentelemetry.api.GlobalOpenTelemetry$ObfuscatedOpenTelemetry")
              .getDeclaredField("delegate");
      delegateField.setAccessible(true);
      Object delegate = delegateField.get(openTelemetry);
      return (OpenTelemetry) delegate;
    } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
      throw new IllegalStateException("Error unobfuscating OpenTelemetry", e);
    }
  }
}
