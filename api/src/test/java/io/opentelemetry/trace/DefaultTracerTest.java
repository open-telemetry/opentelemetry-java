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

import io.opentelemetry.scope.DefaultScopeManager;
import io.opentelemetry.scope.Scope;
import io.opentelemetry.scope.ScopeManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultTracer}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
// TODO (trask) delete tests here that were designed to test Tracer methods that are now removed
@SuppressWarnings("MustBeClosedChecker")
public class DefaultTracerTest {
  private static final ScopeManager defaultScopeManager = DefaultScopeManager.getInstance();
  private static final Tracer defaultTracer = new DefaultTracer(defaultScopeManager);
  private static final String SPAN_NAME = "MySpanName";
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final SpanContext spanContext =
      SpanContext.create(
          TraceId.fromBytes(firstBytes, 0),
          SpanId.fromBytes(firstBytes, 8),
          TraceFlags.getDefault(),
          TraceState.getDefault());

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(defaultScopeManager.getSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(defaultScopeManager.getSpan()).isInstanceOf(DefaultSpan.class);
    Scope ws = defaultScopeManager.withSpan(DefaultSpan.createRandom());
    try {
      assertThat(defaultScopeManager.getSpan()).isInstanceOf(DefaultSpan.class);
    } finally {
      ws.close();
    }
    assertThat(defaultScopeManager.getSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void spanBuilderWithName_NullName() {
    thrown.expect(NullPointerException.class);
    defaultTracer.spanBuilder(null);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(defaultTracer.spanBuilder(SPAN_NAME).startSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void testInProcessContext() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = defaultScopeManager.withSpan(span);
    try {
      assertThat(defaultScopeManager.getSpan()).isEqualTo(span);
      Span secondSpan = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      Scope secondScope = defaultScopeManager.withSpan(secondSpan);
      try {
        assertThat(defaultScopeManager.getSpan()).isEqualTo(secondSpan);
      } finally {
        secondScope.close();
        assertThat(defaultScopeManager.getSpan()).isEqualTo(span);
      }
    } finally {
      scope.close();
    }
    assertThat(defaultScopeManager.getSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void testSpanContextPropagationExplicitParent() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(spanContext).startSpan();
    assertThat(span.getContext()).isSameInstanceAs(spanContext);
  }

  @Test
  public void testSpanContextPropagation() {
    DefaultSpan parent = new DefaultSpan(spanContext);

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(parent).startSpan();
    assertThat(span.getContext()).isSameInstanceAs(spanContext);
  }

  @Test
  public void testSpanContextPropagationCurrentSpan() {
    DefaultSpan parent = new DefaultSpan(spanContext);
    Scope scope = defaultScopeManager.withSpan(parent);
    try {
      Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span.getContext()).isSameInstanceAs(spanContext);
    } finally {
      scope.close();
    }
  }

  @Test
  public void testSpanContextPropagationCurrentSpanContext() {
    Scope scope = defaultScopeManager.withSpan(DefaultSpan.create(spanContext));
    try {
      Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span.getContext()).isSameInstanceAs(spanContext);
    } finally {
      scope.close();
    }
  }
}
