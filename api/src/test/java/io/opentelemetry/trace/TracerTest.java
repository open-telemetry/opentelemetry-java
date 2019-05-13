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

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithName_NullName() {
    noopTracer.spanBuilder(null);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(noopTracer.spanBuilder(SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }
}
