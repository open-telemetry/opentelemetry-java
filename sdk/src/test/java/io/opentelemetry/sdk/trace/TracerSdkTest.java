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

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.BlankSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.unsafe.ContextUtils;
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
  public void getCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
    Context origContext = ContextUtils.withValue(Context.current(), span).attach();
    // Make sure context is detached even if test fails.
    try {
      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(span);
    } finally {
      Context.current().detach(origContext);
    }
    assertThat(tracer.getCurrentSpan()).isSameInstanceAs(BlankSpan.INSTANCE);
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
}
