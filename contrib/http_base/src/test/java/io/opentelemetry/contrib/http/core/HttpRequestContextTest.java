/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.contrib.http.core;

import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for {@link HttpRequestContext}. */
public class HttpRequestContextTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldCreateWithProvidedSpanAndDistributedContext() {
    Span span =
        OpenTelemetry.getTracerRegistry()
            .get(INSTRUMENTATION_LIB_ID)
            .spanBuilder("/junit")
            .setNoParent()
            .setSpanKind(Kind.CLIENT)
            .startSpan();
    CorrelationContext correlationContext =
        OpenTelemetry.getCorrelationContextManager().contextBuilder().setNoParent().build();
    HttpRequestContext context = new HttpRequestContext(span, correlationContext);
    assertSame(span, context.getSpan());
    assertSame(correlationContext, context.getCorrlatContext());
  }

  @Test
  public void shouldCreateWithProvidedSpan() {
    Span span =
        OpenTelemetry.getTracerRegistry()
            .get(INSTRUMENTATION_LIB_ID)
            .spanBuilder("/junit")
            .setNoParent()
            .setSpanKind(Kind.CLIENT)
            .startSpan();
    HttpRequestContext context = new HttpRequestContext(span, null);
    assertSame(span, context.getSpan());
  }

  @Test
  public void shouldThrowExceptionOnNullSpan() {
    thrown.expect(NullPointerException.class);
    new HttpRequestContext(null, null);
  }

  @Test
  public void shouldInitializeValuesOnCreation() {
    Span span =
        OpenTelemetry.getTracerRegistry()
            .get(INSTRUMENTATION_LIB_ID)
            .spanBuilder("/junit")
            .setNoParent()
            .setSpanKind(Kind.CLIENT)
            .startSpan();
    HttpRequestContext context = new HttpRequestContext(span, null);
    assertTrue(context.getRequestStartTime() > 0L);
    assertEquals(0L, context.getReceiveMessageSize());
    assertEquals(0L, context.getSentMessageSize());
  }
}
