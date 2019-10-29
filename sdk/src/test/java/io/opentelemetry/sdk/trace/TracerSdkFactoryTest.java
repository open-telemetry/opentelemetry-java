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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracerSdkFactory}. */
@RunWith(JUnit4.class)
public class TracerSdkFactoryTest {
  @Mock private SpanProcessor spanProcessor;
  private final TracerSdkFactory tracerFactory = TracerSdkFactory.create();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    tracerFactory.addSpanProcessor(spanProcessor);
  }

  @Test
  public void defaultGet() {
    assertThat(tracerFactory.get("test")).isInstanceOf(TracerSdk.class);
  }

  @Test
  public void getSameInstanceForSameName_WithoutVersion() {
    assertThat(tracerFactory.get("test")).isSameInstanceAs(tracerFactory.get("test"));
    assertThat(tracerFactory.get("test")).isSameInstanceAs(tracerFactory.get("test", null));
  }

  @Test
  public void getSameInstanceForSameName_WithVersion() {
    assertThat(tracerFactory.get("test", "version"))
        .isSameInstanceAs(tracerFactory.get("test", "version"));
  }

  @Test
  public void updateActiveTraceConfig() {
    assertThat(tracerFactory.getActiveTraceConfig()).isEqualTo(TraceConfig.getDefault());
    TraceConfig newConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOff()).build();
    tracerFactory.updateActiveTraceConfig(newConfig);
    assertThat(tracerFactory.getActiveTraceConfig()).isEqualTo(newConfig);
  }

  @Test
  public void shutdown() {
    tracerFactory.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  public void shutdownTwice_OnlyFlushSpanProcessorOnce() {
    tracerFactory.shutdown();
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
    tracerFactory.shutdown(); // the second call will be ignored
    Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
  }

  @Test
  public void returnNoopSpanAfterShutdown() {
    tracerFactory.shutdown();
    Span span = tracerFactory.get("noop").spanBuilder("span").startSpan();
    assertThat(span).isInstanceOf(DefaultSpan.class);
    span.end();
  }
}
