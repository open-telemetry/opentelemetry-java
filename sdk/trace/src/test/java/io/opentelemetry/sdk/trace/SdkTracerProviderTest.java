/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for {@link SdkTracerProvider}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SdkTracerProviderTest {
  @Mock private SpanProcessor spanProcessor;
  private final SdkTracerProvider tracerFactory = SdkTracerProvider.builder().build();

  @BeforeEach
  void setUp() {
    when(spanProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    tracerFactory.addSpanProcessor(spanProcessor);
  }

  @Test
  void builder_HappyPath() {
    assertThat(
            SdkTracerProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .setIdGenerator(mock(IdGenerator.class))
                .setTraceConfig(mock(TraceConfig.class))
                .build())
        .isNotNull();
  }

  @Test
  void builder_NullTraceConfig() {
    assertThrows(
        NullPointerException.class,
        () -> SdkTracerProvider.builder().setTraceConfig(null),
        "traceConfig");
  }

  @Test
  void builder_NullClock() {
    assertThrows(
        NullPointerException.class, () -> SdkTracerProvider.builder().setClock(null), "clock");
  }

  @Test
  void builder_NullResource() {
    assertThrows(
        NullPointerException.class,
        () -> SdkTracerProvider.builder().setResource(null),
        "resource");
  }

  @Test
  void builder_NullIdsGenerator() {
    assertThrows(
        NullPointerException.class,
        () -> SdkTracerProvider.builder().setIdGenerator(null),
        "idsGenerator");
  }

  @Test
  void defaultGet() {
    assertThat(tracerFactory.get("test")).isInstanceOf(SdkTracer.class);
  }

  @Test
  void getSameInstanceForSameName_WithoutVersion() {
    assertThat(tracerFactory.get("test")).isSameAs(tracerFactory.get("test"));
    assertThat(tracerFactory.get("test")).isSameAs(tracerFactory.get("test", null));
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(tracerFactory.get("test", "version")).isSameAs(tracerFactory.get("test", "version"));
  }

  @Test
  void propagatesInstrumentationLibraryInfoToTracer() {
    InstrumentationLibraryInfo expected =
        InstrumentationLibraryInfo.create("theName", "theVersion");
    Tracer tracer = tracerFactory.get(expected.getName(), expected.getVersion());
    assertThat(((SdkTracer) tracer).getInstrumentationLibraryInfo()).isEqualTo(expected);
  }

  @Test
  void updateActiveTraceConfig() {
    assertThat(tracerFactory.getActiveTraceConfig()).isEqualTo(TraceConfig.getDefault());
    TraceConfig newConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Sampler.alwaysOff()).build();
    tracerFactory.updateActiveTraceConfig(newConfig);
    assertThat(tracerFactory.getActiveTraceConfig()).isEqualTo(newConfig);
  }

  @Test
  void build_traceConfig() {
    TraceConfig initialTraceConfig = mock(TraceConfig.class);
    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().setTraceConfig(initialTraceConfig).build();

    assertThat(sdkTracerProvider.getActiveTraceConfig()).isSameAs(initialTraceConfig);
  }

  @Test
  void shutdown() {
    tracerFactory.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void forceFlush() {
    tracerFactory.forceFlush();
    Mockito.verify(spanProcessor, Mockito.times(1)).forceFlush();
  }

  @Test
  void shutdownTwice_OnlyFlushSpanProcessorOnce() {
    tracerFactory.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
    tracerFactory.shutdown(); // the second call will be ignored
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  void returnNoopSpanAfterShutdown() {
    tracerFactory.shutdown();
    Span span = tracerFactory.get("noop").spanBuilder("span").startSpan();
    assertThat(span.getSpanContext().isValid()).isFalse();
    span.end();
  }

  @Test
  void suppliesDefaultTracerForNullName() {
    SdkTracer tracer = (SdkTracer) tracerFactory.get(null);
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);

    tracer = (SdkTracer) tracerFactory.get(null, null);
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);
  }

  @Test
  void suppliesDefaultTracerForEmptyName() {
    SdkTracer tracer = (SdkTracer) tracerFactory.get("");
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);

    tracer = (SdkTracer) tracerFactory.get("", "");
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(SdkTracerProvider.DEFAULT_TRACER_NAME);
  }
}
