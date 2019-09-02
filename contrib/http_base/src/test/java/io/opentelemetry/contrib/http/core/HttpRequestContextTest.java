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

package io.opentelemetry.contrib.http.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.util.Samplers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Unit tests for {@link HttpRequestContext}. */
public class HttpRequestContextTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldCreateWithProvidedSpanAndDistributedContext() {
    Span span =
        OpenTelemetry.getTracer()
            .spanBuilder("/junit")
            .setNoParent()
            .setSpanKind(Kind.CLIENT)
            .setRecordEvents(true)
            .startSpan();
    DistributedContext distributedContext =
        OpenTelemetry.getDistributedContextManager().contextBuilder().setNoParent().build();
    HttpRequestContext context = new HttpRequestContext(span, distributedContext);
    assertSame(span, context.span);
    assertSame(distributedContext, context.distContext);
  }

  @Test
  public void shouldCreateWithProvidedSpan() {
    Span span =
        OpenTelemetry.getTracer()
            .spanBuilder("/junit")
            .setNoParent()
            .setSpanKind(Kind.CLIENT)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    HttpRequestContext context = new HttpRequestContext(span, null);
    assertSame(span, context.span);
  }

  @Test
  public void shouldThrowExceptionOnNullSpan() {
    thrown.expect(NullPointerException.class);
    new HttpRequestContext(null, null);
  }

  @Test
  public void shouldInitializeValuesOnCreation() {
    Span span =
        OpenTelemetry.getTracer()
            .spanBuilder("/junit")
            .setNoParent()
            .setSpanKind(Kind.CLIENT)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    HttpRequestContext context = new HttpRequestContext(span, null);
    assertTrue(context.requestStartTime > 0L);
    assertEquals(0L, context.receiveMessageSize.longValue());
    assertEquals(0L, context.sentMessageSize.longValue());
  }
}
