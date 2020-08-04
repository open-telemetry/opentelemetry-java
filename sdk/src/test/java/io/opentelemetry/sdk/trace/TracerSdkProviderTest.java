/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracerSdkProvider}. */
class TracerSdkProviderTest {
  @Mock private SpanProcessor spanProcessor;
  private final TracerSdkProvider tracerFactory = TracerSdkProvider.builder().build();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    tracerFactory.addSpanProcessor(spanProcessor);
  }

  @Test
  void builder_HappyPath() {
    assertThat(
            TracerSdkProvider.builder()
                .setClock(mock(Clock.class))
                .setResource(mock(Resource.class))
                .setIdsGenerator(mock(IdsGenerator.class))
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
    TracerSdk tracer = tracerFactory.get(expected.getName(), expected.getVersion());
    assertThat(tracer.getInstrumentationLibraryInfo()).isEqualTo(expected);
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
    assertThat(span).isInstanceOf(DefaultSpan.class);
    span.end();
  }
}
