/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
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
                .setIdsGenerator(mock(IdsGenerator.class))
                .setSpanProcessors(spanProcessor)
                .setTraceConfig(TraceConfig.getDefault())
                .build())
        .isNotNull();
  }

  @Test
  void builder_nullsThrow() {
    assertThatThrownBy(() -> TracerSdkProvider.builder().setClock(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("clock");
    assertThatThrownBy(() -> TracerSdkProvider.builder().setResource(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("resource");
    assertThatThrownBy(() -> TracerSdkProvider.builder().setIdsGenerator(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("idsGenerator");
    assertThatThrownBy(() -> TracerSdkProvider.builder().setSpanProcessors((SpanProcessor[]) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("spanProcessors");
    assertThatThrownBy(
            () -> TracerSdkProvider.builder().setSpanProcessors((Iterable<SpanProcessor>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("spanProcessors");
    assertThatThrownBy(() -> TracerSdkProvider.builder().setTraceConfig(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("traceConfig");
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
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOff()).build();
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
    assertThat(span.getContext().isValid()).isFalse();
    span.end();
  }
}
