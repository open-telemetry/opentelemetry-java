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

/** Unit tests for {@link TracerSdkProvider}. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TracerSdkProviderTest {
  @Mock private SpanProcessor spanProcessor;
  private final TracerSdkProvider tracerFactory = TracerSdkProvider.builder().build();

  @BeforeEach
  void setUp() {
    when(spanProcessor.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(spanProcessor.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    tracerFactory.addSpanProcessor(spanProcessor);
  }

  @Test
  void builder_HappyPath() {
    assertThat(
            TracerSdkProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .setIdsGenerator(mock(IdGenerator.class))
                .build())
        .isNotNull();
  }

  @Test
  void builder_NullClock() {
    assertThrows(
        NullPointerException.class, () -> TracerSdkProvider.builder().setClock(null), "clock");
  }

  @Test
  void builder_NullResource() {
    assertThrows(
        NullPointerException.class,
        () -> TracerSdkProvider.builder().setResource(null),
        "resource");
  }

  @Test
  void builder_NullIdsGenerator() {
    assertThrows(
        NullPointerException.class,
        () -> TracerSdkProvider.builder().setIdsGenerator(null),
        "idsGenerator");
  }

  @Test
  void defaultGet() {
    assertThat(tracerFactory.get("test")).isInstanceOf(TracerSdk.class);
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
    assertThat(((TracerSdk) tracer).getInstrumentationLibraryInfo()).isEqualTo(expected);
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
    TracerSdk tracer = (TracerSdk) tracerFactory.get(null);
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(TracerSdkProvider.DEFAULT_TRACER_NAME);

    tracer = (TracerSdk) tracerFactory.get(null, null);
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(TracerSdkProvider.DEFAULT_TRACER_NAME);
  }

  @Test
  void suppliesDefaultTracerForEmptyName() {
    TracerSdk tracer = (TracerSdk) tracerFactory.get("");
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(TracerSdkProvider.DEFAULT_TRACER_NAME);

    tracer = (TracerSdk) tracerFactory.get("", "");
    assertThat(tracer.getInstrumentationLibraryInfo().getName())
        .isEqualTo(TracerSdkProvider.DEFAULT_TRACER_NAME);
  }
}
