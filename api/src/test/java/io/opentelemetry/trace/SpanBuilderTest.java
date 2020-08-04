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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.trace.Span.Kind;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Span.Builder}. */
class SpanBuilderTest {
  private final Tracer tracer = DefaultTracer.getInstance();

  @Test
  void doNotCrash_NoopImplementation() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    spanBuilder.setSpanKind(Kind.SERVER);
    spanBuilder.setParent(DefaultSpan.getInvalid());
    spanBuilder.setParent(DefaultSpan.getInvalid().getContext());
    spanBuilder.setNoParent();
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext());
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext(), Attributes.empty());
    spanBuilder.addLink(
        new Link() {
          private final SpanContext spanContext = DefaultSpan.getInvalid().getContext();

          @Override
          public SpanContext getContext() {
            return spanContext;
          }

          @Override
          public Attributes getAttributes() {
            return Attributes.empty();
          }
        });
    spanBuilder.setAttribute("key", "value");
    spanBuilder.setAttribute("key", 12345L);
    spanBuilder.setAttribute("key", .12345);
    spanBuilder.setAttribute("key", true);
    spanBuilder.setAttribute("key", AttributeValue.stringAttributeValue("value"));
    spanBuilder.setStartTimestamp(12345L);
    assertThat(spanBuilder.startSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void setParent_NullSpan() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThrows(NullPointerException.class, () -> spanBuilder.setParent((Span) null));
  }

  @Test
  void setParent_NullSpanContext() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThrows(NullPointerException.class, () -> spanBuilder.setParent((SpanContext) null));
  }

  @Test
  void setStartTimestamp_Negative() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThrows(
        IllegalArgumentException.class,
        () -> spanBuilder.setStartTimestamp(-1),
        "Negative startTimestamp");
  }
}
