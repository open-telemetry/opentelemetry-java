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

package io.opentelemetry.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.Scope;
import java.util.concurrent.Callable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Tracer}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Java-6 does not support try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class TracerTest {
  private static final Tracer noopTracer = NoopTrace.newNoopTracer();
  private static final String SPAN_NAME = "MySpanName";

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(noopTracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    Scope ws = noopTracer.withSpan(BlankSpan.INSTANCE);
    try {
      assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    } finally {
      ws.close();
    }
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void wrapRunnable() {
    Runnable runnable;
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    runnable =
        noopTracer.withSpan(
            BlankSpan.INSTANCE,
            new Runnable() {
              @Override
              public void run() {
                assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
              }
            });
    // When we run the runnable we will have the span in the current Context.
    runnable.run();
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void wrapCallable() throws Exception {
    final Object ret = new Object();
    Callable<Object> callable;
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    callable =
        noopTracer.withSpan(
            BlankSpan.INSTANCE,
            new Callable<Object>() {
              @Override
              public Object call() throws Exception {
                assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
                return ret;
              }
            });
    // When we call the callable we will have the span in the current Context.
    assertThat(callable.call()).isEqualTo(ret);
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithName_NullName() {
    noopTracer.spanBuilder(null);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(noopTracer.spanBuilder(SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithParentAndName_NullName() {
    noopTracer.spanBuilderWithExplicitParent(null, null);
  }

  @Test
  public void defaultSpanBuilderWithParentAndName() {
    assertThat(noopTracer.spanBuilderWithExplicitParent(SPAN_NAME, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithRemoteParent_NullName() {
    noopTracer.spanBuilderWithRemoteParent(null, null);
  }

  @Test
  public void defaultSpanBuilderWithRemoteParent_NullParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithRemoteParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, SpanContext.BLANK).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }
}
