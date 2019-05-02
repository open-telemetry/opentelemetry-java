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
import static org.mockito.Mockito.verifyZeroInteractions;

import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.BlankSpan;
import io.opentelemetry.trace.Span;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracerSdk}. */
@RunWith(JUnit4.class)
public class TracerSdkTest {
  @Mock private Span span;
  private final TracerSdk tracer = new TracerSdk();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test
  public void withSpan_NullSpan() {
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
    try (Scope ws = tracer.withSpan(null)) {
      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
    }
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
    try (Scope ws = tracer.withSpan(span)) {
      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(span);
    }
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
  }

  @Test
  public void wrapRunnable() {
    Runnable runnable;
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
    runnable =
        tracer.withSpan(
            span,
            new Runnable() {
              @Override
              public void run() {
                assertThat(tracer.getCurrentSpan()).isSameInstanceAs(span);
              }
            });
    // When we run the runnable we will have the span in the current Context.
    runnable.run();
    verifyZeroInteractions(span);
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
  }

  @Test
  public void wrapCallable() throws Exception {
    final Object ret = new Object();
    Callable<Object> callable;
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
    callable =
        tracer.withSpan(
            span,
            new Callable<Object>() {
              @Override
              public Object call() {
                assertThat(tracer.getCurrentSpan()).isSameInstanceAs(span);
                return ret;
              }
            });
    // When we call the callable we will have the span in the current Context.
    assertThat(callable.call()).isEqualTo(ret);
    verifyZeroInteractions(span);
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
  }
}
